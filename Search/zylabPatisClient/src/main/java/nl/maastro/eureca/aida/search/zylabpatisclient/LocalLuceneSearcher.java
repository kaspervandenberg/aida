// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import checkers.nullness.quals.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.DynamicAdapter;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Use Lucene's {@link IndexSearcher} from within this process allowing complex
 * queries constructed with {@link Query}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class LocalLuceneSearcher extends SearcherBase {
	private static final Logger log = Logger.getLogger(LocalLuceneSearcher.class.getName());
	private static final StandardQueryParser parser = new StandardQueryParser();
	private static final DateFormat dateParser = initDateParser();
	private final DynamicAdapter queryAdapter;
	private final IndexSearcher searcher;
	private final File index;

	public LocalLuceneSearcher(final File index_, String defaultField_,
			int maxResults_, final ForkJoinPool taskPool_,
			DynamicAdapter queryAdapter_) throws IOException {
		super(defaultField_, maxResults_);
		Directory indexDir = FSDirectory.open(index_);
		queryAdapter = queryAdapter_;
		searcher = new IndexSearcher(DirectoryReader.open(indexDir), taskPool_);
		index = index_;
	}

	@Override
	public SearchResult searchFor(
			final nl.maastro.eureca.aida.search.zylabpatisclient.query.Query query,
			final Iterable<SemanticModifier> modifiers,
			final PatisNumber patient) {
		
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(Q,P)",
				String.format("Entering (patient: %s, query: %s)",
					patient.getValue(), query.toString()));

		List<SearchResult> perModifierResults = new LinkedList<>();
		for (SemanticModifier semMod : modifiers) {
			Query modifiedQuery = queryAdapter.applyModifier(query, semMod);
			Query patientQuery = patient.compose(modifiedQuery);
			org.apache.lucene.search.Query luceneQuery = 
					queryAdapter.adapt(LuceneObject.class, patientQuery)
					.getRepresentation();
			try {
					TopDocs result = searcher.search(luceneQuery, getMaxResults());
					perModifierResults.add(new SearchResultImpl(patient, result.totalHits, getMatchingDocs(luceneQuery, semMod, result)));
					
			} catch (IOException ex) {
			log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(Q,P)",
					String.format("Exception (patient: %s, query: %s)",
						patient.getValue(), query.toString()));
				log.log(Level.WARNING, String.format("IOException when querying local Lucene instance"), ex);
				return SearchResultImpl.NO_RESULT();
			}
				
		}
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(Q,P)",
				String.format("Leaving succesful (patient: %s, query: %s)",
					patient.getValue(), query.toString()));
		if(perModifierResults.isEmpty()) {
			return SearchResultImpl.NO_RESULT();
		}
		return SearchResultImpl.combine(perModifierResults.toArray(
				new SearchResult[perModifierResults.size()]));
	}

	private static DateFormat initDateParser() {
		DateFormat result = DateFormat.getDateInstance();	// Try to initialise a Dateformatter as
															// java documents that it should be done
		if (result instanceof SimpleDateFormat) {
			((SimpleDateFormat)result).applyPattern("yyyyMMdd");
		} else {
			// Fallback to constructing a SimpleDateFormat when
			// DateFormat.getInstance does not support the applyPattern()-method.
			result = new SimpleDateFormat("yyyyMMdd");
		}
		return result;
	}

	private Set<ResultDocument> getMatchingDocs(
			org.apache.lucene.search.Query query,
			SemanticModifier modifier,
			TopDocs results) {
		try {
			QueryScorer scorer = new QueryScorer(query);
			
			Highlighter highlighter = new Highlighter(
					new SimpleHTMLFormatter("<span class=\"searchHit\">", "</span>"),
					new SimpleHTMLEncoder(),
					scorer);

			Set<ResultDocument> docs = 
					new HashSet<>(results.scoreDocs.length);
			
			for(int i = 0; i < results.scoreDocs.length; i++) {
				Document doc = searcher.doc(results.scoreDocs[i].doc);
				for (IndexableField indexableField : doc.getFields()) {
					log.log(Level.FINER, "#{0} field: {1}, value: {2}",
							new Object[] {
							Integer.toString(i),
							indexableField.name(),
							indexableField.stringValue()});
				}
				IndexableField content = doc.getField("content");
				
				TokenStream stream = content.tokenStream(new StandardAnalyzer(Version.LUCENE_41));
				try {
					Set<Snippet> docFragments = new HashSet<>();
					for (String snippet : highlighter.getBestFragments(
								stream, content.stringValue(), 5)) {
						docFragments.add(new Snippet(snippet));
					}
					
					String id = doc.get("id");
					if(id == null) {
						id = "unknown_id?"+ UUID.randomUUID().toString();
					}

					URI uri = null;
					try {
						@SuppressWarnings("nullness") // URI constructor accepts nulls
						URI toResolve = new URI(null, null, id, "index=" + index.getName(), null);
						uri = Config.instance().getDocumentServer().resolve(toResolve);
					} catch (URISyntaxException ex) {
						log.log(Level.WARNING, 
								String.format("Exception when creating URI from %s, %s, and %s",
									Config.instance().getDocumentServer(),
									id,
									index.getName()), ex);
					}
					
					String docType = doc.get("Document_type");
					Date patientBirthDate = getPatientBirthDate(doc, id);
					Gender patientGender = getPatientGender(doc, id);
					
					docs.add(new ResultDocument(
							new DocumentId(id),
							uri,
							docType,
							patientBirthDate,
							patientGender,
							Collections.<SemanticModifier, Set<Snippet>>singletonMap(
								modifier, docFragments)));
				} catch (InvalidTokenOffsetsException ex) {
					throw new Error(ex);
				}
			}
					
			return docs;
		} catch (IOException ex) {
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "getFragments(Q, TD)", String.format("Exception (query: %s, results: %s)", query.toString(), results.toString()));
			log.log(Level.WARNING, String.format("IOException when querying local Lucene instance"), ex);
			return Collections.emptySet();
		}
	}

	private static @Nullable Date getPatientBirthDate(Document doc, String id) {
		String str_patientBirthDate = doc.get("Geboortedatum");
		try {
			Date result = dateParser.parse(str_patientBirthDate);
			return result;
		}
		catch (ParseException ex) {
			log.log(Level.WARNING, String.format(
					"Document %s does not contain patient's birth date in " +
					"the format yyyyMMdd (value in document: \"%s\").\n" +
					"Continuing with unknown date of birth.",
					id, str_patientBirthDate),
					ex);
			return null;
		}
	}

	private static Gender getPatientGender(Document doc, String id) {
		String str_patientGender = doc.get("Geslacht");
		try {
			Gender patientGender = Gender.parse(str_patientGender);
			return patientGender;
		} catch (IllegalArgumentException ex) {
			log.log(Level.WARNING, String.format(
					"Document %s does not contain patient's Gender in " +
					"the expected format ('M'/'V') (value in document: \"%s\").\n" +
					"Continuing with unknown Gender.",
					id, str_patientGender),
					ex);
			return Gender.UNKNOWN;
		}
		
	}
}
