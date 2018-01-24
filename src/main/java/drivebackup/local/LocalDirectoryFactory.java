package drivebackup.local;

import drivebackup.encryption.EncryptionService;
import drivebackup.encryption.StringEncryptionService;
import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

public class LocalDirectoryFactory {
  public LocalDirectory getLocalDirectory(
      String pathToDirectory,
      String pathToIgnoreFile,
      EncryptionService encryptionService,
      StringEncryptionService stringEncryptionService)
      throws IOException {
    if (pathToIgnoreFile == null) {
      return new LocalDirectoryImpl(
          new File(pathToDirectory), encryptionService, stringEncryptionService, (file) -> false);
    } else {
      Predicate<File> fileIgnoredPredicate = new FileIgnoredPredicate(new File(pathToIgnoreFile));
      return new LocalDirectoryImpl(
          new File(pathToDirectory),
          encryptionService,
          stringEncryptionService,
          fileIgnoredPredicate);
    }
  }
}
