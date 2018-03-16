package drivebackup.encryption;

import java.io.InputStream;
import java.util.function.Function;

public interface EncryptionService {
  Function<InputStream, InputStream> encrypt();

  Function<InputStream, InputStream> decrypt();
}
