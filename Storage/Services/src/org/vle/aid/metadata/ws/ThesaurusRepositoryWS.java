/*
 * ThesaurusRepositoryWS.java
 *
 * Created on March 7, 2006, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata.ws;

import java.lang.reflect.Array;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.vle.aid.metadata.ThesaurusRepository;
import org.vle.aid.metadata.exception.UnknownException;
import org.vle.aid.thesaurus.client.ThesaurusConceptNode;

/**
 * 
 * @author wrvhage
 */
@Path("/thesaurusbrowser")
public class ThesaurusRepositoryWS 
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

	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RepositoryWS.class);
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
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
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
	
	@SuppressWarnings("unchecked")
  private static <T> T[] arrayMerge(T[]... arrays) {

	    int count = 0;
	    for (T[] array : arrays) {
	        count += array.length;
	    }

	    T[] mergedArray = (T[]) Array.newInstance( arrays[0][0].getClass(),count);

	    int start = 0;
	    for (T[] array : arrays) {
	        System.arraycopy(array, 0,
	           mergedArray, start, array.length);
	        start += array.length;
	    }
	    return (T[]) mergedArray;
	} 

	
	/**
   * Returns the narrower and alternative concepts for a concept
   * 
   * @param form the parameters
   * @return narrower and alternative concepts 
   * @throws ServletException When a param is missing
   * @throws RemoteException 
   */
  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  @Path("/narroweralts")
  public ThesaurusConceptNode[] narrowerAndAlts(
      MultivaluedMap<String, String> param
  ) throws ServletException, RemoteException {
    
    ThesaurusConceptNode[] narrower = narrower(param);
    ThesaurusConceptNode[] alts = alternatives(param);
    if (alts.length > 0 && narrower.length > 0) {
      return arrayMerge(alts, narrower);
    } else if (alts.length == 0) {
      return narrower;
    } else if (narrower.length == 0) {
      return alts;
    } else {
      return new ThesaurusConceptNode[0];
    }
  }
  
  /**
   * Returns the narrower and alternative concepts for a concept
   * 
   * @param ui the parameters
   * @return narrower and alternative concepts  
   * @throws ServletException When a param is missing
   * @throws RemoteException 
   */
  @GET
  @Produces("application/json")
  @Path("/narroweralts")
  public ThesaurusConceptNode[] narrowerAndAlts(
      @Context UriInfo ui
  ) throws ServletException, RemoteException {
    return narrowerAndAlts(ui.getQueryParameters());
  }
	
  /**
   * Returns the narrower concepts for a concept
   * 
   * @param form the parameters
   * @return children
   * @throws ServletException When a param is missing
   * @throws RemoteException 
   */
  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  @Path("/narrower")
  public ThesaurusConceptNode[] narrower(
      MultivaluedMap<String, String> param
  ) throws ServletException, RemoteException {
    
    ThesaurusRepository tr = new ThesaurusRepository(param);
    
    String focus_term = null;
    if (!param.containsKey("term")) 
    {
      focus_term = null;
    } 
    else 
    {
      focus_term = param.getFirst("term");
    }
    
    // extjs specific
    if (param.containsKey("node"))
      focus_term = param.getFirst("node");
    
    // if it contains an explicit id, use that
    if (param.containsKey("uri"))
      focus_term = param.getFirst("uri");
    
    if (focus_term != null) {
      // extjs
      if (focus_term.startsWith("extjs_s_"))
        focus_term = focus_term.replaceFirst("extjs_s_", "");
  
      // Check if we're dealing with a term or a uri
      if (!focus_term.startsWith("http://")) {
        return new ThesaurusConceptNode[0];
        //String terms[][] = tr.getTermUri(focus_term);
        //if (terms.length > 0)
          //focus_term = terms[0][0];
        //else 
          //throw new ServletException(focus_term + " not found!");
        
      }
    }
    
    String[][] _narrower = tr.getNarrowerTerms(focus_term);

    if (_narrower == null)
      return new ThesaurusConceptNode[0];;

    ThesaurusConceptNode[] out = new ThesaurusConceptNode[_narrower.length];
    for (int j = 0; j < _narrower.length; j++) {
      out[j] = new ThesaurusConceptNode(
          _narrower[j][0], // URI 
          _narrower[j][1], // label
          "Narrower Term: " + _narrower[j][0], // qtip
          "narrower-icon" // iconCls
          );
    }
    
    return out;
  }
  
  /**
   * Returns the narrower concepts for a concept
   * 
   * @param ui the parameters
   * @return children
   * @throws ServletException When a param is missing
   * @throws RemoteException 
   */
  @GET
  @Produces("application/json")
  @Path("/narrower")
  public ThesaurusConceptNode[] narrower(
      @Context UriInfo ui
  ) throws ServletException, RemoteException {
    return narrower(ui.getQueryParameters());
  }

	public String[][] getBroaderTerms(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
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
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
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
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
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

	public String[] getPreferedTerms(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getPreferedTerms(term);
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
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
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
	
	/**
   * Returns the alternative labels for a concept
   * 
   * @param form the parameters
   * @return labels
   * @throws ServletException When a param is missing
   * @throws RemoteException 
   */
  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  @Path("/alternatives")
  public ThesaurusConceptNode[] alternatives(
      MultivaluedMap<String, String> param
  ) throws ServletException, RemoteException {
    
    ThesaurusRepository tr = new ThesaurusRepository(param);
    
    String focus_term = null;
    if (!param.containsKey("term")) 
    {
      focus_term = null;
    } 
    else 
    {
      focus_term = param.getFirst("term");
    }
    
    // extjs specific
    if (param.containsKey("node"))
      focus_term = param.getFirst("node");
    
    // if it contains an explicit id, use that
    if (param.containsKey("uri"))
      focus_term = param.getFirst("uri");
    
    if (focus_term != null) {
      // extjs
      if (focus_term.startsWith("extjs_s_"))
        focus_term = focus_term.replaceFirst("extjs_s_", "");
  
      // Check if we're dealing with a term or a uri
      if (!focus_term.startsWith("http://")) {
        return new ThesaurusConceptNode[0];
        //String terms[][] = tr.getTermUri(focus_term);
        //if (terms.length > 0)
          //focus_term = terms[0][0];
        //else 
          //throw new ServletException(focus_term + " not found!");
        
      }
    }
    
    String[] alts = tr.getAlternativeTerms(focus_term);

    if (alts == null)
      return new ThesaurusConceptNode[0];;

    ArrayList<ThesaurusConceptNode> temp = new ArrayList<ThesaurusConceptNode>();
    
    for (int j = 0; j < alts.length; j++) {
      
      if (focus_term.equalsIgnoreCase(alts[j]))
        continue;     
      
      temp.add(new ThesaurusConceptNode(
          alts[j], // URI 
          alts[j], // label
          "Alternative Term: " + alts[j], // qtip
          "alt-icon" // iconCls
      ));
      
    }
    
    return temp.toArray(new ThesaurusConceptNode[0]);
  }
  
  /**
   * Returns the alternative terms for a concept
   * 
   * @param ui the parameters
   * @return alternative labels
   * @throws ServletException When a param is missing
   * @throws RemoteException 
   */
  @GET
  @Produces("application/json")
  @Path("/alternatives")
  public ThesaurusConceptNode[] alternatives(
      @Context UriInfo ui
  ) throws ServletException, RemoteException {
    return alternatives(ui.getQueryParameters());
  }

	/*
	 * Documentation
	 */

	public String[] getDefinitions(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getDefinitions(term);
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

	public String[] getNotes(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getNotes(term);
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

	public String[] getScopeNotes(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getScopeNotes(term);
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

	public String[] getChangeNotes(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getChangeNotes(term);
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

	public String[] getHistoryNotes(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getHistoryNotes(term);
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

	public String[] getEditorialNotes(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getEditorialNotes(term);
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

	public String[] getPublicNotes(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getPublicNotes(term);
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

	public String[] getPrivateNotes(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getPrivateNotes(term);
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
	 * Subject Indexing
	 */

	/**
	 * Get skos:Concepts that are used to annotate the resource identified by
	 * "url".
	 * 
	 * @param url
	 *            the URI of a resource that is annotated with skos:Concepts
	 *            using skos:isSubjectOf
	 * @return an array of URI's of skos:Concepts
	 */
	public String[] getSubjectsOf(String server_url, String repository,
			String username, String password, String url) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getSubjectsOf(url);
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
	 * Get URI's of resources that have been annotated with skos:Concepts
	 * "term".
	 * 
	 * @param term
	 *            the URI of a skos:Concept that is used to annotate resources
	 *            using skos:subject
	 * @return an array of URI's of resources that have been annotated with
	 *         skos:Concept "term"
	 */
	public String[] getSubjects(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getSubjects(term);
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

	public String[] getPrimarySubjects(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getPrimarySubjects(term);
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

	public String[] getPrimarySubjectsOf(String server_url, String repository,
			String username, String password, String url) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getPrimarySubjectsOf(url);
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
	 * Concept Schemes
	 */

	public String[][] getTopConcepts(String server_url, String repository,
			String username, String password, String scheme_label) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
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
   * Returns the topconcepts for a given repository
   * 
   * @param form the parameters
   * @return top concepts
   * @throws ServletException When a param is missing
	 * @throws RemoteException 
   */
  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  @Path("/rootnodes")
  public Topterms rootnodes(
      // Form form
      MultivaluedMap<String, String> param
  ) throws ServletException, RemoteException {
    
    String ns = "null";
    ThesaurusRepository tr = new ThesaurusRepository(param);
    if (param.containsKey("ns")) 
    {
      ns = param.getFirst("ns");
    }

    Vector<String> done = new Vector<String>();
    String[][] cschemes = tr.getConceptSchemes(ns);

    // sort here
    java.util.TreeMap<String,String> out = new java.util.TreeMap<String,String>();

    /* Experiment what if I just ignore the whole concept schemes, does not really worth the wait  */

    if (cschemes == null || cschemes.length >= 0){

	   // Still try to get top concept without concept schemes
	   // Could be needed for non skos types.	
	   String[][] top = tr.getTopConcepts("");
	   for (int k = 0; k < top.length; k++) 
	    {
		  out.put(top[k][1], top[k][0]);
	    }

    	  return new Topterms(out);
    }
    

    for (int i = 0; i < cschemes.length; i++) 
    {
      for (int j = 0; j < cschemes[i].length; j++) 
      {
        String _scheme = cschemes[i][j];
        
        //logger.info("scheme: " + _scheme);

	if (_scheme == null)
	  continue;

        if (!_scheme.startsWith("http://"))
          continue;

        if (done.contains(_scheme))
          continue;

        done.add(_scheme);
        String[][] top = tr.getTopConcepts(_scheme);

        if (top == null)
          continue;

        for (int k = 0; k < top.length; k++) 
        {
          out.put(top[k][1], top[k][0]);
        }
      }
    }
    
    return out == null ? null : new Topterms(out); 
  }
  
  /**
   * Returns the topconcepts for a given repository
   * 
   * @param ui the parameters
   * @return top concepts
   * @throws ServletException When a param is missing
   * @throws RemoteException 
   */
  @GET
  @Produces("application/json")
  @Path("/rootnodes")
  public Topterms rootnodes(
      @Context UriInfo ui
  ) throws ServletException, RemoteException {
    return rootnodes(ui.getQueryParameters());
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
			String namespace) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
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
			String username, String password) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getConceptSchemes();
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

	public String[][] getInSchemes(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getInSchemes(term);
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

	public String[][] getInCollections(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getInCollections(term);
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

	public String[][] getCollectionMembers(String server_url,
			String repository, String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getCollectionMembers(term);
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
	 * SKOS Mapping
	 */
	public String[][] getMatches(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getMatches(term);
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

	public String[][] getExactMatches(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getExactMatches(term);
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

	public boolean removeExactMatch(String server_url, String repository,
			String username, String password, String subject, String object) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.removeExactMatch(subject, object);
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

	public String[][] getDisjointMatches(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getDisjointMatches(term);
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

	public boolean removeDisjointMatch(String server_url, String repository,
			String username, String password, String subject, String object) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.removeDisjointMatch(subject, object);
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

	public String[][] getRelatedMatches(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getRelatedMatches(term);
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

	public boolean removeRelatedMatch(String server_url, String repository,
			String username, String password, String subject, String object) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.removeRelatedMatch(subject, object);
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

	public String[][] getNarrowMatches(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getNarrowMatches(term);
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

	public boolean removeNarrowMatch(String server_url, String repository,
			String username, String password, String subject, String object) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.removeNarrowMatch(subject, object);
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

	public String[][] getBroadMatches(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getBroadMatches(term);
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

	public boolean removeBroadMatch(String server_url, String repository,
			String username, String password, String subject, String object) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.removeBroadMatch(subject, object);
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

	public String[][] getPartMatches(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getPartMatches(term);
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

	public String[][] getTermCompletion(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
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
   * Looks up concepts
   * 
   * @param form the parameters
   * @return suggestions
   * @throws ServletException When a param is missing
	 * @throws RemoteException 
   */
  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/json")
  @Path("/suggest")
  public ThesaurusConceptNode[] suggest(
      MultivaluedMap<String, String> param
  ) throws ServletException, RemoteException {
    
    String focus_term = null;
    if (!param.containsKey("term")) 
    {
      focus_term = null;
    } 
    else 
    {
      focus_term = param.getFirst("term");
    }
    
    // extjs specific
    if (param.containsKey("node"))
      focus_term = param.getFirst("node");
    
    // if it contains an explicit id, use that
    if (param.containsKey("uri"))
      focus_term = param.getFirst("uri");
    
    ThesaurusRepository tr = new ThesaurusRepository(param);
    String[][] suggestions = tr.getTermCompletion(focus_term);
    
    if (suggestions == null || suggestions.length == 0)
      return null;
    
    ThesaurusConceptNode[] out = new ThesaurusConceptNode[suggestions.length];
    for (int j = 0; j < suggestions.length; j++) {
      out[j] = new ThesaurusConceptNode(
          suggestions[j][0], // URI 
          suggestions[j][1]  // label
          );
    }

    return out;
  }
  
  /**
   * Looks up concepts
   * 
   * @param ui the parameters
   * @return suggestions
   * @throws ServletException When a param is missing
   * @throws RemoteException 
   */
  @GET
  @Produces("application/json")
  @Path("/suggest")
  public ThesaurusConceptNode[] suggest(
      @Context UriInfo ui
  ) throws ServletException, RemoteException {
    return suggest(ui.getQueryParameters());
  }

	public boolean removePartMatch(String server_url, String repository,
			String username, String password, String subject, String object) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.removePartMatch(subject, object);
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

	public String[][] getWholeMatches(String server_url, String repository,
			String username, String password, String term) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.getWholeMatches(term);
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

	public boolean removeWholeMatch(String server_url, String repository,
			String username, String password, String subject, String object) throws RemoteException 
	{
		try 
		{
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
					repository, username, password);
			return tr.removeWholeMatch(subject, object);
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
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
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
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
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
			ThesaurusRepository tr = new ThesaurusRepository(server_url,
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
