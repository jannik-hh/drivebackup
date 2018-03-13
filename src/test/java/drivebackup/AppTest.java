package drivebackup;

import static drivebackup.matchers.IsEqualToDriveBackupDirectory.isEqualToDriveBackupDirectory;
import static org.junit.Assert.assertThat;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import drivebackup.drive.DriveServiceFactory;
import drivebackup.local.LocalDirectoryImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Test;

public class AppTest {
  private static final String TEST_DIR = String.format("DriveBackupTest/%s", UUID.randomUUID());

  @Test
  public void testBackupAndRecover() throws NoSuchAlgorithmException, ParseException, IOException {
    File source = new File("./src/test/resources/folder");
    backup(source);
    listContent();
    File target = createTempDir();
    recover(target);

    DriveBackupDirectory sourceAsDriveDir = new LocalDirectoryImpl(new File(source, "subfolder_2"));
    DriveBackupDirectory targetAsDriveDir = new LocalDirectoryImpl(target);

    assertThat(targetAsDriveDir, isEqualToDriveBackupDirectory(sourceAsDriveDir));
  }

  private void backup(File source) {
    String[] args =
        new String[] {
          "backup",
          "-source",
          source.getAbsolutePath(),
          "-target",
          TEST_DIR,
          "-encrypt",
          "-encryptNames",
          "-secretKey",
          "./src/test/resources/drivebackup_encryption.key"
        };
    App.main(args);
  }

  private void listContent() {
    String[] args =
        new String[] {
          "listContent",
          "-baseBackupDir",
          TEST_DIR,
          "-path",
          "subfolder_2",
          "-decryptNames",
          "-secretKey",
          "./src/test/resources/drivebackup_encryption.key"
        };
    App.main(args);
  }

  private void recover(File targetDir) {
    String[] args =
        new String[] {
          "recover",
          "-baseBackupDir",
          TEST_DIR,
          "-recover",
          "subfolder_2",
          "-target",
          targetDir.getAbsolutePath(),
          "-decrypt",
          "-decryptNames",
          "-secretKey",
          "./src/test/resources/drivebackup_encryption.key"
        };
    App.main(args);
  }

  private File createTempDir() throws IOException {
    java.io.File tempDir = Files.createTempDirectory("test").toFile();
    tempDir.deleteOnExit();
    return tempDir;
  }

  @After
  public void deleteTestDir() throws IOException {
    Drive drive = DriveServiceFactory.getDriveService();
    String query =
        String.format(
            "title = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed=false",
            "DriveBackupTest");
    FileList fileList = drive.files().list().setQ(query).execute();
    for (com.google.api.services.drive.model.File file : fileList.getItems()) {
      drive.files().delete(file.getId()).execute();
    }
  }
}
