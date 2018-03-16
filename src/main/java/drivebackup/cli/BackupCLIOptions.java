package drivebackup.cli;

import org.apache.commons.cli.*;

public class BackupCLIOptions extends BaseCLIOptions {
  public static final String NAME = "backup";
  private static final String SOURCE_DIR_OPTION = "source";
  private static final String TARGET_DIR_OPTION = "target";
  private static final String SECRET_KEY_OPTION = "secretKey";
  private static final String ENCRYPTION_OPTION = "encrypt";
  private static final String ENCRYPT_NAME_OPTION = "encryptNames";
  private static final String IGNORE_FILE_OPTION = "ignore";
  private CommandLine cmd;
  private final String[] args;

  public BackupCLIOptions(String[] args) {
    this.args = args;
    addOption(SOURCE_DIR_OPTION, "local Directory to copy", true, true);
    addOption(TARGET_DIR_OPTION, "Google Drive Directory to copy to", true, true);
    addOption(
        ENCRYPTION_OPTION,
        "All files are encrypted before uploading to google drive",
        false,
        false);
    addOption(
        ENCRYPT_NAME_OPTION,
        "All Names of files and directories are encrypted before uploading to google drive",
        false,
        false);
    addOption(
        SECRET_KEY_OPTION,
        "Path to secret key. If not given, a secret key file will be created",
        true,
        false);
    addOption(IGNORE_FILE_OPTION, "Path to ignore file.", true, false);
  }

  String getName() {
    return NAME;
  }

  public void parseArgsRunBackupCommand() {
    cmd = parseArguments(args);
    buildBackupCommand().backup();
  }

  private BackupCommand buildBackupCommand() {
    return new BackupCommand(
        sourcePath(),
        targetPath(),
        ignoreFilePath(),
        encryptContent(),
        encryptNames(),
        secretKeyPath());
  }

  private String sourcePath() {
    return cmd.getOptionValue(SOURCE_DIR_OPTION);
  }

  private String targetPath() {
    return cmd.getOptionValue(TARGET_DIR_OPTION);
  }

  private String ignoreFilePath() {
    return cmd.getOptionValue(IGNORE_FILE_OPTION);
  }

  private boolean encryptContent() {
    return cmd.hasOption(ENCRYPTION_OPTION);
  }

  private boolean encryptNames() {
    return cmd.hasOption(ENCRYPT_NAME_OPTION);
  }

  private String secretKeyPath() {
    return cmd.getOptionValue(SECRET_KEY_OPTION);
  }
}
