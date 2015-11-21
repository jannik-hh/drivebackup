package drivebackup.gdrive.calls;

import java.io.IOException;
import java.util.Arrays;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

import drivebackup.encryption.EncryptionService;
import drivebackup.gdrive.EncryptedFileContent;
import drivebackup.gdrive.OriginMD5ChecksumAccessor;
import drivebackup.local.LocalFile;

public class SaveFileCall implements IOCallable<File> {
	private final LocalFile localFile;
	private final String parentId;
	private final Drive drive;
	private final EncryptionService encryptionService;

	public SaveFileCall(LocalFile localFile, String parentId, Drive drive,EncryptionService encryptionService){
		this.localFile = localFile;
		this.parentId = parentId;
		this.drive = drive;
		this.encryptionService = encryptionService;
	}

	@Override
	public File call() throws IOException {
		File body = new File();
		body.setTitle(localFile.getName());
		body.setParents(Arrays.asList(new ParentReference().setId(parentId)));
		OriginMD5ChecksumAccessor md5ChecksumAccessor = new OriginMD5ChecksumAccessor(body);
		md5ChecksumAccessor.set(localFile.getOriginMd5Checksum());
		
		AbstractInputStreamContent mediaContent = new EncryptedFileContent(null, localFile, encryptionService);
		return drive.files().insert(body, mediaContent).execute();
	}

}
