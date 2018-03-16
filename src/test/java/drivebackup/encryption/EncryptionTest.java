package drivebackup.encryption;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class EncryptionTest {
  @Test
  public void testEncryption()
      throws NoSuchAlgorithmException, IOException, InvalidParameterSpecException,
          InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(128);
    SecretKey skey = kg.generateKey();

    String str = "Lorem ipsum dolor sit amet";
    InputStream stream = IOUtils.toInputStream(str);

    AESEncryptionService encryptor = new AESEncryptionService(skey);
    InputStream encrypted = encryptor.encrypt().apply(stream);
    InputStream decrypted = encryptor.decrypt().apply(encrypted);

    String resultStr = IOUtils.toString(decrypted);
    assertEquals(str, resultStr);
  }
}
