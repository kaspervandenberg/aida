/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tryout;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import nl.maastro.eureca.aida.search.zylabpatisclient.input.PatisExpectedEmdReader;

/**
 *
 * @author kasper
 */
public class ReadPatisEmd {
	private final Map<PatisNumber, Boolean> hardcoded;
	private final PatisExpectedEmdReader emdReader;

	public ReadPatisEmd() {
		this.hardcoded = initPatients();
		this.emdReader = new PatisExpectedEmdReader();
	}
	
	private static Map<PatisNumber, Boolean> initPatients() {
		// Dummy list of patients; reading a list of patisnumbers is not yet in API
		Map<PatisNumber, Boolean> result = new LinkedHashMap<>();
		result.put(PatisNumber.create("71358"), false);// Exp 0
		result.put(PatisNumber.create("71314"), false);
		result.put(PatisNumber.create("71415"), false); // Exp 0
		result.put(PatisNumber.create("71539"), false);
		result.put(PatisNumber.create("71586"), false);
		result.put(PatisNumber.create("70924"), false);
		result.put(PatisNumber.create("71785"), false);
		result.put(PatisNumber.create("71438"), false);
		result.put(PatisNumber.create("71375"), false);
		result.put(PatisNumber.create("71448"), false);
		
		result.put(PatisNumber.create("71681"), true); // Exp 1
		result.put(PatisNumber.create("71692"), true);
		result.put(PatisNumber.create("71757"), true);
		result.put(PatisNumber.create("70986"), true);
		result.put(PatisNumber.create("46467"), true);
		return result;
	}

	public void check() {
		Map<PatisNumber, EligibilityClassification> found = emdReader.getExpectedMetastasis(hardcoded.keySet());

		Set<PatisNumber> missing = new LinkedHashSet<>(hardcoded.keySet());
		missing.removeAll(found.keySet());
		System.out.println("Missing: " + missing);

		Set<PatisNumber> aliens = new LinkedHashSet<>(found.keySet());
		aliens.removeAll(hardcoded.keySet());
		System.out.println("Aliens: " + aliens);

		Set<PatisNumber> intersection = new LinkedHashSet<>(hardcoded.keySet());
		intersection.retainAll(found.keySet());
		System.out.println("Divergents:");
		for (PatisNumber patient : intersection) {
			if(!hardcoded.get(patient).equals(found.get(patient))) {
				System.out.println(String.format("Patient: %s, hardcoded: %s, found: %s",
						patient.getValue(), hardcoded.get(patient), found.get(patient)));
			}
		}
	}

	public static void main(String[] args) {
		new ReadPatisEmd().check();
	}
}
