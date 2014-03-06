/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

/**
 *
 * @author kasper
 */
@RunWith(Theories.class)
public class FileNamesTest {
	public interface TestData {
		public Path getFile();
		public Date getExpectedDate();
	}

	public enum DataItems implements TestData {
		@DataPoint
		FILE_JUNE_9TH("results-" + CONCEPT_LOCALNAME + "-19800609-105316.json",
				new Date(329388796000l) /* "9 jun 1980 10:53:16" */),
		FILE_CONCEPT_OCT_22("results-" + CONCEPT_LOCALNAME + "-20131022-190714.json",
				new Date(1382461634000l) /* "22 oct 2013 19:07:14" */),
		DIR_FILE_2030("results-" + CONCEPT_LOCALNAME + "-20300407-152345.json",
				new Date(1901798625000l) /* "7 apr 2030 15:23:45" */),
		DIR_FILE_2030_JUST_BEFORE("results-" + CONCEPT_LOCALNAME + "-20300407-152344.json",
				new Date(1901798624000l));

		private DataItems(String filename, Date date_) {
			this.file = Paths.get(filename);
			this.expectedDate = date_;
		}
		
		private DataItems(Path dir, String filename, Date date_) {
			this.file = dir.resolve(Paths.get(filename));
			this.expectedDate = date_;
		}
		
		private final Path file;
		private final Date expectedDate;

		@Override
		public Path getFile() {
			return file;
		}

		@Override
		public Date getExpectedDate() {
			return expectedDate;
		}
	}
	@DataPoints
	public static final TestData[] ALL_DATA_ITEMS = DataItems.values();


	@DataPoint
	public static final List<Path> SEQ_SORTED = Arrays.asList(
			DataItems.FILE_JUNE_9TH.getFile(),
			DataItems.FILE_CONCEPT_OCT_22.getFile(),
			DataItems.DIR_FILE_2030_JUST_BEFORE.getFile(),
			DataItems.DIR_FILE_2030.getFile());

	@DataPoint
	public static final List<Path> SEQ_REVERSED = Arrays.asList(
			DataItems.DIR_FILE_2030.getFile(),
			DataItems.DIR_FILE_2030_JUST_BEFORE.getFile(),
			DataItems.FILE_CONCEPT_OCT_22.getFile(),
			DataItems.FILE_JUNE_9TH.getFile());

	@DataPoint
	public static final List<Path> SINGLE = Arrays.asList(
			DataItems.DIR_FILE_2030.getFile());
	
	private static final String CONCEPT_LOCALNAME="testconcept";
	
	private final List<Path> filesInTestDir;
	private Concept concept; 
	private FileNames testee;
	private Path tmpDir;

	public FileNamesTest(List<Path> filesInTestDir_) {
		this.filesInTestDir = filesInTestDir_;
	}
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		concept = mockConcept();
		
		try {
			tmpDir = createTmpDir(filesInTestDir);
			testee = new FileNames(tmpDir);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("Cannot create tmp dir", ex);
		}
	}

	@Theory
	public void testParseDate(TestData data) {
		try {
			assertThat(testee.parseDate(data.getFile()), is(equalTo(data.getExpectedDate())));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			throw ex;
		}
	}

	@Test
	public void testMostRecentFile() {
		try {
			assertThat(testee.getMostRecent(filesInTestDir), is(equalTo(DataItems.DIR_FILE_2030.getFile())));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			throw ex;
		}
	}

	@Test
	public void testCreateHtmlFile() throws IOException {
		File testFile = testee.createHtmlResultsFile();
		
		assertCanCreateWritableFile(testFile);
	}

	@Test
	public void testCreateJsonFile() throws IOException {
		File testFile = testee.createJsonResultsFile(concept);

		assertCanCreateWritableFile(testFile);
	}

	@Test
	public void testGetMostRecentJson() throws IOException {
		File result = testee.getMostRecentJson(concept);
		Path path_result = result.toPath();

		assertThat(path_result, is(equalTo(tmpDir.resolve(DataItems.DIR_FILE_2030.getFile()))));
	}
	
	private void displayDateAsLong() {
		try {
			System.out.println(new SimpleDateFormat("d MMM yyyy hh:mm:ss").parse("7 apr 2030 15:23:45").getTime());
		} catch (ParseException ex) {
			Logger.getLogger(FileNamesTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private Concept mockConcept() {
		QNameUtil qnames = QNameUtil.instance();
		Concept mocked = mock(Concept.class);
		try {
			when (mocked.getName()) .thenReturn(qnames.createQNameInPreconstructedNamespace(CONCEPT_LOCALNAME));
		} catch (Exception ex) {
			throw new Error(ex);
		}
		
		return mocked;
	}

	private static void assertCanCreateWritableFile(File destination) throws IOException {
		boolean createdFreshFile = destination.createNewFile();
		if (createdFreshFile) {
			try {
				if (destination.canWrite()) {
					try (FileWriter writer = new FileWriter(destination)) {
						writer.append("dummy test output");
					}
				} else {
					fail(String.format("Cannot write to %s", destination));
				}
			} finally {
				destination.delete();
			}
		} else {
			fail(String.format("Cannot create file: %s", destination));
		}
	}

	private static Path createTmpDir(List<Path> files) throws IOException {
		Path tmpDir = Files.createTempDirectory("eligibilityToolTestFileNames");
		tmpDir.toFile().deleteOnExit();
		
		for (Path tmpFile : files) {
			touchTmpFile(tmpDir, tmpFile);
		}

		return tmpDir;
	}
	
	private static void touchTmpFile(Path tmpDir, Path file) throws IOException {
		try {
			Path composed = tmpDir.resolve(file);
			Path parent = composed.getParent();
			ensureCanWriteTmpFiles(parent);
			
			File tmpFile = composed.toFile();
			boolean createdFreshFile = tmpFile.createNewFile();
			if(createdFreshFile) {
				tmpFile.deleteOnExit();
			}
		} catch (IOException ex) {
			System.out.println(tmpDir);
			System.out.println(file);
			throw ex;
		}
	}

	private static void createTmpDir(Path tmpDir) throws IOException {
		Path parent = tmpDir.getParent();
		ensureCanWriteTmpFiles(parent);
		
		File filesystemDir = tmpDir.toFile();
		boolean createdDir = filesystemDir.mkdir();
		System.out.println("mkdir: " + tmpDir);
		if(createdDir) {
			filesystemDir.deleteOnExit();
		}
	}

	private static void ensureCanWriteTmpFiles(Path dir) throws IOException {
		File filesystemDir = dir.toFile();
		if (!filesystemDir.exists()) {
			createTmpDir(dir);
		}
		
		if (!filesystemDir.isDirectory()) {
			throw new NotDirectoryException(dir.toString());
		}
		if (!filesystemDir.canWrite()) {
			throw new AccessDeniedException(dir.toString());
		}
	}
	
}