package drivebackup.drive.calls;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import drivebackup.DriveBackupFile;
import drivebackup.drive.OriginMD5ChecksumAccessor;
import java.io.IOException;

public class UpdateFileCall implements IOCallable<File> {
  private final DriveBackupFile driveBackupFile;
  private final File fileToUpdate;
  private final Drive drive;

  public UpdateFileCall(DriveBackupFile driveBackupFile, File fileToUpdate, Drive drive) {
    this.driveBackupFile = driveBackupFile;
    this.fileToUpdate = fileToUpdate;
    this.drive = drive;
  }

  @Override
  public File call() throws IOException {
    OriginMD5ChecksumAccessor md5ChecksumAccessor = new OriginMD5ChecksumAccessor(fileToUpdate);
    md5ChecksumAccessor.set(driveBackupFile.getOriginalMd5Checksum());
    AbstractInputStreamContent mediaContent =
        new InputStreamContent(null, driveBackupFile.getContent());
    return drive.files().update(fileToUpdate.getId(), fileToUpdate, mediaContent).execute();
  }
}
