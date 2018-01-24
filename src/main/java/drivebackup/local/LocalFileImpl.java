package drivebackup.local;

import drivebackup.encryption.EncryptionService;
import drivebackup.encryption.StringEncryptionService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class LocalFileImpl implements LocalFile {
  private final File file;
  private final EncryptionService encryptionService;
  private final StringEncryptionService stringEncryptionService;
  private String md5Checksum;

  public LocalFileImpl(
      File file,
      EncryptionService encryptionService,
      StringEncryptionService stringEncryptionService) {
    this.file = file;
    this.encryptionService = encryptionService;
    this.stringEncryptionService = stringEncryptionService;
  }

  public String getName() {
    return stringEncryptionService.decrypt(file.getName());
  }

  @Override
  public String getEncryptedName() {
    return stringEncryptionService.encrypt(file.getName());
  }

  @Override
  public String getPath() {
    return file.getPath();
  }

  @Override
  public InputStream getEncryptedInputStream() throws FileNotFoundException {
    return encryptionService.encrypt(new FileInputStream(file));
  }

  @Override
  public InputStream getDecryptedInputStream() throws FileNotFoundException {
    return encryptionService.decrypt(new FileInputStream(file));
  }

  @Override
  public String getOriginMd5Checksum() throws IOException {
    if (md5Checksum == null) {
      md5Checksum = calculateMd5Checksum();
    }
    return md5Checksum;
  }

  private String calculateMd5Checksum() throws IOException {
    InputStream inputStream = new FileInputStream(file);
    return org.apache.commons.codec.digest.DigestUtils.md5Hex(inputStream);
  }
}
