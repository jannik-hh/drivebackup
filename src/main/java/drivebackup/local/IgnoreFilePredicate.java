package drivebackup.local;

import drivebackup.Named;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class IgnoreFilePredicate implements Predicate<Named> {
  private final Collection<Pattern> pattern;

  public IgnoreFilePredicate(File ignoreFile) throws IOException {
    pattern = patternsFromFile(ignoreFile);
  }

  @Override
  public boolean test(Named t) {
    String filename = t.getName();
    return getIgnorePatterns().anyMatch((pattern) -> pattern.matcher(filename).matches());
  }

  private Stream<Pattern> getIgnorePatterns() {
    return pattern.stream();
  }

  private Collection<Pattern> patternsFromFile(File file) throws IOException {
    List<Pattern> patterns = new ArrayList<>();
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        Pattern pattern = Pattern.compile(line);
        patterns.add(pattern);
      }
    }
    ;
    return patterns;
  }
}
