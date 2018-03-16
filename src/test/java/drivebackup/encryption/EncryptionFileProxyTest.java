package drivebackup.encryption;

import static org.junit.Assert.assertEquals;

import drivebackup.DriveBackupFile;
import java.io.IOException;
import org.junit.Test;

public class EncryptionFileProxyTest extends BaseEncryptionProxyTest {
  @Test
  public void testGetPath() throws IOException {
    EncryptionFileProxy file = buildEncryptionFileProxy(localFile1);
    String path = file.getPath();

    String expectedPath = directory.getPath() + "/" + localFile1.getName();
    assertEquals(expectedPath, path);
  }

  private EncryptionFileProxy buildEncryptionFileProxy() throws IOException {
    return buildEncryptionFileProxy(localFile1);
  }

  private EncryptionFileProxy buildEncryptionFileProxy(DriveBackupFile file) throws IOException {
    directory.saveOrUpdateFile(file);
    return (EncryptionFileProxy) directory.findFile(file.getName()).get();
  }
}
