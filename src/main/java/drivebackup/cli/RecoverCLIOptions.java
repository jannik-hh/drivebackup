package drivebackup.cli;

import org.apache.commons.cli.*;

public class RecoverCLIOptions extends BaseCLIOptions {
  public static final String NAME = "recover";
  private static final String BASE_BACKUP_DIR_OPTION = "baseBackupDir";
  private static final String RECOVER_DIR_OPTION = "recover";
  private static final String TARGET_DIR_OPTION = "target";
  private static final String SECRET_KEY_OPTION = "secretKey";
  private static final String DECRYPTION_OPTION = "decrypt";
  private static final String DECRYPT_NAMES_OPTION = "decryptNames";
  private CommandLine cmd;
  private final String[] args;

  public RecoverCLIOptions(String[] args) {
    this.args = args;
    addOption(BASE_BACKUP_DIR_OPTION, "Google Drive Directory to recover from", true, true);
    addOption(RECOVER_DIR_OPTION, "Relative path to dir that should be recovered.", true, false);
    addOption(TARGET_DIR_OPTION, "Local directory to recover to", true, true);
    addOption(DECRYPTION_OPTION, "All files will be decrypted", false, false);
    addOption(
        DECRYPT_NAMES_OPTION, "All Names of files and directories will be decrypted", false, false);
    addOption(SECRET_KEY_OPTION, "Path to secret key.", true, false);
  }

  String getName() {
    return NAME;
  }

  public void parseArgsRunRecoverCommand() {
    cmd = parseArguments(args);
    buildRecoverCommand().recover();
  }

  private RecoverCommand buildRecoverCommand() {
    return new RecoverCommand(
        sourcePath(),
        recoverDirPath(),
        targetPath(),
        encryptContent(),
        encryptNames(),
        secretKeyPath());
  }

  private String sourcePath() {
    return cmd.getOptionValue(BASE_BACKUP_DIR_OPTION);
  }

  private String recoverDirPath() {
    return cmd.getOptionValue(RECOVER_DIR_OPTION);
  }

  private String targetPath() {
    return cmd.getOptionValue(TARGET_DIR_OPTION);
  }

  private boolean encryptContent() {
    return cmd.hasOption(DECRYPTION_OPTION);
  }

  private boolean encryptNames() {
    return cmd.hasOption(DECRYPT_NAMES_OPTION);
  }

  private String secretKeyPath() {
    return cmd.getOptionValue(SECRET_KEY_OPTION);
  }
}
