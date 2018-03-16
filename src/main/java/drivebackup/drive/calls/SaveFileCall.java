package drivebackup.drive.calls;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import drivebackup.DriveBackupFile;
import drivebackup.drive.OriginMD5ChecksumAccessor;
import java.io.IOException;
import java.util.Arrays;

public class SaveFileCall implements IOCallable<File> {
  private final DriveBackupFile driveBackupFile;
  private final String parentId;
  private final Drive drive;

  public SaveFileCall(DriveBackupFile driveBackupFile, String parentId, Drive drive) {
    this.driveBackupFile = driveBackupFile;
    this.parentId = parentId;
    this.drive = drive;
  }

  @Override
  public File call() throws IOException {
    File body = new File();
    body.setTitle(driveBackupFile.getName());
    body.setParents(Arrays.asList(new ParentReference().setId(parentId)));
    OriginMD5ChecksumAccessor md5ChecksumAccessor = new OriginMD5ChecksumAccessor(body);
    md5ChecksumAccessor.set(driveBackupFile.getOriginalMd5Checksum());

    AbstractInputStreamContent mediaContent =
        new InputStreamContent(null, driveBackupFile.getContent());
    return drive.files().insert(body, mediaContent).execute();
  }
}
