package drivebackup.gdrive;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.model.File;

public class DeleteAllExceptOfDefaultGDirectory extends BaseGDirTest {
	@Test
	public void deleteAllExceptOfTest() throws IOException {
		GDirectory dir1 = gDir.findOrCreateDirectory("Dir A");
		GDirectory dir2 = gDir.findOrCreateDirectory("Dir B");
		File file1 = gDir.saveOrUpdateFile(new java.io.File("./src/test/resources/test.txt"));
		File file2 = gDir.saveOrUpdateFile(new java.io.File("./src/test/resources/test2.txt"));

		gDir.deleteAllExceptOf(Arrays.asList("test.txt", "Dir B"));

		assertTrue(fileExists(gDir.getID(), dir2.getID()));
		assertTrue(fileExists(gDir.getID(), file1.getId()));

		assertFalse(fileExists(gDir.getID(), dir1.getID()));
		assertFalse(fileExists(gDir.getID(), file2.getId()));
	}

	private boolean fileExists(String folderId, String fileId) throws IOException {
		try {
			File file = googleDrive.files().get(fileId).execute();
			return !file.getLabels().getTrashed();
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == 404) {
				return false;
			} else {
				throw e;
			}
		}
	}
}
