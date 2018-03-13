package drivebackup.cli;

import com.google.api.services.drive.Drive;
import drivebackup.DriveBackupDirectory;
import drivebackup.Named;
import drivebackup.drive.DriveDirectory;
import drivebackup.drive.DriveServiceFactory;
import drivebackup.encryption.*;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import javax.crypto.SecretKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ListContentCommand {
  private final String baseSourcePath;
  private final String path;
  private final boolean decryptNames;
  private final String secretKeyPath;
  private SecretKey secretKey;
  private static final Logger logger = LogManager.getLogger("DriveBackup");

  ListContentCommand(
      String baseSourcePath, String path, boolean encryptNames, String secretKeyPath) {
    this.baseSourcePath = baseSourcePath;
    this.path = path;
    this.decryptNames = encryptNames;
    this.secretKeyPath = secretKeyPath;
  }

  public void listContent() {
    logger.info(buildInfo());
  }

  private String buildInfo() {
    try {
      StringBuilder builder = new StringBuilder();
      Consumer<Named> printName = (dir) -> builder.append(String.format("  %s\n", dir.getName()));
      builder.append(String.format("Content of %s/%s\nDirectories:\n", baseSourcePath, path));
      source().getSubDirectories().forEach(printName);
      builder.append("Files:\n");
      source().getFiles().forEach(printName);
      return builder.toString();
    } catch (IOException e) {
      throw new RuntimeException("Could not list content", e);
    }
  }

  private DriveBackupDirectory source() {
    if (path == null) {
      return baseSource();
    } else {
      DriveBackupDirectory source = baseSource();
      String[] subDirs = path.split("/");
      for (String subDir : subDirs) {
        if (!subDir.trim().isEmpty()) {
          try {
            source = source.findDirectory(subDir).get();
          } catch (IOException | NoSuchElementException e) {
            throw new RuntimeException(String.format("dir to list content not found %s", path));
          }
        }
      }
      return source;
    }
  }

  private DriveBackupDirectory baseSource() {
    if (decryptNames) {
      return decryptedSource();
    } else {
      return plainSource();
    }
  }

  private DriveBackupDirectory decryptedSource() {
    return new EncryptionDirectoryProxy(
        plainSource(), encryptionService(), stringEncryptionService());
  }

  private DriveBackupDirectory plainSource() {
    try {
      return DriveDirectory.findFromPath(baseSourcePath, drive()).get();
    } catch (NoSuchElementException | IOException e) {
      throw new RuntimeException(
          String.format("Could not find the source directory %s", baseSourcePath));
    }
  }

  private Drive drive() {
    try {
      return DriveServiceFactory.getDriveService();
    } catch (IOException e) {
      throw new RuntimeException("Could not connect to google drive");
    }
  }

  private EncryptionService encryptionService() {
    return new NoEncryptionService();
  }

  private StringEncryptionService stringEncryptionService() {
    return new StringAESAndBase64EncryptionService(secretKey());
  }

  private SecretKey secretKey() {
    if (secretKey == null) {
      try {
        secretKey = AESSecretKeyProvider.getSecretKey(new File(secretKeyPath));
      } catch (IOException e) {
        throw new RuntimeException(
            String.format("could not load secret Key File %s", secretKeyPath));
      }
    }
    return secretKey;
  }
}
