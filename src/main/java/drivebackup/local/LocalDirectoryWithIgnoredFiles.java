package drivebackup.local;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LocalDirectoryWithIgnoredFiles extends SimpleLocalDirectory{
	private final Predicate<File> fileNotIgnoredPredicate;
	
	public LocalDirectoryWithIgnoredFiles(File directory, Predicate<File> fileNotIgnoredPredicate) {
		super(directory);
		this.fileNotIgnoredPredicate= fileNotIgnoredPredicate;
	}

	@Override
	public Stream<File> getChildren() throws IOException {
		return super.getChildren().filter(fileNotIgnoredPredicate);
	}
	
	@Override
	public Stream<LocalDirectory> getSubDirectories() throws IOException {
		return getChildren().filter((file) -> file.isDirectory()).map((dir) -> new LocalDirectoryWithIgnoredFiles(dir, fileNotIgnoredPredicate));
	}

}
