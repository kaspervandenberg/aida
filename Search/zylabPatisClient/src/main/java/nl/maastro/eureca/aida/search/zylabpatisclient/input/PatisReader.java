// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.input;

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

	public Map<PatisNumber, Boolean> lookup(Iterable<PatisNumber> patients) {
		return getEmdReader().getExpectedMetastasis(patients);
	}

	public Map<PatisNumber, Boolean> readCsvAndLookup(InputStreamReader csvSource) {
		return lookup(getCsvReader().read(csvSource));
	}

	public Map<PatisNumber, Boolean> readFromJSON(InputStreamReader jsonSource) {
		Type mapT = new TypeToken<LinkedHashMap<String, Boolean>>(){ }.getType();
		LinkedHashMap<String, Boolean> items = getGson().fromJson(jsonSource, mapT);
		LinkedHashMap<PatisNumber, Boolean> result = new LinkedHashMap<>(items.size());
		for (Map.Entry<String, Boolean> entry : items.entrySet()) {
			result.put(PatisNumber.create(entry.getKey()), entry.getValue());
		}
		return result;
	}

	public void writeToJSON(Appendable jsonDest, 
			Map<PatisNumber, Boolean> expectedMatches) {
		getGson().toJson(expectedMatches, jsonDest);
	}

	public void convertCsvToJSON(InputStreamReader csvSource, Appendable jsonDest) {
		writeToJSON(jsonDest, readCsvAndLookup(csvSource));
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

	public static void main(String[] args) {
		final String msg_useage = String.format(
				"java %s {patients.csv} {patients.json}",
				PatisReader.class.getName());
		final String errMsg_illegalArgument = 
				"Specify two files as arguments.";
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

		try {
			try(FileReader in = new FileReader(args[0])) {
				try(FileWriter out = new FileWriter(args[1])) {
					instance.convertCsvToJSON(in, out);
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
