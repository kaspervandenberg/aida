// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.input;

import checkers.nullness.quals.Nullable;
import nl.maastro.eureca.aida.search.zylabpatisclient.DummySearcher;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 * Read a collection of {@link PatisNumber} and expected eligibility from
 * serveral sources: json, comma separated values, emd.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PatisReader {
	public class ParseFailedException extends IOException {

		public ParseFailedException() {
		}

		public ParseFailedException(String message) {
			super(message);
		}

		public ParseFailedException(String message, Throwable cause) {
			super(message, cause);
		}

		public ParseFailedException(Throwable cause) {
			super(cause);
		}

		public ParseFailedException(Map.Entry<String, ConceptFoundStatus> problematicEntry) {
			super(String.format(
					"error parsing entry: %s",
					problematicEntry.toString()));
		}
		
	}
	
	private @Nullable PatisCsvReader csvReader = null;
	private @Nullable PatisExpectedEmdReader emdReader = null;
	private @Nullable Gson gson = null;

	public List<PatisNumber> readFromCsv(InputStreamReader csvSource) {
		return getCsvReader().read(csvSource);
	}

	public Map<PatisNumber, ConceptFoundStatus> readFromJSON(InputStreamReader jsonSource) throws ParseFailedException {
		Type mapT = new TypeToken<LinkedHashMap<String, ConceptFoundStatus>>(){ }.getType();
		LinkedHashMap<String, ConceptFoundStatus> items = getGson().fromJson(jsonSource, mapT);
		LinkedHashMap<PatisNumber, ConceptFoundStatus> result = new LinkedHashMap<>(items.size());
		for (Map.Entry<String, ConceptFoundStatus> entry : items.entrySet()) {
			assertCorrectlyParsed(entry);
			result.put(PatisNumber.create(entry.getKey()), entry.getValue());
		}
		return result;
	}

	public void writeToJSON(Appendable jsonDest, 
			Map<PatisNumber, ConceptFoundStatus> expectedMatches) {
		getGson().toJson(expectedMatches, jsonDest);
	}

	public PatisCsvReader getCsvReader() {
		if(csvReader == null) {
			csvReader = new PatisCsvReader();
		}
		return csvReader;
	}

	public PatisExpectedEmdReader getEmdReader() {
		if(emdReader == null) {
			emdReader = new PatisExpectedEmdReader();
		}
		return emdReader;
	}

	public Gson getGson() {
		if(gson == null) {
			gson = new Gson();
		}
		return gson;
	}

	public DummySearcher getDummySearcherFromCsv(InputStreamReader csvSource,
			PatisCsvReader.Classifier classifier) {
		final LinkedHashMap<PatisNumber, ConceptFoundStatus> read =
				getCsvReader().read(csvSource, classifier);
		return getDummySearcher(read);
	}

	public DummySearcher getDummySearcherFromJson(InputStreamReader jsonSource) throws ParseFailedException {
		final Map<PatisNumber, ConceptFoundStatus> read = readFromJSON(jsonSource);
		return getDummySearcher(read);
	}

	private DummySearcher getDummySearcher(final Map<PatisNumber, ConceptFoundStatus> results) {
		return new DummySearcher(results);
	}

	private void assertCorrectlyParsed(Map.Entry<String, ConceptFoundStatus> entry)
			throws ParseFailedException
	{
		if (entry.getKey() == null) {
			throw new ParseFailedException(entry);
		}
		if (entry.getKey().isEmpty()) {
			throw new ParseFailedException(entry);
		}
		if (entry.getValue() == null) {
			throw new ParseFailedException(entry);
		}
	}

	public static void main(String[] args) {
		final String msgUseage = String.format(
				"java %s {patients.csv} {patients.json} {classifier_class}",
				PatisReader.class.getName());
		final String errMsgIllegalArgument = 
				"Specify two files and optionally a classifier as arguments.";
		final String errMsgInputFileNotFound = 
				"File, %s, not found (or unreadable).";
		final String errMsgOutputFileNotAvailabe =
				"File, %s, cannot be created.";
		final String errMsgClosing = 
				"Error closing input fle.";
		
		final PatisReader instance = new PatisReader();

		if(args.length < 2) {
			System.out.println(msgUseage);
			throw new Error(new IllegalArgumentException(errMsgIllegalArgument));
		}
		PatisCsvReader.Classifier classifier;
		if (args.length >= 3) {
			try {
				Class <?> c = Class.forName(args[2]);
				classifier = (PatisCsvReader.Classifier) c.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
				throw new Error(ex);
			}
		} else {
			classifier = new PatisExpectedEmdReader();
		}

		try {
			try(FileReader in = new FileReader(args[0])) {
				try(FileWriter out = new FileWriter(args[1])) {
					LinkedHashMap<PatisNumber, ConceptFoundStatus> expected =
							instance.getCsvReader().read(in, classifier);
					instance.writeToJSON(out, expected);
				} catch (IOException ex) {
					throw new Error(String.format(errMsgOutputFileNotAvailabe, args[1]), ex);
				}
			} catch (FileNotFoundException ex) {
				throw new Error(String.format(errMsgInputFileNotFound, args[0]), ex);
			}
		} catch (IOException ex) {
			throw new Error(errMsgClosing, ex);
		}
	}
}
