package drivebackup.gdrive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Property;

import drivebackup.encryption.EncryptionService;
import drivebackup.local.LocalFile;

public class DefaultGDirectory implements GDirectory {
	private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
	private final static String MD5_PROPERTY_NAME = "orig_md5";
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
			this.files = new FilesQuery(drive).getAllFilesOf(parentID);
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
				QueryExecutorWithRetry.executeWithRetry(drive.files().trash(file.getId()));
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
		File body = new File();
		body.setTitle(name);
		body.setMimeType("application/vnd.google-apps.folder");
		body.setParents(Arrays.asList(new ParentReference().setId(parentID)));
		File newDir = QueryExecutorWithRetry.executeWithRetry(drive.files().insert(body));
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
		File body = new File();
		body.setTitle(localFile.getName());
		body.setParents(Arrays.asList(new ParentReference().setId(parentID)));
		body.setProperties(Arrays.asList(createMd5ChecksumProperty(localFile)));
		AbstractInputStreamContent mediaContent = fileContent(localFile);
		File savedFile = QueryExecutorWithRetry.executeWithRetry(drive.files().insert(body, mediaContent));
		files.add(savedFile);
		return savedFile;
	}

	private File updateFile(File remoteFile, LocalFile localFile) throws IOException {
		remoteFile.setProperties(Arrays.asList(createMd5ChecksumProperty(localFile)));
		AbstractInputStreamContent mediaContent = fileContent(localFile);
		File updatedRemoteFile =  QueryExecutorWithRetry.executeWithRetry(drive.files().update(remoteFile.getId(), remoteFile, mediaContent));
		files.remove(remoteFile);
		files.add(updatedRemoteFile);
		
		return updatedRemoteFile;
	}

	private AbstractInputStreamContent fileContent(LocalFile localFile) throws FileNotFoundException {
		return new EncryptedFileContent(null, localFile, encryptionService);
	}

	private boolean needsUpdate(LocalFile localFile, File gFile) throws IOException {
		Optional<Property> optional = gFile.getProperties().stream()
				.filter((property) -> property.getKey().equals(MD5_PROPERTY_NAME)).findFirst();
		if (optional.isPresent()) {
			Property md5Property = optional.get();
			return !md5Property.getValue().equals(localFile.getOriginMd5Checksum());
		} else {
			return true;
		}
	}

	private Property createMd5ChecksumProperty(LocalFile localFile) throws IOException {
		Property property = new Property();
		property.setKey(MD5_PROPERTY_NAME).setValue(localFile.getOriginMd5Checksum()).setVisibility("PUBLIC");
		return property;
	}
	
	private void logExecutionTime(String action, long start){
		logger.info("{} in {} sec", action, Duration.ofMillis(System.currentTimeMillis() - start).getSeconds());
	}

}
