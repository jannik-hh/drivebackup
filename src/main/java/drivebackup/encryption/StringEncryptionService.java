package drivebackup.encryption;

import java.util.function.Function;

public interface StringEncryptionService {
  Function<String, String> encrypt();

  Function<String, String> decrypt();
}
