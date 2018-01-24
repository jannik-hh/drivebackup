package drivebackup.gdrive;

import drivebackup.local.LocalFile;
import java.io.IOException;
import java.util.Collection;

public interface GDirectory {
  public com.google.api.services.drive.model.File saveOrUpdateFile(LocalFile file)
      throws IOException;

  public GDirectory findOrCreateDirectory(String name) throws IOException;

  public void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException;

  public String getID();
}
