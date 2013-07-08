// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import java.util.LinkedHashMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Patients {
	private static Patients instance = null;

	private final LinkedHashMap<PatisNumber, Boolean> expectedMetastasis;

	private Patients() {
		expectedMetastasis = initExpectedMetastasis();
	}

	public LinkedHashMap<PatisNumber, Boolean> getExpectedMetastasis() {
		return expectedMetastasis;
	}

	private static LinkedHashMap<PatisNumber, Boolean> initExpectedMetastasis() {
		// Dummy list of patients; reading a list of patisnumbers is not yet in API
		LinkedHashMap<PatisNumber, Boolean> result = new LinkedHashMap<>();
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
		
		result.put(PatisNumber.create("71441"), true);
		result.put(PatisNumber.create("71121"), false);
		result.put(PatisNumber.create("71089"), false);
		result.put(PatisNumber.create("70657"), false);
		result.put(PatisNumber.create("70979"), false);

		result.put(PatisNumber.create("71367"), false);
		result.put(PatisNumber.create("71369"), false);
		result.put(PatisNumber.create("71118"), false);
		result.put(PatisNumber.create("71363"), false);
		result.put(PatisNumber.create("70933"), false);
		result.put(PatisNumber.create("71105"), false);
		result.put(PatisNumber.create("71190"), false);
		result.put(PatisNumber.create("70946"), false);
		result.put(PatisNumber.create("71074"), false);
		result.put(PatisNumber.create("70996"), false);
		result.put(PatisNumber.create("71422"), false);
		result.put(PatisNumber.create("71193"), false);
		result.put(PatisNumber.create("71454"), false);
		result.put(PatisNumber.create("71169"), false);
		result.put(PatisNumber.create("71739"), false);
		result.put(PatisNumber.create("71464"), false);
		return result;

	}
}
