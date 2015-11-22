package drivebackup.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;

public class AESSecretKeyProvider {
	private static final String DEFAULT_SECRET_KEY_FILENAME = "drivebackup_encryption.key";

	public static SecretKey getOrCreateSecretKey(String path) throws NoSuchAlgorithmException, IOException {
		if (path == null) {
			path = DEFAULT_SECRET_KEY_FILENAME;
		}
		File file = new File(path);
		if (file.exists()) {
			return getSecretKey(file);
		} else {
			return generateAndSaveSecretKey(file);
		}
	}

	private static SecretKey generateAndSaveSecretKey(File file) throws NoSuchAlgorithmException, IOException {
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(256);
		SecretKey skey = kg.generateKey();
		writeKeyToFile(skey, file);
		return skey;
	}

	private static void writeKeyToFile(SecretKey sKey, File file) throws IOException {
		if (file.exists()) {
			throw new RuntimeException(String.format("Secret Key %s already exists", file.getPath()));
		}
		try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
			fileOutputStream.write(sKey.getEncoded());
		}
	}

	public static SecretKey getSecretKey(File file) throws FileNotFoundException, IOException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			byte[] raw_key = IOUtils.toByteArray(inputStream);
			SecretKeySpec skspec = new SecretKeySpec(raw_key, "AES");
			return skspec;
		}
	}
}
