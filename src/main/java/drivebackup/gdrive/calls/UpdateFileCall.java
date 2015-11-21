package drivebackup.gdrive.calls;

import java.io.IOException;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import drivebackup.encryption.EncryptionService;
import drivebackup.gdrive.EncryptedFileContent;
import drivebackup.gdrive.OriginMD5ChecksumAccessor;
import drivebackup.local.LocalFile;

public class UpdateFileCall implements IOCallable<File>{
	private final LocalFile localFile;
	private final File fileToUpdate;
	private final Drive drive;
	private final EncryptionService encryptionService;
	
	public UpdateFileCall(LocalFile localFile, File fileToUpdate, Drive drive,EncryptionService encryptionService){
		this.localFile = localFile;
		this.fileToUpdate = fileToUpdate;
		this.drive = drive;
		this.encryptionService = encryptionService;
	}

	@Override
	public File call() throws IOException {
		OriginMD5ChecksumAccessor md5ChecksumAccessor = new OriginMD5ChecksumAccessor(fileToUpdate);
		md5ChecksumAccessor.set(localFile.getOriginMd5Checksum());
		AbstractInputStreamContent mediaContent = new EncryptedFileContent(null, localFile, encryptionService);
		return drive.files().update(fileToUpdate.getId(), fileToUpdate, mediaContent).execute();
	}
	
	
}
