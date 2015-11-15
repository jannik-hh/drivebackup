package drivebackup.local;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class SimpleLocalDirectoryTest {

	@Test
	public void testGetName() {
		File dir = new File("./src/test/resources/folder");
		SimpleLocalDirectory simpleLocalDirectory = new SimpleLocalDirectory(dir);

		assertEquals(simpleLocalDirectory.getName(), "folder");
	}

	@Test
	public void testGetFiles() throws IOException {
		File dir = new File("./src/test/resources/folder");
		SimpleLocalDirectory simpleLocalDirectory = new SimpleLocalDirectory(dir);

		Stream<File> files = simpleLocalDirectory.getFiles();

		File file_1 = new File("./src/test/resources/folder/file_1.txt");
		File file_2 = new File("./src/test/resources/folder/file_2.txt");

		assertThat(files.collect(Collectors.toList()), contains(file_1, file_2));
	}

	@Test
	public void testGetSubDirectories() throws IOException {
		File dir = new File("./src/test/resources/folder");
		SimpleLocalDirectory simpleLocalDirectory = new SimpleLocalDirectory(dir);

		Stream<LocalDirectory> subDir = simpleLocalDirectory.getSubDirectories();
		assertThat(subDir.map((d) -> d.getName()).collect(Collectors.toList()), contains("subfolder_1", "subfolder_2"));
	}

}
