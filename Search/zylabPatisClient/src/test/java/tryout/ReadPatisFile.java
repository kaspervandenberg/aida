/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tryout;

import nl.maastro.eureca.aida.search.zylabpatisclient.input.PatisCsvReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;

/**
 *
 * @author kasper
 */
public class ReadPatisFile {
	
	public static void main(String args[]) {
		File dataFile = new File("data/timePerPatient21-12-2012_AD2.csv");
		try (FileReader fileReader = new FileReader(dataFile)) {
			PatisCsvReader patisReader = new PatisCsvReader();
			List<PatisNumber> patients = patisReader.read(fileReader);
			System.out.println(patients);
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}
}
