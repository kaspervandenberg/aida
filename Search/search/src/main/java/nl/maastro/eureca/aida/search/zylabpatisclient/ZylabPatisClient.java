// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.vocab.axis.services.SearcherWS.SearcherWS;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.xml.CoreParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ZylabPatisClient {
	private static final ForkJoinPool taskPool = new ForkJoinPool();
	private static final String DEFAULT_FIELD = "contents";
	private static final Logger log = Logger.getLogger(ZylabPatisClient.class.getName());
	private static final int MAX_RESULTS = 1000;

	public static void main(String[] args) {
//		String sQuery = PreconstructedQueries.instance().getQuery(PreconstructedQueries.UriFragments.METASTASIS_IV).toString("contents");
		CoreParser parser = new CoreParser("content", new StandardAnalyzer(Version.LUCENE_41));
		String sQuery = "metastasis~ stage~\"~10";
		Query query = parser.parse(null);
		try {
			Directory indexDir = FSDirectory.open(
					new File("/home/kasper2/mnt/aida/indexes/Zylab_test-20130415-02"));
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
			TopDocs result = searcher.search(
					PreconstructedQueries.instance().getQuery(
						PreconstructedQueries.UriFragments.METASTASIS_IV),
					1000);

			System.out.printf("#results: %d\n", result.totalHits);
			
		} catch (IOException ex) {
			throw new Error(ex);
		}

		
//		StandardQueryParser parser = new StandardQueryParser();
//		try {
//			Query q = parser.parse(sQuery, "contents");
//			System.out.append(dumpQuery("", q));
//			int i = 1;
//		} catch (QueryNodeException ex) {
//			throw new Error(ex);
//		}
	}


}
