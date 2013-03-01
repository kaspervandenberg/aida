/* © Maastro, 2012
 */
package org.vle.aid.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static org.vle.aid.lucene.SearcherWSTest.IndexedDocuments.builder;

/**
 * Test whether {@link SearcherWS} returns the expected results.  See also
 * {@link SearcherWSTest_Basic} which contains legacy tests.
 * 
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> 
 */
@RunWith(Theories.class)
public class SearcherWSTest {
	/**
	 * @see Documents
	 * @see Queries
	 */
	public enum FieldContents {
		V1("one"),
		V2("two"),
		V3("Nederlandse tekst");

		public final String value;
		
		private FieldContents(String value_) {
			value = value_;
		}
	}

	/**
	 * @see Documents
	 */
	public enum Fields {
		F1,
		F2,
		F3;
	}
	
	/**
	 * Documents to use in the test.  Each document has zero or more {@link Fields}
	 * with each having zero or more {@link FieldContents}.
	 */
	public enum Documents {
		D1,
		D2,
		D3;

		public static Set<FieldContents> combinedContents(Map<Fields, Set<FieldContents>> fields) {
			Set<FieldContents> result = EnumSet.noneOf(FieldContents.class);
			for(Set<FieldContents> fieldContents : fields.values()) {
				result.addAll(fieldContents);
			}
			return Collections.unmodifiableSet(result);
		}
	}

	
	/**
	 * Stores the indexed {@link Documents} — their {@link Fields}, and their 
	 * {@link FieldContents} — upon which {@link SearcherWS} is tested by firing
	 * {@link Queries}.
	 */
	public static class IndexedDocuments implements SelfDescribing {
		private final String name;
		private final Map<Documents, Map<Fields, Set<FieldContents>>> toIndex;
		
