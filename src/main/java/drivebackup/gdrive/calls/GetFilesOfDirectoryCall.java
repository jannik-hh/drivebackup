package drivebackup.gdrive.calls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import drivebackup.gdrive.QueryExecutorWithRetry;

public class GetFilesOfDirectoryCall implements IOCallable<List<File>> {
	private final Drive drive;
	private final String parentDirectoryID;
	
	public GetFilesOfDirectoryCall(String parenDirectoryID, Drive drive){
		this.drive = drive;
		this.parentDirectoryID = parenDirectoryID;
		
	}

	@Override
	public List<File> call() throws IOException {
		String query = String.format("'%s' in parents and trashed=false", parentDirectoryID);
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
