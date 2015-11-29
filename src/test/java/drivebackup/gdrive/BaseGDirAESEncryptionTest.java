package drivebackup.gdrive;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.After;
import org.junit.Before;

import com.google.api.services.drive.Drive;

import drivebackup.encryption.AESEncryptionService;
import drivebackup.encryption.EncryptionService;
import drivebackup.encryption.StringNoEncrytionService;
import drivebackup.local.LocalFile;
import drivebackup.local.LocalFileImpl;

public class BaseGDirAESEncryptionTest {
	private final static String TEST_DIR = "DriveBackupTest";
	protected Drive googleDrive;
	protected EncryptionService encryptionService;
	protected GDirectory gDir;

	@Before
	public void before() throws IOException, NoSuchAlgorithmException {
		googleDrive = DriveServiceFactory.getDriveService();
		encryptionService = new AESEncryptionService(generateSecretKey());
		gDir = DefaultGDirectory.fromPath(TEST_DIR, googleDrive);
	}

	@After
	public void after() throws IOException {
		deleteGDirectory(gDir, googleDrive);
	}

	protected void deleteGDirectory(GDirectory gDir, Drive drive) throws IOException {
		drive.files().delete(gDir.getID()).execute();
	}

	private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		return kg.generateKey();
	}
	
	protected LocalFile localFile(String path){
		return new LocalFileImpl(new java.io.File(path), encryptionService, new StringNoEncrytionService());
	}

}
