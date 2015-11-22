package drivebackup.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import drivebackup.encryption.EncryptionService;

public class LocalFileImpl implements LocalFile {
	private final File file;
	private final EncryptionService encryptionService;
	private String md5Checksum;
	
	public LocalFileImpl(File file, EncryptionService encryptionService){
		this.file= file;
		this.encryptionService = encryptionService;
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
	public InputStream getEncryptedInputStream() throws FileNotFoundException  {
		return encryptionService.encrypt(new FileInputStream(file));
	}
	
	public InputStream getDecryptedInputStream() throws FileNotFoundException {
		return encryptionService.decrypt(new FileInputStream(file));
	}

	@Override
	public String getOriginMd5Checksum() throws IOException {
		if(md5Checksum == null){
			md5Checksum = calculateMd5Checksum();
		}
		return md5Checksum;
	}
	
	private String calculateMd5Checksum() throws IOException {
		InputStream inputStream = new FileInputStream(file);
		return org.apache.commons.codec.digest.DigestUtils.md5Hex(inputStream);
	}
}
