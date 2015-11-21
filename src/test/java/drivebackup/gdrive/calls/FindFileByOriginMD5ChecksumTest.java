package drivebackup.gdrive.calls;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import com.google.api.services.drive.model.File;

import drivebackup.gdrive.BaseGDirTest;
import drivebackup.gdrive.OriginMD5ChecksumAccessor;
import drivebackup.local.LocalFile;

public class FindFileByOriginMD5ChecksumTest extends BaseGDirTest{
	@Test
	public void testReqularFile() throws IOException{
		LocalFile file = localFile("./src/test/resources/test.txt");
		gDir.saveOrUpdateFile(file);
		
		File foundFile = new FindFileByOriginMD5Checksum(file.getOriginMd5Checksum(), googleDrive).call();
		assertNotNull(foundFile);
		OriginMD5ChecksumAccessor md5Accessor = new OriginMD5ChecksumAccessor(foundFile);
		assertEquals(file.getOriginMd5Checksum(), md5Accessor.get().get());
	}
	
	@Test
	public void testTrashedFile() throws IOException{
		LocalFile file = localFile("./src/test/resources/test.txt");
		gDir.saveOrUpdateFile(file);
		gDir.deleteAllExceptOf(Collections.emptyList());
		
		File foundFile = new FindFileByOriginMD5Checksum(file.getOriginMd5Checksum(), googleDrive).call();
		assertNotNull(foundFile);
		OriginMD5ChecksumAccessor md5Accessor = new OriginMD5ChecksumAccessor(foundFile);
		assertEquals(file.getOriginMd5Checksum(), md5Accessor.get().get());
	}
}
