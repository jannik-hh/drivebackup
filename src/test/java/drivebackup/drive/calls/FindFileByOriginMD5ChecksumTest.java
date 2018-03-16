package drivebackup.drive.calls;

import static org.junit.Assert.*;

import com.google.api.services.drive.model.File;
import drivebackup.DriveBackupFile;
import drivebackup.drive.BaseDriveTest;
import drivebackup.drive.OriginMD5ChecksumAccessor;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class FindFileByOriginMD5ChecksumTest extends BaseDriveTest {
  @Test
  public void testReqularFile() throws IOException {
    DriveBackupFile file = localFile("./src/test/resources/test.txt");
    directory.saveOrUpdateFile(file);

    Optional<File> foundFile =
        new FindFileByOriginMD5Checksum(
                file.getOriginalMd5Checksum(), file.getMd5Checksum(), googleDrive)
            .call();
    assertTrue(foundFile.isPresent());
    OriginMD5ChecksumAccessor md5Accessor = new OriginMD5ChecksumAccessor(foundFile.get());
    assertEquals(file.getOriginalMd5Checksum(), md5Accessor.get().get());
  }

  @Test
  public void testTrashedFile() throws IOException {
    DriveBackupFile file = localFile("./src/test/resources/test.txt");
    directory.saveOrUpdateFile(file);
    directory.deleteAllExceptOf(Collections.emptyList());

    Optional<File> foundFile =
        new FindFileByOriginMD5Checksum(
                file.getOriginalMd5Checksum(), file.getMd5Checksum(), googleDrive)
            .call();
    assertTrue(foundFile.isPresent());
    OriginMD5ChecksumAccessor md5Accessor = new OriginMD5ChecksumAccessor(foundFile.get());
    assertEquals(file.getOriginalMd5Checksum(), md5Accessor.get().get());
  }
}
