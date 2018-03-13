package drivebackup.matchers;

import drivebackup.DriveBackupDirectory;
import drivebackup.DriveBackupFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsEqualToDriveBackupDirectory extends TypeSafeMatcher<DriveBackupDirectory> {
  private final DriveBackupDirectory expected;
  private String failure = "";

  private IsEqualToDriveBackupDirectory(DriveBackupDirectory expected) {
    this.expected = expected;
  }

  @Override
  public boolean matchesSafely(DriveBackupDirectory actual) {
    try {
      return matchDirs(expected, actual);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean matchDirs(DriveBackupDirectory expected, DriveBackupDirectory actual)
      throws IOException {
    List<DriveBackupFile> expectedFiles = expected.getFiles().collect(Collectors.toList());
    List<DriveBackupFile> actualFiles = actual.getFiles().collect(Collectors.toList());
    if (!(expectedFiles.size() == actualFiles.size())) {
      failure =
          String.format(
              "%s instead of %s files in %s",
              expectedFiles.size(), actualFiles.size(), actual.getPath());
      return false;
    }
    for (DriveBackupFile file : expectedFiles) {
      Optional<DriveBackupFile> actualFile = actual.findFile(file.getName());
      if (!actualFile.isPresent()) {
        failure = String.format("%s not found in %s", file.getName(), expected.getPath());
        return false;
      }
      if (!actualFile.get().getMd5Checksum().equals(file.getMd5Checksum())) {
        failure =
            String.format("%s and %s have different checksums", file.getName(), expected.getPath());
        return false;
      }
    }
    for (DriveBackupDirectory subDir : expected.getSubDirectories().collect(Collectors.toList())) {
      Optional<DriveBackupDirectory> actualDir = actual.findDirectory(subDir.getName());
      if (!actualDir.isPresent()) {
        failure = String.format("%s not found in %s", subDir.getName(), expected.getPath());
        return false;
      }
      if (!matchDirs(actualDir.get(), subDir)) {
        return false;
      }
    }
    return true;
  }

  public void describeTo(Description description) {
    description.appendText(failure);
  }

  @Factory
  public static Matcher isEqualToDriveBackupDirectory(DriveBackupDirectory expected) {
    return new IsEqualToDriveBackupDirectory(expected);
  }
}
