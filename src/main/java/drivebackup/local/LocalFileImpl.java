package drivebackup.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class LocalFileImpl implements LocalFile {
	private final File file;
	private String md5Checksum;
	
	public LocalFileImpl(File file){
		this.file= file;
	}
	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getPath() {
		return file.getPath();
	}

	@Override
	public InputStream getInputStream() throws FileNotFoundException  {
		return new FileInputStream(file);
	}

	@Override
	public String getOriginMd5Checksum() throws IOException {
		if(md5Checksum == null){
			md5Checksum = calculateMd5Checksum();
		}
		return md5Checksum;
	}
	
	private String calculateMd5Checksum() throws IOException {
		InputStream inputStream = getInputStream();
		return org.apache.commons.codec.digest.DigestUtils.md5Hex(inputStream);
	}
}
