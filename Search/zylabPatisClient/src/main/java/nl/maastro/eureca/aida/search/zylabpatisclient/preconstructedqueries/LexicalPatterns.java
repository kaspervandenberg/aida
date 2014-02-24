// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import checkers.nullness.quals.NonNull;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LexicalPattern;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LexicalPatternBuilder;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.spans.SpanQuery;

/**
 * Preconstructed patterns used by the {@link nl.maastro.eureca.aida.search.zylabpatisclient.Searcher} to search 
 * for (modified) concepts.
 * 
 * Extend the list of preconstructed lexical by adding new enum values.C  onstruct the value using one of the
 * following constructors:
 * <ul><li>{@link #LexicalPatterns(String)};</li>
 * 		<li>{@link #LexicalPatterns(int, LexicalPatterns[])}; or</li>
 * 		<li>{@link #LexicalPatterns(Class, LexicalPatterns[])}.</li></ul>
 * 
 * Examples of using the constructors:
 * <ul><li>{@code MY_CONCEPT(“concept”)}, a pattern that searches for “concept”.</li>
 * 		<li>{@code MY_CONCEPT(“con*”)}, a wildcard pattern that searches for all words starting with “con”.</li>
 * 		<li>{@code MY_CONCEPT(“concept~”)}, a fuzy concept that searches for any work similar to “concept”,
 * 			words such as “koncept”, “concapt” match the pattern.</li>
 * 		<li>{@code MY_CONCEPT(3, METASTASIS_NL, CHEMO)}, a ‘near’ pattern that matches documents that match the
 * 			lexical patterns {@link #METASTASIS_NL} and {@link #CHEMO} within a distance of 3 words.  You can 
 * 			specify two or more lexical patterns.</li>
 * 		<li>{@code MY_CONCEPT(OrBuilder.class METASTASIS_NL, CHEMO)}, an ‘or’ pattern that matches any document
 * 			that matches either the lexical pattern {@link #METASTASIS_NL}, or {@link #CHEMO}, or both.</li></ul>
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
enum LexicalPatterns {
	METASTASIS_NL("*metastase*"),
	METASTASIS_SHORT("meta"),
	
	STAGE_NL("stadium"),
	FOUR_ROMAN("IV"),
	FOUR_DIGIT("4"),
	STAGE_IV_NL(2, STAGE_NL, FOUR_ROMAN),
	STAGE_4_NL(2, STAGE_NL, FOUR_DIGIT),
	ANY_STAGE4(OrBuilder.class, STAGE_IV_NL, STAGE_4_NL),
	
	UITZAAI_NL("uitzaai*"),
	UITGEZAAID_NL("uitgezaaid"),
	ANY_UITZAAI(OrBuilder.class, UITZAAI_NL, UITGEZAAID_NL),

	CHEMO("chemo*"),
	KUUR("kuur"),
	KUREN("kuren"),
	GEMCITABINE("gemcitabine"),
	CARBOPLATINE_EN("carboplatin"),
	CARBOPLATINE_NL("carboplatine"),
	CARBOPLATINE(OrBuilder.class, CARBOPLATINE_NL, CARBOPLATINE_EN),
	ANY_CHEMOKUUR(OrBuilder.class, CHEMO, KUUR, KUREN, GEMCITABINE),
	
	NOT_NL1("geen"),
	NOT_NL2("niet"),
        ZONDER("zonder"),
	ANY_NEGATION(OrBuilder.class, NOT_NL1, NOT_NL2, ZONDER),
	
	DIFFERENTIAL_DIAGNOSISIS_ABBREV("d.d"),
	DIFFERENTIAL_DIAGNOSISIS_SPELLING_ERROR("differenttaal*"),
	DIFFERENTIAL_DIAGNOSISIS_FULL("differentiaal*"),
	DIFFERENTIAL_DIAGNOSISIS(OrBuilder.class, 
			DIFFERENTIAL_DIAGNOSISIS_ABBREV,
			DIFFERENTIAL_DIAGNOSISIS_FULL,
			DIFFERENTIAL_DIAGNOSISIS_SPELLING_ERROR),
	SIGNS_NL1("aanwijzing*"),
	SIGNS_NL2("teken*"),
	MOGELIJK("mogelijk"),
	MISSCHIEN("misschien"),
	SUSPECT("suspect"),
	BEELD("beeld"),
	PAS("pas*"),
	BEVESTIG("bevestig*"),
	BEELD_PAST(3,BEELD, PAS),
	VERDACHT("verdacht"),
	VERDENKING("verdenking"),
	KAN("kan"),
	KUNNEN("kunnen"),
	KUNNEN_ALL(OrBuilder.class, KAN, KUNNEN),
	ZIJN("zijn"),
	KUNNEN_ZIJN(4, KUNNEN_ALL, ZIJN),
	ZOU("zou"),
	VRAAG("vraag"),
	INDERDAAD("inderdaad"),
	INDERDAAD_DE_VRAAG(7, VRAAG, INDERDAAD),
	ANY_SIGNS(OrBuilder.class, SIGNS_NL1, SIGNS_NL2, VERDACHT, VERDENKING, SUSPECT, 
			PAS, BEELD_PAST, BEVESTIG, MOGELIJK, DIFFERENTIAL_DIAGNOSISIS,
			INDERDAAD_DE_VRAAG, KUNNEN_ZIJN, KUNNEN_ALL, ZOU, MISSCHIEN);

	private abstract class Builder {
		public abstract LexicalPattern build(final Config config);

		protected LexicalPatternBuilder getBuilder(final Config config) {
			return LexicalPatternBuilder.instance().
					setDefaultField(config.getDefaultField());
		}
	}

	private class AutoBuilder extends Builder {
		private final String termExpr;

		public AutoBuilder(final String termExpr_) {
			this.termExpr = termExpr_;
		}
		
		@Override
		public LexicalPattern build(final Config config) {
			return getBuilder(config).auto(getName(), termExpr);
		}
	}
	
	private class NearBuilder extends Builder {
		private final int distance;
		private final Iterable<LexicalPatterns> patterns;

		public NearBuilder(final int distance_,
				final Iterable<LexicalPatterns> patterns_) {
			this.distance = distance_;
			this.patterns = patterns_;
		}

		@Override
		public LexicalPattern build(Config config) {
			return getBuilder(config).near(
					getName(),
					distance,
					toPatternIterable(config, patterns));
		}
	}

	private class OrBuilder extends Builder {
		private final Iterable<LexicalPatterns> patterns;

		public OrBuilder(final Iterable<LexicalPatterns> patterns_) {
			this.patterns = patterns_;
		}

		@Override
		public LexicalPattern build(Config config) {
			return getBuilder(config).or(
					getName(),
					toPatternIterable(config, patterns));
		}
	}

	private static final float EDIT_DISTANCE = 2.0f;
	private transient @MonotonicNonNull QName id = null;

	private final Builder builder;
	private transient final Map<Config, LexicalPattern> instances =
			new HashMap<>(1);

	static List<QueryNode> containedNodes(final Iterable<LexicalPattern> pats) {
		final List<QueryNode> nodes = new ArrayList<>();
		for (LexicalPattern p : pats) {
			nodes.add(p.getParsetree_representation());
		}
		return nodes;
	}

	static SpanQuery[] containedSpans(final Iterable<LexicalPattern> pats) {
		final ArrayList<SpanQuery> result = new ArrayList<>();
		Iterator<LexicalPattern> i = pats.iterator();
		while(i.hasNext()) {
			result.add(i.next().getLuceneObject_representation());
		}
		return result.toArray(new SpanQuery[result.size()]);
	}

	private LexicalPatterns(final String term) {
		this.builder = new AutoBuilder(term);
	}

	private LexicalPatterns(final int distance,
			final LexicalPatterns... pats) {
		this.builder = new NearBuilder(distance, Arrays.asList(pats));
	}

	private LexicalPatterns(final Class<OrBuilder> dummy, final LexicalPatterns... pats) {
		this.builder = new OrBuilder(Arrays.asList(pats));
	}

	static Iterable<LexicalPattern> toPatternIterable(
			final Config config, final Iterable<LexicalPatterns> items) {
		return new Iterable<LexicalPattern>() {

			@Override
			public Iterator<LexicalPattern> iterator() {
				final Iterator<LexicalPatterns> delegate = items.iterator();
				return new Iterator<LexicalPattern>() {

					@Override
					public boolean hasNext() { return delegate.hasNext(); }

					@Override
					public LexicalPattern next() { return delegate.next().getPattern(config); }

					@Override
					public void remove() { delegate.remove(); }
				};
			}
		};
	}

	public LexicalPattern getPattern(final Config config) {
		if(!instances.containsKey(config)) {
			LexicalPattern pat = builder.build(config);
			instances.put(config, pat);
		}
		@SuppressWarnings("nullness")
		@NonNull LexicalPattern result = instances.get(config);
		return result;
	}

	@EnsuresNonNull("id")
	private QName getName() {
		if (id == null) {
			try {
				id = QNameUtil.instance().createQName_inPreconstructedNamespace(
						LexicalPatterns.this.name().toLowerCase());
			} catch (URISyntaxException ex) {
				throw new Error(ex);
			}
		}
		return id;
	}
}
