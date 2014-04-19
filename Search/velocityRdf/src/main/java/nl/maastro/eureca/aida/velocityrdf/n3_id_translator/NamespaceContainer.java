// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import org.openrdf.model.Namespace;

/**
 * Contain {@link Namespace}s of a sesame rdf {@link org.openrdf.model.Model}.
 */
class NamespaceContainer {
	private final Map<String, Namespace> namespacesByPrefix =
			new HashMap<>();

	/**
	 * Create a {@code NamespaceContainer} filled with {@code
	 * initialNamespaces_}.
	 *
	 * @param initialNamespaces_	iterable of {@link Namespace}
	 *		When {@code initialNamespaces_} contains two or	{@code Namespaces}
	 *		with equal {@link Namespaces#getName()}, the last is retained and
	 *		others are discarded.
	 */
	public NamespaceContainer(
			Iterable<Namespace> initialNamespaces_)
	{
		for (Namespace ns: initialNamespaces_)
		{
			namespacesByPrefix.put(ns.getName(), ns);
		}
	}


	/**
	 * @param uri	uri (i.e. {@link Namespace#getName()} of the 
	 *		{@code Namespace} to search for.
	 *
	 * @return <ul><li>{@code true}: this {@code NamespaceContainer} contains
	 *		a {@link Namespace} with {@link Namespace#getName()} equal to
	 *		{@code uri}; or,</li>
	 *	<li>{@code false}: this {@code NamespaceContainer} does not contain
	 *		a {@code Namespace} for {@code uri}.</li></ul>
	 */
	public boolean containsPrefixForUri(String uri)
	{
		return namespacesByPrefix.containsKey(uri);
	}


	/**
	 * Retrieve {@link Namespace} for {@code uri}.
	 *
	 * @param uri	uri (i.e. {@link Namespace#getName()} of the 
	 *		{@code Namespace} to search for.
	 *
	 * @return	{@link Namespace} for {@code uri}.
	 *
	 * @throws NoSuchElementException	when this {@code NamespaceContainer}
	 *		does not {@link #containsPrefixForUri contain} a {@code Namespace}
	 *		for {@code uri}.
	 */
	public Namespace getNamespaceByUri(String uri) 
			throws NoSuchElementException
	{
		Namespace result = namespacesByPrefix.get(uri);
		if (result != null) 
		{
			return result;
		}
		else
		{
			throw new NoSuchElementException(String.format(
					"Model contains no namespace for %s",
					uri));
		}
	}
}


/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

