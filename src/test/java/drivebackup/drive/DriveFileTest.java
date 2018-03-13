package drivebackup.drive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import drivebackup.DriveBackupFile;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class DriveFileTest extends BaseDriveTest {
  @Test
  public void testGetPath() throws IOException {
    DriveBackupFile file = localFile();
    DriveFile driveFile = buildDriveFile(file);

    assertEquals(driveFile.getPath(), "/" + TEST_DIR + "/" + localFile().getName());
  }

  @Test
  public void testGetId() throws IOException {
    DriveFile file = buildDriveFile();

    assertNotNull(file.getId());
  }

  @Test
  public void testGetContent() throws IOException {
    DriveBackupFile file = localFile();
    DriveFile driveFile = buildDriveFile(file);

    InputStream content = driveFile.getContent();
    assertEquals(md5Checksum(file.getContent()), md5Checksum(content));
  }

  private DriveFile buildDriveFile() throws IOException {
    return buildDriveFile(localFile());
  }

  private DriveFile buildDriveFile(DriveBackupFile file) throws IOException {
    directory.saveOrUpdateFile(file);
    return (DriveFile) directory.findFile(file.getName()).get();
  }

  private String md5Checksum(InputStream in) throws IOException {
    return org.apache.commons.codec.digest.DigestUtils.md5Hex(in);
  }
}
