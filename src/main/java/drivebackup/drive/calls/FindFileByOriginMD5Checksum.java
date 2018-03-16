package drivebackup.drive.calls;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import drivebackup.drive.OriginMD5ChecksumAccessor;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FindFileByOriginMD5Checksum implements IOCallable<Optional<File>> {
  private final String originMd5Checksum;
  private final String md5ChecksumOfContent;
  private final Drive drive;

  public FindFileByOriginMD5Checksum(
      String originMd5Checksum, String md5ChecksumOfContent, Drive drive) {
    this.originMd5Checksum = originMd5Checksum;
    this.md5ChecksumOfContent = md5ChecksumOfContent;
    this.drive = drive;
  }

  @Override
  public Optional<File> call() throws IOException {
    try {
      String query =
          String.format(
              "properties has { key='%s' and value='%s' and visibility='PUBLIC' }",
              OriginMD5ChecksumAccessor.MD5_PROPERTY_NAME, originMd5Checksum);
      FileList fileList = drive.files().list().setQ(query).execute();
      List<File> files = fileList.getItems();
      return files
          .stream()
          .filter((file) -> file.getMd5Checksum().equals(md5ChecksumOfContent))
          .findFirst();
    } catch (GoogleJsonResponseException e) {
      if (e.getStatusCode() == 404) {
        return Optional.empty();
      } else {
        throw e;
      }
    }
  }
}
