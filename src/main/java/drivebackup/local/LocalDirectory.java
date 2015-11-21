package drivebackup.local;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import drivebackup.gdrive.GDirectory;

public interface LocalDirectory {
	static final Logger logger = LogManager.getLogger("DriveBackup");

	public String getName();

	public Stream<LocalFile> getFiles() throws IOException;

	public Stream<LocalDirectory> getSubDirectories() throws IOException;

	public default void backup(GDirectory gDir) throws IOException {
		getFiles().parallel().forEach((file) -> {
			try {
				gDir.saveOrUpdateFile(file);
			} catch (IOException e) {
				logger.warn("unable to save or update {}", file.getPath());
			}
		});
		getSubDirectories().parallel().forEach((subDir) -> {
			try {
				GDirectory subGDir = gDir.findOrCreateDirectory(subDir.getName());
				subDir.backup(subGDir);
			} catch (IOException e) {
				logger.warn("unable to backup {}", subDir.getName());
			}

		});
		Collection<String> childrenNames = Stream.concat(
				getSubDirectories().map((subDir) -> subDir.getName()),
				getFiles().map((file) -> file.getName())
		).collect(Collectors.toCollection(TreeSet::new));
		
		gDir.deleteAllExceptOf(childrenNames);
	}

}
