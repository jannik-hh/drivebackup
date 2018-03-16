package drivebackup.drive.calls;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

import com.google.api.services.drive.model.File;
import drivebackup.DriveBackupDirectory;
import drivebackup.DriveBackupFile;
import drivebackup.drive.BaseDriveTest;
import drivebackup.drive.DriveFile;
import drivebackup.drive.OriginMD5ChecksumAccessor;
import java.io.IOException;
import java.util.stream.Collectors;
import org.junit.Test;

public class CopyFileCallTest extends BaseDriveTest {

  @Test
  public void test() throws IOException {
    DriveBackupFile file = localFile("./src/test/resources/test.txt");
    directory.saveOrUpdateFile(file);
    File sourceFile = ((DriveFile) directory.findFile(file.getName()).get()).getRemoteFile();
    DriveBackupDirectory targetDir = directory.findOrCreateDirectory("another Dir");

    File copiedFile =
        new CopyFileCall(sourceFile, targetDir.getId(), "text2.txt", googleDrive).call();
    assertNotNull(copiedFile);
    assertEquals(sourceFile.getMd5Checksum(), copiedFile.getMd5Checksum());
    assertEquals(
        new OriginMD5ChecksumAccessor(sourceFile).get(),
        new OriginMD5ChecksumAccessor(copiedFile).get());
    assertThat(
        copiedFile.getParents().stream().map((ref) -> ref.getId()).collect(Collectors.toList()),
        contains(targetDir.getId()));
  }
}
