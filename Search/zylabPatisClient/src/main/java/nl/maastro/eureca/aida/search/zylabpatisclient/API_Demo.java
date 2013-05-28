/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.xml.rpc.ServiceException;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import org.apache.lucene.search.Query;

/**
 *
 * @author kasper2
 */
public class API_Demo {
	static public void main(String[] args) {
		try {
			// Read config file
			// InputStream s = new FileInputStream("/home/kasper2/git/aida.git/Search/zylabPatisClient/src/main/webapp/WEB-INF/zpsc-config.xml");
			InputStream s = new FileInputStream("/home/administrator/aida.git/Search/zylabPatisClient/src/main/webapp/WEB-INF/zpsc-config.xml");
			Config config = Config.init(s);

			// Use config to initialise a searcher
			Searcher searcher = config.getSearcher();

			// Dummy list of patients; reading a list of patisnumbers is not yet in API
			List<PatisNumber> patients = Arrays.<PatisNumber>asList(
					PatisNumber.create("12345"),
					PatisNumber.create("11111"),
					PatisNumber.create("46467"),
					PatisNumber.create("70986"));

			// Get a QueryPattern; normally the Query is retrieved via its
			// URI and not via an internal enum constant
			Query queryPattern = PreconstructedQueries.instance().getQuery(
					PreconstructedQueries.LocalParts.METASTASIS_IV);
			Iterable<SearchResult> result = searcher.searchForAll(
					queryPattern,
					patients);

			// Do something with the results
			for (SearchResult searchResult : result) {
				System.out.printf("PatisNr: %s found in %d documents",
						searchResult.patient.value, searchResult.nHits);
			}
			
			
			
		} catch (ServiceException | IOException ex) {
			throw new Error(ex);
		}
		
	}
}
