package drivebackup.local;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface LocalFile {
  String getName();

  String getEncryptedName();

  String getPath();

  InputStream getEncryptedInputStream() throws FileNotFoundException;

  InputStream getDecryptedInputStream() throws FileNotFoundException;

  String getOriginMd5Checksum() throws IOException;

  String getMd5ChecksumOfEncryptedContent() throws IOException;
}
