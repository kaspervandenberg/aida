// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.jmx;

import javax.management.MXBean;

/**
 * JMX MXBean for the IndexerService.
 * 
 * JMX is Java Management eXtensions, see Oracle's<a 
 * href=http://www.oracle.com/technetwork/java/javase/tech/docs-jsp-135989.html>
 * JMX Documentation</a>.  It is a specification of a standard intertace to
 * manage applications.  An administrator can use JConsole, SNMP, or any other
 * program that can communicate with JMX to manage the indexer application
 * via the functionallity that {@code IndexerServiceMXBean} provides.
 * 
 * {@link IndexerService} is the implementation of this interface.
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
