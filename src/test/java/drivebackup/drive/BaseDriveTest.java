package drivebackup.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import drivebackup.DriveBackupFile;
import drivebackup.local.LocalFileImpl;
import java.io.IOException;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class BaseDriveTest {
  protected static final String TEST_DIR = String.format("DriveBackupTest/%s", UUID.randomUUID());
  protected static Drive googleDrive;
  protected DriveDirectory directory;

  @BeforeClass
  public static void beforeClass() throws IOException {
    googleDrive = DriveServiceFactory.getDriveService();
  }

  @Before
  public void before() throws IOException {
    directory = (DriveDirectory) DriveDirectory.findOrCreateFromPath(TEST_DIR, googleDrive);
  }

  @After
  public void after() throws IOException {
    deleteGDirectory(directory, googleDrive);
  }

  @BeforeClass
  public static void authenticate() {
    if (System.getenv("DRIVEBACKUP_CREDENTIAL_REFRESH_TOKEN") == null) {
      Credential credential = DriveServiceFactory.authorize();
      System.out.println(
          String.format(
              "export DRIVEBACKUP_CREDENTIAL_REFRESH_TOKEN='%s'", credential.getRefreshToken()));
    }
  }

  protected void deleteGDirectory(DriveDirectory gDir, Drive drive) throws IOException {
    drive.files().delete(gDir.getId()).execute();
  }

  protected DriveBackupFile localFile(String path) {
    return new LocalFileImpl(new java.io.File(path));
  }

  protected DriveBackupFile localFile() {
    return localFile("./src/test/resources/folder/file_1.txt");
  }
}
