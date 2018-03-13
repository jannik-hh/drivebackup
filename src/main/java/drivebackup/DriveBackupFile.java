package drivebackup;

import java.io.IOException;
import java.io.InputStream;

public interface DriveBackupFile extends Named {
  String getName();

  String getPath();

  String getId();

  InputStream getContent() throws IOException;

  String getOriginalMd5Checksum() throws IOException;

  String getMd5Checksum() throws IOException;
}
