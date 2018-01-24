package drivebackup.encryption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class AESEncryptionService implements EncryptionService {
  private static final String ENCRYPTION_MODE = "AES/CBC/PKCS5Padding";
  private static final int IV_LENGTH = 16;
  private final SecretKey secretKey;

  public AESEncryptionService(SecretKey secretKey) {
    this.secretKey = secretKey;
  }

  @SuppressWarnings("resource")
  public InputStream encrypt(InputStream unencryptedInputStream) {
    Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, null);
    return new SequenceInputStream(
        getIV(cipher), new CipherInputStream(unencryptedInputStream, cipher));
  }

  public InputStream decrypt(InputStream encryptedInputStream) {
    IvParameterSpec iv = readIV(encryptedInputStream);
    Cipher cipher = getCipher(Cipher.DECRYPT_MODE, iv);
    return new CipherInputStream(encryptedInputStream, cipher);
  }

  private Cipher getCipher(int encryptionMode, IvParameterSpec iv) {
    try {
      Cipher cipher = Cipher.getInstance(ENCRYPTION_MODE);
      cipher.init(encryptionMode, secretKey, iv);
      return cipher;
    } catch (NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e);
    }
  }

  private InputStream getIV(Cipher cipher) {
    try {
      byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
      if (iv.length != IV_LENGTH) {
        throw new RuntimeException(
            String.format("Generated IV does not have the expected length of %s bytes", IV_LENGTH));
      }
      return new ByteArrayInputStream(iv);
    } catch (InvalidParameterSpecException e) {
      throw new RuntimeException(e);
    }
  }

  private IvParameterSpec readIV(InputStream inputStream) {
    try {
      byte[] iv = new byte[IV_LENGTH];
      inputStream.read(iv);
      return new IvParameterSpec(iv);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
