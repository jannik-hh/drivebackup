package drivebackup.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
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
	
	public default void decrypt(File targetDir) throws IOException{
		targetDir.mkdirs();
		getFiles().forEach((localFile) -> {
			try{
				File newFile = new File(Paths.get(targetDir.getPath(), localFile.getName()).toString());
				try (FileOutputStream fileOutputStream = new FileOutputStream(newFile);) {
					IOUtils.copy(localFile.getDecryptedInputStream(), fileOutputStream);
				}
				logger.info("file encrypted to {}", newFile.getPath());
			}catch(IOException e){
				logger.error(e);
			}
		});
		getSubDirectories().forEach((subDir) ->{
			File newSubDir = new File(Paths.get(targetDir.getPath(), subDir.getName()).toString());
			try{
				subDir.decrypt(newSubDir);
			}catch(IOException e){
				logger.error(e);
			}
		});
	}

}
