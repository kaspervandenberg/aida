/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.lucene;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.xml.HasXPath;
import org.junit.matchers.JUnitMatchers;
import org.w3c.dom.Node;


/**
 * Stores the indexed {@link Documents} — their {@link Fields}, and their
 * {@link FieldContents} — upon which {@link SearcherWS} is tested by firing
 * {@link Queries}.
 * 
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
public class IndexedDocuments implements SelfDescribing {
	/**
	 * @see Documents
	 * @see Queries
	 */
	public enum FieldContents {

		V1("one"), V2("two"), V3("Nederlandse tekst");
		public final String value;

		private FieldContents(String value_) {
			value = value_;
		}
	}

	/**
	 * @see Documents
	 */
	public enum Fields {

		F1, F2, F3
	}

	/**
	 * Documents to use in the test.  Each document has zero or more {@link Fields}
	 * with each having zero or more {@link FieldContents}.
	 */
	public enum Documents {

		D1, D2, D3;

		public <T> Matcher<T> containedIn(Class<T> itemType) {
			if (Node.class.isAssignableFrom(itemType)) {
				String xpath = String.format("%s[.=\"%s\"]", XpathExpr.DOC_ID_VALUE.expr, this.name());
				return (Matcher<T>) HasXPath.hasXPath(xpath);
			} else if (Iterable.class.isAssignableFrom(itemType)) {
				return (Matcher<T>) JUnitMatchers.hasItem(this);
			}
			throw new Error(new IllegalArgumentException(String.format("contained in not supported for %s", itemType.getName())));
		}

		public static Set<FieldContents> combinedContents(Map<Fields, Set<FieldContents>> fields) {
			Set<FieldContents> result = EnumSet.noneOf(FieldContents.class);
			for (Set<FieldContents> fieldContents : fields.values()) {
				result.addAll(fieldContents);
			}
			return Collections.unmodifiableSet(result);
		}
	}

	/**
	 * Queries to test {@link SearcherWS} with, each query has text invoke 
	 * {@code SearcherWS} with and a set of {@link FieldContents} on which the 
	 * query is expected to match.
	 */
	public enum Queries {
		Q1(FieldContents.V1.value, Fields.F1, EnumSet.of(FieldContents.V1)),
		// Lucene searches on whole words not parts of words
//		Q2("o", Fields.F1, EnumSet.of(FieldContents.V1, FieldContents.V2)),
		Q3(FieldContents.V2.value, Fields.F1, EnumSet.of(FieldContents.V2)),
		Q4("unexisting", Fields.F1, EnumSet.noneOf(FieldContents.class)),
		// Query for a word part of a term
		Q5("nederlandse", Fields.F1, EnumSet.of(FieldContents.V3)),
		// Query for a term in a different field than the indexed documents
		Q6(FieldContents.V1.value, Fields.F2, EnumSet.of(FieldContents.V1));

		public enum MatchStrategy {
			ANY {
				@Override
				public boolean isHitting(Set<FieldContents> queryContents, Set<FieldContents> documentContents) {
					Set<FieldContents> intersection = EnumSet.noneOf(FieldContents.class);
					intersection.addAll(documentContents);
					intersection.retainAll(queryContents);
					return !intersection.isEmpty();
				}
			},
			ALL {
				@Override
				public boolean isHitting(Set<FieldContents> queryContents, Set<FieldContents> documentContents) {
					return documentContents.containsAll(queryContents);
				}
			};

			public abstract boolean isHitting(
					Set<FieldContents> queryContents, 
					Set<FieldContents> documentContents);
		
		}
		
		public final String queryText;
		public final Fields field;
		public final Set<FieldContents> foundIn;

		public static Matcher<Queries> matchFieldContents(
				final Set<FieldContents> contents, final MatchStrategy strategy) {
			return new BaseMatcher<Queries>() {	
				@Override
				public boolean matches(Object item) {
					if(item instanceof Queries) {
						Queries q =(Queries)item;
						return strategy.isHitting(q.foundIn, contents); 
					} else {
						return false;
					}
				}

				@Override
				public void describeTo(Description description) {
					description.appendText(strategy.name());
					description.appendText(" of foundIn present in ");
					description.appendValue(contents);
				}
			};
		}
				
		/**
		 * Create a Lucene {@link org.apache.lucene.search.Query} for documents with 
		 * {@code field} containing {@code query}'s {@link Queries#queryText}.
		 * 
		 * @param field	{@link Fields#name()} to use as {@code fld} in 
		 * 		{@link org.apache.lucene.index.Term#Term(java.lang.String, java.lang.String) }
		 * @param query {@code text} for 
		 * 		{@link org.apache.lucene.index.Term#Term(java.lang.String, java.lang.String) }
		 * 
		 * @return a Lucene {@link org.apache.lucene.search.TermQuery}
		 */
		public Query createTermQuery() {
			return new TermQuery(new Term(field.name(), queryText));
		}

		private Queries(String queryText_, Fields field_, Set<FieldContents> foundIn_) {
			queryText = queryText_;
			field = field_;
			foundIn = Collections.unmodifiableSet(foundIn_);
		}
	}
	

	private enum AuxFields {

		_ID {
			@Override
			public IndexableField genField(final Documents doc) {
				return new TextField(this.name(), doc.name(), Field.Store.YES);
			}
		};

		public static Iterable<IndexableField> prependAll(final Documents doc, final Iterable<IndexableField> chained_) {
			return new Iterable<IndexableField>() {
				@Override
				public Iterator<IndexableField> iterator() {
					return new Iterator<IndexableField>() {
						private final Iterator<AuxFields> iAux = Arrays.asList(AuxFields.values()).iterator();
						private final Iterator<IndexableField> iChained = chained_.iterator();
						private boolean iteratingChained = false;

						@Override
						public boolean hasNext() {
							return iAux.hasNext() || iChained.hasNext();
						}

						@Override
						public IndexableField next() {
							if (iAux.hasNext()) {
								return iAux.next().genField(doc);
							} else {
								iteratingChained = true;
								return iChained.next();
							}
						}

						@Override
						public void remove() {
							if (iteratingChained) {
								iChained.remove();
							} else {
								throw new UnsupportedOperationException("AuxFields is a fixed collection of fields");
							}
						}
					};
				}
			};
		}

		public abstract IndexableField genField(final Documents doc);
	}

	/**
	 * XPath expressions used to test XML output of {@link SearcherWS}.
	 *
	 * @see {@link HasXPath#hasXPath(java.lang.String, org.hamcrest.Matcher) }
	 */
	private enum XpathExpr {

		DOC_ID(String.format(".//doc/field[@name=\"%s\"]", IndexedDocuments.AuxFields._ID.name())), DOC_ID_VALUE(DOC_ID.expr + "/value");

		private XpathExpr(final String expr_) {
			expr = expr_;
		}
		public final String expr;
	}
	private static final Set<String> ID_FIELD = Collections.singleton(AuxFields._ID.name());
	private final String name;
	private final Map<Documents, Map<Fields, Set<FieldContents>>> toIndex;

	/**
	 * Use {@link #builder} to construct {@code IndexedDocuments}.
	 */
	private IndexedDocuments(final String name_, final Map<Documents, Map<Fields, Set<FieldContents>>> structure_) {
		name = name_;
		toIndex = structure_;
	}

	public static class IdBuilder {

		final String name;
		final Map<Documents, Map<Fields, Set<FieldContents>>> structure;

		IdBuilder(final String name_) {
			name = name_;
			structure = new EnumMap<>(Documents.class);
		}

		/**
		 * Build an {@link IndexedDocuments} object from this
		 * specification.
		 *
		 * @return a configured {@link IndexedDocuments}-object.
		 */
		public IndexedDocuments build() {
			return new IndexedDocuments(name, Collections.unmodifiableMap(new EnumMap<>(structure)));
		}

		/**
		 * Configure document with id {@code doc}.
		 *
		 * @param doc	{@link Documents} item to identify the document to
		 * configure with
		 */
		public DocBuilder of(Documents doc) {
			return new DocBuilder(this, doc);
		}
	}

	public static class DocBuilder {

		final IdBuilder container;
		final Documents doc;
		final Map<Fields, Set<FieldContents>> docStructure;

		DocBuilder(IdBuilder container_, Documents doc_) {
			container = container_;
			doc = doc_;
			docStructure = new EnumMap<>(Fields.class);
		}

		public IndexedDocuments build() {
			addThis();
			return container.build();
		}

		public DocBuilder of(Documents doc) {
			addThis();
			return container.of(doc);
		}

		public FieldBuilder with(Fields field) {
			return new FieldBuilder(this, field);
		}

		private void addThis() {
			container.structure.put(doc, Collections.unmodifiableMap(new EnumMap(docStructure)));
		}
	}

	public static class FieldBuilder {

		final DocBuilder container;
		final Fields field;
		final Set<FieldContents> contents;

		FieldBuilder(final DocBuilder container_, final Fields field_) {
			container = container_;
			field = field_;
			contents = new HashSet<>();
		}

		public IndexedDocuments build() {
			addThis();
			return container.build();
		}

		public DocBuilder of(Documents doc) {
			addThis();
			return container.of(doc);
		}

		public FieldBuilder with(Fields field) {
			addThis();
			return container.with(field);
		}

		public FieldBuilder value(final FieldContents v) {
			contents.add(v);
			return this;
		}

		public FieldBuilder values(final FieldContents... v) {
			contents.addAll(Arrays.asList(v));
			return this;
		}

		public FieldBuilder values(final Collection<FieldContents> v) {
			contents.addAll(v);
			return this;
		}

		private void addThis() {
			container.docStructure.put(field, Collections.unmodifiableSet(new HashSet(contents)));
		}
	}

	public static IdBuilder builder(final String name) {
		return new IdBuilder(name);
	}

	/**
	 * Setup an index in {@code indexDir} with contents.  The index will contain
	 * all and only all {@link Documents}–{@link Fields}–{@link FieldContents}-combinations
	 * specified of {@code this}.
	 *
	 * <p>If an exception is caught the test will {@link Assert#fail fail}.</p>
	 *
	 * @param indexDir	Lucene's {@link Directory} that will contain the index.
	 * 		{@code indexDir} is opened with {@link IndexWriterConfig.OpenMode.CREATE}:
	 * 		any previous contents is overwritten.
	 */
	public void setupIndex(Directory indexDir) {
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_41, new StandardAnalyzer(Version.LUCENE_41));
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		try (final IndexWriter writer = new IndexWriter(indexDir, conf)) {
			writer.addDocuments(this.asLuceneDocIter());
		} catch (IOException ex) {
			final String msg = "IOException when adding documents to index";
			//				Logger.getLogger(SearcherWSTest.class.getName()).log(Level.SEVERE, msg, ex);
			throw new RuntimeException(msg, ex);
		}
	}

	/**
	 * Convert the indexes from {@code searchResults} to {@link Documents}
	 * using {@code indexDir} as index.
	 *
	 * @param indexDir	the index that was searched to produce
	 * 		{@code searchResults}.
	 * @param searchResults	result of a {@link IndexSearcher#search(org.apache.lucene.search.Query, int) }
	 * 		call.
	 *
	 * @return all {@link Documents} in {@code searchResults}
	 */
	public static Set<Documents> toDocumentSet(final Directory indexDir, final TopDocs searchResults) {
		Set<Documents> result = EnumSet.noneOf(Documents.class);
		try (final DirectoryReader reader = DirectoryReader.open(indexDir)) {
			for (ScoreDoc scoreDoc : searchResults.scoreDocs) {
				try {
					Document luceneDoc = reader.document(scoreDoc.doc, ID_FIELD);
					String idValue = luceneDoc.getField(AuxFields._ID.name()).stringValue();
					result.add(Documents.valueOf(idValue));
				} catch (IOException | NullPointerException ex) {
					final String msg = String.format("Error retrieving field %s for document #%d", AuxFields._ID.name(), scoreDoc.doc);
					Logger.getLogger(SearcherWSTest.class.getName()).log(Level.SEVERE, msg, ex);
					// Continue reading other documents
				}
			}
			return result;
		} catch (IOException ex) {
			final String msg = "Error reading index: converting search results";
			throw new RuntimeException(msg, ex);
		}
	}

	/**
	 * Iterate over all {@link Documents} wrapping their fields as
	 * {@link IndexableField}, for each {@code Document}–{@code Field}-pair
	 * {@link #concat} the {@link FieldContents} into the
	 * {@code IndexableField}'s value.
	 *
	 * @return {@link Iterator} of {@link Iterables} that allows traversing
	 * 		the {@code Documents}–{@code Fields}–{@code FieldContents}-structure.
	 */
	public Iterable<Iterable<IndexableField>> asLuceneDocIter() {
		return new TransformingIterable<>(
				new Transformer<Documents, Iterable<IndexableField>>() {
					@Override public Iterable<IndexableField> transform(Documents input) { return asLuceneFieldIter(input); }
				},
				toIndex.keySet());
	}

	/**
	 * Wrap {@link #concat} called on each item of {@code contents} to an
	 * {@link Iterable} of {@link IndexableField}.
	 *
	 * @param contents	{@link Map} of {@link Fields} and their {@link FieldContents}
	 * @return	{@link Iterable} over all {@link #concat}-ed fields.
	 */
	public Iterable<IndexableField> asLuceneFieldIter(final Documents doc) {
		return AuxFields.prependAll(doc, new TransformingIterable<>(
				new Transformer<Fields, IndexableField>() {
					@Override public IndexableField transform(Fields input) { return createLuceneField(doc, input); }
				},
				toIndex.get(doc).keySet()));
	}

	/**
	 * Create a Lucene {@link IndexableField} from {@code doc}–{@code field}'s
	 * {@link FieldContents}.
	 *
	 * @param doc	{@link Documents} of the field.
	 * @param field	{@link Fields} of the field.
	 * @return 	{@
	 */
	public IndexableField createLuceneField(final Documents doc, final Fields field) {
		return concat(field, toIndex.get(doc).get(field));
	}

	/**
	 * Combine {@link FieldContents} into a {@link TextField}.
	 *
	 * @param field		id of field to use
	 * @param contents	{@link Iterable} of {@link FieldContents} of which the
	 * 			{@link FieldContents#value} to combine onto the {@code value}
	 * 			supplied to {@link TextField#TextField(String, String, Field.Store)}.
	 *
	 * @return a {@link TextField} named {@code field} containing {@link contents}
	 */
	private static IndexableField concat(Fields field, Iterable<FieldContents> contents) {
		StringBuilder valueBuilder = new StringBuilder();
		Iterator<FieldContents> i = contents.iterator();
		for (FieldContents fieldContents : contents) {
			valueBuilder.append(fieldContents.value);
			valueBuilder.append("\n");
		}
		IndexableField result = new TextField(field.name(), valueBuilder.toString(), Field.Store.YES);
		return result;
	}

	boolean contains(Documents doc) {
		return toIndex.containsKey(doc);
	}

	boolean contains(Documents doc, Fields field) {
		if(toIndex.containsKey(doc)) {
			return toIndex.get(doc).containsKey(field);
		}
		return false;
	}


	public Iterable<Fields> fieldsOf(final Documents doc) {
		return toIndex.get(doc).keySet();
	}

	public Iterable<Documents> allDocuments() {
		return toIndex.keySet();
	}

	public Iterable<FieldContents> contentsOf(final Documents doc, final Fields field) {
		return toIndex.get(doc).get(field);
	}

	public Iterable<FieldContents> contentsOf(final Documents doc) {
		return new NestedIterable<>(
				new Transformer<Fields, Iterable<FieldContents>>() {
					@Override public Iterable<FieldContents> transform(Fields input) { return contentsOf(doc, input); }
				},
				fieldsOf(doc));
	}

	public Iterable<FieldContents> allContents() {
		return new NestedIterable<>(
				new Transformer<Documents, Iterable<FieldContents>>() {
					@Override public Iterable<FieldContents> transform(Documents input) { return contentsOf(input); }
				},
				allDocuments());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(name);
	}
	
}
