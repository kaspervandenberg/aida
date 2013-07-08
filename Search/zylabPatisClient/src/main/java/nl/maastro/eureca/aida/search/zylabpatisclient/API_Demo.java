/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.PreconstructedQueries;
import java.io.BufferedOutputStream;
import java.io.File;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.SearchResultFormatter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.Classifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.InterDocOverride;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.IntraDocOverride;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.HtmlFormatter;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.LocalParts;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.SemanticModifiers;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.DynamicAdapter;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryProvider;

/**
 *
 * @author kasper2
 */
public class API_Demo {
	private static final EnumMap<LocalParts, String> headers =
			new EnumMap<>(LocalParts.class);
	static {
		headers.put(LocalParts.METASTASIS,
				"\n" +
				"\n" +
				"- - - - - - - - - -\n" +
				"M E T A S T A S I S\n" +
				"- - - - - - - - - -\n" +
				"\n\n");
		
		headers.put(LocalParts.NO_METASTASIS,
				"\n" +
				"\n" +
				"- - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n" +
				"G E E N   M E T A S T A S I S -- (Combine span with term)\n" +
				"- - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n" +
				"\n\n");

		headers.put(LocalParts.HINTS_METASTASIS, 
				"\n" +
				"\n" +
				"- - - - - - - - - - - - - - - - - - - - - - - - - - - \n" +
				"M E T A S T A S I S   O N Z E K E R -- (span of spans)\n" +
				"- - - - - - - - - - - - - - - - - - - - - - - - - - - \n" +
				"\n\n");
	}

	private final Config config;
	private final Searcher searcher;
	private final QueryProvider queryProvider;
	private final DynamicAdapter queryAdapter;
	private final Map<PatisNumber, Boolean> patients;
	private final List<SemanticModifier> modifiers;
	private final Classifier classifier;
	private final SearchResultFormatter formatter;

	public API_Demo() {
		this.config = initConfig();
		this.searcher = initSearcher(config);
		this.queryProvider = new PreconstructedQueries.Provider();
		this.queryAdapter = new DynamicAdapter();
		this.patients = initPatients();
		this.modifiers = initSemanticModifiers();
		this.classifier = initClassifier();
		HtmlFormatter tmp = new HtmlFormatter(); //new PlaintextHumanFormatter();
		tmp.setShowSnippetsStrategy(HtmlFormatter.SnippetDisplayStrategy.DYNAMIC_SHOW);
		this.formatter = tmp;
	}

	
	private static Config initConfig() {
		// Read config file
		InputStream s = API_Demo.class.getResourceAsStream("/zpsc-config.xml");
		return Config.init(s);
		// intentionally keeping s open, since Config will read from it at a later time
	}

	private static Searcher initSearcher(Config config) {
		// Use config to initialise a searcher
		try {
			return config.getSearcher();
		} catch (ServiceException | IOException ex) {
			throw new Error(ex);
		}
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
		
		result.put(PatisNumber.create("71441"), true);
		result.put(PatisNumber.create("71121"), false);
		result.put(PatisNumber.create("71089"), false);
		result.put(PatisNumber.create("70657"), false);
		result.put(PatisNumber.create("70979"), false);
		
		result.put(PatisNumber.create("71367"), false);
		result.put(PatisNumber.create("71369"), false);
		result.put(PatisNumber.create("71118"), false);
		result.put(PatisNumber.create("71363"), false);
		result.put(PatisNumber.create("70933"), false);
		result.put(PatisNumber.create("71105"), false);
		result.put(PatisNumber.create("71190"), false);
		result.put(PatisNumber.create("70946"), false);
		result.put(PatisNumber.create("71074"), false);
		result.put(PatisNumber.create("70996"), false);
		result.put(PatisNumber.create("71422"), false);
		result.put(PatisNumber.create("71193"), false);
		result.put(PatisNumber.create("71454"), false);
		result.put(PatisNumber.create("71169"), false);
		result.put(PatisNumber.create("71739"), false);
		result.put(PatisNumber.create("71464"), false);
		return result;
	}

	private static List<SemanticModifier> initSemanticModifiers() {
		List<SemanticModifier> result = new ArrayList<>(SemanticModifiers.values().length + 1);
		result.add(SemanticModifier.Constants.NULL_MODIFIER);
		result.addAll(Arrays.asList(SemanticModifiers.values()));
		return result;
	}
	
	private static Classifier initClassifier() {
		Classifier instance = Classifier.instance();
		instance.appendRule(new IntraDocOverride(
				SemanticModifiers.NEGATED,
				SemanticModifier.Constants.NULL_MODIFIER));
		instance.appendRule(new IntraDocOverride(
				SemanticModifiers.SUSPICION,
				SemanticModifier.Constants.NULL_MODIFIER));
		instance.appendRule(new InterDocOverride(
				EligibilityClassification.UNCERTAIN,
				EligibilityClassification.NOT_ELIGIBLE));
		return instance;
	}

	private List<SearchResult> initExpectedResults() {
		List<SearchResult> expected = new ArrayList<>(patients.size());
		for (Map.Entry<PatisNumber, Boolean> entry : patients.entrySet()) {
			expected.add(SearchResultImpl.create(entry.getKey(), entry.getValue()));
		}
		return expected;
	}
	
	public void searchAndShow(
			LocalParts preconstructedQuery) {
		Query query = queryProvider.get(preconstructedQuery.getID());
		Iterable<SearchResult> results = searcher.searchForAll(query, modifiers, patients.keySet());
		
		System.out.append(headers.get(preconstructedQuery));
		try {
			formatter.writeList(System.out, results);
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}

	public Iterable<SearchResult> searchConcept(
			Query concept, List<SemanticModifier> modifiers) {
		Iterable<SearchResult> results = searcher.searchForAll(concept, modifiers, patients.keySet());
		List<SearchResult> conclusions = new LinkedList<>();
		for (SearchResult searchResult : results) {
			conclusions.add(classifier.resolve(searchResult));
		}
		return conclusions;
	}
	
	public void writeTable(LinkedHashMap<String, Iterable<SearchResult>> results) {
		Date now = new Date();
		File f = new File(String.format("results-%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS.html", now));
		try {
			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(
					new FileOutputStream(f)), StandardCharsets.UTF_8);
			HtmlFormatter.writeDocStart(out,
					String.format("<h1>Results of %1$tT (on %1$ta %1$te %1$tb)</h1>\n", now));
			formatter.writeTable(out, results);
			HtmlFormatter.writeDocEnd(out);
			out.close();
		} catch (IOException ex) {
			throw new Error(ex);
		}
				
	}
	
	static public void main(String[] args) {
		API_Demo instance = new API_Demo();
//		instance.searchAndShow(PreconstructedQueries.LocalParts.METASTASIS);
//		instance.searchAndShow(PreconstructedQueries.LocalParts.NO_METASTASIS);
//		instance.searchAndShow(PreconstructedQueries.LocalParts.NO_HINTS_METASTASIS);
		LinkedHashMap<String, Iterable<SearchResult>> table = new LinkedHashMap<>();
		table.put("Expected", instance.initExpectedResults());
		table.put(LocalParts.METASTASIS.name(), 
				instance.searchConcept(LocalParts.METASTASIS.getQuery(), instance.modifiers));
		
		instance.writeTable(table);
	}

}
