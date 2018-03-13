package drivebackup.cli;

import com.google.api.services.drive.Drive;
import drivebackup.CopyService;
import drivebackup.DriveBackupDirectory;
import drivebackup.Named;
import drivebackup.drive.DriveDirectory;
import drivebackup.drive.DriveServiceFactory;
import drivebackup.encryption.*;
import drivebackup.local.IgnoreFilePredicate;
import drivebackup.local.LocalDirectoryImpl;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.function.Predicate;
import javax.crypto.SecretKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackupCommand {
  private static final Logger logger = LogManager.getLogger("DriveBackup");
  private final String sourcePath;
  private final String targetPath;
  private final String ignoreFilePath;
  private final boolean encryptContent;
  private final boolean encryptNames;
  private final String secretKeyPath;
  private SecretKey secretKey;

  BackupCommand(
      String sourcePath,
      String targetPath,
      String ignoreFilePath,
      boolean encryptContent,
      boolean encryptNames,
      String secretKeyPath) {
    this.sourcePath = sourcePath;
    this.targetPath = targetPath;
    this.ignoreFilePath = ignoreFilePath;
    this.encryptContent = encryptContent;
    this.encryptNames = encryptNames;
    this.secretKeyPath = secretKeyPath;
  }

  public void backup() {
    try {
      logger.info("Backup started");
      long start = System.currentTimeMillis();
      new CopyService().copy(source(), target(), ignoreFilePredicate());
      long end = System.currentTimeMillis();
      Duration duration = Duration.ofMillis(end - start);
      logger.info("Backup finished in {}", duration);
    } catch (IOException e) {
      throw new RuntimeException("backup failed", e);
    }
  }

  private DriveBackupDirectory target() {
    if (encryptContent || encryptNames) {
      return encryptedTarget();
    } else {
      return plainTarget();
    }
  }

  private DriveBackupDirectory encryptedTarget() {
    return new EncryptionDirectoryProxy(
        plainTarget(), encryptionService(), stringEncryptionService());
  }

  private DriveBackupDirectory source() {
    File file = new File(sourcePath);
    if (file.isDirectory()) {
      return new LocalDirectoryImpl(file);
    } else {
      throw new RuntimeException(String.format("%s is no directory", sourcePath));
    }
  }

  private DriveBackupDirectory plainTarget() {
    try {
      return DriveDirectory.findOrCreateFromPath(targetPath, drive());
    } catch (IOException e) {
      throw new RuntimeException("Could not find or create target directory");
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
    if (ignoreFilePath != null) {
      try {
        return new IgnoreFilePredicate(new File(ignoreFilePath));
      } catch (IOException e) {
        throw new RuntimeException(String.format("could not read ignore file %s", ignoreFilePath));
      }
    } else {
      return (named) -> false;
    }
  }

  private EncryptionService encryptionService() {
    if (encryptContent) {
      return new AESEncryptionService(secretKey());
    } else {
      return new NoEncryptionService();
    }
  }

  private StringEncryptionService stringEncryptionService() {
    if (encryptNames) {
      return new StringAESAndBase64EncryptionService(secretKey());
    } else {
      return new StringNoEncryptionService();
    }
  }

  private SecretKey secretKey() {
    if (secretKey == null) {
      try {
        secretKey = AESSecretKeyProvider.getOrCreateSecretKey(secretKeyPath);
      } catch (IOException e) {
        throw new RuntimeException(
            String.format("could not load secret Key File %s", secretKeyPath));
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }
    return secretKey;
  }
}
