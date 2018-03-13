package drivebackup;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface DriveBackupDirectory extends Named {
  String getPath();

  String getName();

  String getId();

  void saveOrUpdateFile(DriveBackupFile file) throws IOException;

  DriveBackupDirectory findOrCreateDirectory(String name) throws IOException;

  void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException;

  Stream<DriveBackupFile> getFiles() throws IOException;

  Stream<DriveBackupDirectory> getSubDirectories() throws IOException;

  default Optional<DriveBackupFile> findFile(String name) throws IOException {
    return find(getFiles(), (file) -> file.getName().equals(name));
  }

  default Optional<DriveBackupDirectory> findDirectory(String name) throws IOException {
    return find(getSubDirectories(), (file) -> file.getName().equals(name));
  }

  private <T> Optional<T> find(Stream<T> stream, Predicate<T> predicate) {
    return stream.filter(predicate).findFirst();
  }
}
