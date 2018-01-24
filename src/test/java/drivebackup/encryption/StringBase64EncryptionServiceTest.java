package drivebackup.encryption;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.Test;

public class StringBase64EncryptionServiceTest {
  @Test
  public void test() throws NoSuchAlgorithmException {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(128);
    SecretKey skey = kg.generateKey();

    String str = "Lorem ipsum dolor sit amet";

    StringAESAndBase64EncryptionService encryptionService =
        new StringAESAndBase64EncryptionService(skey);
    String encrypted = encryptionService.encrypt(str);
    String decrypted = encryptionService.decrypt(encrypted);

    assertEquals(str, decrypted);
  }

  @Test
  public void testRepeatedExecutionWithSameResult() throws NoSuchAlgorithmException {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(128);
    SecretKey skey = kg.generateKey();

    String str = "Lorem ipsum dolor sit amet";

    StringAESAndBase64EncryptionService encryptionService =
        new StringAESAndBase64EncryptionService(skey);
    String encrypted_1 = encryptionService.encrypt(str);
    String decrypted_2 = encryptionService.encrypt(str);

    assertEquals(encrypted_1, decrypted_2);
  }
}
