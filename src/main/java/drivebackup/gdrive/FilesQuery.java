package drivebackup.gdrive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

class FilesQuery {
	private final Drive drive;
	FilesQuery(Drive drive){
		this.drive = drive;
	}

	List<File> getAllFilesOf(String parenDirectoryID) throws IOException {
		String query = String.format("'%s' in parents and trashed=false", parenDirectoryID);
	    List<File> result = new ArrayList<File>();
	    Files.List request = drive.files().list().setQ(query);
	    do {
	        FileList files = QueryExecutorWithRetry.executeWithRetry(()-> request.execute());
	        result.addAll(files.getItems());
	        request.setPageToken(files.getNextPageToken());
	    } while (request.getPageToken() != null &&
	             request.getPageToken().length() > 0);

	    return result;
	  }

}
