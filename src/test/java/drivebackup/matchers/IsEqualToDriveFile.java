package drivebackup.matchers;

import drivebackup.DriveBackupFile;
import java.io.IOException;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsEqualToDriveFile extends TypeSafeMatcher<DriveBackupFile> {
  private final DriveBackupFile expected;

  private IsEqualToDriveFile(DriveBackupFile expected) {
    this.expected = expected;
  }

  @Override
  public boolean matchesSafely(DriveBackupFile actual) {
    boolean namesAreEqual = actual.getName().equals(expected.getName());
    boolean md5ChecksumsAreEqual = getMd5Checksum(actual).equals(getMd5Checksum(expected));
    return namesAreEqual && md5ChecksumsAreEqual;
  }

  private String getMd5Checksum(DriveBackupFile file) {
    try {
      return file.getMd5Checksum();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void describeTo(Description description) {
    description.appendText("not equal");
  }

  @Factory
  public static Matcher isEqualToDriveFile(DriveBackupFile expected) {
    return new IsEqualToDriveFile(expected);
  }
}
