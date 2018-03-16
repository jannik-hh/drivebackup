package drivebackup.local;

import static org.junit.Assert.*;

import drivebackup.Named;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class IgnoreFilePredicateTest {
  @Test
  public void testPredicate() throws IOException {
    class TestNamed implements Named {
      private final String name;

      public TestNamed(String name) {
        this.name = name;
      }

      @Override
      public String getName() {
        return name;
      }
    }
    IgnoreFilePredicate ignoreFilePredicate =
        new IgnoreFilePredicate(new File("./src/test/resources/drivebackup_ignore"));

    assertFalse(ignoreFilePredicate.test(new TestNamed("test.txt")));
    assertFalse(ignoreFilePredicate.test(new TestNamed("test")));
    assertTrue(ignoreFilePredicate.test(new TestNamed(".ignored")));
    assertTrue(ignoreFilePredicate.test(new TestNamed("@eaDir")));
    assertTrue(ignoreFilePredicate.test(new TestNamed("image.raw")));
  }
}
