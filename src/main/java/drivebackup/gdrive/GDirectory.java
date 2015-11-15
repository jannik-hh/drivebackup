package drivebackup.gdrive;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface GDirectory {
	public com.google.api.services.drive.model.File saveOrUpdateFile(File file) throws IOException;

	public GDirectory findOrCreateDirectory(String name) throws IOException;

	public void deleteAllExceptOf(Collection<String> fileAndDirectoryNames) throws IOException;

	public String getID();
}
