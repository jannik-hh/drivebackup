package drivebackup.drive.calls;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import drivebackup.drive.QueryExecutorWithRetry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetChildrenOfDirectoryCall implements IOCallable<List<File>> {
  private final Drive drive;
  private final String parentDirectoryID;

  public GetChildrenOfDirectoryCall(String parenDirectoryID, Drive drive) {
    this.drive = drive;
    this.parentDirectoryID = parenDirectoryID;
  }

  @Override
  public List<File> call() throws IOException {
    String query = String.format("'%s' in parents and trashed=false", parentDirectoryID);
    List<File> result = new ArrayList<File>();
    Files.List request = drive.files().list().setQ(query);
    do {
      FileList files = QueryExecutorWithRetry.executeWithRetry(() -> request.execute());
      result.addAll(files.getItems());
      request.setPageToken(files.getNextPageToken());
    } while (request.getPageToken() != null && request.getPageToken().length() > 0);

    return result;
  }
}
