/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author kasper2
 */
public class StringToURI {
	private static enum OS {
		WINDOWS("\\\\"),
		UNIX("/"),
		UNKNOWN("") {
			@Override
			public Iterable<String> getPathParts(String s) {
				throw new UnsupportedOperationException("Not splitting on unknown separators."); 
			} };

		private final String partSep;
		
		private static Set<OS> rootPathOS = null;

		private OS(final String partSep_) {
			this.partSep = partSep_;
		}
		
		public Iterable<String> getPathParts(final String s) {
			return Arrays.asList(s.split(partSep));
		};
		
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

	private static enum Relative {
		RELATIVE,
		ABSOLUTE,
		UNKNOWN
	}

	private static enum Location {
		LOCAL("file:///"),
		NETWORK("file://"),
		UNKNOWN("file:///");

		public final String uriPrefix;

		private Location(final String uriPrefix_) {
			uriPrefix = uriPrefix_;
		}
	}
		
	private static enum PathType {
		WIN_DRIVE_ABS("^([a-zA-Z]:)\\\\(.*)",			/* e.g. C:\Data\…, C:\… */
				OS.WINDOWS, Relative.ABSOLUTE, Location.LOCAL) {
			@Override
			protected Iterable<String> getEncodedParts(String s) {
				return getEncodedPartsOmmitingWinDrive(s);
			} },
		
		WIN_DRIVE_REL1("^([a-zA-Z]:)([^\\\\].*)",		/* e.g. C:files, C:..\test (without the leading backslash) */
				OS.WINDOWS, Relative.RELATIVE, Location.LOCAL) {
			@Override
			protected Iterable<String> getEncodedParts(String s) {
				return getEncodedPartsOmmitingWinDrive(s);
			} },
		
		WIN_DRIVE_REL2("^((\\w+)|(\\.{1,2}))\\\\.*",	/* e.g. ..\files, files\…, .\test (must include a blackslash to distinguis it from linux) */
				OS.WINDOWS, Relative.RELATIVE, Location.LOCAL),
		WIN_UNC("^\\\\\\\\",					/* e.g. \\server\share\folder */
				OS.WINDOWS, Relative.ABSOLUTE, Location.NETWORK),
		UNIX_ABS("^/",							/* e.g. /etc/hosts */
				OS.UNIX, Relative.ABSOLUTE, Location.LOCAL),
		UNIX_REL("^\\w+/",						/* e.g. home/user/config */
				OS.UNIX, Relative.RELATIVE, Location.LOCAL),

		URI("^\\w+://", OS.UNKNOWN, Relative.UNKNOWN, Location.UNKNOWN) {
			@Override
			protected URI toUriImpl(String path) throws URISyntaxException {
				return new URI(path);
			} },
		
		UNKNOWN("", OS.UNKNOWN, Relative.UNKNOWN, Location.UNKNOWN) {
			@Override
			protected URI toUriImpl(String path) throws URISyntaxException {
				/* let java handle this unknown type of path. */
				return toFileToURI(path);
			} };

		private final static String uriPartSeparator = "/";
		private final String patExpr;
		private final Pattern pat;
		
		private final OS os;
		private final Relative isRelative;
		private final Location location;

		/**
		 * 
		 * @param patExpr_	a regular expression that defines this 
		 * 		{@code PathType}.
		 * @param os_
		 * @param isRelative_
		 * @param location_ 
		 */
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
		protected URI toUriImpl(final String path) throws URISyntaxException {
			if(os.isNative() && Location.LOCAL.equals(location)) {
				// Let Java convert the String to an URI
				return toFileToURI(path);
			} else if(Relative.ABSOLUTE.equals(isRelative)) {
				// Java does not support Files of non-native operating systems,
				// therefore convert the origPath manually.
				StringBuilder builder = new StringBuilder();
				builder.append(location.uriPrefix);

				Iterator<String> iParts = getEncodedParts(path).iterator();
				while (iParts.hasNext()) {
					builder.append(iParts.next());
					if(iParts.hasNext()) {
						builder.append(uriPartSeparator);
					}
				}
				return new URI(builder.toString());
				
			} else {
				String msg = String.format(
						"Unable to compose URI for non-native relative file path: %s",
						path);
				throw new URISyntaxException(path, msg);
			}
		} 

		/**
		 * Template method to retrieve an {@link Iterable} of strings containing
		 * the parts of {@code s} encoded if the part must be encoded and not 
		 * encoded if the part must not be encoded.
		 * 
		 * @see #getEncodedPartsOmmitingWinDrive(java.lang.String) 
		 * 
		 * @param s	string containing a the path to convert to an URI, 
		 * 		{@code s}' {@code PathType} must be equal to {@code this}. 
		 * 
		 * @return an {@link Iterable} of the parts of {@code s} correctly 
		 * 		URLencoded.
		 */
		protected Iterable<String> getEncodedParts(final String s) {
			return urlEncodeParts(os.getPathParts(s));
		}

