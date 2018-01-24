package drivebackup.gdrive.calls;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

import com.google.api.services.drive.model.File;
import drivebackup.gdrive.BaseGDirTest;
import drivebackup.gdrive.GDirectory;
import drivebackup.gdrive.OriginMD5ChecksumAccessor;
import drivebackup.local.LocalFile;
import java.io.IOException;
import java.util.stream.Collectors;
import org.junit.Test;

public class CopyFileCallTest extends BaseGDirTest {

  @Test
  public void test() throws IOException {
    LocalFile file = localFile("./src/test/resources/test.txt");
    File originFile = gDir.saveOrUpdateFile(file);
    GDirectory targetDir = gDir.findOrCreateDirectory("another Dir");

    File copiedFile =
        new CopyFileCall(originFile, targetDir.getID(), "text2.txt", googleDrive).call();
    assertNotNull(copiedFile);
    assertEquals(originFile.getMd5Checksum(), copiedFile.getMd5Checksum());
    assertEquals(
        new OriginMD5ChecksumAccessor(originFile).get(),
        new OriginMD5ChecksumAccessor(copiedFile).get());
    assertThat(
        copiedFile.getParents().stream().map((ref) -> ref.getId()).collect(Collectors.toList()),
        contains(targetDir.getID()));
  }
}
