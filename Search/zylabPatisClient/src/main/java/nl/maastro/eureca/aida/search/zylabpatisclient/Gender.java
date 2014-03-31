// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public enum Gender {
	MALE,
	FEMALE,

	/**
	 * Used when documents do no contain a patient's Gender and 
	 * when creating dummy or empty documents.
	 */
	UNKNOWN;

	public static Gender parse(String string) {
		switch (string) {
			case "M":
			case "m":
				return MALE;

			// Dutch meaning 'vrouw', as used in Zylab Documents within maastro.
			case "V":
			case "v":
				return FEMALE;

			default:
				throw new IllegalArgumentException(String.format("Unable to parse %s into a Gender.", string));
		}
	}
}