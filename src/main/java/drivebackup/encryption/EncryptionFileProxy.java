package drivebackup.encryption;

import drivebackup.DriveBackupFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public class EncryptionFileProxy implements DriveBackupFile {
  private final DriveBackupFile driveBackupFile;
  private final Function<InputStream, InputStream> cipherFunction;
  private final Function<String, String> stringCipherFunction;
  private final EncryptionDirectoryProxy parent;
  private String md5Checksum;

  EncryptionFileProxy(
      DriveBackupFile driveBackupFile,
      EncryptionDirectoryProxy parent,
      Function<InputStream, InputStream> cipherFunction,
      Function<String, String> stringCipherFunction) {
    this.driveBackupFile = driveBackupFile;
    this.parent = parent;
    this.cipherFunction = cipherFunction;
    this.stringCipherFunction = stringCipherFunction;
  }

  @Override
  public String getName() {
    return stringCipherFunction.apply(driveBackupFile.getName());
  }

  @Override
  public String getPath() {
    return parent.getPath() + "/" + getName();
  }

  @Override
  public String getId() {
    return driveBackupFile.getId();
  }

  @Override
  public InputStream getContent() throws IOException {
    return cipherFunction.apply(driveBackupFile.getContent());
  }

  @Override
  public String getOriginalMd5Checksum() throws IOException {
    return driveBackupFile.getOriginalMd5Checksum();
  }

  @Override
  public String getMd5Checksum() throws IOException {
    if (md5Checksum == null) {
      md5Checksum = org.apache.commons.codec.digest.DigestUtils.md5Hex(getContent());
    }
    return md5Checksum;
  }
}
