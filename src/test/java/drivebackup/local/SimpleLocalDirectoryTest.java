package drivebackup.local;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

import drivebackup.encryption.NoEncryptionService;
import drivebackup.encryption.StringNoEncrytionService;
import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class SimpleLocalDirectoryTest {

  @Test
  public void testGetName() {
    File dir = new File("./src/test/resources/folder");
    LocalDirectoryImpl simpleLocalDirectory =
        new LocalDirectoryImpl(
            dir, new NoEncryptionService(), new StringNoEncrytionService(), (file) -> false);

    assertEquals(simpleLocalDirectory.getEncryptedName(), "folder");
  }

  @Test
  public void testGetFiles() throws IOException {
    File dir = new File("./src/test/resources/folder");
    LocalDirectoryImpl simpleLocalDirectory =
        new LocalDirectoryImpl(
            dir, new NoEncryptionService(), new StringNoEncrytionService(), (file) -> false);

    Stream<LocalFile> files = simpleLocalDirectory.getFiles();

    String file_1_path = new File("./src/test/resources/folder/file_1.txt").getPath();
    String file_2_path = new File("./src/test/resources/folder/file_2.txt").getPath();

    assertThat(
        files.map((localFile) -> localFile.getPath()).collect(Collectors.toList()),
        containsInAnyOrder(file_1_path, file_2_path));
  }

  @Test
  public void testGetSubDirectories() throws IOException {
    File dir = new File("./src/test/resources/folder");
    LocalDirectoryImpl simpleLocalDirectory =
        new LocalDirectoryImpl(
            dir, new NoEncryptionService(), new StringNoEncrytionService(), (file) -> false);

    Stream<LocalDirectory> subDir = simpleLocalDirectory.getSubDirectories();
    assertThat(
        subDir.map((d) -> d.getEncryptedName()).collect(Collectors.toList()),
        containsInAnyOrder("subfolder_1", "subfolder_2"));
  }

  @Test
  public void testGetFilesWithIgnoredFile() throws IOException {
    File dir = new File("./src/test/resources/folder");
    Predicate<File> fileIgnoredPredicate = (file) -> file.getName().equals("file_2.txt");
    LocalDirectoryImpl localDirectory =
        new LocalDirectoryImpl(
            dir, new NoEncryptionService(), new StringNoEncrytionService(), fileIgnoredPredicate);

    Stream<LocalFile> files = localDirectory.getFiles();

    File file_1 = new File("./src/test/resources/folder/file_1.txt");

    assertThat(
        files.map((localFile) -> localFile.getPath()).collect(Collectors.toList()),
        contains(file_1.getPath()));
  }

  @Test
  public void testGetSubDirectoriesWithIgnoredFile() throws IOException {
    File dir = new File("./src/test/resources/folder");
    Predicate<File> fileIgnoredPredicate = (file) -> file.getName().equals("subfolder_2");
    LocalDirectoryImpl localDirectory =
        new LocalDirectoryImpl(
            dir, new NoEncryptionService(), new StringNoEncrytionService(), fileIgnoredPredicate);

    Stream<LocalDirectory> subDir = localDirectory.getSubDirectories();

    assertThat(
        subDir.map((d) -> d.getEncryptedName()).collect(Collectors.toList()),
        contains("subfolder_1"));
  }
}
