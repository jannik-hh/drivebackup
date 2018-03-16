package drivebackup.drive.calls;

import java.io.IOException;

@FunctionalInterface
public interface IOCallable<V> {
  V call() throws IOException;
}
