// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;

/**
 * Read a list of {@link PatisNumber}s from a comma separated file.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PatisCsvReader {
	public interface Classifier {
		public Boolean expectedClassification(PatisNumber patient,
				String[] textFields); 
	}
	
	private static final Logger log =
			Logger.getLogger(PatisCsvReader.class.getCanonicalName());
	private String separator = ";";
	private int column = 0;
	private int ignoredLines = 1;

	public List<PatisNumber> read(InputStreamReader input) {
		return new ArrayList<>(
				read(input, new Classifier() {
					@Override
					public Boolean expectedClassification(PatisNumber patient, String[] textFields) {
						return false;
					}
				}).keySet());
	}
	
	public LinkedHashMap<PatisNumber, Boolean> read(
			InputStreamReader input,
			Classifier classifier) {
		BufferedReader reader = new BufferedReader(input);
		LinkedHashMap<PatisNumber, Boolean> result = new LinkedHashMap<>();
		try {
			String line = reader.readLine();
			int linesRead = 0;
			while (line != null && linesRead < ignoredLines) {
				line = reader.readLine();
				linesRead++;
			}
			while (line != null) {
				String fields[] = line.split(separator);
				try {
					if(fields.length > column) {
						PatisNumber p = PatisNumber.create(fields[column]);
						boolean classification = classifier.expectedClassification(p, fields);
						result.put(p, classification);
					}
				} catch (IllegalArgumentException ex) {
					log.log(Level.WARNING, "Invalid patisnumber: \"{0}\"", fields[column]);
				}
				line = reader.readLine();
				linesRead++;
			}
		} catch (IOException ex) {
			throw new Error(ex);
		}
		return result;
	}
	
	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getIgnoredLines() {
		return ignoredLines;
	}

	public void setIgnoredLines(int ignoredLines) {
		this.ignoredLines = ignoredLines;
	}
	
	
}
