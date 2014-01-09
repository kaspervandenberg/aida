/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.jmx;

import javax.management.InstanceAlreadyExistsException;

/**
 *
 * @author kasper
 */
public class TryJmx {
	
	public static int main(String[] args) {
		try {
			IndexerService service = IndexerService.createAndregister();
			for ( ;; ) {
				// run forever
			}
		} catch (InstanceAlreadyExistsException ex) {
			throw new Error(ex);
		}
		//return 0;
	}
}
