package drivebackup.gdrive;


import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.util.Preconditions;

import drivebackup.encryption.EncryptionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public final class EncryptedFileContent extends AbstractInputStreamContent {

  private final File file;
  private final EncryptionService encryptionService;

  public EncryptedFileContent(String type, File file, EncryptionService encryptionService) {
    super(type);
    this.file = Preconditions.checkNotNull(file);
    this.encryptionService = encryptionService;
  }

  public long getLength() {
    return -1;
  }

  public boolean retrySupported() {
    return true;
  }

  @Override
  public InputStream getInputStream() throws FileNotFoundException {
    return encryptionService.encrypt(new FileInputStream(file));
  }

  @Override
  public EncryptedFileContent setType(String type) {
    return (EncryptedFileContent) super.setType(type);
  }

  @Override
  public EncryptedFileContent setCloseInputStream(boolean closeInputStream) {
    return (EncryptedFileContent) super.setCloseInputStream(closeInputStream);
  }
}