package drivebackup.encryption;

public interface StringEncryptionService {
	public String encrypt(String plain);

	public String decrypt(String encrypted);
}
