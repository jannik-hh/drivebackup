package drivebackup.local;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FileNotIgnoredPredicateTest {
	@Test
	public void testPredicate() throws IOException{
		FileNotIgnoredPredicate fileNotIgnoredPredicate = new FileNotIgnoredPredicate(new File("./src/test/resources/drivebackup_ignore"));
		
		assertTrue(fileNotIgnoredPredicate.test(mockFileWithName("test.txt")));
		assertTrue(fileNotIgnoredPredicate.test(mockFileWithName("test")));
		assertFalse(fileNotIgnoredPredicate.test(mockFileWithName(".ignored")));
		assertFalse(fileNotIgnoredPredicate.test(mockFileWithName("@eaDir")));
		assertFalse(fileNotIgnoredPredicate.test(mockFileWithName("image.raw")));
	}
	
	private File mockFileWithName(String name){
		File file = mock(File.class);
		when(file.getName()).thenReturn(name);
		return file;
	}
}
