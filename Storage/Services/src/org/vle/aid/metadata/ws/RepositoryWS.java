/*
 * RepositoryWS.java
 *
 * Created on January 30, 2006, 11:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata.ws;

import java.rmi.RemoteException;

import org.vle.aid.metadata.Repository;
import org.vle.aid.metadata.RepositoryFactory;
import org.vle.aid.metadata.exception.UnknownException;

/**
 *
 * @author wrvhage
 */
public class RepositoryWS 
{
    
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RepositoryWS.class);
    
    /** Creates a new instance of RepositoryWS */
    public RepositoryWS() 
    {
        
    }

    /**
     * Return a list of all repositories that match the "read_write" criterion.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame
     * @param read_write a string containing the optional characters "r" or "w" that indicate whether you want to see read-only repositories ("r") or read-write reposiory ("rw") for the indicated user. An empty string imposes no restriction.
     */
    public String[] getRepositories(
            String server_url,
            String username, String password,
            String read_write) throws RemoteException
    {
        try 
        {
            Repository r = RepositoryFactory.createRepository();
            return r.getRepositories(server_url,username,password,read_write);
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
     * Return a list of all repositories that match the "read_write" criterion.
	 * This version returns:
	 * 	-	<repository-id> and <repository-title> for Sesame repository
	 * 	-   <namedgraph> and    <namegraph label> for non Sesame repository
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame
     * @param read_write a string containing the optional characters "r" or "w" that indicate whether you want to see read-only repositories ("r") or read-write reposiory ("rw") for the indicated user. An empty string imposes no restriction.
     */
    public String[][] getRepositoriesLabel(
            String server_url,
            String username, String password,
            String read_write) throws RemoteException
    {
        try 
        {
            Repository r = RepositoryFactory.createRepository();
            return r.getRepositoriesLabel(server_url,username,password,read_write);
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
     * Add a string of RDF data (that encodes a set of triples) into a repository.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param rdf_format the encoding of the RDF (e.g. rdfxml, turtle, ntriples, etc.)
     * @param data_uri the default URI for the RDF snippet (in the sense of the "base uri" used in Sesame, e.g. to name blank nodes. Use the name of an ontology or file to which the snippet belongs.)
     * @param data the string of RDF data encoded in "rdf_format"
     * @return true or false, indicating success of the operation
     * @throws RemoteException 
     */    
    public boolean addRdf(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri, String data) throws RemoteException 
    {
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.addRdf(data_uri,data);
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
     * Add a string of RDF data (that encodes a set of triples) into a repository.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param rdf_format the encoding of the RDF (e.g. rdfxml, turtle, ntriples, etc.)
     * @param data_uri the default URI for the RDF snippet (in the sense of the "base uri" used in Sesame, e.g. to name blank nodes. Use the name of an ontology or file to which the snippet belongs.)
     * @param data the string of RDF data encoded in "rdf_format"
     * @param context the URI of an rdf:Bag that should "contain" a reified version of the triple after addition to the repository, using rdf:member (increases memory usage by a factor 6)
     * @return true or false, indicating success of the operation
     * @throws RemoteException
     */
    public boolean addRdfWithContext(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri, String data, String context) throws RemoteException
    {
        try
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.addRdf(data_uri,data,context);
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
     * Add an RDF triple into a repository.
     * @param subject the URI of the subject of the triple (e.g. http://www.example.com/eg#aResource)
     * @param predicate the URI of the predicate of the triple (e.g. http://www.w3.org/2000/01/rdf-schema#subClassOf)
     * @param object either the URI of the object of the triple, or a literal value that can be typed using XML Schema, or can be assigned a language. (e.g. http://www.example.com/eg#aResource or Reticulation or "Reticulation"^^<http://www.w3.org/2001/XMLSchema#string> or "Reticulation"@en. NB! that typing or assignment of a language requires you to (double)quote the string.)
     * @return true or false, indicating success of the operation
     * @throws RemoteException 
     */ 
    public boolean addRdfStatement(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.addRdfStatement(subject,predicate,object);
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
     * Add an RDF triple into a repository.
     * @param subject the URI of the subject of the triple (e.g. http://www.example.com/eg#aResource)
     * @param predicate the URI of the predicate of the triple (e.g. http://www.w3.org/2000/01/rdf-schema#subClassOf)
     * @param object either the URI of the object of the triple, or a literal value that can be typed using XML Schema, or can be assigned a language. (e.g. http://www.example.com/eg#aResource or Reticulation or "Reticulation"^^<http://www.w3.org/2001/XMLSchema#string> or "Reticulation"@en. NB! that typing or assignment of a language requires you to (double)quote the string.)
     * @param context the URI of an rdf:Bag that should "contain" a reified version of the triple after addition to the repository, using rdf:member (increases memory usage by a factor 6)
     * @return true or false, indicating success of the operation
     * @throws RemoteException 
     */ 
    public boolean addRdfStatementWithContext(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object, String context) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.addRdfStatement(subject,predicate,object,context);
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
     * Add an RDF triple into a repository.
     * @param file the URL of the file containing the RDF (e.g. http://... or file://..., check with your browser if the URL is correct.)
     * @return true or false, indicating success of the operation
     * @throws RemoteException 
     */ 
    public boolean addRdfFile(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.addRdfFile(data_uri);
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

    public boolean addRdfFileWithContext(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri, String context) throws RemoteException
	{
        try
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.addRdfFile(data_uri,context);
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
    
    public boolean removeRdf(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri, String data) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.removeRdf(data_uri,data);
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

    public boolean removeRdfWithContext(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri, String data, String context) throws RemoteException
	{
        try
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.removeRdf(data_uri,data,context);
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
    
    public boolean removeRdfStatement(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.removeRdfStatement(subject,predicate,object);
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
    
    public boolean removeRdfStatementWithContext(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object, String context) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.removeRdfStatement(subject,predicate,object,context);
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
    
    public boolean removeRdfFile(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.removeRdfFile(data_uri);
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

    public boolean removeRdfFileWithcontext(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri,String context) throws RemoteException
	{
        try
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.removeRdfFile(data_uri,context);
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

    public boolean clear(
            String server_url,String repository,
            String username,String password) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.clear();
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

    public String extractRdf(
            String server_url, String repository,
            String username, String password,
            String rdf_format) throws RemoteException 
	{
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.extractRdf();
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
     * Pose a "CONSTRUCT" query to the repository.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param query_language the language in which the query is formulated (e.g. sparql, serql, etc.)
     * @param query the query (e.g. construct {s} p {o} from {o} p {s})
     * @param rdf_format the format of the resulting RDF that is returned
     * @return a string containing an encoding of all constructed triples
     * @throws RemoteException 
     */
    public String constructQuery(
            String server_url, String repository,
            String username, String password,
            String query_language, String rdf_format,
            String query) throws RemoteException 
    {
        try 
        {
            Repository r = RepositoryFactory.createRepository();
            return r.constructQuery(server_url,repository,username,password,query_language,rdf_format,query);
        } 
        catch (RemoteException e1)
        {
        	if (e1.detail.getClass().toString().equalsIgnoreCase(
					"class org.openrdf.repository.http.HTTPQueryEvaluationException"))
        	{
        		String msg = modifyRemoteExceptionMessage(e1);

        		throw new RemoteException(msg);
        	}
        	else throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
    }
    

    /**
     * Pose a "SELECT" query to the repository. (for HTML table output)
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param query_language the language in which the query is formulated (e.g. sparql, serql, etc.)
     * @param query the query (e.g. select distint p from {s} p {o})
     * @param select_output_format the format of the resulting table that contains the results (currently only "html_table" is supported. This operation is only meant for the presentation of the results to human viewers.)
     * @return a string containing an encoding of all selected values
     * @throws RemoteException 
     */
    public String selectQuerySerialized(
            String server_url, String repository,
            String username, String password,
            String query_language, String select_output_format,
            String query) throws RemoteException {
        try 
        {
            Repository r = RepositoryFactory.createRepository();
            return r.selectQuery(server_url,repository,username,password,query_language,select_output_format,query);
        } 
        catch (RemoteException e1)
        {
        	if (e1.detail.getClass().toString().equalsIgnoreCase(
					"class org.openrdf.repository.http.HTTPQueryEvaluationException"))
        	{
        		String msg = modifyRemoteExceptionMessage(e1);
        	
        		throw new RemoteException(msg);
        	}
        	else throw e1;
        }
        catch (Throwable e2)
        {
           logger.error(e2.getMessage(),e2);
           throw new UnknownException("Unknown Exception:"+e2.getMessage(),e2);
        }
    }

    /**
     * Pose a "SELECT" query to the repository. (for use in a program)
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param query_language the language in which the query is formulated (e.g. serql, sparql, etc.)
     * @param query the query (e.g. select distinct p from {s} p {o})
     * @return an array of arrays containing the result table (e.g. "select s, o from {s} p {o}" could return [["http://www.example.com/eg#Doormat","Doormat"],["http://www.example.com/eg#Doorknob","Doorknob"], ...])
     * @throws RemoteException 
     */    
    public String[][] selectQuery(
            String server_url, String repository,
            String username, String password,
            String query_language, String query) throws RemoteException
    {
        try 
        {
        	Repository r = RepositoryFactory.createRepository();
            return r.selectQuery(server_url,repository,username,password,query_language,query);
        }
        catch (RemoteException e1)
        {
			if (e1.detail.getClass().toString().equalsIgnoreCase(
							"class org.openrdf.repository.http.HTTPQueryEvaluationException"))
            {
				String msg = modifyRemoteExceptionMessage(e1);

				throw new RemoteException(msg);
			}
            else
				throw e1;
		} 
        catch (Throwable e2)
        {
			logger.error(e2.getMessage(), e2);
			throw new UnknownException("Unknown Exception:" + e2.getMessage(),e2);
		}
    }
    
    /**
     * Reformats the message of the remote exceptions coming from the implementation of selectQuery,
     * when those are caused by a repository connect/query related error, from a hierarchy of nested 
     * axis Sesame 2 http-repository error texts (each one in html-format) into a human readable text. 
     * This text contains only the last nested exception message, which is the one which will always 
     * make sense to the enduser as caused by wrong connection parameters or malformed queries.
     * @param the remote exception e1
     * @return the reformatted message msg
     */
    private String modifyRemoteExceptionMessage(RemoteException e1)
    {
    	String msg = e1.detail.getMessage();
    	
		msg = msg.replaceAll("</b>\\s<u>", ": ");
		msg = msg.replaceAll("</b>", ":");
		
		String[] msg_init_parse = msg.split("<[^<>]+>");
		String msg_clean_parse = "";
		
		for (int i = 0; i < msg_init_parse.length; i++)
		{
			if (!msg_init_parse[i].trim().equalsIgnoreCase(""))
			{
				msg_clean_parse = msg_clean_parse + msg_init_parse[i].trim() + "\n";  
			}
		}
		String[] msg_clean_parse_array = msg_clean_parse.split("\n");
		
		for (int i = 0; i < msg_clean_parse_array.length; i++)
		{
			if (msg_clean_parse_array[i].contains("description:"))
			{
				msg = "\n\nA Repository Error Occured:\n"
					+ "---------------------------\n"
					+ msg_clean_parse_array[i].substring(13) + "\n\n"
					+ "Detailed Report follows:\n"
					+ "------------------------\n"
					+ msg_clean_parse + "\n";
				break;
			}					
        }			
		return msg;    	
    }
}
