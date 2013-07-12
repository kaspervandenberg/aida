// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.input;

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
import java.util.ServiceLoader;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;

/**
 * Read a collection of {@link PatisNumber} and expected eligibility from
 * serveral sources: json, comma separated values, emd.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PatisReader {
	private PatisCsvReader csvReader = null;
	private PatisExpectedEmdReader emdReader = null;
	private Gson gson = null;

	public List<PatisNumber> readFromCsv(InputStreamReader csvSource) {
		return getCsvReader().read(csvSource);
	}

	public Map<PatisNumber, EligibilityClassification> readFromJSON(InputStreamReader jsonSource) {
		Type mapT = new TypeToken<LinkedHashMap<String, EligibilityClassification>>(){ }.getType();
		LinkedHashMap<String, EligibilityClassification> items = getGson().fromJson(jsonSource, mapT);
		LinkedHashMap<PatisNumber, EligibilityClassification> result = new LinkedHashMap<>(items.size());
		for (Map.Entry<String, EligibilityClassification> entry : items.entrySet()) {
			result.put(PatisNumber.create(entry.getKey()), entry.getValue());
		}
		return result;
	}

	public void writeToJSON(Appendable jsonDest, 
			Map<PatisNumber, EligibilityClassification> expectedMatches) {
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
		final LinkedHashMap<PatisNumber, EligibilityClassification> read =
				getCsvReader().read(csvSource, classifier);
		return getDummySearcher(read);
	}

	public DummySearcher getDummySearcherFromJson(InputStreamReader jsonSource) {
		final Map<PatisNumber, EligibilityClassification> read = readFromJSON(jsonSource);
		return getDummySearcher(read);
	}

	private DummySearcher getDummySearcher(final Map<PatisNumber, EligibilityClassification> results) {
		return new DummySearcher(results);
	}

	public static void main(String[] args) {
		final String msg_useage = String.format(
				"java %s {patients.csv} {patients.json} {classifier_class}",
				PatisReader.class.getName());
		final String errMsg_illegalArgument = 
				"Specify two files and optionally a classifier as arguments.";
		final String errMsg_inputFileNotFound = 
				"File, %s, not found (or unreadable).";
		final String errMsg_outputFileNotAvailabe =
				"File, %s, cannot be created.";
		final String errMsg_closing = 
				"Error closing input fle.";
		
		final PatisReader instance = new PatisReader();

		if(args.length < 2) {
			System.out.println(msg_useage);
			throw new Error(new IllegalArgumentException(errMsg_illegalArgument));
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
					LinkedHashMap<PatisNumber, EligibilityClassification> expected =
							instance.getCsvReader().read(in, classifier);
					instance.writeToJSON(out, expected);
				} catch (IOException ex) {
					throw new Error(String.format(errMsg_outputFileNotAvailabe, args[1]), ex);
				}
			} catch (FileNotFoundException ex) {
				throw new Error(String.format(errMsg_inputFileNotFound, args[0]), ex);
			}
		} catch (IOException ex) {
			throw new Error(errMsg_closing, ex);
		}
	}
}
