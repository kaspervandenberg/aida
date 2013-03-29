// © Maastro, 2013
package nl.maastro.eureca.aida.indexer.tika.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Use a table of URIs to resolve references
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ReferenceResolver implements ZylabMetadataXml.FileRefResolver {
	private enum OS {
		WINDOWS {
			@Override public String replaceSeparators(final String s) { return s.replaceAll("\\\\", "/"); } },
		UNIX {
			@Override public String replaceSeparators(final String s) { return s; } };

		public abstract String replaceSeparators(final String s);
		
		private static Set<OS> rootPathOS = null;

		private static Set<OS> getRootPathOS() {
			if(rootPathOS == null) {
				EnumSet<OS> result = EnumSet.noneOf(OS.class);
				for (File rootPath : File.listRoots()) {
					result.add(PathType.getType(rootPath.getPath()).os);
				}
				rootPathOS = Collections.unmodifiableSet(result);
			}
			return rootPathOS;
		}
		
		public boolean isNative() {
			return getRootPathOS().contains(this);
		}
	}

	private enum Relative {
		RELATIVE,
		ABSOLUTE
	}

	private enum Location {
		LOCAL("file:///"),
		NETWORK("file://");

		public final String uriPrefix;

		private Location(final String uriPrefix_) {
			uriPrefix = uriPrefix_;
		}
	}
		
	private enum PathType {
		WIN_DRIVE_ABS("^[a-zA-Z]:\\", OS.WINDOWS, Relative.ABSOLUTE, Location.LOCAL),
		WIN_DRIVE_REL("^([a-zA-Z]:)?[^\\]", OS.WINDOWS, Relative.RELATIVE, Location.LOCAL),
		WIN_UNC("^\\\\", OS.WINDOWS, Relative.ABSOLUTE, Location.NETWORK);

		private final String patExpr;
		private final Pattern pat;
		
		private final OS os;
		private final Relative isRelative;
		private final Location location;

		private PathType(final String patExpr_, 
				final OS os_, final Relative isRelative_, final Location location_) {
			patExpr = patExpr_;
			pat = Pattern.compile(patExpr);
			os = os_;
			isRelative = isRelative_;
			location = location_;
		}

		public static PathType getType(String path) throws IllegalArgumentException {
			for (PathType t : PathType.values()) {
				if(t.pat.matcher(path).find()) {
					return t;
				}
			}
			String msg = String.format("\"%s\" not recognized as PathType.", path);
			throw new IllegalArgumentException(msg);
		}

		/**
		 * Convert {@code origPath} to a {@code file://}-{@link URI}.  Both native 
		 * and non-native paths are supported. 
		 * 
		 * @param origPath	{@link OS#WINDOWS Windows} or {@link OS#UNIX Unix} origPath
		 * 		expression.  {@link OS#isNative() Foreign} paths must be
		 * 		{@link Relative#ABSOLUTE absolute} paths.  Native paths may be
		 * 		{@link Relative#RELATIVE relative} or absolute.  When
		 * 		the origPath is relative, the current working directory is prepended
		 * 		to it (see {@link File#File(java.lang.String)}).
		 * 
		 * @return	a {@code file://}-{@link URI} pointing to the file specified
		 * 		by {@code origPath}.
		 * 
		 * @throws URISyntaxException <ul><li>when {@code origPath} is {@link 
		 * 			OS#isNative() non-native} and {@link Relative#RELATIVE 
		 * 			relative}; or</li>
		 * 		<li>{@link URI#URI(java.lang.String)} cannot parse the converted
		 * 			origPath</li></ul>
		 */
		private URI toUriImpl(final String path) throws URISyntaxException {
			if(os.isNative() && Location.LOCAL.equals(location)) {
				// Let Java convert the String to an URI
				return new File(path).toURI();
			} else if(Relative.ABSOLUTE.equals(isRelative)) {
				// Java does not support Files of non-native operating systems,
				// therefore convert the origPath manually.
				StringBuilder builder = new StringBuilder();
				builder.append(location.uriPrefix);
				builder.append(os.replaceSeparators(path));

				return new URI(builder.toString());
			} else {
				String msg = String.format(
						"Unable to compose URI for non-native relative file path: %s",
						path);
				throw new URISyntaxException(path, msg);
			}
		} 

		/**
		 * Convert {@code origPath} to a {@code file://}-{@link URI}.  Both native 
		 * and non-native paths are supported. 
		 * 
		 * @param origPath	{@link OS#WINDOWS Windows} or {@link OS#UNIX Unix} origPath
		 * 		expression.  {@link OS#isNative() Foreign} paths must be
		 * 		{@link Relative#ABSOLUTE absolute} paths.  Native paths may be
		 * 		{@link Relative#RELATIVE relative} or absolute.  When
		 * 		the origPath is relative, the current working directory is prepended
		 * 		to it (see {@link File#File(java.lang.String)}).
		 * 
		 * @return	a {@code file://}-{@link URI} pointing to the file specified
		 * 		by {@code origPath}.
		 * 
		 * @throws URISyntaxException <ul><li>when {@code origPath} is {@link 
		 * 			OS#isNative() non-native} and {@link Relative#RELATIVE 
		 * 			relative}; or</li>
		 * 		<li>{@link URI#URI(java.lang.String)} cannot parse the converted
		 * 			origPath</li></ul>
		 */
		public static URI toURI(final String path) 
				throws URISyntaxException {
			try {
				PathType instance = getType(path);
				return instance.toUriImpl(path);
			} catch(IllegalArgumentException ex) {
				String msg = String.format("No PathType matches \"%s\"", path);
				throw (URISyntaxException)(new URISyntaxException(path, msg).initCause(ex));
			}
		}

		/**
		 * Convert a Zylab metadata reference to a {@code file://}-{@link URI}.
		 * 
		 * @see #toURI(java.lang.String) 
		 * 
		 * @param ref	a origPath–name-pair as found in Zylab's XmlFields metadata.
		 * @return	a {@code file://}-{@link URI} pointing to the file specified
		 * 		by {@code ref}.
		 * @throws URISyntaxException 	when {@link #toURI(java.lang.String) }
		 * 		throws it.
		 */
		public static URI toURI(final ZylabMetadataXml.FileRef ref)
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
			return toURI(buildPath.toString());
		}
	}
	
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
			URI original = PathType.toURI(reference);
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
		return ((prefix.getScheme().compareTo(target.getScheme()) == 0)
				&& (prefix.getHost().compareTo(target.getHost()) == 0)
				&& (target.getPath().startsWith(prefix.getPath())));
	}
	
}
