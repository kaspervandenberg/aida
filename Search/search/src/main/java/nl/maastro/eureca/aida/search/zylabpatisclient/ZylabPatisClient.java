// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.xml.CoreParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Search for documents that match clinical in- or exclusion criteria by 
 * patisnumber.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ZylabPatisClient extends HttpServlet {
	private enum RequestParameters {
		PATIS_NUMBER("patisNr[]", 
				OptionBuilder.withArgName("patisnumber")
							.hasArg()
							.withDescription(
								"Specify the PatisNumber to search for; "
								+ "specifying multiple patisnumbers results in "
								+ "the matching documents of any of the "
								+ "specified patisnumbers.")
							.create("patisNr"),
				null),
		CONFIG_FILE(null,
				OptionBuilder.withArgName("file")
							.hasArg()
							.withDescription(
								"Read configuration from file.")
							.create("config"),
				"configFile");

		public final String httpKey;
		private final Option option;
		private final String servletInitParamKey;

		private RequestParameters(
				final String httpKey_,
				final Option option_,
				final String servletInitParamKey_) {
			this.httpKey = httpKey_;
			this.option = option_;
			this.servletInitParamKey = servletInitParamKey_;
		}

		public Option getOption() {
			return option;
		}
	}
	
	private static final ForkJoinPool taskPool = new ForkJoinPool();

	private Config config = null;

	/**
	 * Construct a Zylab Patis Search client using {@code config_} as source of
	 * configuration settings.
	 * 
	 * @param config_	{@link Config} from which to read properties such as 
	 * 		the {@link Searcher} to use and stored concept queries.
	 */
	public ZylabPatisClient(final Config config_) {
		config = config_;
	}
	
	/**
	 * Constructor for servlet container; client (that is the servlet container
	 * such as Apache Tomcat) must call {@link #init()}
	 */
	public ZylabPatisClient() {
		// Initialisation via init()
	}

	/**
	 * Set init parameter {@link RequestParameters#CONFIG_FILE} to the name of
	 * the configuration s_file used.
	 * 
	 * @throws ServletException 
	 */
	@Override
	public void init() throws ServletException {
		if(config == null) {
			String s_file = this.getInitParameter(
					RequestParameters.CONFIG_FILE.servletInitParamKey);
			if(s_file == null) {
				throw new UnavailableException(String.format(
						"Specify config file in init parameter (%s).",
						RequestParameters.CONFIG_FILE.servletInitParamKey));
			}
			config = Config.init(new File(s_file), taskPool);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// URL structure: {protocol}://{server}/{context}/{conceptQueryID}/results?patisNr="{patisNr}...}
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Read the {@link PatisNumber}s in {@code request}.  Any request parameter
	 * named {@link RequestParameters#PATIS_NUMBER} is added to the result.
	 * 
	 * @param request	{@link HttpServletRequest} used to call this 
	 * 		{@link HttpServlet}; {@code getRequestedPatisNumbers} will sanitise
	 * 		the Patisnumbers in {@code request}.	
	 * 
	 * @return	a list of requested {@link PatisNumber}.  The returned list is
	 * 		empty when the servlet's client did not specify any PatisNumbers 
	 * 		otherwise it contains one or more {@code PatisNumber}s. 
	 * 
	 * @throws IllegalArgumentException	when one (or more) of the patisnumbers
	 * 		in the request are not well formed.
	 * 
	 */
	private List<PatisNumber> getRequestedPatisNumbers(HttpServletRequest request) 
			throws IllegalArgumentException {
		String numbers[] = request.getParameterValues(
				RequestParameters.PATIS_NUMBER.httpKey);
		return getRequestedPatisNumbers(numbers);
	}

	/**
	 * Read the {@link PatisNumber}s in {@code cmd}.  Any parameter
	 * named {@link RequestParameters#PATIS_NUMBER} is added to the result.
	 * 
	 * @param cmd	{@link CommandLine} used to execute this Java program;
	 * 		{@code getRequestedPatisNumbers} will sanitise the Patisnumbers 
	 * 		in {@code cmd}.	
	 * 
	 * @return	a list of requested {@link PatisNumber}.  The returned list is
	 * 		empty when the java program client did not specify any PatisNumbers 
	 * 		otherwise it contains one or more {@code PatisNumber}s. 
	 * 
	 * @throws IllegalArgumentException	when one (or more) of the patisnumbers
	 * 		in the commandline are not well formed.
	 * 
	 */
	private List<PatisNumber> getRequestedPatisNumbers(CommandLine cmd) {
		String[] numbers = cmd.getOptionValues(
				RequestParameters.PATIS_NUMBER.getOption().getLongOpt());
		return getRequestedPatisNumbers(numbers);
	}

	/**
	 * Santise the array of patisnumber strings.  Called by 
	 * {@link #getRequestedPatisNumbers(javax.servlet.http.HttpServletRequest)}
	 * and {@link #getRequestedPatisNumbers(org.apache.commons.cli.CommandLine)}. 
	 * 
	 * @param numbers	array of strings representing patisnumbers
	 * @return	a list of {@link PatisNumber}
	 * 
	 * @throws IllegalArgumentException	when one (or more) of the patisnumbers
	 * 		in the array are not well formed.
	 */
	private List<PatisNumber> getRequestedPatisNumbers(String[] numbers)
			throws IllegalArgumentException {
		ArrayList<PatisNumber> result = new ArrayList<>(numbers.length);
		
		for (String number : numbers) {
			result.add(PatisNumber.create(number));
		}

		return result;
	}

	private void test() {
		
	}

	

	public static void main(String[] args) {
		
//		String sQuery = PreconstructedQueries.instance().getQuery(PreconstructedQueries.LocalParts.METASTASIS_IV).toString("contents");
		CoreParser parser = new CoreParser("content", new StandardAnalyzer(Version.LUCENE_41));
		String sQuery = "metastasis~ stage~\"~10";
//		Query query = parser.parse(null);
		try {
			Directory indexDir = FSDirectory.open(
					new File("/home/kasper2/mnt/aida/indexes/Zylab_test-20130415-02"));
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
			TopDocs result = searcher.search(
					PreconstructedQueries.instance().getQuery(
						PreconstructedQueries.LocalParts.METASTASIS_IV),
					1000);

			System.out.printf("#results: %d\n", result.totalHits);
			
		} catch (IOException ex) {
			throw new Error(ex);
		}

		
//		StandardQueryParser parser = new StandardQueryParser();
//		try {
//			Query q = parser.parse(sQuery, "contents");
//			System.out.append(dumpQuery("", q));
//			int i = 1;
//		} catch (QueryNodeException ex) {
//			throw new Error(ex);
//		}
	}


}
