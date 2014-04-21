// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.NoSuchElementException;

/**
 * Translate {@link URI}s into QName-style identifiers.
 * For example an {@code URI} {@code http://example.org/ns#bar} could be
 * translated to the identifier {@code "foo:bar"}, given that the 
 * namespace–prefix-pair {@code <"http://example.org/ns#", "foo">} exists
 * in the used {@link NamespaceContainer}.
 *
 * @see <a href="http://www.w3.org/TeamSubmission/turtle/#terms">Turle §RDF
 * Term</a>
 * @see FullUriTranslator
 * @see UriTranslator
 * 
 *
 * @author Kasper van den Berg &lt;kasper.vandenberg@maastro.nl&gt; &lt;kasper@kaspervandenberg.net&gt;
 */
class QNameTranslator implements Translator<URI> {
	private static final String PREFIX_GRP = "prefix";
	private static final String LOCAL_GRP = "local";
	private static final Pattern QNAME_PARSE_PATTERN;
	static {
		try
		{
			QNAME_PARSE_PATTERN = Pattern.compile(
				"(?<prefix>" + N3SyntaxPatterns.PREFIX_NAME.patternExpr() + "?)"
				+ ":(?<local>"  + N3SyntaxPatterns.NAME.patternExpr() + ")");
		}
		catch (Throwable ex)
		{
			throw new Error("Malformed pattern", ex);
		}
	}
	private final NamespaceContainer namespaces;

	QNameTranslator(final NamespaceContainer namespaces_) {
		this.namespaces = namespaces_;
	}

	@Override
	public boolean isWellFormed(String id) {
		return N3SyntaxPatterns.QNAME.matches(id);
	}

	@Override
	public String getId(final URI uri) {
		try
		{
			Namespace ns = namespaces.getNamespaceByUri(uri);
			return getQnameId(ns, uri);
		}
		catch (NoSuchElementException ex)
		{
			throw new Error(String.format(
					"Attempt to create a QName for namespace %s without "
					+ "a defined prefix (URI: %s); "
					+ "fix code to use FullUriTranslator instead.",
					uri.getNamespace(), uri));
		}
	}

	public String getQnameId(final Namespace ns, final URI uri) {
		return String.format("%s:%s", ns.getPrefix(), uri.getLocalName());
	}


	@Override
	public boolean matches(URI val, String id)
	{
		if (isWellFormed(id))
		{
			Matcher parsedId = parseWellFormedId(id);

			return localPartsMatching(val, parsedId)
					&& namespacesMatching(val, parsedId);
		}
		else
		{
			return false;
		}
	}

	
	/**
	 * Parse the pattern into two groups: {@code PREFIX_GRP} and {@code
	 * LOCAL_GRP}.
	 *
	 * @param wellformedId	{@link #wellformed} identifier.
	 *
	 * @return {@link Matcher} matching {@code wellformedId} containing 
	 *		the two groups:: {@code PREFIX_GRP} and {@code LOCAL_GRP}.
	 *
	 * @throws IllegalArgumentException		when {@code wellformedId}
	 *		is not well formed.
	 */
	private Matcher parseWellFormedId(String wellformedId)
	{
		Matcher result = QNAME_PARSE_PATTERN.matcher(wellformedId);
		boolean matches = result.matches(); // Used for its side effects of
											// setting group
		if (!matches)
		{
			if (isWellFormed(wellformedId))
			{
				throw new Error(String.format(
						"id (\"%s\") is wellformed, but QNAME_PARSE_PATTERN "
						+ "(\"%s\") does not match.",
						wellformedId, QNAME_PARSE_PATTERN));
			}
			else
			{
				throw new IllegalArgumentException(String.format(
						"Expecting a well formed identifier; id (\"%s\") "
						+ "is not well formed.",
						wellformedId));
			}
		}
		return result;
	}


	private boolean localPartsMatching(URI val, Matcher parsedId)
	{
		String valLocalName = val.getLocalName();
		String idLocalName = parsedId.group(LOCAL_GRP);

		return valLocalName.equals(idLocalName);
	}


	private boolean namespacesMatching(URI val, Matcher parsedId)
	{
		String valNamespaceUri = val.getNamespace();
		String idPrefix = parsedId.group(PREFIX_GRP);

		if (namespaces.containsUriForPrefix(idPrefix))
		{
			Namespace idNamespace = namespaces.getNamespaceByPrefix(idPrefix);
			String idNamespaceUri = idNamespace.getName();

			return valNamespaceUri.equals(idNamespaceUri);
		}
		else
		{
			return false;
		}
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

