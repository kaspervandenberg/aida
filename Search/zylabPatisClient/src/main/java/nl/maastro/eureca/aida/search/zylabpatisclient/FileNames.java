/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author kasper
 */
public class FileNames {
	private class FileRecencyComparator implements Comparator<Path> {
		@Override
		public int compare(Path o1, Path o2) {
			Date d1 = parseDate(o1);
			Date d2 = parseDate(o2);
			return d1.compareTo(d2);
		}
	}
	
	private static final String FILENAME_FORMAT = "results%1$s-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS.%3$s";
	private static final String GLOB_FORMAT = "results-%s-*-*.%s";
	private static final Pattern FILE_DATE;
	static {
		try {
			FILE_DATE = Pattern.compile(
					"results(?<concept>-.*)?-" +
					"(?<datetime>" +
						"(?<year>[0-9][0-9][0-9][0-9])(?<month>[0-9][0-9])(?<day>[0-9][0-9])-" +
						"(?<hour>[0-9][0-9])(?<minute>[0-9][0-9])(?<second>[0-9][0-9])" +
					")" +
					"(?<extension>\\..*)");
		} catch (PatternSyntaxException ex) {
			throw new Error(ex);
		}
	}
	private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	private Path workingDirectory;

	public FileNames() {
		this.workingDirectory = Paths.get("");
	}

	public FileNames(Path workingDirectory_) {
		this.workingDirectory = workingDirectory_;
	}
	
	public File createHtmlResultsFile() {
		return createResultsFile("", "html");
	}

	public File createJsonResultsFile(Concept concept) {
		return createResultsFile(concept.getName().getLocalPart(), "json");
	}

	public File getMostRecentJson(Concept concept) throws IOException, IllegalArgumentException {
		String glob = getGlobPattern(concept, "json");
		DirectoryStream<Path> dir = Files.newDirectoryStream(workingDirectory, glob);
		Path mostRecent = getMostRecent(dir);

		return mostRecent.toFile();
	}

	private File createResultsFile(String conceptPart, String extension) {
		String seperatedConceptPart = prependSeparator(conceptPart);
		Date now = new Date();
		String fileName = String.format(FILENAME_FORMAT, seperatedConceptPart, now, extension);
		File f = workingDirectory.resolve(fileName).toFile();

		return f;
	}

	private String prependSeparator(String emptyOrConcept) {
		if(!emptyOrConcept.isEmpty() && !startsWithSeparator(emptyOrConcept)) {
			return "-" + emptyOrConcept;
		} else {
			return emptyOrConcept;
		}
	}

	private boolean startsWithSeparator(String s) {
		return s.startsWith("-");
	}

	private String getGlobPattern(Concept concept, String extension) {
		String conceptPart = concept.getName().getLocalPart();
		return String.format(GLOB_FORMAT, conceptPart, extension);
	}
	
	Path getMostRecent(Iterable<Path> directory) {
		FileRecencyComparator recencyComperator = new FileRecencyComparator();
		Iterator<Path> iter = directory.iterator();
		if (iter.hasNext()) {
			Path mostRecent = iter.next();
			while(iter.hasNext()) {
				Path cur = iter.next();
				if(recencyComperator.compare(mostRecent, cur) < 0) {
					mostRecent = cur;
				}
			}
			return mostRecent;
		} else {
			throw new IllegalArgumentException("No files in directory");
		}
	}

	Date parseDate(Path file) {
		String filename = file.getFileName().toString();
		Matcher datePartMatcher = FILE_DATE.matcher(filename);
		datePartMatcher.find();
		String datePart = datePartMatcher.group("datetime");
		try {
			Date result = DATE_PARSER.parse(datePart);
			return result;
		} catch (ParseException ex) {
			throw new Error(ex);
		}
	}
}
