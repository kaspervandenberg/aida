// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.config;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.EnsuresNonNullIf;
import checkers.nullness.quals.MonotonicNonNull;
import checkers.nullness.quals.Nullable;
import checkers.nullness.quals.NonNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import nl.maastro.eureca.aida.search.zylabpatisclient.LocalLuceneSearcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryProvider;
import nl.maastro.eureca.aida.search.zylabpatisclient.Searcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.WebserviceSearcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.input.PatisReader;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.DualRepresentationQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.DynamicAdapter;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTree;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQueryBase;
import nl.maastro.ad.clinisearch.axis.services.SearcherWS.SearcherWS;
import nl.maastro.ad.clinisearch.axis.services.SearcherWS.SearcherWSServiceLocator;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatientProvider;
import nl.maastro.eureca.aida.search.zylabpatisclient.input.EmdPatientReader;
import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Config {
	private interface XPathOp {
		boolean nodeExists(Content context);
		
		String getAttrValue(Content context);
		
		Content getFirstNode(Content context); 

		List<? extends Content> getAllNodes(Content context);

		List<Attribute> getAllAttributes(Content context);

		void setVariable(String name, Object value) 
				throws IllegalArgumentException;
	}

	private static abstract class XPathOpBaseImpl<T extends /*@NonNull*/ Object> implements XPathOp {
		private final Map<String, Object> variables;
		
		protected XPathOpBaseImpl() {
			variables = Collections.emptyMap();
		}

		protected XPathOpBaseImpl(final Map<String, Object> variables_) {
			variables = variables_;
		}

		protected Map<String, Object> getVariables() {
			return variables;
		}
		
		protected abstract XPathExpression<T> getExpr();
		
		@Override
		public boolean nodeExists(Content context) {
			return !getExpr().evaluate(context).isEmpty();
		}

		@Override
		public String getAttrValue(Content context) {
			throw new IllegalStateException("XPath expression is not an attribute");
		}
		
		@Override
		public Content getFirstNode(Content context) {
			throw new IllegalStateException("XPath expression is not a Node");
		}
		
		@Override
		public List<? extends Content> getAllNodes(Content context) {
			throw new IllegalStateException("XPath expression is not a Node");
		}

		@Override
		public List<Attribute> getAllAttributes(Content context) {
			throw new IllegalStateException("XPath expression is not an attribute");
		}

		@Override
		public void setVariable(String name, Object value) throws IllegalArgumentException {
			if(variables.containsKey(name)) {
				getExpr().setVariable(name, value);
			} else {
				throw new IllegalArgumentException(String.format("No variable named %s", name));
			}
		}
	}
	
	private static class XPathOpNodeImpl extends XPathOpBaseImpl<Element> {
		
		private final String s_expr;
		private transient @MonotonicNonNull XPathExpression<Element>  expr = null;
		
		public XPathOpNodeImpl(final String s_expr_) {
			this.s_expr = s_expr_;
		}
		
		public XPathOpNodeImpl(final String s_expr_, final Map<String, Object> variables_) {
			super(variables_);
			this.s_expr = s_expr_;
		}

		@Override
		public Content getFirstNode(Content context) {
			return getExpr().evaluateFirst(context);
		}

		@Override
		public List<Element> getAllNodes(Content context) {
			return getExpr().evaluate(context);
		}

		@EnsuresNonNull("expr")
		@Override
		protected XPathExpression<Element> getExpr() {
			if(expr == null) {
				expr = compileExpr(s_expr, getVariables(), Filters.element());
			}
			return expr;
		}

	}

	private static class XPathOpAttrImpl extends XPathOpBaseImpl<Attribute> {
		private final String s_expr;
		private transient @MonotonicNonNull XPathExpression<Attribute>  expr = null;

		public XPathOpAttrImpl(String s_expr_) {
			this.s_expr = s_expr_;
		}

		public XPathOpAttrImpl(String s_expr_, final Map<String, Object> variables_) {
			super(variables_);
			this.s_expr = s_expr_;
		}

		@Override
		public String getAttrValue(Content context) {
			return getExpr().evaluateFirst(context).getValue();
		}

		@Override
		public List<Attribute> getAllAttributes(Content context) {
			return getExpr().evaluate(context);
		}

		@EnsuresNonNull("expr")
		@Override
		protected XPathExpression<Attribute> getExpr() {
			if(expr == null) {
				expr = compileExpr(s_expr, getVariables(), Filters.attribute());
			}
			return expr;
		}
	}

	private static class XPathOpCompoundImpl implements XPathOp {
		private final List<XPathOp> ops;
			
		private XPathOpCompoundImpl(XPathOp... ops_) {
			ops = Arrays.asList(ops_);
		}

		@Override
		public boolean nodeExists(Content context) {
			return ops.get(ops.size() -1).nodeExists(
					getContext(context, ops.subList(0, ops.size() -1)));
		}

		@Override
		public String getAttrValue(Content context) {
			return ops.get(ops.size() -1).getAttrValue(
					getContext(context, ops.subList(0, ops.size() -1)));
		}

		@Override
		public Content getFirstNode(Content context) {
			return ops.get(ops.size() -1).getFirstNode(
					getContext(context, ops.subList(0, ops.size() -1)));
		}

		@Override
		public List<? extends Content> getAllNodes(Content context) {
			return ops.get(ops.size() -1).getAllNodes(
					getContext(context, ops.subList(0, ops.size() -1)));
		}

		@Override
		public List<Attribute> getAllAttributes(Content context) {
			return ops.get(ops.size() -1).getAllAttributes(
					getContext(context, ops.subList(0, ops.size() -1)));
		}

		@SuppressWarnings("serial")
		@Override
		public void setVariable(String name, Object value) throws IllegalArgumentException {
			final List<IllegalArgumentException> suppressedExceptions = new LinkedList<>();
			
			for (XPathOp pathComponent : ops) {
				try {
					pathComponent.setVariable(name, value);
				} catch (IllegalArgumentException ex) {
					suppressedExceptions.add(ex);
				}
			}
			if(suppressedExceptions.size() >= ops.size()) {
				throw new IllegalArgumentException(
						String.format("None of the component XPath accepted %s", name.toString()),
						new Exception("Multiple causes") {
							@SuppressWarnings("nullness")
							final List<IllegalArgumentException> causes = new ArrayList<>(suppressedExceptions);
						});
			}
		}


		private Content getContext(Content context, List<XPathOp> pathExpr) {
			if(pathExpr.size() <= 1) {
				return pathExpr.get(0).getFirstNode(context);
			} else {
				return pathExpr.get(pathExpr.size() -1).getFirstNode(
						getContext(context, pathExpr.subList(0, pathExpr.size() -1)));
			}
		}
		
	}
			
	private class QueryPatterns implements QueryProvider {
		final Map<QName, Query> patterns;

		public QueryPatterns() {
			List<? extends Content> nodes = XPaths.QUERY.getAllNodes(getConfigDoc());
			patterns = new HashMap<>(nodes.size());
			for (Content n : nodes) {
				final QName id = readId(getNamespaces(), getConfigDoc(), XPaths.ID);
				final String value = n.getValue();
				patterns.put(id, new StringQueryBase() {

					@Override
					public String getRepresentation() {
						return value;
					}

					@Override
					public QName getName() {
						return id;
					}
				});
			}
		}
		
		@Override
		public Collection<QName> getQueryIds() {
			return Collections.unmodifiableSet(patterns.keySet());
		}

		@SuppressWarnings("nullness")
		@EnsuresNonNullIf(expression = "get(#1)", result = true)
		@Override
		public boolean provides(final QName id) {
			return patterns.containsKey(id);
		}

		@Override
		public Query get(final QName id) {
			if(!provides(id)) {
				throw new NoSuchElementException(
						String.format("No query with id %s configured.", id.toString()));
			}
			@SuppressWarnings("nullness")
			@NonNull Query result = patterns.get(id);
			return result;
		}
		

		@Override
		@Deprecated
		public boolean hasString(QName id) {
			return patterns.containsKey(id);
		}

		@Override
		@Deprecated
		public boolean hasObject(QName id) {
			return false;
		}

		@Deprecated
		@Override
		public String getAsString(QName id) throws NoSuchElementException {
			return get(id).accept(new Query.Visitor<String>() {
				@Override
				public String visit(LuceneObject element) {
					throw new IllegalStateException("Expected visiting a StringQuery.");
				}

				@Override
				public String visit(StringQuery element) {
					return element.getRepresentation();
				}

				@Override
				public String visit(ParseTree element) {
					throw new IllegalStateException("Expected visiting a StringQuery.");
				}
			});
		}

		@Override
		@Deprecated
		public org.apache.lucene.search.Query getAsObject(QName id) throws NoSuchElementException {
			throw new NoSuchElementException("Not supported yet.");
		}
	}
	
	private enum XPathImpls {
		// Node expressions
		N_BASE(XPathOpNodeImpl.class,
				"/" + NS_PREFIX + ":zylabPatisClientConfig"),
		N_INDEX(XPathOpNodeImpl.class,
				NS_PREFIX + ":index"),
		N_ABS_INDEX(XPathOpNodeImpl.class,
				N_BASE.s_expr + "/" + N_INDEX.s_expr),
		N_LOCAL_INDEX(XPathOpNodeImpl.class,
				NS_PREFIX + ":localIndex"),
		N_WEBSERVICE_INDEX(XPathOpNodeImpl.class,
				NS_PREFIX + ":webservice"),
		N_DEFAULT_FIELD(XPathOpNodeImpl.class,
				NS_PREFIX + ":defaultField"),
		N_ABS_DEFAULT_FIELD(XPathOpNodeImpl.class,
				N_BASE.s_expr + "/" + N_DEFAULT_FIELD.s_expr),
		N_RESULT_LIMIT(XPathOpNodeImpl.class,
				NS_PREFIX + ":resultLimit"),
		N_ABS_RESULT_LIMIT(XPathOpNodeImpl.class,
				N_BASE.s_expr + "/" + N_RESULT_LIMIT.s_expr),
		N_QUERY(XPathOpNodeImpl.class,
				NS_PREFIX + ":query"),
		N_ABS_QUERY(XPathOpNodeImpl.class,
				N_BASE.s_expr + "/" + N_QUERY.s_expr),
		N_PATIENTS_JSON(XPathOpNodeImpl.class,
				NS_PREFIX + ":patients"),
		N_ABS_PATIENTS_JSON(XPathOpNodeImpl.class,
				N_BASE.s_expr + "/" + N_PATIENTS_JSON.s_expr),
		N_ABS_PATIENTS_JSON_BY_ID(XPathOpNodeImpl.class,
				N_ABS_PATIENTS_JSON.s_expr + "[@concept-ref=$conceptRef]",
				new Object[][]{{"conceptRef", ""}}),

		// Attribute expressions
		A_ADDRESS(XPathOpAttrImpl.class,
				"@address"),
		A_INDEX(XPathOpAttrImpl.class,
				"@index"),
		A_FILE(XPathOpAttrImpl.class,
				"@file"),
		A_FIELD(XPathOpAttrImpl.class,
				"@field"),
		A_ABS_DEFAULT_FIELD(XPathOpAttrImpl.class,
				N_ABS_DEFAULT_FIELD.s_expr + "/" + A_FIELD.s_expr),
		A_N_HITS(XPathOpAttrImpl.class,
				"@nHits"),
		A_ABS_RESULT_LIMIT(XPathOpAttrImpl.class,
				N_ABS_RESULT_LIMIT.s_expr + "/" + A_N_HITS.s_expr),
		A_ID(XPathOpAttrImpl.class,
				"@id"),
		A_ABS_QUERY_ID(XPathOpAttrImpl.class,
				N_ABS_QUERY.s_expr + "/" + A_ID.s_expr),
		A_CONCEPT_REF(XPathOpAttrImpl.class,
				"@concept-ref"),

		// Compound paths
		C_INDEX_WEBSERVICE(XPathOpCompoundImpl.class, N_ABS_INDEX, N_WEBSERVICE_INDEX),
		C_INDEX_LOCAL(XPathOpCompoundImpl.class, N_ABS_INDEX, N_LOCAL_INDEX),
		C_WS_ADDRESS(XPathOpCompoundImpl.class, N_ABS_INDEX, N_WEBSERVICE_INDEX, A_ADDRESS),
		C_WS_INDEX(XPathOpCompoundImpl.class, N_ABS_INDEX, N_WEBSERVICE_INDEX, A_INDEX),
		C_LOCAL_FILE(XPathOpCompoundImpl.class, N_ABS_INDEX, N_LOCAL_INDEX, A_FILE),
		C_PATIENTS_JSON_FILE(XPathOpCompoundImpl.class, N_ABS_PATIENTS_JSON_BY_ID, A_FILE),
		
		;
		public final XPathOp delegate;
		public final @Nullable String s_expr;
		
		private XPathImpls(final Class<? extends XPathOp> implementation, final String s_expr_) {
			try {
				delegate = implementation.getConstructor(String.class).newInstance(s_expr_);
				s_expr = s_expr_;
			} catch (InstantiationException | NoSuchMethodException | 
					IllegalAccessException | InvocationTargetException ex) {
				throw new Error(ex);
			}
		}

		private XPathImpls(final Class<? extends XPathOp> implementation,
				String s_expr_, Object[][] variables_) {
			Map<String, Object> variables = new HashMap<>();
			for (Object[] var : variables_) {
				if((var.length == 2) && (var[0] instanceof String)) {
					variables.put((String)var[0], var[1]);
				} else {
					throw new Error(
							String.format("XPath variable incorrect %s", var.toString()));
				}
			}
			try {
				delegate = implementation.getConstructor(String.class, Map.class).
						newInstance(s_expr_, variables);
				s_expr = s_expr_;
			} catch (InstantiationException | NoSuchMethodException | 
					IllegalAccessException | InvocationTargetException ex) {
				throw new Error(ex);
			}
		}

		private XPathImpls(final Class<XPathOpCompoundImpl> implementation, XPathImpls... parts) {
			XPathOp ops[] = new XPathOp[parts.length];
			for (int i = 0; i < parts.length; i++) {
				ops[i] = parts[i].delegate;
			}
			delegate = new XPathOpCompoundImpl(ops);
			s_expr = null;
		}
	}
		
	private enum XPaths implements XPathOp {
		// Node expressions
		ROOT(XPathImpls.N_BASE),
		INDEX_WEBSERVICE(XPathImpls.C_INDEX_WEBSERVICE),
		INDEX_LOCAL(XPathImpls.C_INDEX_LOCAL),
		WS_ADDRES(XPathImpls.C_WS_ADDRESS),
		WS_INDEX(XPathImpls.C_WS_INDEX, "Zylab_test-20130415-02"),
		LOCAL_FILE(XPathImpls.C_LOCAL_FILE),
		DEFAULT_FIELD(XPathImpls.A_ABS_DEFAULT_FIELD, "content"),
		RESULT_LIMIT(XPathImpls.A_ABS_RESULT_LIMIT, "1000"),
		QUERY(XPathImpls.N_ABS_QUERY),
		ID(XPathImpls.A_ID),
		PATIS_FILE_JASON(XPathImpls.N_ABS_PATIENTS_JSON),
		CONCEPT_REF(XPathImpls.A_CONCEPT_REF),
		FILE(XPathImpls.A_FILE);

		private final XPathOp delegate;
		private final String defaultValue;

		private XPaths(XPathImpls impl) {
			delegate = impl.delegate;
			defaultValue = "";
		}

		private XPaths(XPathImpls impl, final String defaultValue_) {
			delegate = impl.delegate;
			defaultValue = defaultValue_;
		}

		@Override
		public boolean nodeExists(Content context) {
			return delegate.nodeExists(context);
		}

		@Override
		public String getAttrValue(Content context) {
			String result = delegate.getAttrValue(context);
			if(result != null && !"".equals(result)) {
				return result;
			} else {
				if(!nodeExists(context)) {
					return defaultValue;
				} else {
					return result;
				}
			}
		}

		public String getDefaultValue() {
			return this.defaultValue;
		}
		
		@Override
		public Content getFirstNode(Content context) {
			return delegate.getFirstNode(context);
		}

		@Override
		public List<? extends Content> getAllNodes(Content context) {
			return delegate.getAllNodes(context);
		}

		@Override
		public List<Attribute> getAllAttributes(Content context) {
			return delegate.getAllAttributes(context);
		}

		@Override
		public void setVariable(String name, Object value) throws IllegalArgumentException {
			delegate.setVariable(name, value);
		}
		
	}

	public enum PropertyKeys {
		VERSION("nl.maastro.eureca.aida.search.version"),
		PRECONSTRUCTED_URI(
				"nl.maastro.eureca.aida.search.zylabpatisclient.preconstructed.namespace.uri",
				"http://clinisearch.ad.maastro.nl/zylabpatis/"),
		PRECONSTRUCTED_PREFIX(
				"nl.maastro.eureca.aida.search.zylabpatisclient.preconstructed.namespace.prefix",
				"pcq"),
//		CONFIG_NS("nl.maastro.eureca.aida.search.zylabpatisclient.config.namespace.uri",  "http://search.aida.eureca.maastro.nl/zylabpatisclient/config"),
//		CONFIG_NS_PREFIX("nl.maastro.eureca.aida.search.zylabpatisclient.config.namespace.prefix", "zpsc")
		DOCUMENT_SERVER("nl.maastro.eureca.aida.search.zylabpatisclient.documentServer",
				"http://clinisearch.ad.maastro.nl:80/search/item/"),
		;

		private static @MonotonicNonNull Properties props = null;
		private final String key;
		private final String defaultValue;
		
		private PropertyKeys(final String key_) {
			this(key_, "");
		}

		private PropertyKeys(final String key_, final String defaultValue_) {
			key = key_;
			defaultValue = defaultValue_;
		}

		public String getValue() {
			@SuppressWarnings("nullness")
			@NonNull String result = getProperties().getProperty(key, defaultValue);
			return result;
		}

		@EnsuresNonNull("props")
		private static Properties getProperties() {
			if(props == null) {
				InputStream propertyFile = Config.PropertyKeys.class.getResourceAsStream(SEARCH_PROPERTY_RESOURCE);
				if(propertyFile != null) {
					props = new Properties();
					try {
						props.load(propertyFile);
					} catch (IOException ex) {
						throw new Error(ex);
					}
				} else {
					throw new Error("Unable to read property file");
				}
			}
			return props;
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger(Config.class.getName());
	private static final String NS = "http://search.aida.eureca.maastro.nl/zylabpatisclient/config";
	private static final String NS_PREFIX = "zpsc";
	private static final String SCHEMA_RESOURCE = "/zylabPatisClientConfig.xsd";
	private static final String SEARCH_PROPERTY_RESOURCE = "/search.properties";
	private static final String SPRING_BEANS_RESOURCE = "/META-INF/beans.xml";
	private static final String EMD_DATASOURCE_BEAN_ID = "emd.datasource";
	private static final DualRepresentationQuery.Visitable VISITABLE_DELEGATE =
			DualRepresentationQuery.Visitable.AS_LUCENE_OBJECT;

	
	private static @MonotonicNonNull Config singleton = null;
	
	private static @MonotonicNonNull XPathFactory xpathfactory = null;
	private static @MonotonicNonNull Collection<Namespace> xpathNamespaces = null;

	private final InputStream configStream;
	private final CommandLineParser commandline;
	private final ForkJoinPool taskPool;
	private @MonotonicNonNull Element configDoc = null;
	private @MonotonicNonNull ApplicationContext springContext = null;
	private @MonotonicNonNull NameSpaceResolver namespaces = null; 
	private @MonotonicNonNull QueryPatterns queries = null;
	private @MonotonicNonNull Map<QName, File> jsonFiles = null;

	private @MonotonicNonNull Searcher searcher = null;
	
	private Config(InputStream configStream_, CommandLineParser commandline_, ForkJoinPool taskPool_) {
		this.configStream = configStream_;
		this.commandline = commandline_;
		this.taskPool = taskPool_;
	}
	
	@EnsuresNonNull("singleton")
	public static Config init(InputStream configStream, CommandLineParser commandline_) {
		return init(configStream, commandline_, new ForkJoinPool());
	}
	
	@EnsuresNonNull("singleton")
	public static Config init(InputStream configStream, CommandLineParser commandline_, ForkJoinPool taskPool_) {
		if(singleton != null) {
			throw new IllegalStateException("Call init() exactly once.");
		}
		singleton = new Config(configStream, commandline_, taskPool_);
		return singleton;
	}
	
	public static Config instance() throws IllegalStateException {
		if(singleton == null) {
			throw new IllegalStateException(
					"Call init(…) before calling instance()");
		}
		return singleton;
	}

	public static String getHardcodedDefaultField() {
		return XPaths.DEFAULT_FIELD.getDefaultValue();
	}

	public static DualRepresentationQuery.Visitable getDefaultVisitableDelegate() {
		return VISITABLE_DELEGATE;
	}

	@EnsuresNonNull("searcher")
	public Searcher getSearcher() throws ServiceException, IOException {
		if(searcher == null) {
			String defaultField = XPaths.DEFAULT_FIELD.getAttrValue(getConfigDoc());
			int maxHits = Integer.parseInt(XPaths.RESULT_LIMIT.getAttrValue(getConfigDoc()));
			
			if(useWebservice()) {
				SearcherWSServiceLocator wsLocator = new SearcherWSServiceLocator();
					SearcherWS webservice = wsLocator.getSearcherWS(getWebserviceAddress());
					searcher = new WebserviceSearcher(webservice, 
							XPaths.WS_INDEX.getAttrValue(getConfigDoc()),
							defaultField, maxHits, taskPool);
			} else {
				searcher = new LocalLuceneSearcher(getLocalIndexFile(), defaultField, maxHits, taskPool, new DynamicAdapter());
			}
		}
		return searcher;
	}

	@EnsuresNonNull("queries")
	public QueryProvider getConfiguredQueries() {
		if(queries == null) {
			queries = this.new QueryPatterns();
		}
		return queries;
	}

	@EnsuresNonNull("namespaces")
	public NameSpaceResolver getNamespaces() {
		if(namespaces == null) {
			namespaces = NameSpaceResolver.createDefault();
			try {
				namespaces.addAll(getXpathNamespaces());
				namespaces.addAll(
					XPaths.ROOT.getFirstNode(getConfigDoc()).getNamespacesInScope());
			} catch (URISyntaxException ex) {
				throw new Error(
						String.format("Hardcoded URI %s not well formed", NS), ex);
			}
		}
		return namespaces;
	}

	public String getDefaultField() {
		String defaultField = XPaths.DEFAULT_FIELD.getAttrValue(getConfigDoc());
		return defaultField;
	}

	public URI getDocumentServer() {
		try {
			return new URI(PropertyKeys.DOCUMENT_SERVER.getValue());
		} catch (URISyntaxException ex) {
			throw new Error(ex);
		}
	}

	public PatientProvider getPatients() {
		String[] beans = getSpringContext().getBeanNamesForType(PatientProvider.class);
		if (beans.length >= 1) {
			PatientProvider firstPatientProvider = 
					getSpringContext().getBean(beans[0], PatientProvider.class);
			if (firstPatientProvider instanceof EmdPatientReader) {
				initEmdDataSource();
			}
			return firstPatientProvider;
		} else {
			throw new Error(new IllegalStateException("No patients configured"));
		}
	}

	public Map<PatisNumber, ConceptFoundStatus> getPatients(QName concept) {
		File f = getPatientsJsonFile(concept);
		PatisReader p = new PatisReader();
		try {
			try (FileReader in = new FileReader(f)) {
				return p.readFromJSON(in);
			} catch (PatisReader.ParseFailedException ex) {
				throw new Error(String.format(
						"Failed to parse %s\nCause: %s",
						f, ex.getMessage()),
						ex);
			} catch (FileNotFoundException ex) {
				throw new Error(String.format(
						"Configured file of patisnumbers %s not found.",
						f.getPath()),
						ex);
			}
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}

	private static Document parseXml(InputStream configStream) {
		try {
			Document doc = getParser().build(configStream);
			return doc;
		} catch (JDOMException | IOException ex) {
			throw new Error(ex);
		}
	}

	private static SAXBuilder getParser() {
		@Nullable InputStream schemaStream = Config.class.getResourceAsStream(SCHEMA_RESOURCE);
		if (schemaStream == null) {
			throw new Error ("Cannot read schema");
		}
		Source schemaSource = new StreamSource(schemaStream);
		
		try {
			Schema  schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
					newSchema(schemaSource);
			SAXBuilder result = new SAXBuilder(new XMLReaderSchemaFactory(schema));
					
			return result;
		
		} catch (SAXException ex) {
			throw new Error(ex);
		}
	}

	@EnsuresNonNull("xpathfactory")
	private static XPathFactory getXPathFactory() {
		if(xpathfactory == null) {
			xpathfactory = XPathFactory.instance();
		}
		return xpathfactory;
	}

	@EnsuresNonNull("xpathNamespaces")
	private static Collection<Namespace> getXpathNamespaces() {
		if(xpathNamespaces == null) {
			xpathNamespaces = new ArrayDeque<>(1);
			xpathNamespaces.add(Namespace.getNamespace(NS_PREFIX, NS));
		}
		return xpathNamespaces;
	}
	
	private static <T extends /*@NonNull*/ Object> XPathExpression<T> compileExpr(
			String expr, Map<String, Object> variables, Filter<T> filter) {
		return getXPathFactory().compile(expr, filter, variables, getXpathNamespaces());
	}

	@EnsuresNonNull("configDoc")
	private Element getConfigDoc() {
		if(configDoc == null) {
			configDoc = parseXml(configStream).getContent(Filters.element()).get(0);
		}
		return configDoc;
	}

	private ApplicationContext getSpringContext()
	{
		if (springContext == null) {
			springContext = new ClassPathXmlApplicationContext(SPRING_BEANS_RESOURCE);
		}
		return springContext;
	}
	
	private URL getWebserviceAddress() {
		String addr = XPaths.WS_ADDRES.getAttrValue(getConfigDoc());
		try {
			if(addr != null) {
					return new URL(addr);
			} else {
				return new URL(new SearcherWSServiceLocator().getSearcherWSAddress());
			}
		} catch (MalformedURLException ex) {
			throw new Error(String.format("Webservice URL (%s) malformed", addr),
					ex);
		}
	}

	private File getLocalIndexFile() {
		return getFile(getConfigDoc(), XPaths.LOCAL_FILE, 
				"Local index URI (%s) malformed",
				"Local index file is not configured.");
	}

	@EnsuresNonNull("jsonFiles")
	private File getPatientsJsonFile(QName concept) {
		if(jsonFiles == null) {
			List<? extends Content> nodes = XPaths.PATIS_FILE_JASON.getAllNodes(getConfigDoc());
			jsonFiles = new HashMap<>(nodes.size());
			for (Content n : nodes) {
				QName id = readId(getNamespaces(), n, XPaths.CONCEPT_REF);
				File f = getFile(n, XPaths.FILE,
						"Json file of patients URI (%s) malforme.d",
						"Json file of ptient not configued.");
				jsonFiles.put(id, f);
			}
		}
		File result = jsonFiles.get(concept);
		if (result != null) {
			return result;
		} else {
			throw new NoSuchElementException(String.format(
					"No JSON file configured for concept %s.",
					concept));
		}
		
	}

	
	private void initEmdDataSource() {
		try {
			DriverManagerDataSource emdDataSource = getSpringContext().getBean(EMD_DATASOURCE_BEAN_ID, DriverManagerDataSource.class);
			initEmdUsername(emdDataSource);
			initEmdPassword(emdDataSource);
		} catch (BeanNotOfRequiredTypeException ex) {
			LOGGER.log(
					Level.WARNING, 
					String.format(
							"%s bean is not a DriverManagerDataSource; "
							+ "cannot set username and password; "
							+ "continuing without setting username and password.",
							EMD_DATASOURCE_BEAN_ID),
					ex);
		} catch (NoSuchBeanDefinitionException ex) {
			LOGGER.log(
					Level.WARNING,
					String.format(
							"No spring bean named %s configured; "
							+ "cannot set username and password for datasource EMD; "
							+ "continuing without setting username and password.",
							EMD_DATASOURCE_BEAN_ID),
					ex);
		}
	}

	
	private void initEmdUsername(DriverManagerDataSource emdDatasource) {
		if (commandline.isEmdUsernameSpecified()) {
			emdDatasource.setUsername(commandline.getEmdUsername());
			return;
		} 

		String configuredUsername = emdDatasource.getUsername();
		if (configuredUsername != null && !configuredUsername.isEmpty()) {
			emdDatasource.setUsername(configuredUsername);
			return;
		}

		try {
			String inputUsername = askEmdUsername(emdDatasource);
			emdDatasource.setUsername(inputUsername);
		} catch (IllegalStateException | IOException ex) {
			throw new Error(ex);
		}
	}


	private String askEmdUsername(DriverManagerDataSource emdDatasource) 
			throws IllegalStateException, IOException {
		return UserInput.promptUser(
				"EMD username",
				String.format("Connecting to %s", emdDatasource.getUrl()),
				"username: ");
	}


	private void initEmdPassword(DriverManagerDataSource emdDatasource) {
		String configuredPassword = emdDatasource.getPassword();
		if (configuredPassword != null && !configuredPassword.isEmpty()) {
			emdDatasource.setPassword(configuredPassword);
			return;
		}

		try {
			char[] inputPassword = askEmdpassword(emdDatasource);
			emdDatasource.setPassword(new String(inputPassword));
		} catch (IllegalStateException | IOException ex) {
			throw new Error(ex);
		}
	}


	private char[] askEmdpassword(DriverManagerDataSource emdDatasource) 
			throws IllegalStateException, IOException {
		return UserInput.promptUserPassword("EMD password",
				String.format("Connection to %s\nUsername: %s",
						emdDatasource.getUrl(),
						emdDatasource.getUsername()),
				"password: ");
	}
			

	private File getFile(final Content context, final XPathOp fileAttr, 
			final String msgUriSyntaxException, 
			final String msgNotConfigured) {
		String addr = fileAttr.getAttrValue(context);
		if(addr != null) {
			try {
				return new File(new URI(addr));
			} catch (URISyntaxException ex) {
				throw new Error(String.format(msgUriSyntaxException, addr), ex);
			}
		} else {
			throw new Error(msgNotConfigured);
		}
		
	}

	private boolean useWebservice() {
		boolean existsWs = XPaths.INDEX_WEBSERVICE.nodeExists(getConfigDoc());
		boolean existsLocal = XPaths.INDEX_LOCAL.nodeExists(getConfigDoc());
		return existsWs || ! existsLocal;
	}

	private static void dumpNodeList(List<? extends Content> nodes) {
		System.out.println("List:");
		for (int i = 0; i < nodes.size(); i++) {
			Content n = nodes.get(i);
			System.out.print(String.format("[%d]\t",i));
			dumpNode(n);
		}
	}
	
	private static QName readId(NameSpaceResolver context, Content n, XPaths attrExpr) {
		NameSpaceResolver nsr = context.pushContext();
		List<Attribute> attrs = attrExpr.getAllAttributes(n);
		if(attrs.isEmpty() || attrs.size() != 1) {
			throw new Error(new IllegalStateException(String.format(
					"Expected one id (%s @ %s).", attrExpr.toString(), n.toString())));
		}
		Attribute attr = attrs.get(0);
		try {
			nsr.addAll(attr.getNamespacesInScope());
			QName id = nsr.createQName(attr.getValue());
			return id;
		} catch (URISyntaxException ex) {
			throw new Error(String.format(
					"Namespace URI in config file is not well formed."),
					ex);
		}
	}

	private static void dumpNode(Content n) {
		switch (n.getCType()) {
			case Element:
				Element e = (Element)n;
				String value = e.getValue();
				System.out.println(String.format("node: <%s:%s> value: %s", 
						e.getNamespace().getPrefix(),
						e.getName(),
						value));
				break;
			case Comment:
				Comment c = (Comment)n;
				System.out.println(String.format("comment: %s", c.getText()));
				break;
			default:
				System.out.println(String.format("Unknown element type %s", n.getClass().getName()));
		}
//		} else if(n instanceof Attribute) {
//				break;
//			case Node.ATTRIBUTE_NODE:
//				System.out.println(String.format("%s:%s=\"%s\"",
//						n.getPrefix(), n.getNodeName(), n.getNodeValue()));
//		}
//				break;
//			case Node.TEXT_NODE:
//				System.out.println(String.format("text: \"%s\"", n.getTextContent()));
//				break;
//			case Node.PROCESSING_INSTRUCTION_NODE:
//				System.out.println(String.format("PI: <?%s:%s?>", n.getPrefix(), n.getNodeName()));
//			default:
//				System.out.println(String.format("unknown type %d, %s:%s",
//						n.getNodeType(), n.getPrefix(), n.getNodeName()));
//		}
	}	
}
