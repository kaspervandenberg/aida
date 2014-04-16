// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import java.util.regex.Pattern;

/**
 * Regular expressions matchings part of the N3/Turle syntax.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
 enum N3SyntaxPatterns {
	NAME_START_CHAR(
			// From Turtle spec: http://www.w3.org/TeamSubmission/turtle/#nameStartChar
			"[A-Z]|_|[a-z]|"
			+ "[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|"
			+ "[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|"
			+ "[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]|[\\x{10000}-\\x{EFFFF}]"),

	NAME_CHAR(
			// From Turtle spec: http://www.w3.org/TeamSubmission/turtle/#nameChar
			NAME_START_CHAR.patternExpr()+ "|"
			+ "-|[0-9]|\u00B7|[\u0300-\u036f]|[\u203F-\u2040]"),
	
	NAME(
			// From Turtle spec: http://www.w3.org/TeamSubmission/turtle/#name
			NAME_START_CHAR.patternExpr() 
			+ NAME_CHAR.patternExpr()),
	
	PREFIX_NAME(
			// From Turtle spec: http://www.w3.org/TeamSubmission/turtle/#prefixName
			"[" + NAME_START_CHAR.patternExpr()+ "&&[^_]]"
			+ NAME_CHAR.patternExpr()),

	NODE_ID(
			// From Turtle spec: http://www.w3.org/TeamSubmission/turtle/#nodeID
			"_:"
			+ NAME.patternExpr()),

	HEX(
			// From Turtle spec: http://www.w3.org/TeamSubmission/turtle/#hex
			"[\u0030-\u0039]|[\u0041-\u0046]"),
	
	CHARACTER(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#character
			"(?:\\\\u" + HEX.patternExpr() + "{4}+)"
			+ "|(?:\\\\U" + HEX.patternExpr() + "{8}+)"
			+ "|(?:\\\\\\\\)"
			+ "[\u0020-\u005B]|[\u005D-\\x{10FFFF}]"),
	
	U_CHARACTER(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#ucharacter
			"[" + CHARACTER.patternExpr() + "&&[^\u003E]]"
			+ "|(?:\\\\>)"),

	E_CHARACTER(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#echaracter
			CHARACTER.patternExpr()
			+ "|(?:\\\\t)|(?:\\\\n)|(?:\\\\r)"),

	S_CHARACTER(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#scharacter
			"[" + E_CHARACTER.patternExpr() + "&&[^\"]]"
			+ "|\\\\\""),

	RELATIVE_URI(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#relativeURI
			U_CHARACTER.patternExpr() + "*"),

	URI_REF(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#uriref
			"<" + RELATIVE_URI.patternExpr() + ">"),

	QNAME(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#qname
			PREFIX_NAME.patternExpr() + "?:" 
			+ NAME.patternExpr()),

	STRING(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#string
			"\"" + S_CHARACTER.patternExpr() + "*\""),

	RESOURCE(
			// From Turle spec: http://www.w3.org/TeamSubmission/turtle/#resource
			URI_REF.patternExpr() + "|" + QNAME.patternExpr()),

	SUBJECT(
			// Based on Turle spec, restricted blank node:
			// * http://www.w3.org/TeamSubmission/turtle/#subject
			// * http://www.w3.org/TeamSubmission/turtle/#blank
			RESOURCE.patternExpr() + "|" + NODE_ID.patternExpr()),

	OBJECT(
			// Based on Turle spec, restricted blank node and literal:
			// * http://www.w3.org/TeamSubmission/turtle/#object
			// * http://www.w3.org/TeamSubmission/turtle/#blank
			// * http://www.w3.org/TeamSubmission/turtle/#literal
			RESOURCE.patternExpr() + "|" + NODE_ID.patternExpr() 
			+ "|" + STRING.patternExpr()),

	;
	private final Pattern pattern;

	private N3SyntaxPatterns(final String patternExpr)
	{
		this.pattern = Pattern.compile(patternExpr);
	}

	public String patternExpr()
	{
		return "(?:" + pattern.pattern()+ ")";
	}

	public boolean matches(final String instance)
	{
		return pattern.matcher(instance).matches();
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

