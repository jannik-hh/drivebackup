package drivebackup.encryption;

import java.io.InputStream;

public class NoEncryptionService implements EncryptionService {

  @Override
  public InputStream encrypt(InputStream plain) {
    return plain;
  }

  @Override
  public InputStream decrypt(InputStream encrypted) {
    return encrypted;
  }
}
