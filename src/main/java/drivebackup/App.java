package drivebackup;

import drivebackup.cli.BackupCLIOptions;
import drivebackup.cli.ListContentCLIOptions;
import drivebackup.cli.RecoverCLIOptions;

public class App {
  private static final String HELP_COMMAND = "help";

  public static void main(String[] args) {
    String firstArg = args.length > 1 ? args[0] : "";
    switch (firstArg) {
      case BackupCLIOptions.NAME:
        new BackupCLIOptions(args).parseArgsRunBackupCommand();
        break;
      case RecoverCLIOptions.NAME:
        new RecoverCLIOptions(args).parseArgsRunRecoverCommand();
        break;
      case ListContentCLIOptions.NAME:
        new ListContentCLIOptions(args).parseArgsRunListContentCommand();
        break;
      case HELP_COMMAND:
        printCommandHelp(args);
        break;
      default:
        printHelpAndExit();
    }
  }

  private static void printCommandHelp(String[] args) {
    String secondArg = args.length > 2 ? args[1] : "";
    switch (secondArg) {
      case BackupCLIOptions.NAME:
        new BackupCLIOptions(null).printHelp();
        break;
      case RecoverCLIOptions.NAME:
        new RecoverCLIOptions(null).printHelp();
        break;
      case ListContentCLIOptions.NAME:
        new ListContentCLIOptions(null).printHelp();
        break;
      default:
        printHelpAndExit();
    }
  }

  private static void printHelpAndExit() {
    System.out.println("select an option: backup, recover, listContent, help <command>");
    System.exit(-1);
  }
}
