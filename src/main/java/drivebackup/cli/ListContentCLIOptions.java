package drivebackup.cli;

import org.apache.commons.cli.*;

public class ListContentCLIOptions extends BaseCLIOptions {
  public static final String NAME = "listContent";
  private static final String BASE_BACKUP_DIR_OPTION = "baseBackupDir";
  private static final String RECOVER_DIR_OPTION = "path";
  private static final String SECRET_KEY_OPTION = "secretKey";
  private static final String DECRYPT_NAMES_OPTION = "decryptNames";

  private final String[] args;
  private CommandLine cmd;

  public ListContentCLIOptions(String[] args) {
    this.args = args;
    addOption(BASE_BACKUP_DIR_OPTION, "Google Drive Directory to recover from", true, true);
    addOption(RECOVER_DIR_OPTION, "Relative path to dir that should be recovered.", true, false);
    addOption(
        DECRYPT_NAMES_OPTION, "All Names of files and directories will be decrypted", false, false);
    addOption(SECRET_KEY_OPTION, "Path to secret key.", true, false);
  }

  String getName() {
    return NAME;
  }

  public void parseArgsRunListContentCommand() {
    cmd = parseArguments(args);
    buildListContentCommand().listContent();
  }

  private ListContentCommand buildListContentCommand() {
    return new ListContentCommand(sourcePath(), recoverDirPath(), encryptNames(), secretKeyPath());
  }

  private String sourcePath() {
    return cmd.getOptionValue(BASE_BACKUP_DIR_OPTION);
  }

  private String recoverDirPath() {
    return cmd.getOptionValue(RECOVER_DIR_OPTION);
  }

  private boolean encryptNames() {
    return cmd.hasOption(DECRYPT_NAMES_OPTION);
  }

  private String secretKeyPath() {
    return cmd.getOptionValue(SECRET_KEY_OPTION);
  }
}
