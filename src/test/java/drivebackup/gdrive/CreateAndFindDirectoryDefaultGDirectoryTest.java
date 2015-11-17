package drivebackup.gdrive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.google.api.services.drive.model.FileList;

public class CreateAndFindDirectoryDefaultGDirectoryTest extends BaseGDirTest {

	@Test
	public void testCreate() throws IOException {
		assertFalse(directoryExists(gDir.getID(), "ADIR"));

		gDir.findOrCreateDirectory("ADIR");

		assertTrue(directoryExists(gDir.getID(), "ADIR"));
	}

	@Test
	public void testFind() throws IOException {
		GDirectory createdDir = gDir.findOrCreateDirectory("ADIR");

		GDirectory foundDir = gDir.findOrCreateDirectory("ADIR");
		directoryExists(gDir.getID(), "ADIR");
		assertEquals(createdDir.getID(), foundDir.getID());
	}

	private boolean directoryExists(String parentID, String name) throws IOException {
		String query = String.format(
				"'%s' in parents and title = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed=false",
				parentID, name);
		FileList filelist = googleDrive.files().list().setQ(query).execute();
		return filelist.getItems().size() == 1;
	}
}
