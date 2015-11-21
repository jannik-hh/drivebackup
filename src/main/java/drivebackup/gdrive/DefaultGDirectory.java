package drivebackup.gdrive;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import drivebackup.encryption.EncryptionService;
import drivebackup.gdrive.calls.CreateDirectoryCall;
import drivebackup.gdrive.calls.GetFilesOfDirectoryCall;
import drivebackup.gdrive.calls.SaveFileCall;
import drivebackup.gdrive.calls.UpdateFileCall;
import drivebackup.local.LocalFile;

public class DefaultGDirectory implements GDirectory {
	private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
	private static final Logger logger = LogManager.getLogger("DriveBackup");
	private final Drive drive;
	private final EncryptionService encryptionService;
	private final List<File> files;

	private final String parentID;

	public static GDirectory fromPath(String directoryPath, Drive drive, EncryptionService encryptionService)
			throws IOException {
		GDirectory gDirectory = new DefaultGDirectory(drive, "root", encryptionService);
		String[] subdirs = directoryPath.split("/");
		for (String subdir : subdirs) {
			if (!subdir.trim().isEmpty()) {
				gDirectory = gDirectory.findOrCreateDirectory(subdir);
			}
		}
		return gDirectory;
	}

	private DefaultGDirectory(Drive drive, String parentID, EncryptionService encryptionService) {
		this.drive = drive;
		this.parentID = parentID;
		this.encryptionService = encryptionService;
		try {
			this.files = QueryExecutorWithRetry.executeWithRetry(new GetFilesOfDirectoryCall(parentID, drive));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public File saveOrUpdateFile(LocalFile file) throws IOException {
		String localFileName = file.getName();
		String localFilePath = file.getPath();
		Optional<File> remoteFile = findFile(localFileName);
		if (remoteFile.isPresent()) {
			File existentRemoteFile = remoteFile.get();
			if (needsUpdate(file, existentRemoteFile)) {
				long start = System.currentTimeMillis();
				File updatedFile = updateFile(existentRemoteFile, file);
				logExecutionTime(String.format("%s updated", localFilePath), start);
				return updatedFile;
			} else {
				logger.info("{} is up-to-date", localFilePath);
				return existentRemoteFile;
			}
		} else{
			long start = System.currentTimeMillis();
			File savedFile = saveFile(file);
			logExecutionTime(String.format("%s saved", localFilePath), start);
			return savedFile;
		}
	}

	@Override
	public GDirectory findOrCreateDirectory(String name) throws IOException {
		Optional<GDirectory> directory = findDirectory(name);
		if(directory.isPresent()){
			return directory.get();
		}else{
			return createDirectory(name);
		}
	}

	@Override
	public String getID() {
		return parentID;
	}

	@Override
	public void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException {
		for (File file : files) {
			String title = file.getTitle();
			if (!fileAndDirectoryNames.contains(title)) {
				QueryExecutorWithRetry.executeWithRetry(
						()-> drive.files().trash(file.getId()).execute()
				);
				logger.info("{} trashed", title);
			}
		}

	}

	private Optional<GDirectory> findDirectory(String name) throws IOException {
		if(name== null){
			return Optional.empty();
		}
		Optional<File> dir= files.stream()
			.filter((file) -> FOLDER_MIME_TYPE.equals(file.getMimeType()) && name.equals(file.getTitle()))
			.findFirst();
		return dir.map((file) -> new DefaultGDirectory(drive, file.getId(), encryptionService));
	}

	private GDirectory createDirectory(String name) throws IOException {
		File newDir = QueryExecutorWithRetry.executeWithRetry(new CreateDirectoryCall(name, parentID, drive));
		files.add(newDir);
		return new DefaultGDirectory(drive, newDir.getId(), encryptionService);
	}

	private Optional<File> findFile(String fileName) throws IOException {
		if(fileName== null){
			return Optional.empty();
		}
		return files.stream()
			.filter((aFile) -> !FOLDER_MIME_TYPE.equals(aFile.getMimeType()) &&  fileName.equals(aFile.getTitle()))
			.findFirst();
	}

	private File saveFile(LocalFile localFile) throws IOException {
		File savedFile = QueryExecutorWithRetry.executeWithRetry(new SaveFileCall(localFile, parentID, drive, encryptionService));
		files.add(savedFile);
		return savedFile;
	}

	private File updateFile(File remoteFile, LocalFile localFile) throws IOException {
		File updatedRemoteFile =  QueryExecutorWithRetry.executeWithRetry(
			new UpdateFileCall(localFile, remoteFile, drive, encryptionService)
		);
		files.remove(remoteFile);
		files.add(updatedRemoteFile);
		
		return updatedRemoteFile;
	}

	private boolean needsUpdate(LocalFile localFile, File gFile) throws IOException {
		OriginMD5ChecksumAccessor md5ChecksumAccessor = new OriginMD5ChecksumAccessor(gFile);
		Optional<String> optional = md5ChecksumAccessor.get();
		if (optional.isPresent()) {
			String checksum = optional.get();
			return !checksum.equals(localFile.getOriginMd5Checksum());
		} else {
			return true;
		}
	}
	
	private void logExecutionTime(String action, long start){
		logger.info("{} in {} sec", action, Duration.ofMillis(System.currentTimeMillis() - start).getSeconds());
	}

}
