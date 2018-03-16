package drivebackup.drive.calls;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import java.io.IOException;
import java.util.Arrays;

public class CreateDirectoryCall implements IOCallable<File> {
  private final String name;
  private final String parentId;
  private final Drive drive;

  public CreateDirectoryCall(String name, String parentId, Drive drive) {
    this.name = name;
    this.parentId = parentId;
    this.drive = drive;
  }

  @Override
  public File call() throws IOException {
    File body = new File();
    body.setTitle(name);
    body.setMimeType("application/vnd.google-apps.folder");
    body.setParents(Arrays.asList(new ParentReference().setId(parentId)));
    return drive.files().insert(body).execute();
  }
}
