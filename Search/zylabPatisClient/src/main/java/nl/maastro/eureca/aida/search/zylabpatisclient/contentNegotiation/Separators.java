// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.contentNegotiation;

/**
 * Separators used to parse strings into {@link ContentTypeNegotiator.MediaType}s.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
 enum Separators {
	LIST_ELEMENT(",", true),
	PARAMETER(";", true),
	TYPE_SUBTYPE("/", false, 2,
			"Expression \"%s\" is not a type–subtype-pair."),
	PARAM_VALUE("=", true, 2,
			"Parameter expression \"%s\" is no key–value-pair.");
	
	public static final String WHITESPACE = "[\\p{Space}]*";
	private static final int EXPECT_ANY = -1;

	private final String separator;
	private final String regExp;
	private final int nExpected;
	private final String msg;
	
	/**
	 * Construct a separator that {@link java.lang.String#split(java.lang.String) splits}
	 * an input String on {@code sep_}.
	 *
	 * @param sep_	the pattern to split the input strings on
	 * @param ignoreWhiteSpace	<ul><li>{@code true}, whitespace before
	 * 			and after this separator is ignored; or</li>
	 * 		<li>{@code false}, whitespace is included in the returned parts.</li>
	 * 		</ul>
	 */
	private Separators(final String sep_, final boolean ignoreWhiteSpace) {
		separator = sep_;
		if (ignoreWhiteSpace) {
			regExp = WHITESPACE + sep_ + WHITESPACE;
		} else {
			regExp = sep_;
		}
		nExpected = EXPECT_ANY;
		msg = "";
	}

	/**
	 * Construct a separator that {@link java.lang.String#split(java.lang.String) splits}
	 * an input String on {@code sep_}.
	 *
	 * @param sep_	the pattern to split the input strings on 
	 * @param ignoreWhiteSpace	<ul><li>{@code true}, whitespace before
	 * 			and after this separator is ignored; or</li>
	 * 		<li>{@code false}, whitespace is included in the returned parts.</li>
	 * 		</ul>
	 * @param nExpected_	the number of parts that splitting inputs should
	 * 		result in.
	 * @param msg_		the message pattern to use in thrown
	 * 		{@link IllegalArgumentException}s, {@code msg_} may contain one
	 * 		"%s" expression which is replaced by
	 * 		{@link #split(java.lang.String)}'s {@code input}.
	 */
	private Separators(final String sep_,
			final boolean ignoreWhiteSpace, final int nExpected_, final String msg_) {
		separator = sep_;
		if (ignoreWhiteSpace) {
			regExp = WHITESPACE + sep_ + WHITESPACE;
		} else {
			regExp = sep_;
		}
		nExpected = nExpected_;
		msg = msg_;
	}

	/**
	 * @return the regExp
	 */
	public String getRegExp() {
		return regExp;
	}

	/**
	 * @return the nExpected
	 */
	public int getnExpected() {
		return nExpected;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * {@link String#split(java.lang.String) Split} {@code input} in
	 * parts using {@code this} a separator.
	 *
	 * @param input	the String to split
	 * @return	an array containing the parts
	 * @throws IllegalArgumentException	when the number of parts differs
	 * 		from what is {@link #nExpected expected}.
	 */
	public String[] split(final String input) throws IllegalArgumentException {
		String[] result = input.split(getRegExp());
		if (getnExpected() != EXPECT_ANY && result.length != getnExpected()) {
			throw new IllegalArgumentException(String.format(getMsg(), input));
		}
		return result;
	}

	public String getSeparator() {
		return this.separator;
	}
}
