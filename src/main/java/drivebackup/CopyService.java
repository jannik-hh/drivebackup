package drivebackup;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CopyService {
  final Logger logger = LogManager.getLogger("DriveBackup");

  public void copy(
      DriveBackupDirectory source,
      DriveBackupDirectory target,
      Predicate<Named> ignoreFilePredicate)
      throws IOException {
    backupFiles(source, target, ignoreFilePredicate);
    backupSubDirectories(source, target, ignoreFilePredicate);
    deleteOtherFilesAndDirectories(source, target);
  }

  private void backupFiles(
      DriveBackupDirectory source,
      DriveBackupDirectory target,
      Predicate<Named> ignoreFilePredicate)
      throws IOException {
    source
        .getFiles()
        .filter(ignoreFilePredicate.negate())
        .parallel()
        .forEach(
            (file) -> {
              try {
                target.saveOrUpdateFile(file);
              } catch (IOException e) {
                logger.warn("unable to save or update {}", file.getPath());
              }
            });
  }

  private void backupSubDirectories(
      DriveBackupDirectory source,
      DriveBackupDirectory target,
      Predicate<Named> ignoreFilePredicate)
      throws IOException {
    source
        .getSubDirectories()
        .filter(ignoreFilePredicate.negate())
        .parallel()
        .forEach(
            (subDir) -> {
              try {
                DriveBackupDirectory subGDir = target.findOrCreateDirectory(subDir.getName());
                copy(subDir, subGDir, ignoreFilePredicate);
              } catch (IOException e) {
                logger.warn("unable to copy {}", subDir.getName());
              }
            });
  }

  private void deleteOtherFilesAndDirectories(
      DriveBackupDirectory source, DriveBackupDirectory target) throws IOException {
    Collection<String> childrenNames =
        Stream.concat(
                source.getSubDirectories().map((subDir) -> subDir.getName()),
                source.getFiles().map((file) -> file.getName()))
            .collect(Collectors.toCollection(TreeSet::new));

    target.deleteAllExceptOf(childrenNames);
  }
}
