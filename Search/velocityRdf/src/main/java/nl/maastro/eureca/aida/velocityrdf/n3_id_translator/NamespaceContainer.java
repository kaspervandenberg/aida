// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

/**
 * Contain {@link Namespace}s of a sesame rdf {@link org.openrdf.model.Model}.
 */
class NamespaceContainer {
	private final Map<String, Namespace> namespacesByUri =
			new HashMap<>();
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
			namespacesByUri.put(ns.getName(), ns);
			namespacesByPrefix.put(ns.getPrefix(), ns);
		}
	}


	/**
	 * Check whether this {@code NamespaceContainer} contains a prefix for
	 * {@code uri}.
	 *
	 * @param uri	uri (i.e. {@link Namespace#getName()} or {@link
	 * 		URI#getNamespace()}) of the	{@code Namespace} to search for.
	 *
	 * @return <ul><li>{@code true}: this {@code NamespaceContainer} contains
	 *		a {@link Namespace} with {@link Namespace#getName()} equal to
	 *		{@code uri}; or,</li>
	 *	<li>{@code false}: this {@code NamespaceContainer} does not contain
	 *		a {@code Namespace} for {@code uri}.</li></ul>
	 */
	public boolean containsPrefixForUri(String uri)
	{
		return namespacesByUri.containsKey(uri);
	}


	/**
	 * Check whether this {@code NamespaceContainer} contains a prefix for
	 * {@code uri}.
	 *
	 * @param uri	uri of the {@code Namespace} to search for.
	 *
	 * @return <ul><li>{@code true}: this {@code NamespaceContainer} contains
	 *		a {@link Namespace} with {@link Namespace#getName()} equal to
	 *		{@code uri}; or,</li>
	 *	<li>{@code false}: this {@code NamespaceContainer} does not contain
	 *		a {@code Namespace} for {@code uri}.</li></ul>
	 */
	public boolean containsPrefixForUri(URI uri)
	{
		return containsPrefixForUri(uri.getNamespace());
	}


	/**
	 * Check whether this {@code NamespaceContainer} contains a namespace for
	 * {@code prefix}.
	 *
	 * @param prefix	{@link Namespace#getPrefix()} of the {@code Namespace}
	 * 		to search for.
	 *
	 * @return <ul><li>{@code true}: this {@code NamespaceContainer} contains
	 *		a {@link Namespace} with {@link Namespace#getPrefix()} equal to
	 *		{@code prefix}; or,</li>
	 *	<li>{@code false}: this {@code NamespaceContainer} does not contain
	 *		a {@code Namespace} for {@code prefix}.</li></ul>
	 */
	public boolean containsUriForPrefix(String prefix)
	{
		return namespacesByPrefix.containsKey(prefix);
	}


	/**
	 * Retrieve {@link Namespace} for {@code uri}.
	 *
	 * @param uri	uri (i.e. {@link Namespace#getName()} or {@link
	 * 		URI#getNamespace()}) of the {@code Namespace} to search for.
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
		Namespace result = namespacesByUri.get(uri);
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


	/**
	 * Translate {@link URI#getNamespace()} into a {@link Namespace} — which
	 * includes {@link Namespace#getPrefix()} and {@link Namespace#getName()}.
	 *
	 * @param uri	uri of the {@code Namespace} to search for.
	 *
	 * @return	{@link Namespace} for {@code uri}.
	 *
	 * @throws NoSuchElementException	when this {@code NamespaceContainer}
	 *		does not {@link #containsPrefixForUri contain} a {@code Namespace}
	 *		for {@code uri}.
	 */
	public Namespace getNamespaceByUri(URI uri)
			throws NoSuchElementException
	{
		return getNamespaceByUri(uri.getNamespace());
	}


	/**
	 * Retrieve the {@link Namespace} for {@code prefix}.
	 *
	 * @param prefix	{@link Namespace#getPrefix()} of the {@code Namespace}
	 * 		to search for.
	 *
	 * @return	{@link Namespace} for {@code prefix}.
	 *
	 * @throws NoSuchElementException	when this {@code NamespaceContainer}
	 *		does not {@link #containsUriForPrefix contain} a {@code Namespace}
	 *		for {@code prefix}.
	 */
	public Namespace getNamespaceByPrefix(String prefix)
	{
		Namespace result = namespacesByPrefix.get(prefix);
		if (result != null) 
		{
			return result;
		}
		else
		{
			throw new NoSuchElementException(String.format(
					"Model contains no namespace for %s",
					prefix));
		}
	}
}


/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

