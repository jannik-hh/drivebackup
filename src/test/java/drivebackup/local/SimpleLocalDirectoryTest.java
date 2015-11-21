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

		Stream<LocalFile> files = simpleLocalDirectory.getFiles();

		String file_1_path = new File("./src/test/resources/folder/file_1.txt").getPath();
		String file_2_path = new File("./src/test/resources/folder/file_2.txt").getPath();

		assertThat(files.map((localFile)-> localFile.getPath()).collect(Collectors.toList()), contains(file_1_path, file_2_path));
	}

	@Test
	public void testGetSubDirectories() throws IOException {
		File dir = new File("./src/test/resources/folder");
		SimpleLocalDirectory simpleLocalDirectory = new SimpleLocalDirectory(dir);

		Stream<LocalDirectory> subDir = simpleLocalDirectory.getSubDirectories();
		assertThat(subDir.map((d) -> d.getName()).collect(Collectors.toList()), contains("subfolder_1", "subfolder_2"));
	}

}
