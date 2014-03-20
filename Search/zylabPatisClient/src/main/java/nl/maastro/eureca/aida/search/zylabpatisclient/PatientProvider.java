// © Maastro, 2014
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Collection;

/**
 * Interface for classes that provide a list of patients to search.
 
 * <p>Register {@code PatientProvider}s via Java's Service Provider mechanism.
 * The API documentation of {@link java.util.ServiceLoader} describes how to
 * register a service provider.</p>
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public interface PatientProvider {
	/**
	 * @return a collection of {@link PatisNumber}s of the patients to search
	 *		for.
	 */
	public Collection<PatisNumber> getPatients();
}

/* vim: set tabstop=4 shiftwidth=4 autoindent fo=cqwa2 : */

