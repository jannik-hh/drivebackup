package drivebackup;

import com.google.api.services.drive.Drive;
import drivebackup.encryption.*;
import drivebackup.gdrive.DefaultGDirectory;
import drivebackup.gdrive.DriveServiceFactory;
import drivebackup.gdrive.GDirectory;
import drivebackup.local.LocalDirectory;
import drivebackup.local.LocalDirectoryFactory;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import javax.crypto.SecretKey;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
  private static final String TARGET_DIR_OPTION = "target";
  private static final String SOURCE_DIR_OPTION = "source";
  private static final String SECRET_KEY_OPTION = "secretKey";
  private static final String ENCRYPTION_OPTION = "encrypt";
  private static final String ENCRYPT_NAME_OPTION = "encryptNames";
  private static final String IGNORE_FILE_OPTION = "ignore";
  private static final String DRIVE_CREDENTIALS_OPTIONS = "driveCredentials";
  private static final String DECRYPT_NAME_OPTION = "decryptNames";
  private static final Logger logger = LogManager.getLogger("DriveBackup");
  private static final LocalDirectoryFactory localDirectoryFactory = new LocalDirectoryFactory();

  public static void main(String[] args)
      throws ParseException, IOException, NoSuchAlgorithmException {
    if (args == null || args.length == 0) {
      System.out.println("select an option: backup or decrypt");
    } else if (args[0].equals("backup")) {
      backup(args);
    } else if (args[0].equals("decrypt")) {
      decrypt(args);
    } else if (args[0].equals("authorize")) {
      authorize(args);
    } else {
      System.out.println("select an option: backup or decrypt");
    }
  }

  private static void backup(String[] args)
      throws ParseException, IOException, NoSuchAlgorithmException {
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(cmdBackupOptions(), args);
      EncryptionService encryptionService;
      StringEncryptionService stringEncryptionService;
      if (cmd.hasOption(ENCRYPTION_OPTION)) {
        String path = cmd.getOptionValue(SECRET_KEY_OPTION);
        SecretKey secretKey = AESSecretKeyProvider.getOrCreateSecretKey(path);
        encryptionService = new AESEncryptionService(secretKey);
        if (cmd.hasOption(ENCRYPT_NAME_OPTION)) {
          stringEncryptionService = new StringAESAndBase64EncryptionService(secretKey);
        } else {
          stringEncryptionService = new StringNoEncrytionService();
        }
      } else {
        encryptionService = new NoEncryptionService();
        stringEncryptionService = new StringNoEncrytionService();
      }
      LocalDirectory localDirectory =
          localDirectoryFactory.getLocalDirectory(
              cmd.getOptionValue(SOURCE_DIR_OPTION),
              cmd.getOptionValue(IGNORE_FILE_OPTION),
              encryptionService,
              stringEncryptionService);
      Drive drive =
          DriveServiceFactory.getDriveService(cmd.getOptionValue(DRIVE_CREDENTIALS_OPTIONS));
      GDirectory gDir =
          DefaultGDirectory.findOrCreateFromPath(cmd.getOptionValue(TARGET_DIR_OPTION), drive);
      logger.info("Backup started");
      long start = System.currentTimeMillis();
      localDirectory.backup(gDir);
      long end = System.currentTimeMillis();
      Duration duration = Duration.ofMillis(end - start);
      logger.info("Backup finished in {}", duration);

    } catch (MissingOptionException e) {
      System.out.println(e.getLocalizedMessage());
      printHelp(cmdBackupOptions());
    }
  }

  private static void authorize(String[] args) throws IOException {
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(cmdAuthorizeOptions(), args);

      DriveServiceFactory.authorize(cmd.getOptionValue(DRIVE_CREDENTIALS_OPTIONS));
      logger.info("finished");

    } catch (ParseException e) {
      System.out.println(e.getLocalizedMessage());
      printHelp(cmdBackupOptions());
    }
  }

  private static void decrypt(String[] args)
      throws ParseException, IOException, NoSuchAlgorithmException {
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(cmdDecryptOptions(), args);

      String path = cmd.getOptionValue(SECRET_KEY_OPTION);
      SecretKey secretKey = AESSecretKeyProvider.getSecretKey(new File(path));
      EncryptionService encryptionService = new AESEncryptionService(secretKey);
      StringEncryptionService stringEncryptionService;
      if (cmd.hasOption(DECRYPT_NAME_OPTION)) {
        stringEncryptionService = new StringAESAndBase64EncryptionService(secretKey);
      } else {
        stringEncryptionService = new StringNoEncrytionService();
      }
      LocalDirectory localDirectory =
          localDirectoryFactory.getLocalDirectory(
              cmd.getOptionValue(SOURCE_DIR_OPTION),
              null,
              encryptionService,
              stringEncryptionService);

      logger.info("Decryption started");
      long start = System.currentTimeMillis();
      localDirectory.decrypt(new File(cmd.getOptionValue(TARGET_DIR_OPTION)));
      long end = System.currentTimeMillis();
      Duration duration = Duration.ofMillis(end - start);
      logger.info("Decryption finished in {}", duration);

    } catch (MissingOptionException e) {
      System.out.println(e.getLocalizedMessage());
      printHelp(cmdDecryptOptions());
    }
  }

  private static void printHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("drivebackup", cmdBackupOptions());
  }

  private static Options cmdBackupOptions() {
    Options options = new Options();
    Option srcDir =
        Option.builder(SOURCE_DIR_OPTION)
            .desc("local Directory to backup")
            .hasArg(true)
            .argName("sourceDir")
            .required()
            .build();
    Option targetDir =
        Option.builder(TARGET_DIR_OPTION)
            .desc("Google Drive Directory to backup to")
            .hasArg(true)
            .argName("targetDir")
            .required()
            .build();
    Option encryptOption =
        Option.builder(ENCRYPTION_OPTION)
            .desc("All files are encrypted before uploading to google drive")
            .hasArg(false)
            .build();
    Option encryptNameOption =
        Option.builder(ENCRYPT_NAME_OPTION)
            .desc(
                "All Names of files and directories are encrypted before uploading to google drive")
            .hasArg(false)
            .build();
    Option secretKey =
        Option.builder(SECRET_KEY_OPTION)
            .desc("Path to secret key. If not given, a secret key file will be created")
            .hasArg(true)
            .argName("secretKeyFile")
            .build();
    Option ignoreFile =
        Option.builder(IGNORE_FILE_OPTION)
            .desc("Path to ignore file.")
            .hasArg(true)
            .argName("driveIgnoreFile")
            .build();
    Option driveCredentials =
        Option.builder(DRIVE_CREDENTIALS_OPTIONS)
            .desc("Path to drive credentials.")
            .hasArg(true)
            .argName("driveCredentialsDir")
            .build();
    options.addOption(srcDir);
    options.addOption(targetDir);
    options.addOption(encryptOption);
    options.addOption(encryptNameOption);
    options.addOption(secretKey);
    options.addOption(ignoreFile);
    options.addOption(driveCredentials);
    return options;
  }

  private static Options cmdAuthorizeOptions() {
    Options options = new Options();
    Option secretKey =
        Option.builder(SECRET_KEY_OPTION)
            .desc("Path to secret key. If not given, a secret key file will be created")
            .hasArg(true)
            .argName("secretKeyFile")
            .build();
    Option driveCredentials =
        Option.builder(DRIVE_CREDENTIALS_OPTIONS)
            .desc("Path to drive credentials.")
            .hasArg(true)
            .argName("driveCredentialsDir")
            .build();
    options.addOption(secretKey);
    options.addOption(driveCredentials);
    return options;
  }

  private static Options cmdDecryptOptions() {
    Options options = new Options();
    Option srcDir =
        Option.builder(SOURCE_DIR_OPTION)
            .desc("local Directory to decrypt from")
            .hasArg(true)
            .argName("sourceDir")
            .required()
            .build();
    Option targetDir =
        Option.builder(TARGET_DIR_OPTION)
            .desc("local Directory to decrypt to")
            .hasArg(true)
            .argName("targetDir")
            .required()
            .build();
    Option secretKey =
        Option.builder(SECRET_KEY_OPTION)
            .desc("Path to secret key")
            .hasArg(true)
            .required()
            .argName("secretKeyFile")
            .build();
    Option decryptNameOption =
        Option.builder(DECRYPT_NAME_OPTION)
            .desc("All Names of files and directories will be decrypted")
            .hasArg(false)
            .build();
    options.addOption(srcDir);
    options.addOption(targetDir);
    options.addOption(secretKey);
    options.addOption(decryptNameOption);
    return options;
  }
}
