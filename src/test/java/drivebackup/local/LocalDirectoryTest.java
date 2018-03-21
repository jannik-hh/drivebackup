package drivebackup.local;

import static drivebackup.matchers.IsEqualToDriveFile.isEqualToDriveFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import drivebackup.DriveBackupDirectory;
import drivebackup.DriveBackupFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;

public class LocalDirectoryTest {
  private DriveBackupDirectory directory;
  private static DriveBackupFile localFile1 =
      new LocalFileImpl(new File("./src/test/resources/folder/file_1.txt"));
  private static DriveBackupFile localFile2 =
      new LocalFileImpl(new File("./src/test/resources/folder/file_2.txt"));

  @Before
  public void before() throws IOException {
    File tempDir = Files.createTempDirectory("test").toFile();
    tempDir.deleteOnExit();
    directory = new LocalDirectoryImpl(tempDir);
  }

  @Test
  public void testGetName() {
    File dir = new File("./src/test/resources/folder");
    LocalDirectoryImpl simpleLocalDirectory = new LocalDirectoryImpl(dir);

    assertEquals("folder", simpleLocalDirectory.getName());
  }

  @Test
  public void testGetFiles() throws IOException {
    directory.saveOrUpdateFile(localFile1);
    directory.saveOrUpdateFile(localFile2);

    Stream<DriveBackupFile> files = directory.getFiles();

    List<DriveBackupFile> fileNames = files.collect(Collectors.toList());
    assertThat(
        fileNames,
        containsInAnyOrder(isEqualToDriveFile(localFile1), isEqualToDriveFile(localFile2)));
  }

  @Test
  public void testFindOrCreateDirectory() throws IOException {
    DriveBackupDirectory new_dir = directory.findOrCreateDirectory("subfolder_1");

    assertEquals("subfolder_1", new_dir.getName());
    assertEquals(directory.getPath() + "/subfolder_1", new_dir.getPath());
  }

  @Test
  public void testGetSubDirectories() throws IOException {
    directory.findOrCreateDirectory("subfolder_1");
    directory.findOrCreateDirectory("subfolder_2");

    List<String> subDirsNames =
        directory.getSubDirectories().map((d) -> d.getName()).collect(Collectors.toList());

    assertEquals(subDirsNames.size(), 2);
    assertThat(subDirsNames, containsInAnyOrder("subfolder_1", "subfolder_2"));
  }

  @Test
  public void testSaveOrUpdateFile() throws IOException {
    directory.saveOrUpdateFile(localFile1);

    Optional<DriveBackupFile> savedFile = directory.findFile(localFile1.getName());

    assertTrue(savedFile.isPresent());
    assertThat(savedFile.get(), isEqualToDriveFile(localFile1));
  }

  @Test
  public void testDeleteAllExceptOf() throws IOException {
    // to be deleted
    DriveBackupDirectory subfolder1 = directory.findOrCreateDirectory("subfolder_1");
    subfolder1.findOrCreateDirectory("another_folder");
    subfolder1.saveOrUpdateFile(localFile1);
    directory.saveOrUpdateFile(localFile1);

    // to be not deleted
    directory.findOrCreateDirectory("subfolder_2");
    directory.saveOrUpdateFile(localFile2);

    directory.deleteAllExceptOf(Arrays.asList("subfolder_2", localFile2.getName()));

    List<String> subDirsNames =
        directory.getSubDirectories().map((d) -> d.getName()).collect(Collectors.toList());
    assertEquals(Arrays.asList("subfolder_2"), subDirsNames);

    List<DriveBackupFile> files = directory.getFiles().collect(Collectors.toList());
    assertEquals(1, files.size());
    assertThat(files, containsInAnyOrder(isEqualToDriveFile(localFile2)));
  }
}
