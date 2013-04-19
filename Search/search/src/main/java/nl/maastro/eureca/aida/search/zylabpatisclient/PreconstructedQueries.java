// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

/**
 * Contains preconstructed queries to search for oncological concepts
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PreconstructedQueries {
	/**
	 * Contant parts of URIs used to identify queries in {@link #storedPredicates}.
	 * Use {@link UriParts#createURI(java.lang.String)} to create {@link URI}s
	 * composed of these standard parts and a specific fragment.
	 */
	private enum UriParts {
		SCHEME("http"),
		HOST("vocab.maastro.nl"),
		PATH("/concepts");

		private final String value;

		private UriParts(final String value_) {
			value = value_;
		}

	}

	/**
	 * URI fragments that prefixed with the {@link UriParts} from the 
	 * {@link URI}s that idenentify the {@link Query}s in 
	 * {@link #storedPredicates}.
	 */
	public enum UriFragments {
		METASTASIS_IV("metastasisStage_IV");

		private final String value;
		private URI uri = null;

		private UriFragments(final String value_) {
			value = value_;
		}

		/**
		 * Return the URI composed from this {@code UriFragment} and 
		 * {@link UriParts}.
		 * 
		 * @see #createURI(java.lang.String) 
		 * 
		 * @return	the {@link URI}
		 */
		public URI getURI() {
			if(uri == null) {
				try {
					uri = createURI(value);
				} catch (URISyntaxException ex) {
					throw new Error("URISyntaxException in hardcoded URI", ex);
				}
			}
			return uri;
		}
			
		/**
		 * Build an {@link URI} from the parts defined in {@link UriParts} and
		 * this fragment.  {@code createURI()} uses 
		 * {@link URI#URI(java.lang.String, java.lang.String, java.lang.String, java.lang.String) }
		 * to build the requested {@code URI}.
		 * 
		 * @param fragment	the fragment part of the URI to create
		 * 
		 * @return	the constructed {@link URI}
		 * 
		 * @throws URISyntaxException when the constructed URI has an syntax 
		 * 		error. 
		 */
		private static URI createURI(String fragment) 
				throws URISyntaxException {
			return new URI(
					UriParts.SCHEME.value,
					UriParts.HOST.value,
					UriParts.PATH.value,
					fragment);
		}
	}

	/**
	 * Lucene DEFAULT_FIELD to use for the {@link SearchTerms}.
	 */
	private static String DEFAULT_FIELD = "content";

	/**
	 * {@link Term#text()text-part} of the {@link Term}s used in preconstructed 
	 * queries.
	 * 
	 * 
	 */
	private enum SearchTerms {
		METASTASIS_EN("metastasis"),
		METASTASIS_NL("metastase"),
		STAGE_EN("stage"),
		STAGE_NL("stadium"),
		FOUR_ROMAN("IV"),
		FOUR_DIGIT("4"),
		EXTENSIVE("extensive"),
		DISEASE("disease"),
		UITZAAIING("uitzaaiing")
		;

		private final Term value;
		
		/**
		 * Create a SearchTerm for {2code value_} in the {@link #DEFAULT_FIELD}.
		 * 
		 * @param value_	the text part of the {@link Term} to search for.
		 */
		private SearchTerms(final String value_) {
			value = new Term(DEFAULT_FIELD, value_);
		}

		/**
		 * @return the {@link Term} to search for
		 */
		public Term getTerm() {
			return value;
		}

		/**
		 * Create a {@link FuzzyQuery} from this {@code SearchTerm}; that is a 
		 * {@link Query} that allows minor variations of this search term (for 
		 * example those caused by OCR).
		 * 
		 * @return the {@link FuzzyQuery#FuzzyQuery(org.apache.lucene.index.Term) constructed}
		 * 		{@link FuzzyQuery}
		 */
		public FuzzyQuery getFuzzyQuery() {
			return new FuzzyQuery(getTerm());
		}

		/**
		 * Create a 'fuzzy span' for this {@code SearchTerm}.  Fuzzy means that 
		 * minor variations of this {@code SearchTerm} match. {@link SpanQuery 
		 * Spans} allow construction of queries where parts are near other parts.
		 * 
		 * @return	this {@code SearchTerm} as a {@link #getFuzzyQuery()} 
		 * {@link SpanMultiTermQueryWrapper#SpanMultiTermQueryWrapper(org.apache.lucene.search.MultiTermQuery) wrapped}
		 * into a {@link SpanQuery}
		 */
		public SpanMultiTermQueryWrapper<FuzzyQuery> getFuzzySpan() {
			return new SpanMultiTermQueryWrapper<>(getFuzzyQuery());
		}

		/**
		 * Create a span that exactly matches this {@code SearchTerm}.  The 
		 * {@link SpanQuery} is usable a part of a span, which allows matching
		 * when parts are found near each other.
		 * 
		 * @return	a {@link SpanTermQuery} of this {@code SearchTerm} 
		 */
		public SpanTermQuery getExactSpan() {
			return new SpanTermQuery(getTerm());
		}
	}
	
	/**
	 * Queries to search for patients that match certain predicates.
	 * 
	 */
	// TODO Move to SearcherWS and provide interface to access stored queries
	private static final Map<URI, Query> storedPredicates;
	static {
		Map<URI,Query> tmp = new HashMap<>();
		// add URIs to query mappings here
		
		// Stage IV metastasis
		tmp.put(UriFragments.METASTASIS_IV.getURI(), buildStageIVmetastasis());
			
		
		storedPredicates = Collections.unmodifiableMap(tmp);
	}

	/**
	 * Singleton instance, use {@link #instance()} to access
	 */
	private static PreconstructedQueries instance;

	/**
	 * Singleton, use {@link #instance()} to retrieve the sole instance.
	 */
	private PreconstructedQueries() {
		// Intentionally empty
	}

	/**
	 * Access this singleton
	 * 
	 * @return the sole instance of {@code PreconstructedQueries}
	 */
	public static PreconstructedQueries instance() {
		if(instance == null) {
			instance = new PreconstructedQueries();
		}
		return instance;
	}

	/**
	 * Retrieve the {@link Query} id
	 * @param key
	 * @return 
	 */
	public Query getQuery(final URI key) {
		return storedPredicates.get(key);
	}

	public Query getQuery(final UriFragments keyFragment) {
		return storedPredicates.get(keyFragment.getURI());
	}
	
	private static Query buildStageIVmetastasis() {
		BooleanQuery result = new BooleanQuery();
		
		SpanOrQuery metastasis = new SpanOrQuery(
			SearchTerms.METASTASIS_EN.getFuzzySpan(),
			SearchTerms.METASTASIS_NL.getFuzzySpan());

		SpanQuery stage_enRoman = new SpanNearQuery(new SpanQuery[]{
			SearchTerms.STAGE_EN.getFuzzySpan(),
			SearchTerms.FOUR_ROMAN.getExactSpan()}, 2, false);
		SpanQuery stage_enDigit = new SpanNearQuery(new SpanQuery[]{
			SearchTerms.STAGE_EN.getFuzzySpan(),
			SearchTerms.FOUR_DIGIT.getExactSpan()}, 2, false);
		SpanQuery stage_nlRoman = new SpanNearQuery(new SpanQuery[]{
			SearchTerms.STAGE_NL.getFuzzySpan(),
			SearchTerms.FOUR_ROMAN.getExactSpan()}, 2, false);
		SpanQuery stage_nlDigit = new SpanNearQuery(new SpanQuery[]{
			SearchTerms.STAGE_NL.getFuzzySpan(),
			SearchTerms.FOUR_DIGIT.getExactSpan()}, 2, false);
		SpanOrQuery stage = new SpanOrQuery(
				stage_enRoman, stage_enDigit, stage_nlRoman, stage_nlDigit);

		SpanQuery metastasisStage = new SpanNearQuery(new SpanQuery[]{
			metastasis, 
			stage}, 5, false);
		
		SpanQuery extensiveDisease = new SpanNearQuery(new SpanQuery[]{
			SearchTerms.EXTENSIVE.getFuzzySpan(),
			SearchTerms.DISEASE.getFuzzySpan()}, 5, false);
		
//		result.add(metastasis, BooleanClause.Occur.SHOULD);
//		result.add(stage, BooleanClause.Occur.SHOULD);
		result.add(metastasisStage, BooleanClause.Occur.SHOULD);
		result.add(extensiveDisease, BooleanClause.Occur.SHOULD);
		result.add(SearchTerms.UITZAAIING.getFuzzyQuery(), BooleanClause.Occur.SHOULD);

		return result;
	}
}
