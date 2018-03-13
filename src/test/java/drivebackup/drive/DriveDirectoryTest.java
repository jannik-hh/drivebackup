package drivebackup.drive;

import static drivebackup.matchers.IsEqualToDriveFile.isEqualToDriveFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

import drivebackup.DriveBackupDirectory;
import drivebackup.DriveBackupFile;
import drivebackup.local.LocalFileImpl;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class DriveDirectoryTest extends BaseDriveTest {
  private static DriveBackupFile localFile1 =
      new LocalFileImpl(new File("./src/test/resources/folder/file_1.txt"));
  private static DriveBackupFile localFile2 =
      new LocalFileImpl(new File("./src/test/resources/folder/file_2.txt"));

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
  public void testFindOrCreateDirectoryWithDirectoryAlreadyExits() throws IOException {
    DriveBackupDirectory new_dir = directory.findOrCreateDirectory("subfolder_1");

    DriveBackupDirectory found = directory.findOrCreateDirectory("subfolder_1");

    assertEquals(new_dir.getName(), found.getName());
    assertEquals(new_dir.getPath(), found.getPath());
    assertEquals(new_dir.getId(), found.getId());
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
  public void testSaveOrUpdateFileNothingChanged() throws IOException {
    DriveBackupFile file = localFile("./src/test/resources/test.txt");

    directory.saveOrUpdateFile(file);
    DriveBackupFile createdFile = directory.findFile(file.getName()).get();

    directory.saveOrUpdateFile(file);
    DriveBackupFile updatedFile = directory.findFile(file.getName()).get();

    assertNotNull(updatedFile);
    assertEquals(updatedFile.getMd5Checksum(), file.getMd5Checksum());
    assertEquals(createdFile.getId(), updatedFile.getId());
    assertEquals(createdFile.getMd5Checksum(), updatedFile.getMd5Checksum());
  }

  @Test
  public void testSaveOrUpdateFileSomethingChanged() throws IOException {
    DriveBackupFile localFile = localFile("./src/test/resources/test.txt");
    directory.saveOrUpdateFile(localFile);
    DriveBackupFile createdFile = directory.findFile(localFile.getName()).get();

    DriveBackupFile updatedLocalFile = localFile("./src/test/resources/changed/test.txt");
    directory.saveOrUpdateFile(updatedLocalFile);
    DriveBackupFile updatedFile = directory.findFile(localFile.getName()).get();

    assertNotNull(updatedFile);
    assertEquals(updatedFile.getMd5Checksum(), updatedLocalFile.getMd5Checksum());
    assertEquals(createdFile.getId(), updatedFile.getId());
    assertNotEquals(createdFile.getMd5Checksum(), updatedFile.getMd5Checksum());
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

    directory.deleteAllExceptOf(List.of("subfolder_2", localFile2.getName()));

    List<String> subDirsNames =
        directory.getSubDirectories().map((d) -> d.getName()).collect(Collectors.toList());
    assertEquals(List.of("subfolder_2"), subDirsNames);

    List<DriveBackupFile> files = directory.getFiles().collect(Collectors.toList());
    assertEquals(1, files.size());
    assertThat(files, containsInAnyOrder(isEqualToDriveFile(localFile2)));
  }
}
