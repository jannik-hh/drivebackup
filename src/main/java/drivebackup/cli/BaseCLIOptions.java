package drivebackup.cli;

import org.apache.commons.cli.*;

abstract class BaseCLIOptions {
  private final Options options = new Options();

  abstract String getName();

  void addOption(String name, String description, boolean hasArg, boolean required) {
    options.addOption(
        Option.builder(name).desc(description).hasArg(hasArg).required(required).build());
  }

  CommandLine parseArguments(String[] args) {
    try {
      CommandLineParser parser = new DefaultParser();
      return parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getLocalizedMessage());
      printHelp();
      System.exit(-1);
      return null;
    }
  }

  public void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(getName(), options);
  }
}