		/**
		 * Do not URLEncode {@link #getWinDrive(java.util.regex.MatchResult)} 
		 * and URLEncode the remaining
		 * {@link #getWinPath(java.util.regex.MatchResult)}.
		 * 
		 * <p>{@link #pat} must contain two groups:
		 * <ol><li>first group for {@link #getWinDrive(java.util.regex.MatchResult)},
		 * 		and</li>
		 * <li>second group for {@link #getWinPath(java.util.regex.MatchResult)}.
		 * </li></ol></p> 
		 * 
		 * @param s	string containing a the path to convert to an URI, 
		 * 		{@code s}' {@code PathType} must be equal to {@code this}. 
		 * 
		 * @return an {@link Iterable} of the parts of {@code s} correctly 
		 * 		URLencoded.
		 */
		protected Iterable<String> getEncodedPartsOmmitingWinDrive(final String s) {
			return new Iterable<String>() {
				@Override
				public Iterator<String> iterator() {
					return new Iterator<String>() {
						private MatchResult match = match(s);
						private boolean afterDrivePart = false;
						private Iterator<String> delegate = 
								urlEncodeParts(os.getPathParts(getWinPath(match))).iterator();

						@Override
						public boolean hasNext() {
							if(afterDrivePart) {
								return delegate.hasNext();
							} else {
								return true;
							}
						}

						@Override
						public String next() {
							if(afterDrivePart) {
								return delegate.next();
							} else {
								afterDrivePart = true;
								return getWinDrive(match);
							}
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException("Not supported yet.");
						}
					};
				}
			};
		}

		/**
		 * Match {@code s} to {@code this.}{@link #pat}.
		 * 
		 * @param s	string containing a the path to convert to an URI, 
		 * 		{@code s}' {@code PathType} must be equal to {@code this}. 
		 * 
		 * @return the {@link MatchResult}s
		 * 
		 * @throws	IllegalArgumentException when {@code s} does not match,
		 * 		{@code this.}{@link #pat}.
		 */
		private MatchResult match(final String s) 
				throws IllegalArgumentException {
			Matcher m = pat.matcher(s);
			if(!m.find()) {
				String msg = String.format(
						"pattern \"%s\" does not match string \"%s\".",
						this.patExpr,
						s);
				throw new IllegalArgumentException(msg);
			}
			return m.toMatchResult();
		}

		/**
		 * Retrieve the drive part of {@code match}.
		 * 
		 * <p>{@link #pat} must contain at least one group; the first group is
		 * the drive part.</p>
		 * 
		 * @param match	{@link MatchResult} of a string matched to 
		 * {@code this.}{@link #pat}.
		 * 
		 * @return the drive part of the matched string.
		 * 
		 * @throws	IllegalStateException when {@link #pat} does not contain a
		 * 		capturing group
		 */
		private String getWinDrive(MatchResult match) 
				throws IllegalStateException {
			try {
				return match.group(1);
			} catch (IndexOutOfBoundsException ex) {
				String msg = String.format(
						"pattern \"%s\" has no drive part.",
						this.patExpr);
				throw new IllegalStateException(msg, ex);
			}
		}

		/**
		 * Retrieve the part after the drive part of {@code match}.
		 * 
		 * <p>{@link #pat} must contain at least two groups; the first group is
		 * the drive part, the second is the path part.</p>
		 * 
		 * @param match	{@link MatchResult} of a string matched to 
		 * {@code this.}{@link #pat}.
		 * 
		 * @return the path part of the matched string.
		 * 
		 * @throws	IllegalStateException when {@link #pat} does not contain two
		 * 		capturing groups
		 */
		private String getWinPath(MatchResult match) {
			try {
				return match.group(2);
			} catch (IndexOutOfBoundsException ex) {
				String msg = String.format(
						"pattern \"%s\" has no group after drive part.",
						this.patExpr);
				throw new IllegalStateException(msg, ex);
			}
		}
	}

	private static StringToURI instance = null;

	/**
	 * Singleton use {@link #getInstance()}
	 */
	private StringToURI() {
		// Intentionally empty
	}

	public static StringToURI getInstance() {
		if(instance == null) {
			instance = new StringToURI();
		}
		return instance;
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
	public URI toURI(final String path) 
			throws URISyntaxException {
		try {
			PathType pathType = PathType.getType(path);
			return pathType.toUriImpl(path);
		} catch(IllegalArgumentException ex) {
			String msg = String.format("No PathType matches \"%s\"", path);
			throw (URISyntaxException)(new URISyntaxException(path, msg).initCause(ex));
		}
	}

	/**
	 * Encode each URI-part in {@code parts} with {@link 
	 * URLEncoder#encode(java.lang.String, java.lang.String)}.
	 * 
	 * @param parts	{@link Iterable} of URI-parts to encode.
	 * @return	{@link Iterable} containing an encoded version of each part of 
	 * 		{@code parts}.  NOTE: results are not stored.
	 */
	private static Iterable<String> urlEncodeParts(final Iterable<String> parts) {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					Iterator<String> delegate = parts.iterator();

					@Override
					public String next() {
						try {
							String encoded = URLEncoder.encode(delegate.next(), StandardCharsets.UTF_8.name());
							return encoded.replace("+", "%20");
						} catch (UnsupportedEncodingException ex) {
							throw new Error ("Standard character set UTF not supported.", ex);
						}
					}

					@Override public boolean hasNext() { return delegate.hasNext(); }
					@Override public void remove() { delegate.remove(); }
				};
			}
		};
	}
		
	/**
	 * Use Java's {@link File#toURI()} to produce an {@link URI}.
	 * 
	 * <p>Replace "{@code file:/}" with the standard compliant "{@code file:///}"</p>
	 * 
	 * @param path	path as supported by {@link File#File(java.lang.String)}
	 * 
	 * @return {@link URI}
	 */
	private static URI toFileToURI(String path) {
		String tmp = new File(path).toURI().toString();
		try {
			return new URI(tmp.replaceAll("file:/(\\w)", "file:///$1"));
		} catch (URISyntaxException ex) {
			throw new Error(ex);
		}
	}
}
