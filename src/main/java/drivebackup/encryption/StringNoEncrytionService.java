package drivebackup.encryption;

public class StringNoEncrytionService implements StringEncryptionService{

	@Override
	public String encrypt(String plain) {
		return plain;
	}

	@Override
	public String decrypt(String encrypted) {
		return encrypted;
	}

}
