// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.NameSpaceResolver;
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
import org.apache.tika.mime.MediaType;
import org.jdom2.Namespace;

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
				Pattern.compile("^[\\p{Digit}]+$"),
				"\"%s\" is not a wellformed number") {
			
			@Override
			protected PatisNumber convertValue(
					final ZylabPatisClient context, final String value) {
				return PatisNumber.create(value);
			}
		},
		QUERY_PATTERN("queryPatternID[]",
				OptionBuilder.withArgName("qname")
							.hasArg()
							.withDescription(
								"Specify the qname of the pattern to use; "
								+ "specifying multiple qnames results in "
								+ "the documents matching any of the specified "
								+ "patterns.")
							.create("queryPatternID"),
				Pattern.compile("^([\\p{Alpha}[_]][\\p{Alnum}[_\\-.]]*:)?([\\p{Alpha}[_]][\\p{Alnum}[_\\-.]]*)$"),
				"\"%s\" is not a valid query identifier (QName)") {
			
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
		
		public /*@Nullable*/ final String httpKey;
		private /*@Nullable*/ final Option option;
		private /*@Nullable*/ final Pattern wellFormedValue;
		private /*@Nullable*/ final String wfErrMsg;

		/**
		 * Create a HTTPRequest and commandline parameter.
		 * 
		 * @param httpKey_
		 * @param option_
		 * @param wellFormedValue_
		 * @param wfErrMsg_ 
		 */
		private RequestParameters(
				final String httpKey_,
				final Option option_,
				final Pattern wellFormedValue_,
				final String wfErrMsg_) {
			this.httpKey = httpKey_;
			this.option = option_;
			this.wellFormedValue = wellFormedValue_;
			this.wfErrMsg = wfErrMsg_;
		}
	
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
			List<T> result = new ArrayList<>(values.length);
			for (String s : values) {
				assertClean(s);
				result.add(this.<T>convertValue(context, s));
			}
			return result;
		}
		
		private Option getOption() {
			return option;
		}
		
		private void assertClean(String s) {
			if(!wellFormedValue.matcher(s).matches()) {
				throw new IllegalArgumentException(String.format(wfErrMsg, s));
			}
		}
	}

	private enum UriParameters {
		QUERY_PATTERN_URI_PART(
				Pattern.compile("(.*/queryPattern/)(.*?)/"),
				Pattern.compile("^([\\p{Alpha}[_]][\\p{Alnum}[_\\-.]]*)$"),
				"\"%s\" is not a well formed local part of a QName."),

		INVALLID_PATTERN(null, null, null)
		;
		
		public /*@Nullable*/ final Pattern uriPart;
		private /*@Nullable*/ final Pattern wellFormedValue;
		private /*@Nullable*/ final String wfErrMsg;

		private UriParameters(
				final Pattern uriPart_,
				final Pattern wellFormedValue_,
				final String wfErrMsg_) {
			this.uriPart = uriPart_;
			this.wellFormedValue = wellFormedValue_;
			this.wfErrMsg = wfErrMsg_;
		}

		public static UriParameters requestedParameter (HttpServletRequest request) {
			for (UriParameters param : values()) {
				if(param.uriPart.matcher(request.getPathInfo()).matches()) {
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
			assertCleanLocalPart(localPart);
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

		private void assertCleanLocalPart(String localPart) {
			if (!wellFormedValue.matcher(localPart).matches()) {
				throw new IllegalArgumentException(
						String.format(wfErrMsg, localPart));
			}
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
		QNAME("^([\\p{Alpha}[_]][\\p{Alnum}[_\\-.]]*:)?([\\p{Alpha}[_]][\\p{Alnum}[_\\-.]]*)$",
				"\"%s\" is not a valid QName"),
		PATTIS_NR("^[\\p{Digit}]+$",
				"\"%s\" is not a wellformed number")
		;
		private final Pattern pattern;
		private final String errMsg;

		private InputSanitation(final String regExpr, final String errMsg_) {
			pattern = Pattern.compile(regExpr);
			errMsg = errMsg_;
		}

		public void assertClean(String s) {
			if(pattern.matcher(s).matches()) {
				throw new IllegalArgumentException(String.format(errMsg, s));
			}
		}
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
			config = Config.init(new File(s_file), taskPool);
			queryPatterns.registerProvider(config.getConfiguredQueries());
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// URL structure: {protocol}://{server}/{context}/{conceptQueryID}/results?patisNr="{patisNr}...}
		UriParameters requestType = UriParameters.requestedParameter(req);
		switch (requestType) {
			case QUERY_PATTERN_URI_PART:
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
				try {
					Iterable<SearchResult> result = strategies.get(QueryRepresentation.determineRepresentation(this, query))
							.get(SearcherCapability.determineCapability(config.getSearcher()))
							.search(this, query, patients);
					OutputFormat.JSON.outputResults(resp, result);
				} catch (ServiceException | IOException ex) {
					throw new ServletException(ex);
				}

				Writer w = resp.getWriter();
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
				w.close();

				break;
			default:
				String msg = 
						String.format(
							"Cannot satisfy request %s; URI pattern not known to %s servlet",
							req.getRequestURL(),
							ZylabPatisClient.class.getName());
				resp.getWriter().append(msg).close();
				throw new ServletException(msg);
		}
//		throw new UnsupportedOperationException("Not yet implemented");
	}

	private enum QueryRepresentation {
		STRING(true, false),
		OBJECT(false, true),
		BOTH(true, true),
		MIXED(false, false),
		UNKNOWN_QUERY(false, false);

		private final boolean asString;
		private final boolean asObject;
		
		private QueryRepresentation(
				final boolean asString_, final boolean asObject_) {
			asString = asString_;
			asObject = asObject_;
		}

		public static QueryRepresentation determineRepresentation(
				ZylabPatisClient context, Iterable<QName> queries) {
			boolean allAsString = true;
			boolean allAsObject = true;

			for (QName q : queries) {
				QueryRepresentation r = determineRepresentation(context, q);
				if(r.equals(UNKNOWN_QUERY)) {
					return UNKNOWN_QUERY;
				}
				allAsString &= r.asString;
				allAsObject &= r.asObject;
			}

			if(allAsString && allAsObject) {
				return BOTH;
			} else if (allAsObject) {
				return OBJECT;
			} else if (allAsString) {
				return STRING;
			} else {
				return MIXED;
			}
		}
		
		public static QueryRepresentation determineRepresentation(
				ZylabPatisClient context, QName query) {
			boolean asString = context.queryPatterns.hasString(query);
			boolean asObject = context.queryPatterns.hasObject(query);

			if(asString && asObject) {
				return BOTH;
			} else if (asObject) {
				return OBJECT;
			} else if (asString) {
				return STRING;
			} else {
				return UNKNOWN_QUERY;
			}
		}
	}

	private enum SearcherCapability {
		STRING,
		OBJECT,
		BOTH,
		NONE;

		public static SearcherCapability determineCapability(Searcher s) {
			if(s.supportsLuceneQueryObjects() && s.supportsStringQueries()) {
				return BOTH;
			} else if (s.supportsLuceneQueryObjects()) {
				return OBJECT;
			} else if (s.supportsStringQueries()) {
				return STRING;
			} else {
				return NONE;
			}
		}
	}

	private enum Strategy { 
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
	
	private static final EnumMap<QueryRepresentation, EnumMap<SearcherCapability, Strategy>>
			strategies;
	static {
		strategies = new EnumMap<>(QueryRepresentation.class);
		for (QueryRepresentation repr : QueryRepresentation.values()) {
			strategies.put(repr, new EnumMap<SearcherCapability, Strategy>(SearcherCapability.class));
		}
		
		strategies.get(QueryRepresentation.STRING).put(SearcherCapability.STRING, Strategy.STRING);
		strategies.get(QueryRepresentation.STRING).put(SearcherCapability.BOTH, Strategy.STRING);
		strategies.get(QueryRepresentation.BOTH).put(SearcherCapability.STRING, Strategy.STRING);
		
		strategies.get(QueryRepresentation.OBJECT).put(SearcherCapability.OBJECT, Strategy.OBJECT);
		strategies.get(QueryRepresentation.OBJECT).put(SearcherCapability.BOTH, Strategy.OBJECT);
		strategies.get(QueryRepresentation.BOTH).put(SearcherCapability.OBJECT, Strategy.OBJECT);
		
		strategies.get(QueryRepresentation.BOTH).put(SearcherCapability.BOTH, Strategy.OBJECT);
	}

	private enum OutputFormat {
		JSON(MediaType.set(MediaType.application("json"), MediaType.application("ANY" /* should be '*' */))) {
			@Override
			public void outputResults(
					HttpServletResponse response, Iterable<SearchResult> results)
					throws IOException {
				ArrayList<SearchResult> tmp = new ArrayList<>();
				for (SearchResult r : results) {
					tmp.add(r);
				}
				new Gson().toJson(tmp.toArray(), response.getWriter());
			}
		},
		;
		
		private final Set<MediaType> supported;
		
		private OutputFormat(final Set<MediaType> supportedTypes_){
			supported = supportedTypes_;
		}
		
		public static OutputFormat getAccepted(String header) {
			if(true) {
				throw new UnsupportedOperationException("MediaType does not support '*'");
			} else {
				SortedMap<Double, MediaType> clientAccepts = new TreeMap<>(Collections.reverseOrder());
				for (String s_mediatype : header.split(",\\p{Space}*")) {
					MediaType mediatype = MediaType.parse(s_mediatype);
					clientAccepts.put(
							mediatype.getParameters().containsKey("q") ?
							Double.valueOf(mediatype.getParameters().get("q")) :
							1d,
							mediatype);
				}
				for (MediaType acceptedType : clientAccepts.values()) {
					
				}
			}
			throw new UnsupportedOperationException("Not yet implemented");	
		} 
		
		public abstract void outputResults(HttpServletResponse response,
				Iterable<SearchResult> results) throws IOException;
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
		return RequestParameters.PATIS_NUMBER.<PatisNumber>get(this, request);
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
		return RequestParameters.PATIS_NUMBER.<PatisNumber>get(this, cmd);
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
			InputSanitation.PATTIS_NR.assertClean(number);
			result.add(PatisNumber.create(number));
		}

		return result;
	}

	/**
	 * Read the requested query patterns from {@code request}.  The query 
	 * wellFormedValue id (as a {@link QName}) in the request URI and any request
	 * parameter named {@link RequestParameters#QUERY_PATTERN} are added to
	 * the result.
	 * 
	 * @param request	{@link HttpServletRequest} used to call this 
	 * 		{@link HttpServlet}; {@code getRequestedQueryPattern} will sanitise
	 * 		the query wellFormedValue ids in {@code request}.
	 * 
	 * @throws IllegalArgumentException	when any of the 
	 */
	private List<QName> getRequestedQueryPattern(HttpServletRequest request) 
			throws NoSuchElementException, IllegalArgumentException {
		List<QName> result = new LinkedList<>();
		
		String path = request.getPathInfo();
//		Matcher m = RequestParameters.QUERY_PATTERN.uriPart.matcher(path);
//		if(m.matches()) {
//			String uriMatch = m.group(1);
//			InputSanitation.LOCAL_PART.assertCleanURL(uriMatch);
//			config.getNamespaces().
//			request.
//			if(uriMatch != null && !uriMatch.isEmpty()) {
//				result.add(sanitiseQName(uriMatch));
//			}
//		}
			
		String args[] = request.getParameterValues(RequestParameters.QUERY_PATTERN.httpKey);
		for (String arg : args) {
			result.add(sanitiseQName(arg));
		}
		
		return result;
	}
	
	private List<QName> getRequestedQueryPattern(CommandLine cmd) 
			throws NoSuchElementException, IllegalArgumentException {
		String qnames[] = cmd.getOptionValues(
				RequestParameters.QUERY_PATTERN.getOption().getLongOpt());
		List<QName> result = new ArrayList<>(qnames.length);
		for (String str : qnames) {
			result.add(sanitiseQName(str));
		}
		return result;
	}

	private QName sanitiseQName(final String qname) 
			throws IllegalArgumentException, NoSuchElementException {
		InputSanitation.QNAME.assertClean(qname);
		return config.getNamespaces().createQName(qname);
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
