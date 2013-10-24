/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

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
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.*;
import org.junit.experimental.theories.DataPoints;

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
		FILE_JUNE_9TH("results-19800609-105316.json", new Date(329388796000l) /* "9 jun 1980 10:53:16" */),
		FILE_CONCEPT_OCT_22("results-20131022-190714.json", new Date(1382461634000l) /* "22 oct 2013 19:07:14" */),
		DIR_FILE_2030(Paths.get("somedir"), "results-20300407-152345.json", new Date(1901798625000l) /* "7 apr 2030 15:23:45" */),
		DIR_FILE_2030_JUST_BEFORE("results-20300407-152344.json", new Date(1901798624000l));

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
	public static final List<Path> SINGLE = Arrays.asList(DataItems.DIR_FILE_2030.getFile());
	
	private FileNames testee;

	@Before
	public void setup() {
		testee = new FileNames();
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

	@Theory
	public void testMostRecentFile(List<Path> directory) {
		try {
			assertThat(testee.getMostRecent(directory), is(equalTo(DataItems.DIR_FILE_2030.getFile())));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			throw ex;
		}
	}

	private void displaytDateAsLong() {
		try {
			System.out.println(new SimpleDateFormat("d MMM yyyy hh:mm:ss").parse("7 apr 2030 15:23:45").getTime());
		} catch (ParseException ex) {
			Logger.getLogger(FileNamesTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
}