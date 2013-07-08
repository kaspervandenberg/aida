/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tryout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.File;
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
import nl.maastro.eureca.aida.search.zylabpatisclient.input.PatisCsvReader;
import nl.maastro.eureca.aida.search.zylabpatisclient.input.PatisExpectedEmdReader;
import org.apache.lucene.analysis.sinks.TokenTypeSinkFilter;

/**
 *
 * @author kasper
 */
public class PatisJSON {
	private static PatisJSON singleton = null;
	
	private final Gson gson;
	private final PatisCsvReader fileReader;
	private final PatisExpectedEmdReader emdReader;
	private final Type patisExpectMatchType;

	private PatisJSON() {
		this.gson = new Gson();
		this.fileReader = new PatisCsvReader();
		this.emdReader = new PatisExpectedEmdReader();
		this.patisExpectMatchType = new TypeToken<Map<PatisNumber, Boolean>>(){ }.getType();

	}

	public static PatisJSON instance() {
		if(singleton == null) {
			singleton = new PatisJSON();
		}
		return singleton;
	}
	
	public Map<PatisNumber, Boolean> expected(InputStreamReader patisCsv) {
		List<PatisNumber> patients = fileReader.read(patisCsv);
		LinkedHashMap<PatisNumber, Boolean> result = 
				new LinkedHashMap<>(patients.size());
		return emdReader.getExpectedMetastasis(patients);
	}

	public void writeJSON(InputStreamReader patisCsv, Appendable out_json) {
		gson.toJson(expected(patisCsv), out_json);
	}
	
	public static void main(String[] args) {
		File f_patisCsv = new File(args[0]);
		File f_out = new File(args[1]);
		try {
			try (InputStreamReader is_patisCsv = new FileReader(f_patisCsv)) {
				try (FileWriter os_out = new FileWriter(f_out)) {
					PatisJSON.instance().writeJSON(is_patisCsv, os_out);
				} catch(IOException ex) {
					throw new Error(String.format("Cannot write to %s", f_out.toString()));
				}
			} catch (FileNotFoundException ex) {
				throw new Error(String.format("Cannot find %s to read from", f_patisCsv.toString()));
			}
		} catch (IOException ex) {
			throw new Error(ex);
		}

		try {
			try (InputStreamReader jsonReader = new FileReader(f_out)) {
//				Map<PatisNumber, Boolean> readPatients = PatisJSON.instance().gson.fromJson(jsonReader, PatisJSON.instance().patisExpectMatchType);
				Type mapT = new TypeToken<LinkedHashMap<String, Boolean>>(){ }.getType();
				LinkedHashMap<String, Boolean> items = PatisJSON.instance().gson.fromJson(jsonReader, mapT);
				System.out.println(items);
			} catch (FileNotFoundException ex) {
				throw new Error(String.format("JSON file, %s, was not written.", f_out));
			}
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}
}
