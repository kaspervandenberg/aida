// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.config;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ForkJoinPool;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import nl.maastro.eureca.aida.search.zylabpatisclient.LocalLuceneSearcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryProvider;
import nl.maastro.eureca.aida.search.zylabpatisclient.Searcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.WebserviceSearcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTree;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQuery;
import nl.maastro.vocab.axis.services.SearcherWS.SearcherWS;
import nl.maastro.vocab.axis.services.SearcherWS.SearcherWSServiceLocator;
import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMConstants;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
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

	private static abstract class XPathOpBaseImpl<T> implements XPathOp {
		private final Map<String, Object> variables;
		
		protected XPathOpBaseImpl() {
			variables = null;
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
		private transient XPathExpression<Element>  expr = null;
		
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
		private transient XPathExpression<Attribute>  expr = null;

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
							final List<IllegalArgumentException> causes = new ArrayList(suppressedExceptions);
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
		private Map<QName, Query> queries = null;

		@Override
		public Collection<QName> getQueryIds() {
			initQueries();
			return Collections.unmodifiableSet(queries.keySet());
		}

		@Override
		public boolean provides(QName id) {
			initQueries();
			return queries.containsKey(id);
		}

		public Query get(final QName id) {
			if(!provides(id)) {
				throw new NoSuchElementException(
						String.format("No query with id %s configured.", id.toString()));
			}
			initQueries();
				
			if(queries.get(id) == null) {
				final QName canQName = findCanonicalQName(id);
				String s_id = canQName.getPrefix().isEmpty() ?
						canQName.getLocalPart() :
						canQName.getPrefix() + ":" + canQName.getLocalPart();
				XPaths.QUERY_BY_ID.setVariable("queryId", s_id);
				final String s_query = XPaths.QUERY_BY_ID.getFirstNode(getConfigDoc()).getValue();
				queries.put(id, new StringQuery() {
					@Override
					public String getRepresentation() {
						return s_query;
					}

					@Override
					public QName getName() {
						return canQName;
					}
				});
			}
			return queries.get(id);
		}
		

		@Override
		@Deprecated
		public boolean hasString(QName id) {
			initQueries();
			return queries.containsKey(id);
		}

		@Override
		@Deprecated
		public boolean hasObject(QName id) {
			return false;
//			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

		private void initQueries() {
			if(queries == null) {
				List<Attribute> attrs = XPaths.QUERY_IDS.getAllAttributes(getConfigDoc());
				queries = new HashMap<>(attrs.size());
				for (Attribute attribute : attrs) {
					NameSpaceResolver nsr = getNamespaces().pushContext();
					try {
						nsr.addAll(attribute.getNamespacesInScope());
						String val = attribute.getValue();
						queries.put(nsr.createQName(val), null);
					} catch (URISyntaxException ex) {
						throw new Error(String.format(
								"Namespace URI in config file is not well formed."),
								ex);
					}
				}
			}
		}

		private QName findCanonicalQName(QName qname) {
			initQueries();
			QName canonicalId = null;
			Iterator<QName> i = queries.keySet().iterator();
			while (canonicalId == null && i.hasNext()) {
				QName item = i.next();
				if(item.equals(qname)) {
					canonicalId = item;
				}	
			}
			if (canonicalId != null) {
				return canonicalId;
			} else {
				throw new NoSuchElementException(
						String.format("No query with id %s configured.", qname.toString()));
			}
			
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
		N_ABS_QUERY_BY_ID(XPathOpNodeImpl.class,
				N_ABS_QUERY.s_expr + "[@id=$queryId]",
				new Object[][]{{"queryId", ""}}),

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

		// Compound paths
		C_INDEX_WEBSERVICE(XPathOpCompoundImpl.class, N_ABS_INDEX, N_WEBSERVICE_INDEX),
		C_INDEX_LOCAL(XPathOpCompoundImpl.class, N_ABS_INDEX, N_LOCAL_INDEX),
		C_WS_ADDRESS(XPathOpCompoundImpl.class, N_ABS_INDEX, N_WEBSERVICE_INDEX, A_ADDRESS),
		C_WS_INDEX(XPathOpCompoundImpl.class, N_ABS_INDEX, N_WEBSERVICE_INDEX, A_INDEX),
		C_LOCAL_FILE(XPathOpCompoundImpl.class, N_ABS_INDEX, N_LOCAL_INDEX, A_FILE),
		
		;
		public final XPathOp delegate;
		public final String s_expr;
		
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
		QUERY_IDS(XPathImpls.A_ABS_QUERY_ID),
		QUERY_BY_ID(XPathImpls.N_ABS_QUERY_BY_ID);

		private final XPathOp delegate;
		private final String defaultValue;

		private XPaths(XPathImpls impl) {
			delegate = impl.delegate;
			defaultValue = null;
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
		VERSION("nl.maastro.eureca.aida.search.version");

		private final String key;
		
		private PropertyKeys(final String key_) {
			key = key_;
		}
	}
	
	private static final String NS = "http://search.aida.eureca.maastro.nl/zylabpatisclient/config";
	private static final String NS_PREFIX = "zpsc";
	private static final String SCHEMA_RESOURCE = "/zylabPatisClientConfig.xsd";
	private static final String QNAME_SEP = ":";
	
	private static Config singleton = null;
	
	private static XPathFactory xpathfactory = null;
	private static Collection<Namespace> xpathNamespaces = null;

	private final InputStream configStream;
	private final ForkJoinPool taskPool;
	private Element configDoc = null;
	private NameSpaceResolver namespaces = null; 

	private Searcher searcher = null;
	
	private Config(InputStream configStream_, ForkJoinPool taskPool_) {
		configStream = configStream_;
		taskPool = taskPool_;
	}
	
	public static Config init(InputStream configStream) {
		return init(configStream, new ForkJoinPool());
	}
	
	public static Config init(InputStream configStream, ForkJoinPool taskPool_) {
		if(singleton != null) {
			throw new IllegalStateException("Call init() exactly once.");
		}
		singleton = new Config(configStream, taskPool_);
		return singleton;
	}
	
	public static Config instance() throws IllegalStateException {
		if(singleton == null) {
			throw new IllegalStateException(
					"Call init(…) before calling instance()");
		}
		throw new UnsupportedOperationException("Not yet implemented");
	}

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
				searcher = new LocalLuceneSearcher(getLocalIndexFile(), defaultField, maxHits, taskPool);
			}
		}
		return searcher;
	}

	public QueryProvider getConfiguredQueries() {
		return this.new QueryPatterns();
	}

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
	
	private static Document parseXml(InputStream configStream) {
		try {
			Document doc = getParser().build(configStream);
			return doc;
		} catch (JDOMException | IOException ex) {
			throw new Error(ex);
		}
	}

	private static SAXBuilder getParser() {
		Source schemaSource = new StreamSource(Config.class.getResourceAsStream(SCHEMA_RESOURCE));
		
		try {
			Schema  schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
					newSchema(schemaSource);
			SAXBuilder result = new SAXBuilder(new XMLReaderSchemaFactory(schema));
					
			return result;
		
		} catch (SAXException ex) {
			throw new Error(ex);
		}
	}

	private static XPathFactory getXPathFactory() {
		if(xpathfactory == null) {
			xpathfactory = XPathFactory.instance();
		}
		return xpathfactory;
	}

	private static Collection<Namespace> getXpathNamespaces() {
		if(xpathNamespaces == null) {
			xpathNamespaces = new ArrayDeque<>(1);
			xpathNamespaces.add(Namespace.getNamespace(NS_PREFIX, NS));
		}
		return xpathNamespaces;
	}
	
	private static <T> XPathExpression<T> compileExpr(
			String expr, Map<String, Object> variables, Filter<T> filter) {
		return getXPathFactory().compile(expr, filter, variables, getXpathNamespaces());
	}

	private Element getConfigDoc() {
		if(configDoc == null) {
			configDoc = parseXml(configStream).getContent(Filters.element()).get(0);
		}
		return configDoc;
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
		String addr = XPaths.LOCAL_FILE.getAttrValue(getConfigDoc());
		if(addr != null) {
			try {
				return new File(new URI(addr));
			} catch (URISyntaxException ex) {
				throw new Error(String.format("Local index URI (%s) malformed", addr), ex);
			}
		} else {
			throw new Error("Local index file is not configured.");
		}
	}

	private boolean useWebservice() {
		boolean existsWs = XPaths.INDEX_WEBSERVICE.nodeExists(getConfigDoc());
		boolean existsLocal = XPaths.INDEX_LOCAL.nodeExists(getConfigDoc());
		return existsWs || ! existsLocal;
	}
	
	public static void main(String[] args) {
//		try {
			URL resource = Config.class.getResource("/testConfig.xml");
			System.out.println(resource);
			Config testConfig = Config.init(Config.class.getResourceAsStream("/testConfig.xml"));
//			XPaths.getXPath().compile("/zlpc/@ver");
//			System.out.println(XPaths.ABS_VERSION.s_expr);
//			System.out.println(testConfig.getConfigDoc().getDocumentElement().getNamespaceURI());
//			System.out.println(testConfig.getConfigDoc().getDocumentElement().getNodeName());
//			System.out.println("Use webservice: " + testConfig.useWebservice());
//			System.out.println("Default field: *" + XPaths.N_DEFAULT_FIELD.getAttrValue(testConfig.getConfigDoc()) + "*");
//			System.out.println("Max hits: " + XPaths.N_RESULT_LIMIT.getAttrValue(testConfig.getConfigDoc()));
//			dumpNodeList((NodeList)XPathAttrs.ABS_QUERY_ID.getExpr().evaluate(testConfig.getConfigDoc(), XPathConstants.NODESET));
//			dumpNodeList(XPathNodes.N_BASE.getLastNode(testConfig.getConfigDoc()).getAttributes().);
//			System.out.println("Address: " + testConfig.getWebserviceAddress().toString());
//			System.out.println("Index: " + testConfig.getWebserviceIndex());
//			System.out.println(XPaths.getXPath().getNamespaceContext().getNamespaceURI(""));
//			dumpNodeList((NodeList)XPaths.getXPath().evaluate("//*[local-name()='zylabPatisClientConfig']", testConfig.getConfigDoc(), XPathConstants.NODESET));
//			dumpNodeList((NodeList)XPaths.getXPath().evaluate("//:zylabPatisClientConfig", testConfig.getConfigDoc(), XPathConstants.NODESET));
//			dumpNodeList((NodeList)XPaths.getXPath().evaluate("/:zylabPatisClientConfig", testConfig.getConfigDoc(), XPathConstants.NODESET));
//			System.out.println("version: " + XPaths.getXPath().evaluate("/" + NS_PREFIX + ":zylabPatisClientConfig/@:version", testConfig.getConfigDoc(), XPathConstants.STRING));
//			dumpNodeList((NodeList)XPaths.getXPath().evaluate("/zylabPatisClientConfig", testConfig.getConfigDoc(), XPathConstants.NODESET));
//			dumpNodeList((NodeList)XPaths.N_BASE.getExpr().evaluate(testConfig.getConfigDoc(), XPathConstants.NODESET));
			dumpNodeList(testConfig.getConfigDoc().getContent(Filters.element()));
//			XPathImpls.N_ABS_QUERY_BY_ID.delegate.setVariable("queryId", "eewrt");
//			dumpNode(XPathImpls.N_ABS_QUERY_BY_ID.delegate.getFirstNode(testConfig.getConfigDoc()));
			
			System.out.println(testConfig.new QueryPatterns().getQueryIds().toString());
			System.out.println(testConfig.new QueryPatterns().getAsString(new QName("http://search.aida.eureca.maastro.nl/zylabpatisclient/config", "scopedQuery1", "zlpc")));
//			System.out.println(testConfig.new QueryPatterns().getAsString(new QName("unscopedQuery2")));
			
//			System.out.println(XPaths.N_BASE.getExpr().evaluate(testConfig.getConfigDoc()));
//			dumpNode(XPaths.N_BASE.getLastNode(testConfig.getConfigDoc()));
//			dumpNode(XPaths.N_ABS_INDEX.getLastNode(testConfig.getConfigDoc()));
//			System.out.println(XPaths.N_WEBSERVICE_INDEX.nodeExists(XPaths.N_ABS_INDEX.getLastNode(testConfig.getConfigDoc())));
//			System.out.println(XPaths.N_LOCAL_INDEX.nodeExists(XPaths.N_ABS_INDEX.getLastNode(testConfig.getConfigDoc())));
//			System.out.println(XPaths.ABS_VERSION.getAttrValue(testConfig.getConfigDoc()));
//		} catch (XPathExpressionException | URISyntaxException ex) {
//		} catch (URISyntaxException ex) {
//			throw new Error(ex);
//		}
	}

	private static void dumpNodeList(List<? extends Content> nodes) {
		System.out.println("List:");
		for (int i = 0; i < nodes.size(); i++) {
			Content n = nodes.get(i);
			System.out.print(String.format("[%d]\t",i));
			dumpNode(n);
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
