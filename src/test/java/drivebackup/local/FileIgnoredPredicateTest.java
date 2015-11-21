package drivebackup.local;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FileIgnoredPredicateTest {
	@Test
	public void testPredicate() throws IOException{
		FileIgnoredPredicate fileIgnoredPredicate = new FileIgnoredPredicate(new File("./src/test/resources/drivebackup_ignore"));
		
		assertFalse(fileIgnoredPredicate.test(mockFileWithName("test.txt")));
		assertFalse(fileIgnoredPredicate.test(mockFileWithName("test")));
		assertTrue(fileIgnoredPredicate.test(mockFileWithName(".ignored")));
		assertTrue(fileIgnoredPredicate.test(mockFileWithName("@eaDir")));
		assertTrue(fileIgnoredPredicate.test(mockFileWithName("image.raw")));
	}
	
	private File mockFileWithName(String name){
		File file = mock(File.class);
		when(file.getName()).thenReturn(name);
		return file;
	}
}
