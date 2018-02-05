package drivebackup.gdrive;

import com.google.api.services.drive.Drive;
import drivebackup.encryption.NoEncryptionService;
import drivebackup.encryption.StringNoEncrytionService;
import drivebackup.local.LocalFile;
import drivebackup.local.LocalFileImpl;
import java.io.IOException;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;

public class BaseGDirTest extends BaseGDriveTest {
  private static final String TEST_DIR = String.format("DriveBackupTest/%s", UUID.randomUUID());
  protected Drive googleDrive;
  protected GDirectory gDir;

  @Before
  public void before() throws IOException {
    googleDrive = DriveServiceFactory.getDriveService();
    gDir = DefaultGDirectory.fromPath(TEST_DIR, googleDrive);
  }

  @After
  public void after() throws IOException {
    deleteGDirectory(gDir, googleDrive);
  }

  protected void deleteGDirectory(GDirectory gDir, Drive drive) throws IOException {
    drive.files().delete(gDir.getID()).execute();
  }

  protected LocalFile localFile(String path) {
    return new LocalFileImpl(
        new java.io.File(path), new NoEncryptionService(), new StringNoEncrytionService());
  }
}
