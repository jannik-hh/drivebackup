package drivebackup.encryption;

import drivebackup.DriveBackupDirectory;
import drivebackup.DriveBackupFile;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EncryptionDirectoryProxy implements DriveBackupDirectory {
  private final DriveBackupDirectory directory;
  private final EncryptionService encryptionService;
  private final StringEncryptionService stringEncryptionService;
  private final EncryptionDirectoryProxy parentProxy;

  public EncryptionDirectoryProxy(
      DriveBackupDirectory directory,
      EncryptionService encryptionService,
      StringEncryptionService stringEncryptionService) {
    this(directory, encryptionService, stringEncryptionService, null);
  }

  private EncryptionDirectoryProxy(
      DriveBackupDirectory directory,
      EncryptionService encryptionService,
      StringEncryptionService stringEncryptionService,
      EncryptionDirectoryProxy parentProxy) {
    this.directory = directory;
    this.encryptionService = encryptionService;
    this.stringEncryptionService = stringEncryptionService;
    this.parentProxy = parentProxy;
  }

  @Override
  public String getPath() {
    if (isRootProxy()) {
      return directory.getPath();
    } else {
      return parentProxy.getPath() + "/" + getName();
    }
  }

  @Override
  public String getName() {
    if (isRootProxy()) {
      return directory.getName();
    } else {
      return stringEncryptionService.decrypt().apply(directory.getName());
    }
  }

  @Override
  public String getId() {
    return directory.getId();
  }

  @Override
  public void saveOrUpdateFile(DriveBackupFile file) throws IOException {
    DriveBackupFile encryptedFile =
        new EncryptionFileProxy(
            file, this, encryptionService.encrypt(), stringEncryptionService.encrypt());
    directory.saveOrUpdateFile(encryptedFile);
  }

  @Override
  public DriveBackupDirectory findOrCreateDirectory(String name) throws IOException {
    String encryptedName = stringEncryptionService.encrypt().apply(name);
    return toEncryptionDirectoryProxy().apply(directory.findOrCreateDirectory(encryptedName));
  }

  @Override
  public void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException {
    Collection<String> encryptedFileAndDirectoryNames =
        fileAndDirectoryNames
            .stream()
            .map((name) -> stringEncryptionService.encrypt().apply(name))
            .collect(Collectors.toList());
    directory.deleteAllExceptOf(encryptedFileAndDirectoryNames);
  }

  @Override
  public Stream<DriveBackupFile> getFiles() throws IOException {
    return directory.getFiles().map(toDecryptionFileProxy());
  }

  @Override
  public Stream<DriveBackupDirectory> getSubDirectories() throws IOException {
    return directory.getSubDirectories().map(toEncryptionDirectoryProxy());
  }

  @Override
  public Optional<DriveBackupFile> findFile(String name) throws IOException {
    return directory
        .findFile(stringEncryptionService.encrypt().apply(name))
        .map(toDecryptionFileProxy());
  }

  @Override
  public Optional<DriveBackupDirectory> findDirectory(String name) throws IOException {
    return directory
        .findDirectory(stringEncryptionService.encrypt().apply(name))
        .map(toEncryptionDirectoryProxy());
  }

  private Function<DriveBackupDirectory, EncryptionDirectoryProxy> toEncryptionDirectoryProxy() {
    return (DriveBackupDirectory directory) ->
        new EncryptionDirectoryProxy(directory, encryptionService, stringEncryptionService, this);
  }

  private Function<DriveBackupFile, EncryptionFileProxy> toDecryptionFileProxy() {
    return (DriveBackupFile file) ->
        new EncryptionFileProxy(
            file, this, encryptionService.decrypt(), stringEncryptionService.decrypt());
  }

  private boolean isRootProxy() {
    return parentProxy == null;
  }
}
