// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nl.maastro.eureca.aida.search.zylabpatisclient.LocalLuceneSearcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.Searcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.WebserviceSearcher;
import nl.maastro.vocab.axis.services.SearcherWS.SearcherWS;
import nl.maastro.vocab.axis.services.SearcherWS.SearcherWSServiceLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Config {
	private interface XPathOp {
		boolean nodeExists(Node context);
		
		String getAttrValue(Node context);
		
		Node getLastNode(Node context); 
	}

	private enum XPathNodes implements XPathOp {
		BASE("/" + NS_PREFIX + ":zylabPatisClientConfig"),
		INDEX(NS_PREFIX + ":index"),
		ABS_INDEX(BASE.s_expr + "/" + INDEX.s_expr),
		LOCAL_INDEX(NS_PREFIX + ":localIndex"),
		WEBSERVICE_INDEX(NS_PREFIX + ":webservice"),
		DEFAULT_FIELD(NS_PREFIX + ":defaultField"),
		ABS_DEFAULT_FIELD(BASE.s_expr + "/" + DEFAULT_FIELD.s_expr),
		RESULT_LIMIT(NS_PREFIX + ":resultLimit"),
		ABS_RESULT_LIMIT(BASE.s_expr + "/" + RESULT_LIMIT.s_expr);


		private final String s_expr;
		private transient XPathExpression  expr = null;

		private XPathNodes(String s_expr_) {
			this.s_expr = s_expr_;
		}

		@Override
		public boolean nodeExists(Node context) {
			try {
				return (Boolean) getExpr().evaluate(context, XPathConstants.BOOLEAN);
			} catch (XPathExpressionException ex) {
				throw new Error(ex);
			}
		}

		@Override
		public String getAttrValue(Node context) {
			throw new IllegalStateException("XPath expression is not an attribute");
		}

		@Override
		public Node getLastNode(Node context) {
			try {
				NodeList items = (NodeList) getExpr().evaluate(context, XPathConstants.NODESET);
				Node result = items.item(items.getLength() -1);
				if(result == null) {
					throw new NoSuchElementException(String.format(
							"Document does not contain elements matching %s",
							s_expr));
				}
				return result;
			} catch (XPathExpressionException ex) {
				throw new Error(ex);
			}
		}

		private XPathExpression getExpr() {
			if(expr == null) {
				try {
					expr = getXPath().compile(s_expr);
				} catch (XPathExpressionException ex) {
					throw new Error("Invallid XPath", ex);
				}
			}
			return expr;
		}
	}
	
	private enum XPathAttrs implements XPathOp {
		ADDRESS("@address"),
		INDEX("@index"),
		FILE("@file"),
		FIELD("@field"),
		ABS_DEFAULT_FIELD(XPathNodes.ABS_DEFAULT_FIELD.s_expr + "/" + FIELD.s_expr),
		N_HITS("@nHits"),
		ABS_RESULT_LIMIT(XPathNodes.ABS_RESULT_LIMIT.s_expr + "/" + N_HITS.s_expr);
		
		
		private final String s_expr;
		private transient XPathExpression  expr = null;

		private XPathAttrs(String s_expr_) {
			this.s_expr = s_expr_;
		}

		@Override
		public boolean nodeExists(Node context) {
			try {
				return (Boolean) getExpr().evaluate(context, XPathConstants.BOOLEAN);
			} catch (XPathExpressionException ex) {
				throw new Error(ex);
			}
		}

		@Override
		public String getAttrValue(Node context) {
			try {
				return (String) getExpr().evaluate(context, XPathConstants.STRING);
			} catch (XPathExpressionException ex) {
				throw new Error(ex);
			}
		}

		@Override
		public Node getLastNode(Node context) {
			throw new IllegalStateException("XPath expression is not a Node");
		}
		
		private XPathExpression getExpr() {
			if(expr == null) {
				try {
					expr = getXPath().compile(s_expr);
				} catch (XPathExpressionException ex) {
					throw new Error("Invallid XPath", ex);
				}
			}
			return expr;
		}
	}

	private enum XPathCompound implements XPathOp {
		INDEX_WEBSERVICE(XPathNodes.ABS_INDEX, XPathNodes.WEBSERVICE_INDEX),
		INDEX_LOCAL(XPathNodes.ABS_INDEX, XPathNodes.LOCAL_INDEX),
		WS_ADDRESS(XPathNodes.ABS_INDEX, XPathNodes.WEBSERVICE_INDEX, XPathAttrs.ADDRESS),
		WS_INDEX(XPathNodes.ABS_INDEX, XPathNodes.WEBSERVICE_INDEX, XPathAttrs.INDEX),
		LOCAL_FILE(XPathNodes.ABS_INDEX, XPathNodes.LOCAL_INDEX, XPathAttrs.FILE)

		;

		private final List<XPathOp> ops;
			
		private XPathCompound(XPathOp... ops_) {
			ops = Arrays.asList(ops_);
		}

		@Override
		public boolean nodeExists(Node context) {
			return ops.get(ops.size() -1).nodeExists(
					getContext(context, ops.subList(0, ops.size() -1)));
		}

		@Override
		public String getAttrValue(Node context) {
			return ops.get(ops.size() -1).getAttrValue(
					getContext(context, ops.subList(0, ops.size() -1)));
		}

		@Override
		public Node getLastNode(Node context) {
			return ops.get(ops.size() -1).getLastNode(
					getContext(context, ops.subList(0, ops.size() -1)));
		}

		private Node getContext(Node context, List<XPathOp> pathExpr) {
			if(pathExpr.size() <= 1) {
				return pathExpr.get(0).getLastNode(context);
			} else {
				return pathExpr.get(pathExpr.size() -1).getLastNode(
						getContext(context, pathExpr.subList(0, pathExpr.size() -1)));
			}
		}
	}
	
	private enum XPaths implements XPathOp {
		INDEX_WEBSERVICE(XPathCompound.INDEX_WEBSERVICE),
		INDEX_LOCAL(XPathCompound.INDEX_LOCAL),
		WS_ADDRES(XPathCompound.WS_ADDRESS),
		WS_INDEX(XPathCompound.WS_INDEX, "Zylab_test-20130415-02"),
		LOCAL_FILE(XPathCompound.LOCAL_FILE),
		DEFAULT_FIELD(XPathAttrs.ABS_DEFAULT_FIELD, "content"),
		RESULT_LIMIT(XPathAttrs.ABS_RESULT_LIMIT, "1000");
		
		private final XPathOp delegate;
		private final String defaultValue;

		private XPaths(XPathOp delegate_) {
			delegate = delegate_;
			defaultValue = null;
		}

		private XPaths(XPathOp delegate_, String defaultValue_) {
			delegate = delegate_;
			defaultValue = defaultValue_;
		}

		@Override
		public boolean nodeExists(Node context) {
			return delegate.nodeExists(context);
		}

		@Override
		public String getAttrValue(Node context) {
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
		public Node getLastNode(Node context) {
			return delegate.getLastNode(context);
		}
	}

	public enum PropertyKeys {
		VERSION("nl.maastro.eureca.aida.search.version");

		private final String key;
		
		private PropertyKeys(final String key_) {
			key = key_;
		}
	}
	
	private static final Logger log = Logger.getLogger(Config.class.getName());
	private static final String NS = "http://search.aida.eureca.maastro.nl/zylabpatisclient/config";
	private static final String NS_PREFIX = "zpsc";
	private static final String SCHEMA_RESOURCE = "/zylabPatisClientConfig.xsd";
	
	private static Config singleton = null;
	
	private static XPath xpath = null;

	private final File configFile;
	private final ForkJoinPool taskPool;
	private Document configDoc = null;

	private Searcher searcher = null;
	
	private Config(File configFile_, ForkJoinPool taskPool_) {
		configFile = configFile_;
		taskPool = taskPool_;
	}
	
	public static Config init(File configFile) {
		return init(configFile, new ForkJoinPool());
	}
	
	public static Config init(File configFile, ForkJoinPool taskPool_) {
		if(singleton != null) {
			throw new IllegalStateException("Call init() exactly once.");
		}
		singleton = new Config(configFile, taskPool_);
		return singleton;
	}
	
	public static Config instance() throws IllegalStateException {
		if(singleton == null) {
			throw new IllegalStateException(
					"Call init(…) before calling instance()");
		}
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public Searcher getSearcher() {
		if(searcher == null) {
			String defaultField = XPaths.DEFAULT_FIELD.getAttrValue(getConfigDoc());
			int maxHits = Integer.parseInt(XPaths.RESULT_LIMIT.getAttrValue(getConfigDoc()));
			
			try {
				if(useWebservice()) {
					SearcherWSServiceLocator wsLocator = new SearcherWSServiceLocator();
						SearcherWS webservice = wsLocator.getSearcherWS(getWebserviceAddress());
						searcher = new WebserviceSearcher(webservice, 
								XPaths.WS_INDEX.getAttrValue(getConfigDoc()),
								defaultField, maxHits, taskPool);
				} else {
					searcher = new LocalLuceneSearcher(getLocalIndexFile(), defaultField, maxHits, taskPool);
				}
			} catch (ServiceException | IOException ex) {
				throw new Error(ex);
			}
		}
		return searcher;
	}

	private static Document parseXml(File configFile) {
		try {
			Document doc = getParser().parse(configFile);
			return doc;
		} catch (SAXException | IOException ex) {
			throw new Error(ex);
		}
	}

	private static DocumentBuilder getParser() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);

		Source schemaSource = new StreamSource(Config.class.getResourceAsStream(SCHEMA_RESOURCE));
		
		try {
			Schema  schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
					newSchema(schemaSource);
					
			dbf.setSchema(schema);
			return dbf.newDocumentBuilder();
		
		} catch (ParserConfigurationException | SAXException ex) {
			throw new Error(ex);
		}
	}

	private static XPath getXPath() {
		if(xpath == null) {
			XPathFactory xpf = XPathFactory.newInstance();
			xpath = xpf.newXPath();
			Map<String, String> nsMappings = new HashMap<>();
			nsMappings.put(NS_PREFIX, NS);
			xpath.setNamespaceContext(new SimpleNamespaceContext(NS, nsMappings));
		}
		return xpath;
	}

	private Document getConfigDoc() {
		if(configDoc == null) {
			configDoc = parseXml(configFile);
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
		try {
			URL resource = Config.class.getResource("/testConfig.xml");
			System.out.println(resource);
			Config testConfig = Config.init(new File(Config.class.getResource("/testConfig.xml").toURI()));
//			XPaths.getXPath().compile("/zlpc/@ver");
//			System.out.println(XPaths.ABS_VERSION.s_expr);
			System.out.println(testConfig.getConfigDoc().getDocumentElement().getNamespaceURI());
			System.out.println(testConfig.getConfigDoc().getDocumentElement().getNodeName());
			System.out.println("Use webservice: " + testConfig.useWebservice());
			System.out.println("Default field: *" + XPaths.DEFAULT_FIELD.getAttrValue(testConfig.getConfigDoc()) + "*");
			System.out.println("Max hits: " + XPaths.RESULT_LIMIT.getAttrValue(testConfig.getConfigDoc()));
//			System.out.println("Address: " + testConfig.getWebserviceAddress().toString());
//			System.out.println("Index: " + testConfig.getWebserviceIndex());
//			System.out.println(XPaths.getXPath().getNamespaceContext().getNamespaceURI(""));
//			dumpNodeList((NodeList)XPaths.getXPath().evaluate("//*[local-name()='zylabPatisClientConfig']", testConfig.getConfigDoc(), XPathConstants.NODESET));
//			dumpNodeList((NodeList)XPaths.getXPath().evaluate("//:zylabPatisClientConfig", testConfig.getConfigDoc(), XPathConstants.NODESET));
//			dumpNodeList((NodeList)XPaths.getXPath().evaluate("/:zylabPatisClientConfig", testConfig.getConfigDoc(), XPathConstants.NODESET));
//			System.out.println("version: " + XPaths.getXPath().evaluate("/" + NS_PREFIX + ":zylabPatisClientConfig/@:version", testConfig.getConfigDoc(), XPathConstants.STRING));
//			dumpNodeList((NodeList)XPaths.getXPath().evaluate("/zylabPatisClientConfig", testConfig.getConfigDoc(), XPathConstants.NODESET));
//			dumpNodeList((NodeList)XPaths.BASE.getExpr().evaluate(testConfig.getConfigDoc(), XPathConstants.NODESET));
			

			
//			System.out.println(XPaths.BASE.getExpr().evaluate(testConfig.getConfigDoc()));
//			dumpNode(XPaths.BASE.getLastNode(testConfig.getConfigDoc()));
//			dumpNode(XPaths.ABS_INDEX.getLastNode(testConfig.getConfigDoc()));
//			System.out.println(XPaths.WEBSERVICE_INDEX.nodeExists(XPaths.ABS_INDEX.getLastNode(testConfig.getConfigDoc())));
//			System.out.println(XPaths.LOCAL_INDEX.nodeExists(XPaths.ABS_INDEX.getLastNode(testConfig.getConfigDoc())));
//			System.out.println(XPaths.ABS_VERSION.getAttrValue(testConfig.getConfigDoc()));
//		} catch (XPathExpressionException | URISyntaxException ex) {
		} catch (URISyntaxException ex) {
			throw new Error(ex);
		}
	}

	private static void dumpNodeList(NodeList nodes) {
		System.out.println("List:");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			System.out.println(String.format("[%d]\t<%s:%s>", i, n.getPrefix(), n.getLocalName()));
		}
	}

	private static void dumpNode(Node n) {
		System.out.println(String.format("node: <%s:%s>", n.getPrefix(), n.getNodeName()));
	}	
}
