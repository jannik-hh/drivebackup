package drivebackup.gdrive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import drivebackup.local.LocalFile;

public class SaveOrUpdateFileDefaultGDirectoryTest extends BaseGDirTest {
	@Test
	public void saveFile() throws IOException {
		LocalFile file = localFile("./src/test/resources/test.txt");

		com.google.api.services.drive.model.File gfile = gDir.saveOrUpdateFile(file);

		assertNotNull(gfile);
		assertEquals(gfile.getMd5Checksum(), file.getOriginMd5Checksum());
	}

	@Test
	public void updateFileNothingChanged() throws IOException {
		LocalFile file = localFile("./src/test/resources/test.txt");

		com.google.api.services.drive.model.File createdGfile = gDir.saveOrUpdateFile(file);
		com.google.api.services.drive.model.File updatedGfile = gDir.saveOrUpdateFile(file);

		assertNotNull(updatedGfile);
		assertEquals(updatedGfile.getMd5Checksum(), file.getOriginMd5Checksum());
		assertEquals(createdGfile.getId(), updatedGfile.getId());
		assertEquals(createdGfile.getModifiedDate(), updatedGfile.getModifiedDate());

	}

	@Test
	public void updateFileSomethingChanged() throws IOException {
		LocalFile file = localFile("./src/test/resources/test.txt");
		com.google.api.services.drive.model.File createdGfile = gDir.saveOrUpdateFile(file);

		LocalFile updatedFile = localFile("./src/test/resources/changed/test.txt");
		com.google.api.services.drive.model.File updatedGfile = gDir.saveOrUpdateFile(updatedFile);

		assertNotNull(updatedGfile);
		assertEquals(updatedGfile.getMd5Checksum(), updatedFile.getOriginMd5Checksum());
		assertEquals(createdGfile.getId(), updatedGfile.getId());
		assertNotEquals(createdGfile.getModifiedDate(), updatedGfile.getModifiedDate());

	}

}
