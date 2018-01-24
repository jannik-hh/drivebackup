package drivebackup.encryption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.apache.commons.io.IOUtils;

public class StringAESAndBase64EncryptionService implements StringEncryptionService {
  private static final Base64.Encoder encoder = Base64.getUrlEncoder();
  private static final Base64.Decoder decoder = Base64.getUrlDecoder();
  private static final String ENCRYPTION_MODE = "AES/ECB/PKCS5Padding";
  private final SecretKey secretKey;

  public StringAESAndBase64EncryptionService(SecretKey secretKey) {
    this.secretKey = secretKey;
  }

  public String encrypt(String plain) {
    InputStream stream = new ByteArrayInputStream(plain.getBytes());
    InputStream encrypted = encrypt(stream);
    return toBase64EncodedString(encrypted);
  }

  public String decrypt(String encrypted) {
    InputStream encrptedStream = fromBase64EncodedString(encrypted);
    InputStream decrypted = decrypt(encrptedStream);
    return toString(decrypted);
  }

  private String toBase64EncodedString(InputStream input) {
    try {
      return encoder.encodeToString(IOUtils.toByteArray(input));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private InputStream fromBase64EncodedString(String input) {
    return new ByteArrayInputStream(decoder.decode(input));
  }

  private String toString(InputStream input) {
    try {
      return IOUtils.toString(input);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public InputStream encrypt(InputStream unencryptedInputStream) {
    Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
    return new CipherInputStream(unencryptedInputStream, cipher);
  }

  public InputStream decrypt(InputStream encryptedInputStream) {
    Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
    return new CipherInputStream(encryptedInputStream, cipher);
  }

  private Cipher getCipher(int encryptionMode) {
    try {
      Cipher cipher = Cipher.getInstance(ENCRYPTION_MODE);
      cipher.init(encryptionMode, secretKey);
      return cipher;
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }
}
