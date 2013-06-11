/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.BufferedOutputStream;
import java.io.File;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.PlaintextHumanFormatter;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.SearchResultFormatter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.output.HtmlFormatter;
import org.apache.lucene.search.Query;

/**
 *
 * @author kasper2
 */
public class API_Demo {
	private static final EnumMap<PreconstructedQueries.LocalParts, String> headers =
			new EnumMap(PreconstructedQueries.LocalParts.class);
	static {
		headers.put(PreconstructedQueries.LocalParts.METASTASIS,
				"\n" +
				"\n" +
				"- - - - - - - - - -\n" +
				"M E T A S T A S I S\n" +
				"- - - - - - - - - -\n" +
				"\n\n");
		
		headers.put(PreconstructedQueries.LocalParts.NO_METASTASIS,
				"\n" +
				"\n" +
				"- - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n" +
				"G E E N   M E T A S T A S I S -- (Combine span with term)\n" +
				"- - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n" +
				"\n\n");

		headers.put(PreconstructedQueries.LocalParts.NO_HINTS_METASTASIS, 
				"\n" +
				"\n" +
				"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - \n" +
				"G E E N   M E T A S T A S I S   A A N G E T O O N D -- (span of spans)\n" +
				"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - \n" +
				"\n\n");
	}

	private final Config config;
	private final Searcher searcher;
	private final List<PatisNumber> patients;
	private final SearchResultFormatter formatter;

	public API_Demo() {
		this.config = initConfig();
		this.searcher = initSearcher(config);
		this.patients = initPatients();
		HtmlFormatter tmp = new HtmlFormatter(); //new PlaintextHumanFormatter();
		tmp.setShowSnippetsStrategy(HtmlFormatter.SnippetDisplayStrategy.DYNAMIC_SHOW);
		this.formatter = tmp;
	}

	
	private static Config initConfig() {
		// Read config file
		try {
			InputStream s = new FileInputStream(
				"/home/administrator/aida.git/Search/zylabPatisClient/src/main/webapp/WEB-INF/zpsc-config.xml");
			return Config.init(s);
			// intentionally keeping s open, since Config will read from it at a later time
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}

	private static Searcher initSearcher(Config config) {
		// Use config to initialise a searcher
		try {
			return config.getSearcher();
		} catch (ServiceException | IOException ex) {
			throw new Error(ex);
		}
	}

	private static List<PatisNumber> initPatients() {
		// Dummy list of patients; reading a list of patisnumbers is not yet in API
		List<PatisNumber> result = Arrays.<PatisNumber>asList(
//					PatisNumber.create("12345"),
//					PatisNumber.create("11111"),
				PatisNumber.create("71358"), // Exp 0
				PatisNumber.create("71314"),
				PatisNumber.create("71415"), // Exp 0
				PatisNumber.create("71539"),
				PatisNumber.create("71586"),
				PatisNumber.create("70924"),
				PatisNumber.create("71785"),
				PatisNumber.create("71438"),
				PatisNumber.create("71375"),
				PatisNumber.create("71448"),
				
				PatisNumber.create("71681"), // Exp 1
				PatisNumber.create("71692"),
				PatisNumber.create("71757"),
				PatisNumber.create("70986"),
				PatisNumber.create("46467"));
		return result;
	}
	
	public void searchAndShow(
			PreconstructedQueries.LocalParts preconstructedQuery) {
		Query query = PreconstructedQueries.instance().getQuery(preconstructedQuery);
		Iterable<SearchResult> results = searcher.searchForAll(query, patients);
		
		System.out.append(headers.get(preconstructedQuery));
		try {
			formatter.writeList(System.out, results);
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}

	public void writeTable(List<PreconstructedQueries.LocalParts> datasets) {
		LinkedHashMap<String, Iterable<SearchResult>> results =
				new LinkedHashMap<>(datasets.size());
		for (PreconstructedQueries.LocalParts queryId : datasets) {
			Query query = PreconstructedQueries.instance().getQuery(queryId);
			results.put(queryId.name(), searcher.searchForAll(query, patients));
		}
		
		Date now = new Date();
		File f = new File(String.format("results-%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS.html", now));
		try {
			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(
					new FileOutputStream(f)), StandardCharsets.UTF_8);
			out.append(
					"<!DOCTYPE html>\n" +
					"<html>\n" +
					"<head>\n");
			out.append(String.format("<meta charset=\"%s\"/>\n", StandardCharsets.UTF_8.name()));
			HtmlFormatter.writeScript(out);
			HtmlFormatter.writeStyle(out);
			out.append(
					"</head>\n" +
					"<body>\n");
			out.append(String.format("<h1>Results of %1$tT (on %1$ta %1$te %1$tb)</h1>\n", now));
			formatter.writeTable(out, results);
			out.append("</body>\n"
					+ "</html>\n\n");
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
		instance.writeTable(Arrays.asList(
				PreconstructedQueries.LocalParts.METASTASIS,
				PreconstructedQueries.LocalParts.NO_METASTASIS,
				PreconstructedQueries.LocalParts.NO_HINTS_METASTASIS));
	}

}
