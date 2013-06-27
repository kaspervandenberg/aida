// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.classification;

import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface Rule {
	@SuppressWarnings("serial")
	public class Inapplicable extends IllegalStateException {

		public Inapplicable() {
		}

		public Inapplicable(String s) {
			super(s);
		}
 }
	
	/**
	 * Check whether this {@code Rule} can be applied to {@code searchResult}.
	 * 
	 * @param searchResult	the {@link SearchResult} to which to apply this 
	 * 		{@code Rule}.
	 * @return 	<ul><li>{@code true}: the rule's preconditions are met, it can
	 * 				be applied; or</li>
	 * 		<li>{@code false}: the rule's preconditions are not met, applying it
	 * 				will fail.</li></ul>
	 */
	public boolean isApplicable(final SearchResult searchResult);
	
	/**
	 * Apply this {@code Rule} to {@code searchResult}.
	 * 
	 * @param searchResult	the {@link SearchResult} to which to apply this 
	 * 		{@code Rule}; {@code searchResult} is not modified; a fresh
	 * 		instance is returned.
	 * @return	a fresh {@link SearchResult} based on the consequent of this 
	 * 		{@code Rule} and {@code searchResult}.
	 * 
	 * @throws nl.maastro.eureca.aida.search.zylabpatisclient.classification.Rule.Inapplicable 
	 * 		when the {@code Rule}'s conditions are not met (i.e.
	 * 		{@link #isApplicable(nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult)}
	 * 		returns false).
	 */
	public SearchResult apply(final SearchResult searchResult)
			throws Inapplicable;
}
