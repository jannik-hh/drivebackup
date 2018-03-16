package drivebackup.encryption;

import java.io.InputStream;
import java.util.function.Function;

public class NoEncryptionService implements EncryptionService {
  private static final Function<InputStream, InputStream> identityFunc =
      (InputStream plain) -> plain;

  @Override
  public Function<InputStream, InputStream> encrypt() {
    return identityFunc;
  }

  @Override
  public Function<InputStream, InputStream> decrypt() {
    return identityFunc;
  }
}
