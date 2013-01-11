/*
 * SkosLensWS.java
 *
 * Created on March 7, 2006, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata.ws;

import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Vector;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.vle.aid.metadata.SkosLens;
import org.vle.aid.metadata.SkosLensType;
import org.vle.aid.metadata.exception.UnknownException;
import org.vle.aid.metadata.ws.ThesaurusRepositoryWS.Topterms.Topterm;

/**
 *
 * @author wrvhage
 */
@Path("/skoslensbrowser")
public class SkosLensWS
{
	@Context UriInfo uriInfo;
	@Context Request request;
	  /**
	   * 
	   * Class to hold the topconcepts
	   * 
	   * @author emeij
	   *
	   */
	  public class Topterms {
	    
	    /**
	     * A single topconcept
	     * @author emeij
	     *
	     */
	    public class Topterm {
	      
	      /**
	       * ID of the concept
	       */
	      public String id;
	      
	      /**
	       * Label of the concept
	       */
	      public String term;
	      
	       
	      /**
	       * Creates a new Topterm
	      * @param id the ID
	      * @param term the label
	      */
	      public Topterm(String id, String term) {
	        this.id = id;
	        this.term = term;
	      }
	      
	    }

	    /** 
	     * The topterms
	     */
	    public final Collection<Topterm> topterms = new Vector<Topterm>();

	    /**
	     * Construct a new Topterms object with the terms in the argument
	     * @param terms the topconcepts to use
	     */
	    public Topterms(AbstractMap<String,String> terms) {
	      for (String k : terms.keySet()) {
	        this.topterms.add(new Topterm(terms.get(k), k));
	      }
	    }
	  }
	  
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SkosLensWS.class);
	
	
	/*
	 * Semantic Relations
	 */

	/**
	 * Find narrower Concepts and their prefLabel.
	 *
	 * @param server_url
	 *            the URL of the meta-data server that will be queried (e.g.
	 *            http://www.host.org:8080/sesame)
	 * @param repository
	 *            the name of the repository or model in the meta-data server
	 *            (e.g. mem-rdfs-db)
	 * @param username
	 *            the username of the user to access the repository as used by
	 *            the meta-data server (e.g. testuser)
	 * @param password
	 *            the password of the user to access the repository as used by
	 *            the meta-data server (e.g. opensesame)
	 * @param term
	 *            the URI of the "broader" skos:Concept, the "focus" concept.
	 * @return an array of pairs of skos:Concepts and their skos:prefLabel
	 * @throws RemoteException
	 */
	public String[][] getNarrowerTerms(String server_url, String repository,
			String username, String password, String term, String TOP_CONCEPT, String PREDICATE, String skosVersion, String virtuosoNamedGraph) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url, repository, username, password);
            tr.setLensType(TOP_CONCEPT, PREDICATE, skosVersion, virtuosoNamedGraph);
            return tr.getNarrowerTerms(term);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

    public Vector<SkosLensType> getAvailableLenses(String server_url, String repository,
			String username, String password){
            SkosLens tr = new SkosLens(server_url,repository, username, password);
			return tr.getAvailableLenses();
    }
    public SkosLensType getCurrentSkosLens(){
            return new SkosLensType("Default", "Default", "Default");

    }
	public String[][] getBroaderTerms(String server_url, String repository,
			String username, String password, String term, String TOP_CONCEPT, String PREDICATE, String skosVersion, String virtuosoNamedGraph) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url,
					repository, username, password);
			tr.setLensType(TOP_CONCEPT, PREDICATE, skosVersion, virtuosoNamedGraph);
            return tr.getBroaderTerms(term);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	public String[][] getRelatedTerms(String server_url, String repository,
			String username, String password, String term) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url,
					repository, username, password);
			return tr.getRelatedTerms(term);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	/*
	 * Labelling
	 */

	/**
	 * Find rdfs:label values for a Class, e.g. a skos:Collection or
	 * skos:ConceptScheme.
	 *
	 * @return an array of rdfs:labels, literal values
	 */
	public String[] getRDFSLabels(String server_url, String repository,
			String username, String password, String term) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url,
					repository, username, password);
			return tr.getRDFSLabels(term);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	public String[] getAlternativeTerms(String server_url, String repository,
			String username, String password, String term) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url,
					repository, username, password);
			return tr.getAlternativeTerms(term);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	/*
	 * Concept Schemes
	 */

	public String[][] getTopConcepts(String server_url, String repository,
			String username, String password, String scheme_label, String TOP_CONCEPT, String PREDICATE, String skosVersion, String virtuosoNamedGraph ) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url, repository, username, password);
            tr.setLensType(TOP_CONCEPT, PREDICATE, skosVersion, virtuosoNamedGraph);
          	return tr.getTopConcepts(scheme_label);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	/**
	 * Return all URI's of skos:ConceptSchemes in namespace "namespace".
	 *
	 * @param namespace
	 *            the URI of the namespace
	 * @return array of pairs of URI's of skos:ConceptSchemes and their
	 *         rdfs:label
	 */
	public String[][] getConceptSchemesWithNamespace(String server_url,
			String repository, String username, String password,
			String namespace, String TOP_CONCEPT, String PREDICATE, String skosVersion, String virtuosoNamedGraph) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url,	repository, username, password);
            tr.setLensType(TOP_CONCEPT, PREDICATE, skosVersion, virtuosoNamedGraph);
            return tr.getConceptSchemes(namespace);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	public String[][] getConceptSchemes(String server_url, String repository,
			String username, String password, String TOP_CONCEPT, String PREDICATE, String skosVersion, String virtuosoNamedGraph) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url, repository, username, password);
            tr.setLensType(TOP_CONCEPT, PREDICATE, skosVersion, virtuosoNamedGraph);
			//tr.setLensType("rdfs:Property", "rdfs:SubPropertyOf");
			logger.debug("CHECK123 "+skosVersion+ " "+ virtuosoNamedGraph);
            return tr.getConceptSchemes();
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("This Unknown Exception ? SV:"+skosVersion + " vng: "+ virtuosoNamedGraph + "  "+  e2.getMessage(),e2);
        }
	}

	public String[][] getTermCompletion(String server_url, String repository,
			String username, String password, String term) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url, repository, username, password);
			return tr.getTermCompletion(term);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	/**
	 * Gets the URI of all skos:Concepts with a skos:prefLabel or skos:altLabel
	 * that matches "term".
	 *
	 * @param term
	 *            a literal string that describes a concept. (e.g.
	 *            "Spline Reticulation")
	 * @return an array of pairs of skos:Concepts and their skos:prefLabel or
	 *         skos:altLabel (that matched "term")
	 * @throws RemoteException
	 */
	public String[][] getTermUri(String server_url, String repository,
			String username, String password, String term) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url,
					repository, username, password);
			return tr.getTermUri(term);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	public String[][] getNumberOfNarrowerTerms(String server_url,
			String repository, String username, String password, String term) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url,
					repository, username, password);
			return tr.getNumberOfNarrowerTerms(term);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}

	public String[][] getNumberOfNarrowerTermsMulti(String server_url,
			String repository, String username, String password, String terms[]) throws RemoteException
	{
		try
		{
			SkosLens tr = new SkosLens(server_url,
					repository, username, password);
			return tr.getNumberOfNarrowerTerms(terms);
		}
		catch (RemoteException e1)
        {
        	throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
	}
}
