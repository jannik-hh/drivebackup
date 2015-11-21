package drivebackup.local;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

import drivebackup.encryption.EncryptionService;

public class LocalDirectoryFactory {
	public LocalDirectory getLocalDirectory(String pathToDirectory,String pathToIgnoreFile, EncryptionService encryptionService) throws IOException{
		if(pathToIgnoreFile == null){
			return new LocalDirectoryImpl(new File(pathToDirectory), encryptionService, (file) -> false);
		}else{
			Predicate<File> fileIgnoredPredicate = new FileIgnoredPredicate(new File(pathToIgnoreFile));
			return new LocalDirectoryImpl(new File(pathToDirectory), encryptionService, fileIgnoredPredicate);
		}
	}
}
