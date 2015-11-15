package drivebackup.encryption;

import java.io.InputStream;

public interface EncryptionService {
	public InputStream encrypt(InputStream plain);

	public InputStream decrypt(InputStream encrypted);
}
