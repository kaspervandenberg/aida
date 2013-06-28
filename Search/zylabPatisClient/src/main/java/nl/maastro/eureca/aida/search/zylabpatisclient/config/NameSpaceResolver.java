// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.jdom2.JDOMConstants;
import org.jdom2.Namespace;

/**
 * Store the {@link org.jdom2.Namespace}s of a context, mapping prefixes to 
 * namespace URIs and vice versa.  Namespace contexts can be nested the methods
 * {@link #pushContext()} and {@link #popContext()} allow entering and leaving
 * a context.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class NameSpaceResolver {
	/**
	 * Adapt {@link NameSpaceResolver} to {@link javax.xml.namespace.NamespaceContext}
	 * conforming to the null value semantics described in {@code NamespaceContext}.
	 * 
	 * <p>When {@link #enforceDefaults} is {@code true}, this adapter will comply
	 * to the semantics for the default prefixes and their namespace URIs.  That 
	 * is, always match the default prefixes to their default URIs: ignoring the 
	 * overriding of namespaces from {@link NameSpaceResolver#DEFAULT_NS}
	 * and adding the {@link NameSpaceResolver#DEFAULT_NS} 
	 * {@code NameSpaceResolver} to those that lack it as outermost context.</p>
	 */
	private class ToNamespaceContextAdapter
			implements NamespaceContext {
		private final boolean enforceDefaults;

		public ToNamespaceContextAdapter(final boolean enforceDefaults_) {
			enforceDefaults = enforceDefaults_;
		}
		
		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null) {
				throw new IllegalArgumentException("Resolving null namespace prefix");
			}
			if(enforceDefaults && DEFAULT_NS.containsPrefix(prefix)) {
				return DEFAULT_NS.resolvePrefix(prefix).getURI();
			}
			if(containsPrefix(prefix)) {
				return resolvePrefix(prefix).getURI();
			} else {
				return javax.xml.XMLConstants.NULL_NS_URI;
			}
		}

		@Override
		public String getPrefix(String namespaceURI) {
			if (namespaceURI == null) {
				throw new IllegalArgumentException("Resolving null namespaceURI");
			}
			try {
				URI uri = new URI(namespaceURI);

				if(enforceDefaults && DEFAULT_NS.containsUri(uri)) {
					return DEFAULT_NS.resolveUri(uri).getPrefix();
				}
				if(containsUri(uri)) {
					return resolveUri(uri).getPrefix();
				}
				return null;
			} catch (URISyntaxException ex) {
				throw new IllegalArgumentException(
						String.format("URI %s not well formed.", namespaceURI),
						ex);
			}
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			if (namespaceURI == null) {
				throw new IllegalArgumentException("Resolving null namespaceURI");
			}
			try {
				URI uri = new URI(namespaceURI);

				Map<String, Namespace> result = 
						_getAllMappedToUri(uri, new HashMap<String, Namespace>());
				if(enforceDefaults) {
					result = DEFAULT_NS._getAllMappedToUri(uri, result);
				}
				return Collections.unmodifiableSet(result.keySet()).iterator();
				
			} catch (URISyntaxException ex) {
				throw new IllegalArgumentException(
						String.format("URI %s not well formed.", namespaceURI),
						ex);
			}
		}
	}
	
	/**
	 * Unmodifiable namespace resolver containing the default prefix–namespace
	 * mappings 
	 * {@value org.jdom2.JDOMConstants#NS_PREFIX_DEFAULT}:{@value org.jdom2.JDOMConstants#NS_URI_DEFAULT} 
	 * ({@link Namespace#NO_NAMESPACE}), 
	 * {@value org.jdom2.JDOMConstants#NS_PREFIX_XML}:{@value org.jdom2.JDOMConstants#NS_URI_XML}
	 * ({@link Namespace#XML_NAMESPACE}), and 
	 * {@value org.jdom2.JDOMConstants#NS_PREFIX_XMLNS}:{@value org.jdom2.JDOMConstants#NS_URI_XMLNS}.
	 * {@link #createDefault()} creates a fresh {@code NameSpaceResolver}
	 * with {@code DEFAULT_NS} as {@link #parent}.
	 */
	private final static NameSpaceResolver DEFAULT_NS  = new NameSpaceResolver() {
		{
//			try {
//				super.add(Namespace.NO_NAMESPACE);
//				super.add(Namespace.XML_NAMESPACE);
//				super.add(Namespace.getNamespace(JDOMConstants.NS_PREFIX_XMLNS, JDOMConstants.NS_URI_XMLNS));
//			} catch (URISyntaxException ex) {
//				throw new Error("URISyntaxException for default namespaces", ex);
//			}
		}

		/**
		 * @throws IllegalStateException	always, since the default namespace
		 * 		context is unmodifiable.
		 */
		@Override
		public Namespace add(Namespace ns) throws URISyntaxException {
			throw new IllegalStateException("Unmodifiable namespace resolver");
		}

		/**
		 * @throws IllegalStateException	always, since the default namespace
		 * 		context is unmodifiable.
		 */
		@Override
		public void addAll(Iterable<? extends Namespace> col) throws URISyntaxException {
			throw new IllegalStateException("Unmodifiable namespace resolver");
		}
	};

	/**
	 * Separates a QName's prefix from its local part.
	 */
	private final static String QNAME_SEPARATOR = ":";
	
	/**
	 * The context that contains this {@link NameSpaceResolver}.  Use 
	 * {@link #popContext()} to return to {@code parent}.
	 */
	private /*@Nullable */ final NameSpaceResolver parent;

	/**
	 * Map prefixes to their {@link Namespace}.  {@code prefixToNs} and 
	 * {@code uriToNs} are consistent.
	 */
	private final Map<String, Namespace> prefixToNs = new ConcurrentHashMap<>();
	
	/**
	 * Reverse mapping of {@link java.net.URI} to the set of {@link Namespace}s.
	 * {@code prefixToNs} and {@code uriToNs} are consistent.
	 */
	private final Map<URI, Map<String, Namespace>> uriToNs = new ConcurrentHashMap<>();

	/**
	 * Construct a {@code NameSpaceResolver} with <em>no</em> {@link #parent}.
	 * External clients use {@link #createEmpty()} to create a 
	 * {@code NameSpaceResolver} without a parent context.
	 */
	private NameSpaceResolver() {
		parent = null;
	}

	/**
	 * Construct a {@code NameSpaceResolver} with {@code parent} as its 
	 * {@link #parent}.  External clients use {@link #createDefault()} and
	 * {@link #pushContext()} to create a {@code NameSpaceResolver} contained in
	 * an other {@code NameSpaceResolver}.
	 * 
	 * @param parent_	the context that contains the constructed 
	 * 		{@code NameSpaceResolver}. 
	 */
	private NameSpaceResolver(final NameSpaceResolver parent_) {
		this.parent = parent_;
	}

	/**
	 * Create a fresh {@link NameSpaceResolver} that contains no 
	 * prefix–namespace-mappings.	Use {@link #add(org.jdom2.Namespace)} and
	 * {@link #addAll(java.lang.Iterable)} to add mappings to the returned
	 * {@code NameSpaceResolver}.
	 * 
	 * @return	a fresh {@link NameSpaceResolver} without any mappings. 
	 */	
	public static NameSpaceResolver createEmpty() {
		return new NameSpaceResolver();
	}

	/**
	 * Create a fresh {@link NameSpaceResolver} that contains the default 
	 * prefix–namespace-mappings. The created {@code NameSpaceResolver} has
	 * mappings for 
	 * {@value org.jdom2.JDOMConstants#NS_PREFIX_DEFAULT}:{@value org.jdom2.JDOMConstants#NS_URI_DEFAULT} 
	 * ({@link Namespace#NO_NAMESPACE}), 
	 * {@value org.jdom2.JDOMConstants#NS_PREFIX_XML}:{@value org.jdom2.JDOMConstants#NS_URI_XML}
	 * ({@link Namespace#XML_NAMESPACE}), and 
	 * {@value org.jdom2.JDOMConstants#NS_PREFIX_XMLNS}:{@value org.jdom2.JDOMConstants#NS_URI_XMLNS}.
	 * Replacing these mappings via {@link #add(org.jdom2.Namespace)} or
	 * {@link #addAll(java.lang.Iterable)} will not affect other (default)
	 * {@code NameSpaceResolver}s.
	 * 
	 * @return	a fresh {@link NameSpaceResolver} that contains the default 
	 * 		prefix–namespace-mappings.
	 */
	public static NameSpaceResolver createDefault() {
		return new NameSpaceResolver(DEFAULT_NS);
	}

	/**
	 * Enter a new context/scope of namespaces.  The returned {@link NameSpaceResolver}
	 * (a.k.a. {@code subcontext} in the example) contains all mappings of
	 * {@code this} (a.k.a. {@code n1} in the example).  Adding or replacing a 
	 * mapping the returned {@code NameSpaceResolver} (via its
	 * {@link #add(org.jdom2.Namespace)}- and 
	 * {@link #addAll(java.lang.Iterable)}-methods) will only affect the 
	 * returned {@code NameSpaceResolver}, {@code this} remains unaffected.
	 * 
	 * <p>{@code pushContext()} and {@link #popContext()} allow entering and 
	 * leaving hierarchical namespace scopes.</p>
	 * 
	 * <p>Example:
	 * <code><pre>	NameSpaceResolver n1;
	 * 	URI uri1, uri2;
	 * 	assert(n1.resolvePrefix("demo").getURI().equals(uri1.toString());
	 * 	NameSpaceResolver subcontext  = n1.pushContext();
	 * 	assert(subcontext.resolvePrefix("demo").getURI().equals(uri1.toString());
	 * 	subcontext.add(Namespace.get("demo", uri2.toString());
	 * 	assert(n1.resolvePrefix("demo").getURI().equals(uri1.toString());</pre></code></p>
	 * 
	 * @return 	a fresh {@link NameSpaceResolver} that is contained in 
	 * 		{@code this} {@code NameSpaceResolver}.
	 */
	public NameSpaceResolver pushContext() {
		return new NameSpaceResolver(this);
	}

	/**
	 * Leave this context/scope and return to the context that contains {@code this}
	 * {@code NameSpaceResolver}.  {@link #pushContext()} and {@code popContext()}
	 * allow entering and leaving hierarchical namespace scopes.
	 * 
	 * <p>Example:
	 * <code><pre>	NameSpaceResolver n1;
	 * 	NameSpaceResolver current = n1;
	 * 	current = current.pushContext();
	 * 	…	// make context local changes
	 * 	current = current.popContext();	// discard all context local changes</pre></code></p>
	 * 
	 * @return	the {@link #parent} {@link NameSpaceResolver} that contains 
	 * 		{@code this} {@code NameSpaceResolver}.
	 * 
	 * @throws	IllegalStateException	when {@code this}{@code NameSpaceResolver}
	 * 		is the outermost scope; i.e. {@code this} has no {@link #parent}.
	 */
	public NameSpaceResolver popContext() throws IllegalStateException {
		if(parent != null) {
			return parent;
		} else {
			throw new IllegalStateException("No stored context to return to.");
		}
	}
	
	/**
	 * Add {@code ns} to the mapped {@link Namespace}s, replacing or overriding
	 * any previous mappings of {@code ns.}{@link Namespace#getPrefix()}.
	 * Let {@code p} be {@code ns.getPrefix()}, then subsequent calls to
	 * {@link #containsPrefix(java.lang.String) containsPrefix(p)} and 
	 * {@link #resolvePrefix(java.lang.String) resolvePrefix(p)} will return
	 * {@code true} and {@code ns} respectively.
	 * 
	 * @param ns	the {@link Namespace} to add.
	 * 
	 * @return	<ul><li>the previous namespace mapped to {@code ns.getPrefix()}; 
	 * 			or</li>
	 * 		<li>{@code null}, when {@code this} {@code NameSpaceResolver} did
	 * 			not contain any previous mapping for {@code ns.getPrefix()}.</li></ul>
	 * 
	 * @throws URISyntaxException	when {@code ns.}{@link Namespace#getURI()}
	 * 		returns an invalid {@link java.net.URI}.
	 */
	public /*@Nullable*/ Namespace add(final Namespace ns) throws URISyntaxException {
		String prefix = ns.getPrefix();
		boolean replace = this.containsPrefix(prefix);
		/*@Nullable*/ Namespace previousValue =
				(replace ? resolvePrefix(prefix) : null);
		prefixToNs.put(prefix, ns);
		addReverseMapping(new URI(ns.getURI()), ns, replace, previousValue);

		return previousValue;
	}

	/**
	 * {@link #add(org.jdom2.Namespace)} all {@link Namespace}s ∈ {@code col}
	 * to the mapped namespaces, replacing or overriding any previously mapped
	 * namespace with the same {@link Namespace#getPrefix() prefix} as one of 
	 * the namespaces ∈ {@code col}.  If {@code col} contains multiple 
	 * namespaces with the same prefix, {@code this} {@code NameSpaceResolver}
	 * will contain a mapping for the last namespace with that prefix encountered 
	 * in the iteration.
	 * 
	 * @see #add(org.jdom2.Namespace) 
	 * 
	 * @param col	{@link Iterable} over a collection of {@link Namespace}s
	 * 		to add.
	 * 
	 * @throws URISyntaxException	when any {@link Namespace#getURI() namespace URI}
	 * 		is not a well formed {@link java.applet.URI}.
	 */
	public void addAll(final Iterable<? extends Namespace> col) throws URISyntaxException {
		for (Namespace ns : col) {
			this.add(ns);
		}
	}

	/**
	 * Return whether {@code prefix} is mapped to a {@link Namespace}.  When 
	 * {@code containsPrefix()} returns {@code true}, 
	 * {@link #resolvePrefix(java.lang.String)} will return a {@code Namespace}.
	 * When {@code containsPrefix()} returns false, 
	 * {@link #resolvePrefix(java.lang.String)} will fail.
	 * 
	 * @param prefix	the prefix to identify a namespace URI with; e.g. 
	 * 		{@link Namespace#getPrefix()}.
	 * 
	 * @return	<ul><li>{@code true}, {@code prefix} is mapped to a 
	 * 			{@link Namespace} in {@code this} {@code NameSpaceResolver} or 
	 * 			in one of its {@link #parent ancestors}; or</li>
	 * 		<li>{@code false}, {@code prefix} is not mapped to a namespace 
	 * 			neither in {@code this} nor in any {@link #parent ancestor}.</li></ul>
	 * 	.   
	 */
	public boolean containsPrefix(final String prefix) {
		if (this.prefixToNs.containsKey(prefix)) {
			return true;
		} else if (parent != null) {
			return parent.containsPrefix(prefix);
		} else {
			return false;
		}
	}

	/**
	 * Return whether a mapping to {@code uri} exists in {@code this} 
	 * {@link NameSpaceResolver}.
	 * 
	 * @param uri	the {@link URI} to search for.
	 * 
	 * @return	<ul><li>{@code true}, a mapping to {@code uri} exists in
	 * 			{@code this} {@code NameSpaceResolver} or in any or its
	 * 			{@link #parent ancestors}; or</li>
	 * 		<li>{@code false}, {@code this} {@code NameSpaceResolver} does not
	 * 			contain any mapping to {@code uri} neither do any of its 
	 * 			{@link #parent ancestors}.</li></ul>
	 */
	public boolean containsUri(final URI uri) {
		if(this.uriToNs.containsKey(uri)) {
			return true;
		} else if (parent  != null) {
			return parent.containsUri(uri);
		} else {
			return false;
		}
	}

	/**
	 * Resolve {@code prefix} into the {@link Namespace} to which it is mapped.
	 * {@code resolvePrefix} searches through all namespaces mapped in 
	 * {@code this} {@code NameSpaceResolver} and those mapped in any
	 * {@link #parent ancestor}.
	 * 
	 * @param prefix	the prefix that identifies the namespace URI searched 
	 * 		for, e.g. {@link Namespace#getPrefix()}.
	 * 
	 * @return	the {@link Namespace} to which {@code prefix} is mapped.
	 * 
	 * @throws NoSuchElementException	when {@code prefix} is not mapped to
	 * 		a namespace.
	 */
	public Namespace resolvePrefix(final String  prefix) 
			throws NoSuchElementException {
		if(prefixToNs.containsKey(prefix)) {
			return prefixToNs.get(prefix);
		} else if (parent != null) {
			return parent.resolvePrefix(prefix);
		} else {
			throw new NoSuchElementException(
					String.format("prefix %s not found", prefix));
		}
	}

	/**
	 * {@link QName#QName(java.lang.String, java.lang.String, java.lang.String) 
	 * Create}  a {@link QName} from {@code prefix}, the {@link java.net.URI} it 
	 * {@link #resolvePrefix(java.lang.String) resolves} to, and 
	 * {@code localPart}.
	 * 
	 * @param prefix	the {@link QName#getPrefix() prefix} of the created
	 * 		{@code QName}.
	 * @param localPart	the {@link QName#getLocalPart() local part}  of the 
	 * 		created {@code QName}.
	 * 
	 * @return	a fresh {@link QName}
	 * 
	 * @throws NoSuchElementException	when {@code prefix} is not mapped to
	 * 		a namespace.
	 */
	public QName createQName(final String prefix, final String localPart) 
			throws NoSuchElementException {
		Namespace ns = resolvePrefix(prefix);
		return new QName(ns.getURI(), localPart, prefix);
	}

	public QName createQName(final String qnameExpr) 
			throws NoSuchElementException, IllegalArgumentException {
		// TODO Check for explicit URI
		String[] parts = qnameExpr.split(QNAME_SEPARATOR);
		
		String prefix;
		String localpart;
		if(parts.length == 2) {
			prefix = parts[0];
			localpart = parts[1];
		} else if(parts.length == 1) {
			prefix = JDOMConstants.NS_PREFIX_DEFAULT;
			localpart = parts[0];
		} else {
			throw new IllegalArgumentException(
					String.format("QName %s is not well formed", qnameExpr));
		}
		return createQName(prefix, localpart);
	}

	/**
	 * Retrieve a {@link Namespace} whose prefix maps to {@code uri}.  The 
	 * returned {@code Namespace} is from the innermost context in which a
	 * mapping to {@code uri} was {@link #add(org.jdom2.Namespace) added}.  If
	 * multiple mappings to {@code uri} where added in that same context, the
	 * returned {@code Namespace} is an arbitrary one from that context.
	 * 
	 * @param uri	the {@link URI} to which a mapping was added; e.g. 
	 * 		{@link Namespace#getURI()}.
	 * 
	 * @return	the {@link Namespace} mapping a prefix to {@code uri}.
	 * 
	 * @throws NoSuchElementException	when no prefix is mapped to {@code uri}.
	 */
	public Namespace resolveUri(final URI uri)
			throws NoSuchElementException {
		if(uriToNs.containsKey(uri)) {
			Map<String, Namespace> namespaces = uriToNs.get(uri);
			if(!namespaces.isEmpty()) {
				return namespaces.values().iterator().next();
			}
		}
		if(parent != null) {
			return parent.resolveUri(uri);
		} else {
			throw new NoSuchElementException(
					String.format("uri %s not found", uri.toString()));
		}
	}

	/**
	 * Return all {@link Namespace}s that map to {@code uri}.  Only 
	 * {@code Namespace}s that in the current context map to {@code uri} are
	 * returned.
	 * 
	 * <p>Example:
	 * <code><pre>	NameSpaceResolver nsrOuter;
	 * 	String prefix1, prefix2
	 * 	URI targetUri, otherUri;
	 * 	nsrOuter.add(new Namespace(prefix1, targetUri.toString());
	 * 	nsrOuter.add(new Namespace(prefix2, targetUri.toString());
	 * 
	 * 	for(Namespace iNs1 : nsrOuter.getAllMappedTo(targetUri)) {
	 * 		assert(iNs1.getPrefix().equalTo(prefix1) ||
	 * 			iNs1.getPrefix().equalTo(prefix2));
	 * 	}
	 * 
	 * 	NameSpaceResolver nsrInner = nsrOuter.pushContext();
	 * 	nsrInner.add(new Namespace(prefix2, otherUri.toString());
	 * 
	 * 	for(Namespace iNs2 : nsrInner.getAllMappedTo(targetUri)) {
	 * 		assert(iNs2.getPrefix().equalTo(prefix1);
	 * 	}
	 * 
	 * 	for(Namespace iNs3 : nsrInner.getAllMappedTo(targetUri)) {
	 * 		assert(!iNs3.getPrefix().equalTo(prefix2));
	 * 	}</code></pre>
	 * 
	 * <p><i>WARNING: this method is computationally expensive!</i></p>
	 * 
	 * @param uri	the {@link URI} to search for
	 * 
	 * @return	an unmodifiable collection of {@link Namespace}s mapping a
	 * 		{@link Namespace#getPrefix() prefix} to a {@link Namespace#getURI() 
	 * 		namespace URI} equal to {@code uri}.  When {@code this} 
	 * 		{@code NameSpaceResolver} does not {@link #containsUri(java.net.URI) 
	 * 		contain} {@code uri}, the returned collection is 
	 * 		{@link Collection#isEmpty() empty}.
	 */
	public Collection<Namespace> getAllMappedTo(final URI uri) {
		return Collections.unmodifiableCollection(
				_getAllMappedToUri(uri, new HashMap<String, Namespace>()).values());
	}

	/**
	 * Add {@code ns} to the set of {@link Namespace}s for {@code nsUri} in
	 * {@link #uriToNs}.
	 * 
	 * @param nsUri	the key in {@code uriToNs}
	 * @param ns	the {@link Namespace} to add
	 * @param replace	<ul><li>{@code true}, remove {@code previous} adding
	 * 			{@code ns} in its place; or</li>
	 * 		<li>{@code false}, add {@code ns} remove nothing.</li></ul>
	 * @param previous 	<ul><li>the previously mapped {@link Namespace} to 
	 * 			remove; or</li>
	 * 		<li>{@code null}, there is no {@code Namespace} to remove, 
	 * 			{@code replace} must be {@code false}</li></ul>
	 */
	private void addReverseMapping(final URI nsUri, final Namespace ns,
			final boolean replace, final /*@Nullable*/ Namespace previous) {
		if (!uriToNs.containsKey(nsUri)) {
			uriToNs.put(nsUri, new ConcurrentHashMap<String, Namespace>());
		} 
		Map<String, Namespace> namespaces = uriToNs.get(nsUri);
		if (replace) {
			assert(previous != null);
			namespaces.remove(previous.getPrefix());
		}
		namespaces.put(ns.getPrefix(), ns);
	}

	/**
	 * Implementation of {@link #getAllMappedTo(java.net.URI)}:
	 * <ul><li>recurse from outer most context to current context;</li>
	 * <li>remove all prefixes overridden in the current context; and</li>
	 * <li>add all prefixes mapped to {@code uri} in the current context</li></ul>
	 * 
	 * @see #getAllMappedTo(java.net.URI)
	 * 
	 * @param uri		the {@link URI} to search for
	 * @param result	map with results that is modified by {@code this}
	 * 		{@code NameSpaceResolver} and all enclosing contexts; {2code result}
	 * 		is returned by this method. 
	 * 
	 * @return	a modifiable map of prefix–namespace (with {@code uri}) mappings
	 * 		valid with respect to {@code this} context.
	 */
	private Map<String, Namespace> _getAllMappedToUri(
			final URI uri, Map<String, Namespace>result) {
		if(parent != null) {
			result = parent._getAllMappedToUri(uri, result);
		}
		result.keySet().removeAll(this.prefixToNs.keySet());
		if(this.uriToNs.containsKey(uri)) {
			result.putAll(this.uriToNs.get(uri));
		}

		return result;
	}
}
