package drivebackup.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

public class SimpleLocalDirectory implements LocalDirectory {
	private final File directory;

	public SimpleLocalDirectory(File directory) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(String.format("%s must be a directory", directory));
		}
		this.directory = directory;
	}

	@Override
	public String getName() {
		return directory.getName();
	}

	@Override
	public Stream<LocalFile> getFiles() throws IOException {
		return getChildren().filter((file) -> file.isFile()).map((file) -> new LocalFileImpl(file));
	}

	@Override
	public Stream<LocalDirectory> getSubDirectories() throws IOException {
		return getChildren().filter((file) -> file.isDirectory()).map((dir) -> new SimpleLocalDirectory(dir));
	}

	protected Stream<File> getChildren() throws IOException {
		return Files.list(directory.toPath()).map((path) -> path.toFile());
	}
}
