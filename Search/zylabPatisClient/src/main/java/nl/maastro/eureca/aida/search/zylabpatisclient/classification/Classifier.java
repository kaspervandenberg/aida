// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.classification;

import java.util.ArrayList;
import java.util.List;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;

/**
 * Apply a collection of resolution rules to {@link SearchResults} in order to
 * classify a Patient's eligibility.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Classifier {
	private static Classifier instance = null;
	
	private final List<Rule> rules;

	private Classifier() {
		rules = new ArrayList<>();
	}
	
	public static Classifier instance() {
		if(instance == null) {
			instance = new Classifier();
		}
		return instance;
	}

	public void appendRule(final Rule rule) {
		rules.add(rule);
	}

	public SearchResult resolve(final SearchResult searchResult) {
		SearchResult intermediate = searchResult;
		for (Rule rule : rules) {
			if(rule.isApplicable(intermediate)) {
				intermediate = rule.apply(intermediate);
			}
		}
		return intermediate;
	}
}
