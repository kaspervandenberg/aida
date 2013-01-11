/*
 * RepositoryAccessException.java
 *
 * Created on February 13, 2006, 1:14 PM
 */

package org.vle.aid.bicad;

/**
 *
 * @author  Camille
 */
public class RepositoryAccessException extends java.lang.Exception {
    
    /**
     * Constructor for the RepositoryAccessException class. 
     *
     * @param   errmsg error message
     * @return  An instance of the RepositoryAccessException class
     */    
    public RepositoryAccessException( String errmsg ) {
        super( errmsg );
    }
}
