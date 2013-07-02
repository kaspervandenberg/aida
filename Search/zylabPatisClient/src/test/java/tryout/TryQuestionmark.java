/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tryout;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.Searcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.HtmlFormatter;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.SearchResultFormatter;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObjectBase;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanTermQuery;

/**
 *
 * @author kasper
 */
public class TryQuestionmark {
	private final Config config;	
	private final Searcher searcher;
	private final Query questionMark;
	private final Map<PatisNumber, Boolean> patients;
	private final SearchResultFormatter formatter;

	public TryQuestionmark() {
		this.config = initConfig();
		this.searcher = initSearcher(config);
		this.questionMark = initQuestionmarkQuery(config);
		this.patients = initPatients();
		this.formatter = initFormatter();
	}
	
	private static Config initConfig() {
		InputStream s = TryQuestionmark.class.getResourceAsStream("/zpsc-config.xml");
		return Config.init(s);
	}

	private static Searcher initSearcher(Config config_) {
		try {
			return config_.getSearcher();
		} catch (ServiceException | IOException ex) {
			throw new Error(ex);
		}
	}
	
	private static Query initQuestionmarkQuery(final Config config_) {
		return new LuceneObjectBase() {
			@Override
			public org.apache.lucene.search.Query getRepresentation() {
				return new SpanTermQuery(new Term(config_.getDefaultField(), "metastase Dr"));
			}

			@Override
			public QName getName() {
				return config_.getNamespaces().createQName("zpsc", "questionmark");
			}
		};
	}

	private static Map<PatisNumber, Boolean> initPatients() {
		// Dummy list of patients; reading a list of patisnumbers is not yet in API
		Map<PatisNumber, Boolean> result = new LinkedHashMap<>();
		result.put(PatisNumber.create("71358"), false);// Exp 0
		result.put(PatisNumber.create("71314"), false);
		result.put(PatisNumber.create("71415"), false); // Exp 0
		result.put(PatisNumber.create("71539"), false);
		result.put(PatisNumber.create("71586"), false);
		result.put(PatisNumber.create("70924"), false);
		result.put(PatisNumber.create("71785"), false);
		result.put(PatisNumber.create("71438"), false);
		result.put(PatisNumber.create("71375"), false);
		result.put(PatisNumber.create("71448"), false);
		
		result.put(PatisNumber.create("71681"), true); // Exp 1
		result.put(PatisNumber.create("71692"), true);
		result.put(PatisNumber.create("71757"), true);
		result.put(PatisNumber.create("70986"), true);
		result.put(PatisNumber.create("46467"), true);
		return result;
	}

	private static SearchResultFormatter initFormatter() {
		HtmlFormatter result = new HtmlFormatter();
		result.setShowSnippetsStrategy(HtmlFormatter.SnippetDisplayStrategy.DYNAMIC_SHOW);
		return result;
	}
		
	private void search() {
		Iterable<SearchResult> results = searcher.searchForAll(
				questionMark,
				Collections.<SemanticModifier>singletonList(SemanticModifier.Constants.NULL_MODIFIER),
				patients.keySet());

		try {
			FileWriter output = new FileWriter("questionmark.html");
			HtmlFormatter.writeDocStart(output, String.format("Try Questionmark – %1$te-%1$tb-%1$tY %1$tk:%1$tM:%1$tS", new Date()));
			formatter.writeList(output, results);
			HtmlFormatter.writeDocEnd(output);
			output.close();
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}
	
	public static void main(String[] args) {
		TryQuestionmark instance = new TryQuestionmark();
		instance.search();
	}
}
