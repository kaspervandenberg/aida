/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author kasper
 */
class StatementTranslator implements Translator<Statement> {
	private final Translator<Resource> subjTrans;
	private final Translator<URI> predTrans;
	private final Translator<Value> objtrans;
	private final String subjGroupName = "subj";
	private final String predGroupName = "pred";
	private final String objGroupName = "obj";
	private final Pattern pattern = Pattern.compile("(?<" + subjGroupName + ">" + N3SyntaxPatterns.SUBJECT.patternExpr() + ")" + " (?<" + predGroupName + ">" + N3SyntaxPatterns.RESOURCE.patternExpr() + ")" + " (?<" + objGroupName + ">" + N3SyntaxPatterns.OBJECT.patternExpr() + ").");

	public StatementTranslator(final Translator<Resource> subjTrans_, final Translator<URI> predTrans_, final Translator<Value> objtrans_) {
		this.subjTrans = subjTrans_;
		this.predTrans = predTrans_;
		this.objtrans = objtrans_;
	}

	@Override
	public boolean isWellFormed(final String id) {
		return pattern.matcher(id).matches();
	}

	@Override
	public String getId(Statement statement) {
		Resource subject = statement.getSubject();
		URI predicate = statement.getPredicate();
		Value object = statement.getObject();
		String subjectId = subjTrans.getId(subject);
		String predicateId = predTrans.getId(predicate);
		String objectId = objtrans.getId(object);
		String result = String.format("%s %s %s.", subjectId, predicateId, objectId);
		return result;
	}

	@Override
	public boolean matches(Statement statement, String targetId) {
		Matcher matcher = pattern.matcher(targetId);
		if (matcher.matches()) {
			String subjPart = matcher.group(subjGroupName);
			String predPart = matcher.group(predGroupName);
			String objPart = matcher.group(objGroupName);
			boolean subjectMatches = subjTrans.matches(statement.getSubject(), subjPart);
			boolean predicateMatches = predTrans.matches(statement.getPredicate(), predPart);
			boolean objectMatches = objtrans.matches(statement.getObject(), objPart);
			return subjectMatches && predicateMatches && objectMatches;
		} else {
			return false;
		}
	}
	
}
