package drivebackup.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Predicate;
import java.util.stream.Stream;

import drivebackup.encryption.EncryptionService;

public class LocalDirectoryImpl implements LocalDirectory {
	private final File directory;
	private final EncryptionService encryptionService;
	private final Predicate<File> fileIgnoredPredicate;

	public LocalDirectoryImpl(File directory, EncryptionService encryptionService, Predicate<File> fileIgnoredPredicate) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(String.format("%s must be a directory", directory));
		}
		this.directory = directory;
		this.encryptionService = encryptionService;
		this.fileIgnoredPredicate = fileIgnoredPredicate;
	}

	@Override
	public String getName() {
		return directory.getName();
	}

	@Override
	public Stream<LocalFile> getFiles() throws IOException {
		return getChildren().filter((file) -> file.isFile()).map((file) -> new LocalFileImpl(file, encryptionService));
	}

	@Override
	public Stream<LocalDirectory> getSubDirectories() throws IOException {
		return getChildren().filter((file) -> file.isDirectory()).map((dir) -> new LocalDirectoryImpl(dir, encryptionService, fileIgnoredPredicate));
	}

	protected Stream<File> getChildren() throws IOException {
		return Files.list(directory.toPath()).map((path) -> path.toFile()).filter(fileIgnoredPredicate.negate());
	}
}