/*
 * RepositoryFactory.java
 *
 * Created on January 31, 2006, 12:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

/**
 * This class is used to create {@link Repository} class instances. (following the Factory design pattern)
 * This allows the service administrator to select which implementation (e.g. SesameRepository, JenaRepository, etc.)
 * is to be used by a server by editing a configuration file.
 * @author wrvhage
 */
public final class RepositoryFactory 
{
    
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RepositoryFactory.class);

    private static String repositoryFactoryImplName;
    private static Class repositoryFactoryImpl;
    private static boolean repositoryFactoryImplOk;
    private static Exception repositoryFactoryImplException;
    
    static 
    {
        // TODO: get this string from a configuration file
        /*
         * By getting this string from a configuration file using apache commons configuration 
         * you can allow dynamic configuration. (not implemented yet)
         */
        repositoryFactoryImplName = "org.vle.aid.metadata.Sesame2RepositoryFactoryImpl";
 //       repositoryFactoryImplName = "org.vle.aid.metadata.JenaRepositoryFactoryImpl";
        repositoryFactoryImplOk = false;
        try 
        {
            // look up the Class with the name repositoryFactoryImplName
            repositoryFactoryImpl = java.lang.Class.forName(repositoryFactoryImplName);
            // check if it implements the RepositoryFactoryImpl interface
            Class[] interfaces = repositoryFactoryImpl.getInterfaces();
            for (int i=0;i<interfaces.length;i++) 
            {
                if (interfaces[i].equals(RepositoryFactoryImpl.class)) 
                {
                    repositoryFactoryImplOk = true;
                }
            }
        } 
        catch (Exception e) 
        {
        	e.printStackTrace(System.err);
            repositoryFactoryImpl = null;
            repositoryFactoryImplException = e;
        }
        
    }
    
    public static Repository createRepository() throws Exception 
    {
        if (repositoryFactoryImplOk) 
        {
            try 
            {
                // try to return the constructor
            	java.lang.reflect.Method m = repositoryFactoryImpl.getMethod("createRepository",null);
                RepositoryFactoryImpl rfi = (RepositoryFactoryImpl)repositoryFactoryImpl.newInstance();
                
                return (Repository)m.invoke(rfi,null);
            } 
            catch (Exception e) 
            {
                logger.error(e.getMessage(),e);
                repositoryFactoryImplException = e;                
            }
        } 
        else 
        {
            throw repositoryFactoryImplException;
        }
        return null;
    }
    
    public static Repository createRepository(
            String server, String repository,
            String username, String password) throws Exception 
    {
        if (repositoryFactoryImplOk) 
        {
            try 
            {
                // try to return the constructor
                java.lang.reflect.Method m = repositoryFactoryImpl.getMethod("createRepository",
                        new Class[]{String.class,String.class,String.class,String.class});
                RepositoryFactoryImpl rfi = (RepositoryFactoryImpl)repositoryFactoryImpl.newInstance();
                return (Repository)m.invoke(rfi,new Object[]{server,repository,username,password});
            } 
            catch (Exception e) 
            {
            	e.printStackTrace(System.err);
                logger.error(e.getMessage(),e);
                repositoryFactoryImplException = e;                
            }
        } 
        else 
        {
            throw repositoryFactoryImplException;
        }
        return null;
    }
    
    public static Repository createRepository(
            String server, String repository,
            String username, String password,
            String rdf_format) throws Exception 
    {
        if (repositoryFactoryImplOk) 
        {
            try 
            {
                // try to return the constructor
                java.lang.reflect.Method m = repositoryFactoryImpl.getMethod("createRepository",
                        new Class[]{String.class,String.class,String.class,String.class,String.class});
                RepositoryFactoryImpl rfi = (RepositoryFactoryImpl)repositoryFactoryImpl.newInstance();
                return (Repository)m.invoke(rfi,new Object[]{server,repository,username,password,rdf_format});
            } 
            catch (Exception e) 
            {
                logger.error(e.getMessage(),e);
                repositoryFactoryImplException = e;                
            }
        } 
        else 
        {
            throw repositoryFactoryImplException;
        }
        return null;
    }
}

