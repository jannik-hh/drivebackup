package drivebackup.local;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface LocalFile {
	public String getName();
	public String getPath();
	public InputStream getInputStream() throws FileNotFoundException ;
	public String getOriginMd5Checksum() throws IOException ;
}
