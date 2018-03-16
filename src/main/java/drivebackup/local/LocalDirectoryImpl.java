package drivebackup.local;

import drivebackup.DriveBackupDirectory;
import drivebackup.DriveBackupFile;
import drivebackup.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;

public class LocalDirectoryImpl implements DriveBackupDirectory {
  private final File directory;

  public LocalDirectoryImpl(File directory) {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(String.format("%s must be a directory", directory));
    }
    this.directory = directory;
  }

  @Override
  public String getPath() {
    return directory.getPath();
  }

  @Override
  public String getName() {
    return directory.getName();
  }

  @Override
  public String getId() {
    return getPath();
  }

  @Override
  public void saveOrUpdateFile(DriveBackupFile file) throws IOException {
    File targetFile = new File(directory, file.getName());
    InputStream contentStream = file.getContent();
    Files.copy(file.getContent(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    IOUtils.closeQuietly(contentStream);
  }

  @Override
  public DriveBackupDirectory findOrCreateDirectory(String name) throws IOException {
    File dir = new File(directory, name);
    dir.mkdir();
    return new LocalDirectoryImpl(dir);
  }

  @Override
  public void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException {
    Predicate<Named> toBeDeleted = (named) -> !fileAndDirectoryNames.contains(named.getName());
    for (DriveBackupFile file : getFiles().filter(toBeDeleted).collect(Collectors.toList())) {
      deleteFile(file);
    }
    for (DriveBackupDirectory dir :
        getSubDirectories().filter(toBeDeleted).collect(Collectors.toList())) {
      dir.deleteAllExceptOf(Collections.emptyList());
      deleteFile(((LocalDirectoryImpl) dir).directory);
    }
  }

  @Override
  public Stream<DriveBackupFile> getFiles() throws IOException {
    return getChildren().filter((file) -> file.isFile()).map((file) -> new LocalFileImpl(file));
  }

  @Override
  public Stream<DriveBackupDirectory> getSubDirectories() throws IOException {
    return getChildren()
        .filter((file) -> file.isDirectory())
        .map((dir) -> new LocalDirectoryImpl(dir));
  }

  private Stream<File> getChildren() throws IOException {
    return Files.list(directory.toPath()).map((path) -> path.toFile());
  }

  private void deleteFile(DriveBackupFile file) throws IOException {
    deleteFile(new File(file.getPath()));
  }

  private void deleteFile(File file) throws IOException {
    if (!file.delete()) {
      throw new IOException(String.format("could not delete %s", file.getPath()));
    }
  }
}
