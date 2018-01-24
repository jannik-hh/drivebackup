package drivebackup.gdrive.calls;

import static org.junit.Assert.*;

import com.google.api.services.drive.model.File;
import drivebackup.gdrive.BaseGDirTest;
import drivebackup.gdrive.OriginMD5ChecksumAccessor;
import drivebackup.local.LocalFile;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class FindFileByOriginMD5ChecksumTest extends BaseGDirTest {
  @Test
  public void testReqularFile() throws IOException {
    LocalFile file = localFile("./src/test/resources/test.txt");
    gDir.saveOrUpdateFile(file);

    Optional<File> foundFile =
        new FindFileByOriginMD5Checksum(
                file.getOriginMd5Checksum(), file.getMd5ChecksumOfEncryptedContent(), googleDrive)
            .call();
    assertTrue(foundFile.isPresent());
    OriginMD5ChecksumAccessor md5Accessor = new OriginMD5ChecksumAccessor(foundFile.get());
    assertEquals(file.getOriginMd5Checksum(), md5Accessor.get().get());
  }

  @Test
  public void testTrashedFile() throws IOException {
    LocalFile file = localFile("./src/test/resources/test.txt");
    gDir.saveOrUpdateFile(file);
    gDir.deleteAllExceptOf(Collections.emptyList());

    Optional<File> foundFile =
        new FindFileByOriginMD5Checksum(
                file.getOriginMd5Checksum(), file.getMd5ChecksumOfEncryptedContent(), googleDrive)
            .call();
    assertTrue(foundFile.isPresent());
    OriginMD5ChecksumAccessor md5Accessor = new OriginMD5ChecksumAccessor(foundFile.get());
    assertEquals(file.getOriginMd5Checksum(), md5Accessor.get().get());
  }
}
