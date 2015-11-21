package drivebackup.gdrive;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class QueryExecutorWithRetry {
	private static final Logger logger = LogManager.getLogger("DriveBackup");
	private static final int NR_OF_RETRIES = 10;
	
	static <T> T executeWithRetry(IOCallable<T> request) throws IOException{
		return executeWithRetry(request, 1);
	}
	
	private static <T> T executeWithRetry(IOCallable<T> request, int retry_count) throws IOException{
		try{
			return request.call();
		}catch(IOException e){
			if(retry_count <= NR_OF_RETRIES){
				sleep(10 * retry_count);
				return executeWithRetry(request, retry_count + 1);
			}else{
				throw e;
			}
		}
	}
	
	private static void sleep(int seconds){
		try {
			logger.info("retry communicate to google drive in {} seconds", seconds);
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e) {
		}
	}
}
