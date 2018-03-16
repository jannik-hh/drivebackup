package drivebackup;

import static drivebackup.matchers.IsEqualToDriveBackupDirectory.isEqualToDriveBackupDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import drivebackup.local.LocalDirectoryImpl;
import drivebackup.local.LocalFileImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;

public class CopyServiceTest {

  @Test
  public void testCopyFilesToTarget() throws IOException {
    DriveBackupDirectory source = new LocalDirectoryImpl(new File("./src/test/resources/folder"));
    DriveBackupDirectory target = createCleanTarget();

    new CopyService().copy(source, target, (f) -> false);

    assertThat(target, isEqualToDriveBackupDirectory(source));
  }

  @Test
  public void testDeletesOtherFilesFromTarget() throws IOException {
    DriveBackupDirectory source = new LocalDirectoryImpl(new File("./src/test/resources/folder"));
    DriveBackupDirectory target = createCleanTarget();
    target.findOrCreateDirectory("extraDir");
    target.saveOrUpdateFile(new LocalFileImpl(new File("./src/test/resources/test.txt")));

    new CopyService().copy(source, target, (f) -> false);

    assertThat(target, isEqualToDriveBackupDirectory(source));
  }

  @Test
  public void testDoesNotCopyIgnoredFiles() throws IOException {
    DriveBackupDirectory source = new LocalDirectoryImpl(new File("./src/test/resources/folder"));
    DriveBackupDirectory target = createCleanTarget();

    new CopyService().copy(source, target, (f) -> f.getName().matches("file_.*txt"));

    assertFalse(target.findFile("file_1.txt").isPresent());
    assertTrue(target.findDirectory("subfolder_2").get().findFile("test3.txt").isPresent());
  }

  private DriveBackupDirectory createCleanTarget() throws IOException {
    File tempDir = Files.createTempDirectory("test").toFile();
    tempDir.deleteOnExit();
    return new LocalDirectoryImpl(tempDir);
  }
}
