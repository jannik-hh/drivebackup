package drivebackup.local;

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

public class FileIgnoredPredicate implements Predicate<File> {
  private final Collection<Pattern> pattern;

  public FileIgnoredPredicate(File ignoreFile) throws IOException {
    pattern = patternsFromFile(ignoreFile);
  }

  @Override
  public boolean test(File t) {
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
