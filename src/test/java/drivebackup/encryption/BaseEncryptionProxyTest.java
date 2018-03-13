package drivebackup.encryption;

import drivebackup.DriveBackupDirectory;
import drivebackup.DriveBackupFile;
import drivebackup.local.LocalDirectoryImpl;
import drivebackup.local.LocalFileImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.Before;

public class BaseEncryptionProxyTest {
  protected DriveBackupDirectory directory;
  protected static DriveBackupFile localFile1 =
      new LocalFileImpl(new File("./src/test/resources/folder/file_1.txt"));
  protected static DriveBackupFile localFile2 =
      new LocalFileImpl(new File("./src/test/resources/folder/file_2.txt"));

  @Before
  public void before() throws IOException, NoSuchAlgorithmException {
    File tempDir = Files.createTempDirectory("test").toFile();
    tempDir.deleteOnExit();
    DriveBackupDirectory localDirectory = new LocalDirectoryImpl(tempDir);
    SecretKey secretKey = generateSecretKey();
    EncryptionService encryptionService = new AESEncryptionService(secretKey);
    StringEncryptionService stringEncryptionService =
        new StringAESAndBase64EncryptionService(secretKey);
    directory =
        new EncryptionDirectoryProxy(localDirectory, encryptionService, stringEncryptionService);
  }

  private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(128);
    return kg.generateKey();
  }
}
