package drivebackup;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import drivebackup.gdrive.DriveServiceFactory;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Test;

public class AppTest {
  private static final String TEST_DIR = "DriveBackupTest";

  @Test
  public void testDefault() throws NoSuchAlgorithmException, ParseException, IOException {
    String[] args =
        new String[] {"backup", "-source", "./src/test/resources/folder", "-target", TEST_DIR};
    App.main(args);
  }

  @After
  public void deleteTestDir() throws IOException {
    Drive drive = DriveServiceFactory.getDriveService();
    String query =
        String.format(
            "title = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed=false",
            TEST_DIR);
    FileList fileList = drive.files().list().setQ(query).execute();
    for (File file : fileList.getItems()) {
      drive.files().delete(file.getId()).execute();
    }
  }
}
