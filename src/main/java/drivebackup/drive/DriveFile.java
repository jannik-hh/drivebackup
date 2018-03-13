package drivebackup.drive;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import drivebackup.DriveBackupFile;
import java.io.IOException;
import java.io.InputStream;

public class DriveFile implements DriveBackupFile {
  private final File remoteFile;
  private final DriveDirectory parent;
  private final Drive drive;

  DriveFile(File remoteFile, DriveDirectory parent, Drive drive) {
    this.remoteFile = remoteFile;
    this.parent = parent;
    this.drive = drive;
  }

  @Override
  public String getName() {
    return remoteFile.getTitle();
  }

  @Override
  public String getPath() {
    return parent.getPath() + "/" + getName();
  }

  @Override
  public String getId() {
    return remoteFile.getId();
  }

  @Override
  public InputStream getContent() throws IOException {
    HttpResponse resp =
        drive
            .getRequestFactory()
            .buildGetRequest(new GenericUrl(remoteFile.getDownloadUrl()))
            .execute();
    return resp.getContent();
  }

  @Override
  public String getOriginalMd5Checksum() throws IOException {
    return new OriginMD5ChecksumAccessor(remoteFile).get().orElse(getMd5Checksum());
  }

  @Override
  public String getMd5Checksum() throws IOException {
    return remoteFile.getMd5Checksum();
  }

  public File getRemoteFile() {
    return remoteFile;
  }
}
