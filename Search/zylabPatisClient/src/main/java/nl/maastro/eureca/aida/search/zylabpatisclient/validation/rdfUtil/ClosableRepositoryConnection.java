/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil;

import java.io.Closeable;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author kasper
 */
public interface ClosableRepositoryConnection extends RepositoryConnection, Closeable {
	
}