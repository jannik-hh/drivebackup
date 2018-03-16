package drivebackup.encryption;

import java.util.function.Function;

public class StringNoEncryptionService implements StringEncryptionService {
  private static final Function<String, String> identityFunc = (String plain) -> plain;

  @Override
  public Function<String, String> encrypt() {
    return identityFunc;
  }

  @Override
  public Function<String, String> decrypt() {
    return identityFunc;
  }
}
