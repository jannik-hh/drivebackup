package drivebackup.cli;

import com.google.api.services.drive.Drive;
import drivebackup.CopyService;
import drivebackup.DriveBackupDirectory;
import drivebackup.Named;
import drivebackup.drive.DriveDirectory;
import drivebackup.drive.DriveServiceFactory;
import drivebackup.encryption.*;
import drivebackup.local.LocalDirectoryImpl;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import javax.crypto.SecretKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecoverCommand {
  private static final Logger logger = LogManager.getLogger("DriveBackup");
  private final String baseSourcePath;
  private final String dirToRecover;
  private final String targetPath;
  private final boolean decryptContent;
  private final boolean decryptNames;
  private final String secretKeyPath;
  private SecretKey secretKey;

  RecoverCommand(
      String baseSourcePath,
      String dirToBeRecovered,
      String targetPath,
      boolean decryptContent,
      boolean encryptNames,
      String secretKeyPath) {
    this.baseSourcePath = baseSourcePath;
    this.dirToRecover = dirToBeRecovered;
    this.targetPath = targetPath;
    this.decryptContent = decryptContent;
    this.decryptNames = encryptNames;
    this.secretKeyPath = secretKeyPath;
  }

  public void recover() {
    try {
      logger.info("Recovery started");
      long start = System.currentTimeMillis();
      new CopyService().copy(source(), target(), ignoreFilePredicate());
      long end = System.currentTimeMillis();
      Duration duration = Duration.ofMillis(end - start);
      logger.info("Recovery finished in {}", duration);
    } catch (IOException e) {
      throw new RuntimeException("Recovery failed", e);
    }
  }

  private DriveBackupDirectory source() {
    if (dirToRecover == null) {
      return baseSource();
    } else {
      DriveBackupDirectory source = baseSource();
      String[] subDirs = dirToRecover.split("/");
      for (String subDir : subDirs) {
        if (!subDir.trim().isEmpty()) {
          try {
            source = source.findDirectory(subDir).get();
          } catch (IOException | NoSuchElementException e) {
            throw new RuntimeException(String.format("dir to recover not found %s", dirToRecover));
          }
        }
      }
      return source;
    }
  }

  private DriveBackupDirectory baseSource() {
    if (decryptContent || decryptNames) {
      return decryptedSource();
    } else {
      return plainSource();
    }
  }

  private DriveBackupDirectory decryptedSource() {
    return new EncryptionDirectoryProxy(
        plainSource(), encryptionService(), stringEncryptionService());
  }

  private DriveBackupDirectory target() {
    File file = new File(targetPath);

    if (file.isDirectory()) {
      return new LocalDirectoryImpl(file);
    } else {
      throw new RuntimeException(String.format("%s is no directory", targetPath));
    }
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

  private Predicate<Named> ignoreFilePredicate() {
    return (named) -> false;
  }

  private EncryptionService encryptionService() {
    if (decryptContent) {
      return new AESEncryptionService(secretKey());
    } else {
      return new NoEncryptionService();
    }
  }

  private StringEncryptionService stringEncryptionService() {
    if (decryptNames) {
      return new StringAESAndBase64EncryptionService(secretKey());
    } else {
      return new StringNoEncryptionService();
    }
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
