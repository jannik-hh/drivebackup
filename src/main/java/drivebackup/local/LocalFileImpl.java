package drivebackup.local;

import drivebackup.DriveBackupFile;
import java.io.*;

public class LocalFileImpl implements DriveBackupFile {
  private final File file;
  private String md5Checksum;

  public LocalFileImpl(File file) {
    this.file = file;
  }

  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public String getPath() {
    return file.getPath();
  }

  @Override
  public String getId() {
    return getPath();
  }

  @Override
  public InputStream getContent() throws FileNotFoundException {
    return new FileInputStream(file);
  }

  @Override
  public String getOriginalMd5Checksum() throws IOException {
    if (md5Checksum == null) {
      md5Checksum = org.apache.commons.codec.digest.DigestUtils.md5Hex(getContent());
    }
    return md5Checksum;
  }

  @Override
  public String getMd5Checksum() throws IOException {
    return getOriginalMd5Checksum();
  }
}
