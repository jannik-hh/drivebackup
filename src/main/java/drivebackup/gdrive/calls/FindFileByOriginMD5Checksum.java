package drivebackup.gdrive.calls;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import drivebackup.gdrive.OriginMD5ChecksumAccessor;
import java.io.IOException;
import java.util.List;

public class FindFileByOriginMD5Checksum implements IOCallable<File> {
  private final String md5checksum;
  private final Drive drive;

  public FindFileByOriginMD5Checksum(String md5checksum, Drive drive) {
    this.md5checksum = md5checksum;
    this.drive = drive;
  }

  @Override
  public File call() throws IOException {
    String query =
        String.format(
            "properties has { key='%s' and value='%s' and visibility='PUBLIC' }",
            OriginMD5ChecksumAccessor.MD5_PROPERTY_NAME, md5checksum);
    FileList fileList = drive.files().list().setQ(query).execute();
    List<File> files = fileList.getItems();
    if (files != null && files.size() >= 1) {
      return files.get(0);

    } else {
      return null;
    }
  }
}
