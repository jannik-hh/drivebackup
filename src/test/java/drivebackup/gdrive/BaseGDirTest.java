package drivebackup.gdrive;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.google.api.services.drive.Drive;

import drivebackup.encryption.NoEncryptionService;
import drivebackup.local.LocalFile;
import drivebackup.local.LocalFileImpl;

public class BaseGDirTest {
	private final static String TEST_DIR = "DriveBackupTest";
	protected Drive googleDrive;
	protected GDirectory gDir;

	@Before
	public void before() throws IOException {
		googleDrive = DriveServiceFactory.getDriveService();
		gDir = DefaultGDirectory.fromPath(TEST_DIR, googleDrive, new NoEncryptionService());
	}

	@After
	public void after() throws IOException {
		deleteGDirectory(gDir, googleDrive);
	}

	protected void deleteGDirectory(GDirectory gDir, Drive drive) throws IOException {
		drive.files().delete(gDir.getID()).execute();
	}
	

	protected LocalFile localFile(String path){
		return new LocalFileImpl(new java.io.File(path));
	}
}
