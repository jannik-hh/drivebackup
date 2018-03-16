package drivebackup.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.collect.ImmutableList;
import drivebackup.DriveBackupDirectory;
import drivebackup.DriveBackupFile;
import drivebackup.drive.calls.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DriveDirectory implements DriveBackupDirectory {
  private static final Predicate<File> IS_FOLDER =
      (file) -> file.getMimeType().equals("application/vnd.google-apps.folder");
  private static final Logger logger = LogManager.getLogger("DriveBackup");
  private final Drive drive;
  private final String id;
  private final String name;
  private final DriveDirectory parent;
  private List<File> children;

  public static DriveBackupDirectory findOrCreateFromPath(String directoryPath, Drive drive)
      throws IOException {
    DriveBackupDirectory gDirectory = new DriveDirectory(drive, "root", null, null);
    String[] subDirs = directoryPath.split("/");
    for (String subDir : subDirs) {
      if (!subDir.trim().isEmpty()) {
        gDirectory = gDirectory.findOrCreateDirectory(subDir);
      }
    }
    return gDirectory;
  }

  public static Optional<DriveBackupDirectory> findFromPath(String directoryPath, Drive drive)
      throws IOException {
    Optional<DriveBackupDirectory> gDirectory =
        Optional.of(new DriveDirectory(drive, "root", null, null));
    String[] subDirs = directoryPath.split("/");
    for (String subDir : subDirs) {
      if (!subDir.trim().isEmpty() && gDirectory.isPresent()) {
        gDirectory = gDirectory.get().findDirectory(subDir);
      }
    }
    return gDirectory;
  }

  private DriveDirectory(Drive drive, String id, String name, DriveDirectory parent) {
    this.drive = drive;
    this.id = id;
    this.name = name;
    this.parent = parent;
  }

  @Override
  public void saveOrUpdateFile(DriveBackupFile file) throws IOException {
    Optional<File> remoteFile =
        getFileChildren().filter((child) -> child.getTitle().equals(file.getName())).findFirst();
    if (remoteFile.isPresent()) {
      File existentRemoteFile = remoteFile.get();
      if (needsUpdate(file, existentRemoteFile)) {
        updateFile(existentRemoteFile, file);
      } else {
        logger.info("{} is up-to-date", file.getPath());
      }
    } else {
      saveFile(file);
    }
  }

  @Override
  public DriveBackupDirectory findOrCreateDirectory(String name) throws IOException {
    Optional<DriveBackupDirectory> dir = findDirectory(name);
    if (dir.isPresent()) {
      return dir.get();
    } else {
      return createDirectory(name);
    }
  }

  @Override
  public void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException {
    List<File> children = getChildren();
    for (File file : children) {
      String title = file.getTitle();
      if (!fileAndDirectoryNames.contains(title)) {
        QueryExecutorWithRetry.executeWithRetry(() -> drive.files().trash(file.getId()).execute());
        removeChild(file);
        logger.info("{} trashed", title);
      }
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getId() {
    return id;
  }

  public String getPath() {
    if (isRoot()) {
      return "/";
    } else {
      if (parent.isRoot()) {
        return "/" + name;
      } else {
        return parent.getPath() + "/" + name;
      }
    }
  }

  @Override
  public Stream<DriveBackupFile> getFiles() throws IOException {
    return getFileChildren().map(fileToDriveFile());
  }

  @Override
  public Stream<DriveBackupDirectory> getSubDirectories() throws IOException {
    return getDirectoryChildren().map(fileToDriveDirectory());
  }

  private DriveBackupDirectory createDirectory(String name) throws IOException {
    File newDir = QueryExecutorWithRetry.executeWithRetry(new CreateDirectoryCall(name, id, drive));
    addChild(newDir);
    return fileToDriveDirectory().apply(newDir);
  }

  private File saveFile(DriveBackupFile driveBackupFile) throws IOException {
    Optional<File> fileWithSameMD5 =
        QueryExecutorWithRetry.executeWithRetry(
            new FindFileByOriginMD5Checksum(
                driveBackupFile.getOriginalMd5Checksum(), driveBackupFile.getMd5Checksum(), drive));
    File savedFile;
    if (fileWithSameMD5.isPresent()) {
      savedFile =
          QueryExecutorWithRetry.executeWithRetryAndLogTime(
              new CopyFileCall(fileWithSameMD5.get(), id, driveBackupFile.getName(), drive),
              String.format("%s saved by copying", driveBackupFile.getPath()));
    } else {
      savedFile =
          QueryExecutorWithRetry.executeWithRetryAndLogTime(
              new SaveFileCall(driveBackupFile, id, drive),
              String.format("%s saved", driveBackupFile.getPath()));
    }
    addChild(savedFile);
    return savedFile;
  }

  private File updateFile(File remoteFile, DriveBackupFile driveBackupFile) throws IOException {
    File updatedRemoteFile =
        QueryExecutorWithRetry.executeWithRetryAndLogTime(
            new UpdateFileCall(driveBackupFile, remoteFile, drive),
            String.format("%s updated", driveBackupFile.getPath()));
    removeChild(remoteFile);
    addChild(updatedRemoteFile);
    return updatedRemoteFile;
  }

  private boolean needsUpdate(DriveBackupFile driveBackupFile, File gFile) throws IOException {
    OriginMD5ChecksumAccessor md5ChecksumAccessor = new OriginMD5ChecksumAccessor(gFile);
    Optional<String> optional = md5ChecksumAccessor.get();
    if (optional.isPresent()) {
      String checksum = optional.get();
      return !checksum.equals(driveBackupFile.getOriginalMd5Checksum());
    } else {
      return true;
    }
  }

  private Function<File, DriveBackupFile> fileToDriveFile() {
    return (file) -> new DriveFile(file, this, drive);
  }

  private Function<File, DriveDirectory> fileToDriveDirectory() {
    return (file) -> new DriveDirectory(drive, file.getId(), file.getTitle(), this);
  }

  private boolean isRoot() {
    return parent == null;
  }

  private Stream<File> getFileChildren() throws IOException {
    return getChildren().stream().filter(IS_FOLDER.negate());
  }

  private Stream<File> getDirectoryChildren() throws IOException {
    return getChildren().stream().filter(IS_FOLDER);
  }

  private List<File> getChildren() throws IOException {
    initChildren();
    return ImmutableList.copyOf(children);
  }

  private void removeChild(File child) throws IOException {
    initChildren();
    children.remove(child);
  }

  private void addChild(File child) throws IOException {
    initChildren();
    children.add(child);
  }

  private void initChildren() throws IOException {
    if (children == null) {
      children = QueryExecutorWithRetry.executeWithRetry(new GetChildrenOfDirectoryCall(id, drive));
    }
  }
}
