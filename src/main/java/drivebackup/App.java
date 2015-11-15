package drivebackup;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import javax.crypto.SecretKey;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.drive.Drive;

import drivebackup.encryption.AESEncryptionService;
import drivebackup.encryption.AESSecretKeyProvider;
import drivebackup.encryption.EncryptionService;
import drivebackup.encryption.NoEncryptionService;
import drivebackup.gdrive.DefaultGDirectory;
import drivebackup.gdrive.DriveServiceFactory;
import drivebackup.gdrive.GDirectory;
import drivebackup.local.LocalDirectory;
import drivebackup.local.SimpleLocalDirectory;

public class App {
	private static final String TARGET_DIR_OPTION = "target";
	private static final String SOURCE_DIR_OPTION = "source";
	private static final String SECRET_KEY_OPTION = "secretKey";
	private static final String ENCRYPTION_OPTION = "encrypt";
	private static final Logger logger = LogManager.getLogger("DriveBackup");

	public static void main(String[] args) throws ParseException, IOException, NoSuchAlgorithmException {
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(cmdOptions(), args);
			EncryptionService encryptionService;
			if (cmd.hasOption(ENCRYPTION_OPTION)) {
				String path = cmd.getOptionValue(SECRET_KEY_OPTION);
				SecretKey secretKey = AESSecretKeyProvider.getOrCreateSecretKey(path);
				encryptionService = new AESEncryptionService(secretKey);
			} else {
				encryptionService = new NoEncryptionService();
			}
			File sourceDir = new File(cmd.getOptionValue(SOURCE_DIR_OPTION));
			LocalDirectory localDirectory = new SimpleLocalDirectory(sourceDir);
			Drive drive = DriveServiceFactory.getDriveService();
			GDirectory gDir = DefaultGDirectory.fromPath(cmd.getOptionValue(TARGET_DIR_OPTION), drive,
					encryptionService);
			logger.info("Backup started");
			long start = System.currentTimeMillis();
			localDirectory.backup(gDir);
			long end = System.currentTimeMillis();
			Duration duration = Duration.ofMillis(end - start);
			logger.info("Backup finished in {}", duration);

		} catch (MissingOptionException e) {
			System.out.println(e.getLocalizedMessage());
			printHelp();
		}
	}

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("drivebackup", cmdOptions());
	}

	private static Options cmdOptions() {
		Options options = new Options();
		Option srcDir = Option.builder(SOURCE_DIR_OPTION).desc("local Directory to backup").hasArg(true)
				.argName("sourceDir").required().build();
		Option targetDir = Option.builder(TARGET_DIR_OPTION).desc("Google Drive Directory to backup to").hasArg(true)
				.argName("targetDir").required().build();
		Option encryptOption = Option.builder(ENCRYPTION_OPTION)
				.desc("All files are encrypted before uploading to google drive").hasArg(false).build();
		Option secretKey = Option.builder(SECRET_KEY_OPTION)
				.desc("Path to secret key. If not given, a secret key file will be created").hasArg(true)
				.argName("secretKeyFile").build();
		options.addOption(srcDir);
		options.addOption(targetDir);
		options.addOption(encryptOption);
		options.addOption(secretKey);
		return options;

	}
}
