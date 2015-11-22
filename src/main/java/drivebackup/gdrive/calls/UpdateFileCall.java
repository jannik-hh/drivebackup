package drivebackup.gdrive.calls;

import java.io.IOException;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import drivebackup.gdrive.OriginMD5ChecksumAccessor;
import drivebackup.local.LocalFile;

public class UpdateFileCall implements IOCallable<File>{
	private final LocalFile localFile;
	private final File fileToUpdate;
	private final Drive drive;
	
	public UpdateFileCall(LocalFile localFile, File fileToUpdate, Drive drive){
		this.localFile = localFile;
		this.fileToUpdate = fileToUpdate;
		this.drive = drive;
	}

	@Override
	public File call() throws IOException {
		OriginMD5ChecksumAccessor md5ChecksumAccessor = new OriginMD5ChecksumAccessor(fileToUpdate);
		md5ChecksumAccessor.set(localFile.getOriginMd5Checksum());
		AbstractInputStreamContent mediaContent = new InputStreamContent(null, localFile.getEncryptedInputStream());
		return drive.files().update(fileToUpdate.getId(), fileToUpdate, mediaContent).execute();
	}
	
	
}
