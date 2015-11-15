package drivebackup.local;

import java.io.File;
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

	public Stream<File> getFiles() throws IOException;

	public Stream<LocalDirectory> getSubDirectories() throws IOException;

	public default void backup(GDirectory gDir) throws IOException {
		getFiles().parallel().forEach((file) -> {
			try {
				final long before = System.currentTimeMillis();
				gDir.saveOrUpdateFile(file);
				final long after = System.currentTimeMillis();
				logger.info("{} saved or updated in {} sec", file.getPath(), (after - before) / 1000);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		getSubDirectories().parallel().forEach((subDir) -> {
			try {
				GDirectory subGDir = gDir.findOrCreateDirectory(subDir.getName());
				subDir.backup(subGDir);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		});
		Collection<String> childrenNames = Stream.concat(
				getSubDirectories().map((subDir) -> subDir.getName()),
				getFiles().map((file) -> file.getName())
		).collect(Collectors.toCollection(TreeSet::new));
		
		gDir.deleteAllExceptOf(childrenNames);
	}

}
