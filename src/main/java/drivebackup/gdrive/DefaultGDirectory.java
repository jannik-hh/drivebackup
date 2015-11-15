package drivebackup.gdrive;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Property;

import drivebackup.encryption.EncryptionService;

public class DefaultGDirectory implements GDirectory {
	private final static String FIND_FOLDER_QUERY = "title = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed=false";
	private final static String MD5_PROPERTY_NAME = "orig_md5";
	private final Drive drive;
	private final EncryptionService encryptionService;

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
	}

	@Override
	public File saveOrUpdateFile(java.io.File file) throws IOException {
		String localFileName = file.getName();
		File remoteFile = findFile(localFileName);
		if (remoteFile == null) {
			return saveFile(file);
		} else if (needsUpdate(file, remoteFile)) {
			return updateFile(remoteFile, file);
		} else {
			return remoteFile;
		}

	}

	@Override
	public GDirectory findOrCreateDirectory(String name) throws IOException {
		GDirectory directory = findDirectory(name);
		if (directory == null) {
			directory = createDirectory(name);
		}
		return directory;

	}

	@Override
	public String getID() {
		return parentID;
	}

	@Override
	public void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException {
		String query = String.format("'%s' in parents and trashed=false", parentID);
		FileList fileList = drive.files().list().setQ(query).execute();
		for (File file : fileList.getItems()) {
			if (!fileAndDirectoryNames.contains(file.getTitle())) {
				drive.files().trash(file.getId()).execute();
			}
		}

	}

	private GDirectory findDirectory(String name) throws IOException {
		String query = String.format(FIND_FOLDER_QUERY, name);
		ChildList list = drive.children().list(parentID).setQ(query).execute();
		List<ChildReference> items = list.getItems();
		if (items != null && items.size() >= 1) {
			String dirId = items.get(0).getId();
			return new DefaultGDirectory(drive, dirId, encryptionService);
		} else {
			return null;
		}
	}

	private GDirectory createDirectory(String name) throws IOException {
		File body = new File();
		body.setTitle(name);
		body.setMimeType("application/vnd.google-apps.folder");
		body.setParents(Arrays.asList(new ParentReference().setId(parentID)));
		com.google.api.services.drive.model.File newDir = drive.files().insert(body).execute();
		return new DefaultGDirectory(drive, newDir.getId(), encryptionService);
	}

	private File findFile(String fileName) throws IOException {
		String query = String.format(
				"title = '%s' and mimeType != 'application/vnd.google-apps.folder' and trashed=false", fileName);
		ChildList list = drive.children().list(parentID).setQ(query).execute();
		List<ChildReference> items = list.getItems();
		if (items != null && items.size() >= 1) {
			String fileId = items.get(0).getId();
			return drive.files().get(fileId).execute();
		} else {
			return null;
		}
	}

	private File saveFile(java.io.File localFile) throws IOException {
		File body = new File();
		body.setTitle(localFile.getName());
		body.setParents(Arrays.asList(new ParentReference().setId(parentID)));
		body.setProperties(Arrays.asList(createMd5ChecksumProperty(localFile)));
		AbstractInputStreamContent mediaContent = fileContent(localFile);
		return drive.files().insert(body, mediaContent).execute();
	}

	private File updateFile(File remoteFile, java.io.File localFile) throws IOException {
		remoteFile.setProperties(Arrays.asList(createMd5ChecksumProperty(localFile)));
		AbstractInputStreamContent mediaContent = fileContent(localFile);
		return drive.files().update(remoteFile.getId(), remoteFile, mediaContent).execute();
	}

	private AbstractInputStreamContent fileContent(java.io.File localFile) throws FileNotFoundException {
		InputStream encryptedFileContent = encryptionService.encrypt(new FileInputStream(localFile));
		return new InputStreamContent(null, encryptedFileContent);
	}

	private boolean needsUpdate(java.io.File localFile, File gFile) throws IOException {
		Optional<Property> optional = gFile.getProperties().stream()
				.filter((property) -> property.getKey().equals(MD5_PROPERTY_NAME)).findFirst();
		if (optional.isPresent()) {
			Property md5Property = optional.get();
			return !md5Property.getValue().equals(md5Checksum(localFile));
		} else {
			return true;
		}
	}

	private String md5Checksum(java.io.File localFile) throws IOException {
		FileInputStream fis = new FileInputStream(localFile);
		return org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
	}

	private Property createMd5ChecksumProperty(java.io.File localFile) throws IOException {
		Property property = new Property();
		property.setKey(MD5_PROPERTY_NAME).setValue(md5Checksum(localFile)).setVisibility("PUBLIC");
		return property;
	}

}
