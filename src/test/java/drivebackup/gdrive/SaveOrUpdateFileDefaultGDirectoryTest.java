package drivebackup.gdrive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

public class SaveOrUpdateFileDefaultGDirectoryTest extends BaseGDirTest {
	@Test
	public void saveFile() throws IOException {

		File file = new File("./src/test/resources/test.txt");
		String md5Checksum = getMD5Checksum(file);

		com.google.api.services.drive.model.File gfile = gDir.saveOrUpdateFile(file);

		assertNotNull(gfile);
		assertEquals(gfile.getMd5Checksum(), md5Checksum);
	}

	@Test
	public void updateFileNothingChanged() throws IOException {
		File file = new File("./src/test/resources/test.txt");
		String md5Checksum = getMD5Checksum(file);

		com.google.api.services.drive.model.File createdGfile = gDir.saveOrUpdateFile(file);
		com.google.api.services.drive.model.File updatedGfile = gDir.saveOrUpdateFile(file);

		assertNotNull(updatedGfile);
		assertEquals(updatedGfile.getMd5Checksum(), md5Checksum);
		assertEquals(createdGfile.getId(), updatedGfile.getId());
		assertEquals(createdGfile.getModifiedDate(), updatedGfile.getModifiedDate());

	}

	@Test
	public void updateFileSomethingChanged() throws IOException {
		File file = new File("./src/test/resources/test.txt");
		com.google.api.services.drive.model.File createdGfile = gDir.saveOrUpdateFile(file);

		File updatedFile = new File("./src/test/resources/changed/test.txt");
		String md5Checksum = getMD5Checksum(updatedFile);
		com.google.api.services.drive.model.File updatedGfile = gDir.saveOrUpdateFile(updatedFile);

		assertNotNull(updatedGfile);
		assertEquals(updatedGfile.getMd5Checksum(), md5Checksum);
		assertEquals(createdGfile.getId(), updatedGfile.getId());
		assertNotEquals(createdGfile.getModifiedDate(), updatedGfile.getModifiedDate());

	}

	private String getMD5Checksum(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		return org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
	}

}
