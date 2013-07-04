// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import nl.maastro.eureca.aida.search.zylabpatisclient.query.DualRepresentationQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryProvider;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTree;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTreeToObjectAdapter;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQuery;

/**
 * Contains preconstructed queries to search for oncological concepts
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PreconstructedQueries {
	
	/**
	 * Build an {@link QName} from {@link #PREFIX}, the namespace URI
	 * {@link #getNamespaceUri() } and this local part.  {@code createQName()}
	 * uses {@link QName#QName(java.lang.String, java.lang.String)}
	 * to build the requested {@code QName}.
	 * 
	 * @param localpart	the local part of the QName to create
	 * 
	 * @return	the constructed {@link QName}
	 * 
	 * @throws URISyntaxException when the constructed URI has an syntax 
	 * 		error. 
	 */
	QName createQName(String localpart) 
			throws URISyntaxException {
		return new QName(getNamespaceUri().toString(), localpart, PREFIX);
	}

	public static class Provider implements QueryProvider {
		@Override
		public Collection<QName> getQueryIds() {
			return PreconstructedQueries.instance().getIds();
		}


		@Override
		public boolean provides(QName id) {
			return PreconstructedQueries.instance().getIds().contains(id);
		}

		@Override
		@Deprecated
		public boolean hasString(QName id) {
			return false;
		}

		@Override
		@Deprecated
		public boolean hasObject(QName id) {
			return PreconstructedQueries.instance().getIds().contains(id);
		}

		@Override
		@Deprecated
		public String getAsString(QName id) throws NoSuchElementException {
			throw new NoSuchElementException("PreconstructedQueries only provides Lucene Query objects");
		}

		@Override
		@Deprecated
		public org.apache.lucene.search.Query getAsObject(QName id) throws NoSuchElementException {
			nl.maastro.eureca.aida.search.zylabpatisclient.query.Query q =
					PreconstructedQueries.instance().getQuery(id);
			return q.accept(new Query.Visitor<org.apache.lucene.search.Query>() { 
				@Override
				public org.apache.lucene.search.Query visit(LuceneObject element) {
					return element.getRepresentation();
				}

				@Override
				public org.apache.lucene.search.Query visit(StringQuery element) {
					throw new IllegalStateException("Expected parse tree.");
				}

				@Override
				public org.apache.lucene.search.Query visit(ParseTree element) {
					return ADAPTER_BUILDER.adapt(element).getRepresentation();
				}
			});
		}

		@Override
		public nl.maastro.eureca.aida.search.zylabpatisclient.query.Query get(QName id) {
			return PreconstructedQueries.instance().getStoredPredicates().get(id).getQuery();
		}
	}
			
	private static final String SEARCH_PROPERTY_RESOURCE = "/search.properties";
	private static final String SERVLET_URI_PROP = "nl.maastro.eureca.aida.search.zylabpatisclient.servletUri";
	private static final String DEFAULT_SERVLET_URI = "http://clinisearch.ad.maastro.nl/zylabpatis";
	private static final String PREFIX = "pcq";
	private static final ParseTreeToObjectAdapter.Builder ADAPTER_BUILDER =
			new ParseTreeToObjectAdapter.Builder();
	
	private static URI servletUri = null;
	
	/**
	 * Lucene DEFAULT_FIELD to use for the {@link SearchTerms}.
	 */
	private final String DEFAULT_FIELD = "content";

	private final DualRepresentationQuery.Visitable VISITABLE_DELEGATE =
			DualRepresentationQuery.Visitable.AS_LUCENE_OBJECT;

	/**
	 * Queries to search for patients that match certain predicates.
	 * 
	 */
	// TODO Move to SearcherWS and provide interface to access stored queries
	private Map<QName, LocalParts> storedPredicates = null;

	/**
	 * Singleton instance, use {@link #instance()} to access
	 */
	private static PreconstructedQueries instance;

	/**
	 * Singleton, use {@link #instance()} to retrieve the sole instance.
	 */
	private PreconstructedQueries() {
	}

	/**
	 * Access this singleton
	 * 
	 * @return the sole instance of {@code PreconstructedQueries}
	 */
	public static PreconstructedQueries instance() {
		if(instance == null) {
			instance = new PreconstructedQueries();
		}
		return instance;
	}

	private nl.maastro.eureca.aida.search.zylabpatisclient.query.Query
			getQuery(final QName key) {
		return getStoredPredicates().get(key).getQuery();
	}
	
	public Collection<QName> getIds() {
		return Collections.unmodifiableSet(getStoredPredicates().keySet());
	}
	
	public String getDefaultField() {
		return DEFAULT_FIELD;
	}

	public DualRepresentationQuery.Visitable getVisitableDelegate() {
		return VISITABLE_DELEGATE;
	}
	
	/**
	 * @return the storedPredicates
	 */
	private Map<QName, LocalParts> getStoredPredicates() {
		if(storedPredicates == null) {
			Map<QName, LocalParts> tmp = new HashMap<>();
			for (LocalParts part : LocalParts.values()) {
				tmp.put(part.getID(), part);
			}
			storedPredicates = Collections.unmodifiableMap(tmp);
		}
		return storedPredicates;
	}

	private static URI getNamespaceUri() {
		if(servletUri == null) {
			InputStream propertyFile = PreconstructedQueries.class.getResourceAsStream(SEARCH_PROPERTY_RESOURCE);
			Properties props = new Properties();
			try {
				props.load(propertyFile);
				String s_uri = props.getProperty(SERVLET_URI_PROP, DEFAULT_SERVLET_URI);
				servletUri = new URI(s_uri);
				
			} catch (IOException | URISyntaxException ex) {
				throw new Error(ex);
			}
		}
		return servletUri;
	}
}
