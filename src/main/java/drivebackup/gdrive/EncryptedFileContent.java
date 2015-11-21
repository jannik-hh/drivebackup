package drivebackup.gdrive;


import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.util.Preconditions;

import drivebackup.encryption.EncryptionService;
import drivebackup.local.LocalFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

public final class EncryptedFileContent extends AbstractInputStreamContent {

  private final LocalFile file;
  private final EncryptionService encryptionService;

  public EncryptedFileContent(String type, LocalFile file, EncryptionService encryptionService) {
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
    return encryptionService.encrypt(file.getInputStream());
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