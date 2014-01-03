// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.jmx;

import javax.management.MXBean;

/**
 * JMX MXBean for the IndexerService
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@MXBean
public interface IndexerServiceMXBean {
	/**
	 * Stop going through the directory tree looking for new files to index.
	 * 
	 * When {@code stopDirectoryCrawl} has been called, no new files are 
	 * submitted to the parse metadata and parse data process queues. Files 
	 * already in the queue will be parsed, after which the IndexerService
	 * is idle.
	 */
	public void stopDirectoryCrawl();	
}
