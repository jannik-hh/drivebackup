package drivebackup.gdrive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;

public class SaveOrUpdateFileDefaultGDirectoryWithAESEncryptionTest extends BaseGDirAESEncryptionTest {
	@Test
	public void saveFile() throws IOException {
		File file = new File("./src/test/resources/test.txt");
		String md5Checksum = getMD5Checksum(file);

		com.google.api.services.drive.model.File gfile = gDir.saveOrUpdateFile(file);

		assertNotNull(gfile);
		assertNotEquals(md5Checksum, gfile.getMd5Checksum());
		String md5ChecksumOfDecryptedFile = getMD5Checksum(encryptionService.decrypt(downloadFile(gfile)));
		assertEquals(md5Checksum, md5ChecksumOfDecryptedFile);
	}

	@Test
	public void updateFileNothingChanged() throws IOException {
		File file = new File("./src/test/resources/test.txt");
		String md5Checksum = getMD5Checksum(file);

		com.google.api.services.drive.model.File createdGfile = gDir.saveOrUpdateFile(file);
		com.google.api.services.drive.model.File updatedGfile = gDir.saveOrUpdateFile(file);

		assertNotNull(updatedGfile);
		assertEquals(md5Checksum, getMD5Checksum(encryptionService.decrypt(downloadFile(updatedGfile))));
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
		assertEquals(md5Checksum, getMD5Checksum(encryptionService.decrypt(downloadFile(updatedGfile))));
		assertEquals(createdGfile.getId(), updatedGfile.getId());
		assertNotEquals(createdGfile.getModifiedDate(), updatedGfile.getModifiedDate());

	}

	private String getMD5Checksum(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		return getMD5Checksum(fis);
	}

	private String getMD5Checksum(InputStream input) throws IOException {
		return org.apache.commons.codec.digest.DigestUtils.md5Hex(input);
	}

	private InputStream downloadFile(com.google.api.services.drive.model.File file) throws IOException {
		HttpResponse resp = googleDrive.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
				.execute();
		return resp.getContent();
	}
}
