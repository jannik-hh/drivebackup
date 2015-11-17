package drivebackup.gdrive;

import java.io.IOException;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;

class QueryExecutorWithRetry {
	private static final int NR_OF_RETRIES = 3;
	
	static <T> T executeWithRetry(AbstractGoogleClientRequest<T> request) throws IOException{
		return executeWithRetry(request, 1);
	}
	
	private static <T> T executeWithRetry(AbstractGoogleClientRequest<T> request, int retry_count) throws IOException{
		try{
			return request.execute();
		}catch(IOException e){
			if(retry_count <= NR_OF_RETRIES){
				sleep(5 * retry_count);
				return executeWithRetry(request, retry_count + 1);
			}else{
				throw e;
			}
		}
	}
	
	private static void sleep(int seconds){
		try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e) {
		}
	}
}