package drivebackup.local;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import java.util.function.Predicate;

public class LocalDirectoryWithIgnoredFilesTest {

	@Test
	public void testGetName() {
		File dir = new File("./src/test/resources/folder");
		LocalDirectoryWithIgnoredFiles localDirectory = new LocalDirectoryWithIgnoredFiles(dir,(file) -> true);

		assertEquals(localDirectory.getName(), "folder");
	}

	@Test
	public void testGetFiles() throws IOException {
		File dir = new File("./src/test/resources/folder");
		Predicate<File> fileNotIgnoredPredicate = (file) -> !file.getName().equals("file_2.txt");
		LocalDirectoryWithIgnoredFiles localDirectory = 
				new LocalDirectoryWithIgnoredFiles(dir,fileNotIgnoredPredicate);

		Stream<File> files = localDirectory.getFiles();

		File file_1 = new File("./src/test/resources/folder/file_1.txt");

		assertThat(files.collect(Collectors.toList()), contains(file_1));
	}

	@Test
	public void testGetSubDirectories() throws IOException {
		File dir = new File("./src/test/resources/folder");
		Predicate<File> fileNotIgnoredPredicate = (file) -> !file.getName().equals("subfolder_2");
		LocalDirectoryWithIgnoredFiles localDirectory = new LocalDirectoryWithIgnoredFiles(dir,fileNotIgnoredPredicate);

		Stream<LocalDirectory> subDir = localDirectory.getSubDirectories();
		
		assertThat(subDir.map((d) -> d.getName()).collect(Collectors.toList()), contains("subfolder_1"));
	}

}