		/**
		 * Use {@link #builder} to construct {@code IndexedDocuments}.
		 */
		private IndexedDocuments(
				final String name_,
				final Map<Documents, Map<Fields, Set<FieldContents>>> structure_) {
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
				return new IndexedDocuments(
						name,
						Collections.unmodifiableMap(new EnumMap<>(structure)));
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
				container.structure.put(doc,
						Collections.unmodifiableMap(new EnumMap(docStructure)));
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
				container.docStructure.put(field,
						Collections.unmodifiableSet(new HashSet(contents)));
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
			IndexWriterConfig conf = new IndexWriterConfig(
					Version.LUCENE_41,
					new StandardAnalyzer(Version.LUCENE_41));
			conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			try (IndexWriter writer = new IndexWriter(indexDir, conf)) {
				writer.addDocuments(this.asLuceneDocIter());
			} catch (IOException ex) {
				final String msg = "IOException when adding documents to index";
				Logger.getLogger(SearcherWSTest.class.getName()).log(Level.SEVERE, msg, ex);
				fail(msg);
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
			return new Iterable<Iterable<IndexableField>>() {
				@Override
				public Iterator<Iterable<IndexableField>> iterator() {
					return new Iterator<Iterable<IndexableField>>() {
						private Iterator<Documents> iDoc = toIndex.keySet().iterator();
						
						@Override public boolean hasNext() { return iDoc.hasNext(); }

						@Override
						public Iterable<IndexableField> next() {
							return asLuceneFieldIter(iDoc.next());
						}

						@Override public void remove() { iDoc.remove(); }
					};
				}
			};
		}
		
		/**
		 * Wrap {@link #concat} called on each item of {@code contents} to an 
		 * {@link Iterable} of {@link IndexableField}.
		 * 
		 * @param contents	{@link Map} of {@link Fields} and their {@link FieldContents}
		 * @return	{@link Iterable} over all {@link #concat}-ed fields. 
		 */
		public Iterable<IndexableField> asLuceneFieldIter(final Documents doc) {
			return new Iterable<IndexableField>() {
				@Override
				public Iterator<IndexableField> iterator() {
					return new Iterator<IndexableField>() {
						private Iterator<Fields> iField = 
								toIndex.get(doc).keySet().iterator();

						@Override public boolean hasNext() { return iField.hasNext(); }

						@Override
						public IndexableField next() {
							return createLuceneField(doc, iField.next());
						}

						@Override public void remove() { iField.remove(); }
					};
				}
			};
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

		public Iterable<Fields> fieldsOf(final Documents doc) {
			return new Iterable<Fields>() {
				@Override
				public Iterator<Fields> iterator() {
					return toIndex.get(doc).keySet().iterator();
				}
			};
		}

		public Iterable<Documents> allDocuments() {
			return new Iterable<Documents>() {
				@Override
				public Iterator<Documents> iterator() {
					return toIndex.keySet().iterator();
				}
			};
		}

		public Iterable<FieldContents> contentsOf(final Documents doc, final Fields field) {
			return new Iterable<FieldContents>() {
				@Override
				public Iterator<FieldContents> iterator() {
					return toIndex.get(doc).get(field).iterator();
				}
			};
		}

		public Iterable<FieldContents> contentsOf(final Documents doc) {
			return new Iterable<FieldContents>() {
				@Override
				public Iterator<FieldContents> iterator() {
					
					return new Iterator<FieldContents>() {
						private final Iterator<Fields> outer = fieldsOf(doc).iterator();
						private Iterator<FieldContents> inner = null;

						@Override
						public boolean hasNext() {
							return moveToNextInner();
						}

						@Override
						public FieldContents next() {
							if(moveToNextInner()) {
								return inner.next();
							} else {
								throw new NoSuchElementException("Iteration at end.");
							} 
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException(
									"remove() has no semantics for nested iterations.");
						}

						/**
						 * Position the wrapped iterators so that {@code inner}.{@link 
						 * Iterator#next()} and {@code inner}.{@link Iterator#hasNext()}
						 * produce valid results for the nested iteration.
						 * 
						 * @result	<ul><li>{@code true}: {@code inner} has more results;
						 * 		postcondition: {@link #innerVallid()}.</li>
						 * 		<li>{@code false}: the iteration is at end;
						 * 		postcondition: {@code !outer.hasNext() && !innerVallid()}
						 */
						private boolean moveToNextInner() {
							if(inner != null) {
								if(inner.hasNext()) {
									assert(innerVallid());
									return true;
								}
							}
							
							assert(!innerVallid());
							while(outer.hasNext()) {
								assert(!innerVallid());
								inner = contentsOf(doc, outer.next()).iterator();
								if(inner.hasNext()) {
									assert(innerVallid());
									return true;
								}
							}
							assert(!outer.hasNext() && !innerVallid());
							return false;
						}

						private boolean innerVallid() {
							return inner != null && inner.hasNext();
						}
						
					};
				}
			};
		}

		public Iterable<FieldContents> allContents() {
			return new Iterable<FieldContents>() {
				@Override
				public Iterator<FieldContents> iterator() {
					return new Iterator<FieldContents>() {
						private final Iterator<Documents> outer = allDocuments().iterator();
						private Iterator<FieldContents> inner = null;

						@Override
						public boolean hasNext() {
							return moveToNextInner();
						}

						@Override
						public FieldContents next() {
							if(moveToNextInner()) {
								return inner.next();
							} else {
								throw new NoSuchElementException("Iteration at end.");
							} 
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException(
									"remove() has no semantics for nested iterations.");
						}

						/**
						 * Position the wrapped iterators so that {@code inner}.{@link 
						 * Iterator#next()} and {@code inner}.{@link Iterator#hasNext()}
						 * produce valid results for the nested iteration.
						 * 
						 * @result	<ul><li>{@code true}: {@code inner} has more results;
						 * 		postcondition: {@link #innerVallid()}.</li>
						 * 		<li>{@code false}: the iteration is at end;
						 * 		postcondition: {@code !outer.hasNext() && !innerVallid()}
						 */
						private boolean moveToNextInner() {
							if(inner != null) {
								if(inner.hasNext()) {
									assert(innerVallid());
									return true;
								}
							}
							
							assert(!innerVallid());
							while(outer.hasNext()) {
								assert(!innerVallid());
								inner = contentsOf(outer.next()).iterator();
								if(inner.hasNext()) {
									assert(innerVallid());
									return true;
								}
							}
							assert(!outer.hasNext() && !innerVallid());
							return false;
						}

						private boolean innerVallid() {
							return inner != null && inner.hasNext();
						}
						
					};
				}
			};
		}

		/**
		 * Return a Hamcrest {@link Matcher} that matches when a query 'hits'
		 * the document {@code doc}.
		 * 
		 * @param doc
		 * @param strategy
		 * @return 
		 */
		public Matcher<Queries> documentMatcher(
				final Documents doc, final Queries.MatchStrategy strategy) {
			if(toIndex.containsKey(doc)) {

				Set<FieldContents> con = new HashSet<>();
				for (FieldContents value : contentsOf(doc)) {
					con.add(value);
				}
				Set<FieldContents>docContents = Collections.unmodifiableSet(con);
				return Queries.matchFieldContents(docContents, strategy);
			} else {
				return CoreMatchers.<Queries>not(CoreMatchers.<Queries>anything());
			}
		}

		public Matcher<Queries> allDocsMatcher(final Queries.MatchStrategy strategy) {
				Set<FieldContents> con = new HashSet<>();
				for (FieldContents value : allContents()) {
					con.add(value);
				}
				Set<FieldContents>docContents = Collections.unmodifiableSet(con);
				return Queries.matchFieldContents(docContents, strategy);
			
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText(name);
		}

	}

	/**
	 * Queries to test {@link SearcherWS} with, each query has text invoke 
	 * {@code SearcherWS} with and a set of {@link FieldContents} on which the 
	 * query is expected to match.
	 */
	public enum Queries {
		Q1("one", EnumSet.of(FieldContents.V1)),
		Q2("o", EnumSet.of(FieldContents.V1, FieldContents.V2)),
		Q3("two", EnumSet.of(FieldContents.V2)),
		Q4("unexisting", Collections.<FieldContents>emptySet());

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
				
		private Queries(String queryText_, Set<FieldContents> foundIn_) {
			queryText = queryText_;
			foundIn = Collections.unmodifiableSet(foundIn_);
		}
	}
	
	private File fIndex;
	private FSDirectory index;
	private final IndexedDocuments storedDocs;

	/**
	 * Interesting configurations to test {@link SearcherWS} with.
	 * 
	 * Add any configuration of {@link Documents}, {@link Fields}, and 
	 * {@link FieldContents} you like to test {@code SearcherWS} with.  The 
	 * tests should succeed with arbitrary configurations.  Extend the 
	 * enumerations {@code Documents}, {@code Fields}, and {@code FieldContents}
	 * with new items if needed.
	 */
	@DataPoints
	static public IndexedDocuments docStoreConfigs[] = {
		builder("empty").build(),
		builder("one_doc_one_field").of(Documents.D1).with(Fields.F1).value(FieldContents.V1).build()
	};

	/**
	 * All items of {@link Queries}.  Extend {@code Queries} with new items to
	 * test {@link SearcherWS} with additional queries.
	 */
	@DataPoints
	static public Queries allQueries[] = Queries.values();
		
	
	
	public SearcherWSTest(final IndexedDocuments storedDocs_) {
		storedDocs = storedDocs_;
	}

	@Before
	public void setUp() {
		try {
			fIndex = assertCreateTmpPath();
			index = FSDirectory.open(fIndex);
			storedDocs.setupIndex(index);
			
		} catch (IOException ex) {
			String msg = String.format(
					"Unable to open index %s in path %s",
					fIndex.getName(),
					fIndex.getParent().toString());
			fail(msg);
			throw new RuntimeException(msg, ex);
		}
	}

	@After
	public void tearDown() {
		deleteIfExists(fIndex);
	}

	/**
	 * Test recall of {@link SearcherWS#_search(Directory,
	 * org.apache.lucene.search.Query, int)}-method, of class {@link SearcherWS}.
	 * 
	 * 
	 */
	@Theory
	public void testUnderscoreSearch_recall_queryTermInDocs_resultsContainDoc(
			final Queries query) {
		// TODO review the generated test code and remove the default call to fail.
		Matcher<Queries> matchesAnyStoredDocument =
				storedDocs.allDocsMatcher(Queries.MatchStrategy.ANY);
		assumeThat(query, matchesAnyStoredDocument);

		fail("The test case is a prototype.");
	}

	private static void deleteIfExists(File file) {
		if (file != null && file.exists()) {
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException ex) {
				Logger.getLogger(SearcherWSTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static File assertCreateTmpPath() {
		String indexDir_env = System.getenv("INDEXDIR");
		File aidaIndexes = (indexDir_env != null) ? 
				FileSystems.getDefault().getPath(indexDir_env).toFile() :
				FileUtils.getTempDirectory();
		UUID uuid = UUID.randomUUID();
		StringBuilder indexName = new StringBuilder()
				.append("testIndex-")
				.append(uuid.toString())
				.append(".tmp");
		File result = new File(aidaIndexes, indexName.toString());
		return result;
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
		
		IndexableField result = new TextField(
				field.name(), valueBuilder.toString(), Field.Store.YES);
		return result;
	}

}

/* vim: set shiftwidth=4 tabstop=4 noexpandtab fo=ctwan ai : */
