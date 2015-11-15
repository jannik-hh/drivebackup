package drivebackup.local;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

public class LocalDirectoryFactory {
	public LocalDirectory getLocalDirectory(String pathToDirectory,String pathToIgnoreFile) throws IOException{
		if(pathToIgnoreFile == null){
			return new SimpleLocalDirectory(new File(pathToDirectory));
		}else{
			Predicate<File> fileNotIgnoredPredicate = new FileNotIgnoredPredicate(new File(pathToIgnoreFile));
			return new LocalDirectoryWithIgnoredFiles(new File(pathToDirectory), fileNotIgnoredPredicate);
		}
	}
}
