// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.classification;

import java.util.EnumSet;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;

/**
 * The classification based one document overrides the classification based on 
 * other documents in the overall classification of the search result 
 * ({@link SearchResult#getClassification()}.  
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class InterDocOverride extends ClassificationCombiner {
	private final ConceptFoundStatus overriden;
	private final ConceptFoundStatus overrider;

	private final EnumSet<ConceptFoundStatus> bothClassifications;

	public InterDocOverride(ConceptFoundStatus overriden_,
			ConceptFoundStatus overrider_) {
		this.overriden = overriden_;
		this.overrider = overrider_;
		this.bothClassifications = EnumSet.of(overriden, overrider);
	}

	@Override
	protected Set<ConceptFoundStatus> getClassification(SearchResult base) {
		Set<ConceptFoundStatus> classifications = EnumSet.copyOf(
				base.getClassification());
		if(classifications.containsAll(bothClassifications)) {
			classifications.remove(overriden);
		}
		return classifications;
	}

	@Override
	public boolean isApplicable(SearchResult searchResult) {
		Set<ConceptFoundStatus> classifications = 
				searchResult.getClassification();
		if(classifications.containsAll(bothClassifications)) {
			return true;
		} else {
			return false;
		}
	}

}
