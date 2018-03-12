package drivebackup.gdrive;

import drivebackup.local.LocalFile;
import java.io.IOException;
import java.util.Collection;

public interface GDirectory {
  com.google.api.services.drive.model.File saveOrUpdateFile(LocalFile file) throws IOException;

  GDirectory findOrCreateDirectory(String name) throws IOException;

  void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException;

  String getID();
}
