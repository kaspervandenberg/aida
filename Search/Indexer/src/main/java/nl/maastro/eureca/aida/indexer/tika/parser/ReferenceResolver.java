// © Maastro, 2013
package nl.maastro.eureca.aida.indexer.tika.parser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Use a table of URIs to resolve references
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ReferenceResolver implements ZylabMetadataXml.FileRefResolver {
	
	private Map<URI, URI> resolutionTable = new HashMap<>();

	/**
	 * Resolve {@code reference} to an URL.  If there exists a mapping in 
	 * {@link #resolutionTable} that {@link #match(java.net.URI, java.net.URI) 
	 * matches} {@code reference}, it is used to resolve {@code reference}.
	 * 
	 * <p><i>NOTE: if more than one mapping matches {@code reference}, results 
	 * are undefined.</i></p>
	 * 
	 * @param reference	the reference found in Zylab's metadata to map to an 
	 * 		{@link URL}.
	 * @return	{@code reference} resolved to an {@link URL}.
	 * 
	 * @throws URISyntaxException	when no URI can be constructed from the path
	 * 		in {@code reference}.
	 */
	@Override
	public URL resolve(ZylabMetadataXml.FileRef reference) 
			throws URISyntaxException {
		try {
			URI original = toURI(reference);
			for (URI prefix : resolutionTable.keySet()) {
				if (match(original, prefix)) {
					URI resolution = resolutionTable.get(prefix);
					String origPath = original.getPath();
					String mappedPath = origPath.replace(
							prefix.getPath(), resolution.getPath());
					return new URI(
							resolution.getScheme(),
							resolution.getHost(),
							mappedPath,
							original.getFragment()).toURL();
				}
			}
			return original.toURL();
		} catch (MalformedURLException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Resolve all {@code reference}s starting with {@code from} to {@link URLs}
	 * that start with {@code to}.
	 * 
	 * <p><i>NOTE: if more than one mapping matches {@code reference}, results 
	 * are undefined.</i></p>
	 * 
	 * @param from	the {@link URI} to which {@code reference}s are 
	 * 		{@link #match(java.net.URI, java.net.URI) matched}.  Normally
	 * 		the {@link URI#getScheme() scheme} should be "{@code file://}",
	 * 		the {@link URI#getHost()}
	 * @param to 
	 */
	public void addMapping(URI from, URI to) {
		resolutionTable.put(from, to);
	}

	/**
	 * Match the scheme and the origPath of the two {@link URI}s.
	 * 
	 * @param target	the {@link URI} to check against {@code prefix}.
	 * @param prefix	the {@link URI} to compare the start of {@code target} 
	 * 		with.
	 * 
	 * @return <ul><li>{@code true}, {@code target} starts with {@code prefix};
	 * 		or</li>
	 * 		<li>{@code false}, {@code prefix} is not a prefix of {@code target}.
	 * 		</li></ul>
	 */
	private static boolean match(final URI target, final URI prefix) {
		NullStringComperator comp = new NullStringComperator();
		return (comp.compare(prefix.getScheme(), target.getScheme()) == 0)
				&& (comp.compare(prefix.getHost(), target.getHost()) == 0)
				&& (comp.startsWith(target.getPath(), prefix.getPath()));
	}
	
	/**
	 * Convert a Zylab metadata reference to a {@code file://}-{@link URI}.
	 * 
	 * @see StringToURI#toURI(java.lang.String) 
	 * 
	 * @param ref	a origPath–name-pair as found in Zylab's XmlFields metadata.
	 * @return	a {@code file://}-{@link URI} pointing to the file specified
	 * 		by {@code ref}.
	 * @throws URISyntaxException 	when {@link #toURI(java.lang.String) }
	 * 		throws it.
	 */
	private static URI toURI(final ZylabMetadataXml.FileRef ref)
			throws URISyntaxException {
		if(ref == null) {
			throw new NullPointerException("null reference");
		}
		
		StringBuilder buildPath = ref.refPath != null ?
				new StringBuilder(ref.refPath) : new StringBuilder();
		if((ref.refPath == null) || !(ref.refPath.endsWith("/") || ref.refPath.endsWith("\\"))) {
			buildPath.append("/");
		}
		buildPath.append(ref.refName != null ? ref.refName : "");
		return StringToURI.getInstance().toURI(buildPath.toString());
	}
}
