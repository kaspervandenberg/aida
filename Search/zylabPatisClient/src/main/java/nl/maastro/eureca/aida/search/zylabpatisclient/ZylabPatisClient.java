// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryProviderRegistry;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryProvider.QueryRepresentation_Old;
import nl.maastro.eureca.aida.search.zylabpatisclient.Searcher.SearcherCapability;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.NameSpaceResolver;
import nl.maastro.eureca.aida.search.zylabpatisclient.contentNegotiation.ContentTypeNegotiator;
import nl.maastro.eureca.aida.search.zylabpatisclient.contentNegotiation.MediaType;
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
import org.jdom2.Namespace;

/**
 * Search for documents that match clinical in- or exclusion criteria by 
 * patisnumber.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ZylabPatisClient extends HttpServlet {
	/**
	 * Request parameters specified as servlet 
	 * {@link javax.servlet.http.HttpServletRequest#getParameter(java.lang.String)  request parameters}
	 * or as commandline parameters when {@code ZylabPatisClient} is invoked 
	 * from the commandline.
	 */
	private enum RequestParameters {
		/**
		 * A {@link PatisNumber} specified as request parameter or commandline
		 * argument.
		 */
		@SuppressWarnings("static-access")
		PATIS_NUMBER("patisNr[]",
				OptionBuilder.withArgName("patisnumber")
							.hasArg()
							.withDescription(
								"Specify the PatisNumber to search for; "
								+ "specifying multiple patisnumbers results in "
								+ "the matching documents of any of the "
								+ "specified patisnumbers.")
							.create("patisNr"),
				InputSanitation.PATTIS_NR) {
			
			@Override
			protected PatisNumber convertValue(
					final ZylabPatisClient context, final String value) {
				return PatisNumber.create(value);
			}
		},

		/**
		 * A query pattern QName as request parameter or commandline argument.
		 */
		@SuppressWarnings("static-access")
		QUERY_PATTERN("queryPatternID[]",
				OptionBuilder.withArgName("qname")
							.hasArg()
							.withDescription(
								"Specify the qname of the pattern to use; "
								+ "specifying multiple qnames results in "
								+ "the documents matching any of the specified "
								+ "patterns.")
							.create("queryPatternID"),
				InputSanitation.QNAME) {
			
			@Override
			protected QName convertValue(
					final ZylabPatisClient context, final String value) {
				try {
					return context.config.getNamespaces().createQName(value);
				} catch (NoSuchElementException ex) {
					throw new IllegalArgumentException(
							String.format("Prefix of %s is not a known namespace URI", value),
							ex);
				}
			}
		};
		
		/**
		 * Key to use in {@link javax.servlet.http.HttpServletRequest#getParameter(java.lang.String)}.
		 */
		public final String httpKey;

		/**
		 * {@link org.apache.commons.cli.Option} to
		 * {@link org.apache.commons.cli.CommandLineParser#parse(org.apache.commons.cli.Options, java.lang.String[]) parse}
		 * the commandline with.
		 */
		private final Option option;

		private final InputSanitation saneInput;
		
		/**
		 * Create a HTTPRequest and commandline parameter.
		 * 
		 * @param httpKey_
		 * @param option_
		 * @param saneInput_ 
		 */
		private RequestParameters(
				final String httpKey_,
				final Option option_,
				final InputSanitation saneInput_) {
			this.httpKey = httpKey_;
			this.option = option_;
			saneInput = saneInput_;
		}

		/**
		 * Convert a String to an object of type {@code <T>}.  Instances must
		 * implement {@code convertValue} with the type of the parameter.
		 * 
		 * @param <T>	the type of value of the parameter.
		 * @param context	a {@link ZylabPatisClient} that the instance can 
		 * 		use when converting parameters.
		 * @param value	the string value to convert; {@code value} matches
		 * 		{@link #wellFormedValue}.
		 * 
		 * @return	{@code value} converted to {@code <T>}
		 * 
		 * @throws IllegalArgumentException	when {@code value} cannot be 
		 * 		converted to {@code <T>}.
		 */
		protected abstract <T> T convertValue(
				final ZylabPatisClient context, final String value)
				throws IllegalArgumentException;

		/**
		 * Read the parameter values for {@link #httpKey} in {@code request}.
		 * 
		 * @param <T>		the type of values to return; 
		 * 		{@link #convertValue(nl.maastro.eureca.aida.search.zylabpatisclient.ZylabPatisClient, java.lang.String)}
		 * 		must return objects of type {@code <T>}.
		 * @param context	the {@link ZylabPatisClient} calling {@code get};
		 * 		{@link #convertValue(nl.maastro.eureca.aida.search.zylabpatisclient.ZylabPatisClient, java.lang.String)} 
		 * 		can use it to make context specific conversions.
		 * @param request	{@link HttpServletRequest} used to call this 
		 * 		{@link HttpServlet}; {@code get} will 
		 * 		{@link #assertCleanURL(java.lang.String) sanitise} the parameter 
		 * 		values in {@code request}.	
		 * 
		 * @return	a list of requested parameters.  The returned list is
		 * 		empty when the servlet's client did not specify any parameter 
		 * 		values otherwise it contains one or more items of type 
		 * 		{@code <T>}.
		 * 
		 * @throws IllegalArgumentException	when one (or more) of the parameters
		 * 		in the request are not well formed.
		 */
		public <T> List<T> get(
				final ZylabPatisClient context, final HttpServletRequest request) {
			return convertValues(context, request.getParameterValues(httpKey));
		}

		/**
		 * Read the parameter values for {@link #option} in {@code cmd}.
		 * 
		 * @param <T>		the type of values to return; 
		 * 		{@link #convertValue(nl.maastro.eureca.aida.search.zylabpatisclient.ZylabPatisClient, java.lang.String)}
		 * 		must return objects of type {@code <T>}.
		 * @param context	the {@link ZylabPatisClient} calling {@code get};
		 * 		{@link #convertValue(nl.maastro.eureca.aida.search.zylabpatisclient.ZylabPatisClient, java.lang.String)} 
		 * 		can use it to make context specific conversions.
		 * @param cmd	{@link CommandLine} used to execute this Java program;
		 * 		{@code getRequestedPatisNumbers} will sanitise the parameters 
		 * 		in {@code cmd}.	
		 * 
		 * @return	a list of requested parameters.  The returned list is
		 * 		empty when the servlet's client did not specify any parameter 
		 * 		values otherwise it contains one or more items of type 
		 * 		{@code <T>}.
		 * 
		 * @throws IllegalArgumentException	when one (or more) of the parameters
		 * 		in the request are not well formed.
		 */
		public <T> List<T> get(
				final ZylabPatisClient context, final CommandLine cmd) {
			return convertValues(context, cmd.getOptionValues(option.getOpt()));
		}

		/**
		 * {@link #assertCleanURL(java.lang.String) Check} that all values match
		 * {@link #wellFormedValue} and 
		 * {@link #convertValue(nl.maastro.eureca.aida.search.zylabpatisclient.ZylabPatisClient, java.lang.String) 
		 * convert} each value to {@code <T>}
		 * 
		 * @param <T>	type of parameter to return
		 * @param context	{@link ZylabPatisClient} context to use for context 
		 * 		specific conversions.
		 * @param values	Strings to convert
		 * 
		 * @return 	a list of items of type {@code <T>}
		 */
		protected <T> List<T> convertValues(
				final ZylabPatisClient context, final String values[]) {
			if (values != null) {
				List<T> result = new ArrayList<>(values.length);
				for (String s : values) {
//					assertClean(s);
					saneInput.assertClean(s);
					result.add(this.<T>convertValue(context, s));
				}
				return result;
			} else {
				return Collections.<T>emptyList();
			}
		}
	}

	/**
	 * Parts of the request URI that specify the resource (or the type of 
	 * operation to perform.)
	 */
	private enum UriParameters {
		QUERY_PATTERN_URI_PART(
				Pattern.compile("(.*/queryPattern/)([\\p{Alnum}-_.]*)/results"),
				InputSanitation.LOCAL_QNAME),

		LIST_QUERIES(
				Pattern.compile("(.*/queryIndex)"),
				InputSanitation.ANY),

		DEMO(
				Pattern.compile("(.*/demo)"),
				InputSanitation.ANY),

		INVALLID_PATTERN(Pattern.compile(".*"), InputSanitation.ANY)
		;
		
		public final Pattern uriPart;
		private final InputSanitation saneInputPattern;

		private UriParameters(
				final Pattern uriPart_,
				final InputSanitation saneInput_) {
			this.uriPart = uriPart_;
			this.saneInputPattern = saneInput_;
		}

		public static UriParameters requestedParameter (HttpServletRequest request) {
			for (UriParameters param : values()) {
				if(param.uriPart != null ? 
						param.uriPart.matcher(request.getPathInfo()).matches() :
						false) {
					return param;
				}
			}
			return INVALLID_PATTERN;
		}

		public QName getResourceQName(final ZylabPatisClient context,
				final HttpServletRequest request) throws IllegalArgumentException {
			final Matcher m = assertCleanURL(request.getRequestURL());
			final NameSpaceResolver nsr = context.config.getNamespaces();
			String s_nsUri = m.group(1);
			String localPart = m.group(2);
			saneInputPattern.assertClean(localPart);
			try { 
				URI nsUri = new URI(s_nsUri);
				Namespace namespace = (nsr.containsUri(nsUri)) ?
						nsr.resolveUri(nsUri) :
						Namespace.getNamespace(s_nsUri);
				return new QName(namespace.getURI(), localPart, namespace.getPrefix());
			} catch (URISyntaxException ex) {
				throw new IllegalArgumentException(
						String.format(
							"Request namespace URI %s is not well fromed.", s_nsUri),
							ex);
			}
		}

		private Matcher assertCleanURL(StringBuffer requestURL) {
			final Matcher m = uriPart.matcher(requestURL.toString());
			if (!m.matches()) {
				throw new IllegalArgumentException(
						String.format("HttpRequest does not match %s", this.name()));
			}
			if(m.groupCount() != 2) {
				throw new Error(
						String.format(
							"Pattern %s has wrong number of groups expected is 2.",
							uriPart.pattern()));
			}
			return m;
		}
	}

	private enum InitialParameters {
		CONFIG_FILE(OptionBuilder.withArgName("file")
							.hasArg()
							.withDescription(
								"Read configuration from file.")
							.create("config"),
				"configFile");

		;
		
		private final Option option;
		private final String servletInitParamKey;

		/**
		 * Create an initialisation parameter.
		 * 
		 * @param option_
		 * @param servletInitParamKey_ 
		 */
		private InitialParameters(
				final Option option_,
				final String servletInitParamKey_) {
			this.option = option_;
			this.servletInitParamKey = servletInitParamKey_;
		}
	}
	
	private enum InputSanitation {
		ANY(".*", ""),
		LOCAL_QNAME("^([\\p{Alpha}[_]][\\p{Alnum}[_\\-.]]*)$",
				"\"%s\" is not a well formed local part of a QName."),
		QNAME("^([\\p{Alpha}[_]][\\p{Alnum}[_\\-.]]*:)?([\\p{Alpha}[_]][\\p{Alnum}[_\\-.]]*)$",
				"\"%s\" is not a valid QName"),
		PATTIS_NR("^[\\p{Digit}]+$",
				"\"%s\" is not a wellformed number")
		;
		
		/**
		 * {@link java.util.regex.Pattern} to restrict input to.
		 */
		private final Pattern pattern;

		/**
		 * Message to show when input is not well formed.
		 */
		private final String errMsg;

		private InputSanitation(final String regExpr, final String errMsg_) {
			pattern = Pattern.compile(regExpr);
			errMsg = errMsg_;
		}

		public void assertClean(String s) {
			if(!pattern.matcher(s).matches()) {
				throw new IllegalArgumentException(String.format(errMsg, s));
			}
		}
	}
	
	public enum Strategy { 
		STRING {
			@Override
			public Iterable<SearchResult> search(
					ZylabPatisClient context,
					QName queryPatId,
					Iterable<PatisNumber> patients)
						throws ServiceException, IOException {
				String query = context.queryPatterns.getAsString(queryPatId);
				return context.config.getSearcher().searchForAll(query, patients);
			}
		},
		
		OBJECT {
			@Override
			public Iterable<SearchResult> search(
					ZylabPatisClient context,
					QName queryPatId,
					Iterable<PatisNumber> patients)
						throws ServiceException, IOException {
				Query query = context.queryPatterns.getAsObject(queryPatId);
				return context.config.getSearcher().searchForAll(query, patients);
			}
		},

		FAIL {
			@Override
			public Iterable<SearchResult> search(ZylabPatisClient context, QName queryPatId, Iterable<PatisNumber> patients) throws ServiceException, IOException {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
			
		}
		;
		public abstract Iterable<SearchResult> search(
				ZylabPatisClient context,
				QName queryPatId,
				Iterable<PatisNumber> patients)
					throws ServiceException, IOException;
	}
	
	private enum OutputFormat {
		JSON("application/json") {
			@Override
			public void doOutputResults(
					Appendable writer, Iterable<SearchResult> results)
					throws IOException {
				ArrayList<SearchResult> tmp = new ArrayList<>();
				for (SearchResult r : results) {
					tmp.add(r);
				}
				new Gson().toJson(tmp.toArray(), writer);
			}

			@Override
			public void outputQueryList(
					HttpServletResponse response, Iterable<QName> queryIDs)
					throws IOException {
				response.setContentType(null);
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		},
		;

		private OutputFormat(final MediaType format_) {
			format = format_;
		}

		private OutputFormat(final String s_format_) {
			this(MediaType.parse(s_format_));
		}
		
		public final MediaType format;
		
		protected abstract void doOutputResults(
				Appendable writer, Iterable<SearchResult> results) throws IOException;
		
		public void outputResults(HttpServletResponse response,
				Iterable<SearchResult> results) throws IOException {
			response.setContentType(format.toString());
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			StringBuilder responseContent = new StringBuilder();
			doOutputResults(responseContent, results);
			response.setContentLength(responseContent.length());
			response.getWriter().append(responseContent);
			response.getWriter().flush();
			response.getWriter().close();
		}

		public abstract void outputQueryList(HttpServletResponse response,
				Iterable<QName> queryIDs) throws IOException;
	}

	private static final ContentTypeNegotiator<OutputFormat> contentTypeRegistry =
			new ContentTypeNegotiator<>();
	static {
		contentTypeRegistry.put(MediaType.parse("application/json"), OutputFormat.JSON);
		contentTypeRegistry.put(MediaType.getAny(), OutputFormat.JSON);
	}

	private static final EnumMap<QueryRepresentation_Old, EnumMap<SearcherCapability, Strategy>>
			strategies;
	static {
		strategies = new EnumMap<>(QueryRepresentation_Old.class);
		for (QueryRepresentation_Old repr : QueryRepresentation_Old.values()) {
			strategies.put(repr, new EnumMap<SearcherCapability, Strategy>(SearcherCapability.class));
		}
		
		strategies.get(QueryRepresentation_Old.STRING).put(SearcherCapability.STRING, Strategy.STRING);
		strategies.get(QueryRepresentation_Old.STRING).put(SearcherCapability.BOTH, Strategy.STRING);
		strategies.get(QueryRepresentation_Old.BOTH).put(SearcherCapability.STRING, Strategy.STRING);
		
		strategies.get(QueryRepresentation_Old.OBJECT).put(SearcherCapability.OBJECT, Strategy.OBJECT);
		strategies.get(QueryRepresentation_Old.OBJECT).put(SearcherCapability.BOTH, Strategy.OBJECT);
		strategies.get(QueryRepresentation_Old.BOTH).put(SearcherCapability.OBJECT, Strategy.OBJECT);
		
		strategies.get(QueryRepresentation_Old.BOTH).put(SearcherCapability.BOTH, Strategy.OBJECT);
	}

	private static final ForkJoinPool taskPool = new ForkJoinPool();

	private final QueryProviderRegistry queryPatterns = new QueryProviderRegistry();
	private Config config = null;

	/**
	 * Construct a Zylab Patis Search client using {@code config_} as source of
	 * configuration settings.
	 * 
	 * @param config_	{@link Config} from which to read properties such as 
	 * 		the {@link Searcher} to use and stored concept queryPatterns.
	 */
	public ZylabPatisClient(final Config config_) {
		config = config_;
		queryPatterns.registerProvider(config.getConfiguredQueries());
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
					InitialParameters.CONFIG_FILE.servletInitParamKey);
			if(s_file == null) {
				throw new UnavailableException(String.format(
						"Specify config file in init parameter (%s).",
						InitialParameters.CONFIG_FILE.servletInitParamKey));
			}
			config = Config.init(this.getServletContext().getResourceAsStream(s_file), taskPool);
			queryPatterns.registerProvider(config.getConfiguredQueries());
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// URL structure: {protocol}://{server}/{context}/{conceptQueryID}/results?patisNr="{patisNr}...}
/*		StringBuilder w = new StringBuilder();
		w.append("<html><body><p>Hello world.</p></body></html>");
		resp.setContentType("text/html;charset=UTF-8");
		resp.setContentLength(w.length());
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().append(w).flush();
		resp.getWriter().close();
		*/
		UriParameters requestType = UriParameters.requestedParameter(req);
		switch (requestType) {
			case QUERY_PATTERN_URI_PART: {
				List<QName> queryPatternIDs = new LinkedList<>();
				queryPatternIDs.add(
					UriParameters.QUERY_PATTERN_URI_PART.getResourceQName(this, req));
				queryPatternIDs.addAll(RequestParameters.QUERY_PATTERN.<QName>get(this, req));

				List<PatisNumber> patients = new LinkedList<>();
				patients.addAll(RequestParameters.PATIS_NUMBER.<PatisNumber>get(this, req));
				
				if(queryPatternIDs.size() != 1) {
					throw new ServletException("Querying multiple patterns not yet implemented.");
				}
				
				QName query = queryPatternIDs.get(0);
//				try {
//					Iterable<SearchResult> result = strategies.get(QueryRepresentation.determineRepresentation(this, query))
//							.get(SearcherCapability.determineCapability(config.getSearcher()))
//							.search(this, query, patients);
//					OutputFormat.JSON.outputResults(resp, result);
//				} catch (ServiceException | IOException ex) {
//					throw new ServletException(ex);
//				}

				resp.setContentType("text/html;charset=UTF-8");
//				Writer w = resp.getWriter();
				StringBuilder w = new StringBuilder();
				w.append("<html><body>"
						+ "<H1>Not yet implemented</H1>"
						+ "<p>Query IDs requested:<br/><dl>");
				for (QName qName : queryPatternIDs) {
					w.append("<dt>");
					w.append(qName.toString());
					w.append("</dt><dd>");
					if(queryPatterns.hasString(qName)) {
						w.append(queryPatterns.getAsString(qName));
					} else if (queryPatterns.hasObject(qName)) {
						w.append("<i>!! OBJECT cannot be represented as text !!</i>");
					} else {
						w.append("<i>!! Unknown Query ID !!</i>");
					}
					w.append("</dd>");
				}
				w.append("</dl></p></body></html>");
				resp.setContentLength(w.length());
				resp.getWriter().append(w).close();
//				w.close();
			}

				break;
			case LIST_QUERIES: {
				resp.setContentType("text/html;charset=UTF-8");
//				Writer w = resp.getWriter();
				StringBuilder w = new StringBuilder();
				w.append("<html><body>\n"
						+ "<H1>Available queries</H1>\n"
						+ "<p><dl>\n");
				for (QName qName : queryPatterns.getQueryIds()) {
					w.append("\t<dt>");
					w.append(qName.toString());
					w.append("</dt>\t<dd>");
					if(queryPatterns.hasString(qName)) {
						w.append(queryPatterns.getAsString(qName));
					} else if (queryPatterns.hasObject(qName)) {
						w.append("<i>!! OBJECT cannot be represented as text !!</i>");
						w.append(queryPatterns.getAsObject(qName).toString());
					} else {
						w.append("<i>!! Unknown Query ID !!</i>");
					}
					w.append("</dd>\n");
				}
				w.append("</dl></p>");
				w.append("</body></html>");
				resp.setContentLength(w.length());
				resp.getWriter().append(w).close();
//				w.close();
					
			}
				break;
			case DEMO: {
				List<QName> queryPatternIDs = new LinkedList<>();
//				queryPatternIDs.add(
//					UriParameters.QUERY_PATTERN_URI_PART.getResourceQName(this, req));
				queryPatternIDs.addAll(RequestParameters.QUERY_PATTERN.<QName>get(this, req));

				List<PatisNumber> patients = new LinkedList<>();
				patients.addAll(RequestParameters.PATIS_NUMBER.<PatisNumber>get(this, req));
				
				QName query = queryPatternIDs.get(0);
//				try {
//					Iterable<SearchResult> result = strategies.get(QueryRepresentation.determineRepresentation(this, query))
//							.get(SearcherCapability.determineCapability(config.getSearcher()))
//							.search(this, query, patients);
//					OutputFormat.JSON.outputResults(resp, result);
//				} catch (ServiceException | IOException ex) {
//					throw new ServletException(ex);
//				}

				resp.setContentType("text/html;charset=UTF-8");
//				Writer w = resp.getWriter();
				StringBuilder w = new StringBuilder();
				w.append("<html><body>\n"
						+ "<H1>Not yet implemented</H1>\n"
						+ "<p>Query IDs requested:<br/><dl>\n");
				for (QName qName : queryPatternIDs) {
					w.append("\t<dt>");
					w.append(qName.toString());
					w.append("</dt>\t<dd>");
					if(queryPatterns.hasString(qName)) {
						w.append(queryPatterns.getAsString(qName));
					} else if (queryPatterns.hasObject(qName)) {
						w.append("<i>!! OBJECT cannot be represented as text !!</i>");
						w.append(queryPatterns.getAsObject(qName).toString());
					} else {
						w.append("<i>!! Unknown Query ID !!</i>");
					}
					w.append("</dd>\n");
				}
				w.append("</dl></p>");
				w.append("<p>Patisnummers requested:<br/><ul>");
				for (PatisNumber patisNumber : patients) {
					w.append("<li>");
					w.append(patisNumber.value);
					w.append("</li>");
							
				}
				w.append("</ul></p></body></html>");
				resp.setContentLength(w.length());
				resp.getWriter().append(w).close();
//				w.close();
			}

				break;
			default:
				String msg = 
						String.format(
							"Cannot satisfy request %s; URI pattern not known to %s servlet",
							req.getRequestURL(),
							ZylabPatisClient.class.getName());
				resp.sendError(400, msg);
		}
//		throw new UnsupportedOperationException("Not yet implemented");
	}


	

	public static void main(String[] args) {
		
		
//		String sQuery = PreconstructedQueries.instance().getQuery(PreconstructedQueries.LocalParts.METASTASIS).toString("contents");
		CoreParser parser = new CoreParser("content", new StandardAnalyzer(Version.LUCENE_41));
		String sQuery = "metastasis~ stage~\"~10";
//		Query query = parser.parse(null);
		try {
			Directory indexDir = FSDirectory.open(
					new File("/home/kasper2/mnt/aida/indexes/Zylab_test-20130415-02"));
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
			TopDocs result = searcher.search(
					new PreconstructedQueries.Provider().getAsObject(
						PreconstructedQueries.LocalParts.METASTASIS.getID()),
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
