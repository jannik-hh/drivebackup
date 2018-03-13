package drivebackup.drive.calls;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import drivebackup.drive.OriginMD5ChecksumAccessor;
import java.io.IOException;
import java.util.Arrays;

public class CopyFileCall implements IOCallable<File> {
  private final File originFile;
  private final String targetParentId;
  private final String targetName;
  private final Drive drive;

  public CopyFileCall(File originFile, String targetParentId, String targetName, Drive drive) {
    this.originFile = originFile;
    this.targetParentId = targetParentId;
    this.targetName = targetName;
    this.drive = drive;
  }

  @Override
  public File call() throws IOException {
    File copiedFile = new File();
    copiedFile.setTitle(targetName);
    copiedFile.setParents(Arrays.asList(new ParentReference().setId(targetParentId)));
    new OriginMD5ChecksumAccessor(copiedFile).set(getOriginMD5Checksum());
    return drive.files().copy(originFile.getId(), copiedFile).execute();
  }

  private String getOriginMD5Checksum() {
    return new OriginMD5ChecksumAccessor(originFile).get().orElse(null);
  }
}
