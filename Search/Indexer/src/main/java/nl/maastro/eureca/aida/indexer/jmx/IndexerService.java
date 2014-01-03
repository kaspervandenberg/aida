// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.jmx;

import java.nio.file.FileVisitor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class IndexerService implements IndexerServiceMXBean {
	private enum JmxObjectName {
		/**
		 * Default {@link ObjectName}-generator for {@link IndexerService}
		 */
		@SuppressWarnings("serial")
		DEFAULT(
				"nl.maastro.eureca.aida.indexer",
				new HashMap<String, String>() {{
					put("type", "IndexerService");
				}});

		
		private final String domain;
		private final Map<String, String> predefined_key_value_pairs;

		
		/**
		 * Create an object name for {@link IndexerService#IndexerService(
		 * javax.management.ObjectName)}.
		 * 
		 * @return		an {@link ObjectName} composed from {@link #domain} and
		 * 				{@link #predefined_key_value_pairs}.
		 * 
		 * @throws MalformedObjectNameException		when {@link 
		 * 				ObjectName#construct(java.lang.String, java.util.Map)} 
		 * 				throws it.
		 */
		public ObjectName createName ()
		{
			try
			{
				return createName(Collections.<String, String>emptyMap());
			}
			catch (MalformedObjectNameException ex)
			{
				throw new Error("Unexpected exception when creating " +
						"default ObjectName", ex);
			}
		}


		/**
		 * Create an object name for {@link IndexerService#IndexerService(
		 * javax.management.ObjectName)}.
		 * 
		 * @param extra_key_value_pairs		a {@link Map} for key–value-pairs
		 * 				that are added to the {@link #predefined_key_value_pairs},
		 * 				for example a name–{value}-pair.
		 * 
		 * @return		an {@link ObjectName} composed from {@link #domain},
		 * 				{@link #predefined_key_value_pairs}, and {@code 
		 * 				extra_key_value_pairs}.
		 * 
		 * @throws MalformedObjectNameException		when {@link 
		 * 				ObjectName#construct(java.lang.String, java.util.Map)} 
		 * 				throws it.
		 */
		public ObjectName createName (Map<String, String> extra_key_value_pairs) 
				throws MalformedObjectNameException
		{
			@SuppressWarnings("UseOfObsoleteCollectionType")
			java.util.Hashtable<String, String> key_values = 
					new java.util.Hashtable<>(this.predefined_key_value_pairs);
			key_values.putAll(extra_key_value_pairs);

			return ObjectName.getInstance(domain, key_values);
		}

		private JmxObjectName (
				String domain_, 
				HashMap<String, String> key_value_pairs_)
		{
			this.domain = domain_;
			this.predefined_key_value_pairs = 
					Collections.unmodifiableMap(key_value_pairs_);
		}
	}
			
	private final ObjectName my_name;
	private final AtomicBoolean continue_directory_traversal = 
			new AtomicBoolean();
	
	/**
	 * Construct an IndexerService named {@code my_name_}.
	 * 
	 * @param my_name_		{@link ObjectName} under which this 
	 * 			{@code IndexerService} is found in the JMX agent.
	 * 			{@link IndexerService.JmxObjectName#DEFAULT}.{@link 
	 * 			IndexerService.JmxObjectName#createName(java.util.Map) create}
	 * 			provides a reasonnable name.
	 */
	public IndexerService(ObjectName my_name_)
	{
		this.my_name = my_name_;
	}

	
	/**
	 * Stop going through the directory tree looking for new files to index.
	 * 
	 * When {@code stopDirectoryCrawl} has been called, no new files are 
	 * submitted to the parse metadata and parse data process queues. Files 
	 * already in the queue will be parsed, after which the IndexerService
	 * is idle.
	 */
	@Override
	public void stopDirectoryCrawl()
	{
		continue_directory_traversal.set(false);
	}


	/**
	 * Decorate a {@link FileVisitor} with {@link TerminatableFileVisitor}
	 * so that it will terminate when {@link #stopDirectoryCrawl()} is called.
	 * 
	 * @param <TDir>	type used to reference to directories and files
	 * 			(see {@link FileVisitor} and {@link
	 * 			java.nio.file.Files#walkFileTree(java.nio.file.Path,
	 * 			java.nio.file.FileVisitor)}).
	 * 
	 * @param delegate	wrapped {@link FileVisitor} to which the 
	 * 			{@code visit*}-method calls are forwarded.
	 * @return	a {@link FileVisitor} that will terminate when {@link
	 * 		#stopDirectoryCrawl()} is called.
	 */
	public <TDir> TerminatableFileVisitor<TDir> createManagedFileVisitor (
			FileVisitor<TDir> delegate)
	{
		return createManagedFileVisitor(delegate, 
				TerminatableFileVisitor.ALWAYS_CALL_POST_VISIT_DEFAULT);
	}


	/**
	 * Decorate a {@link FileVisitor} with {@link TerminatableFileVisitor}
	 * so that it will terminate when {@link #stopDirectoryCrawl()} is called.
	 * 
	 * @param <TDir>	type used to reference to directories and files
	 * 			(see {@link FileVisitor} and {@link
	 * 			java.nio.file.Files#walkFileTree(java.nio.file.Path,
	 * 			java.nio.file.FileVisitor)}).
	 * 
	 * @param delegate	wrapped {@link FileVisitor} to which the 
	 * 			{@code visit*}-method calls are forwarded.
	 * 
	 * @param call_post_visit_before_terminating 	
	 * 		<ul><li>{@code true}, allow {@code delegate} to finish cleanly
	 * 			by forwarding one last call to {@link FileVisitor#
	 * 			postVisitDirectory(java.lang.Object, java.io.IOException)}  or
	 * 			to {@link FileVisitor#visitFileFailed(java.lang.Object,
	 * 			java.io.IOException)} before terminating; or</li>
	 * 		<li>{@code false}, (default = {@value
	 * 			TerminatableFileVisitor#ALWAYS_CALL_POST_VISIT_DEFAULT})
	 * 			termininate immediatly.</li>
	 * 		</ul>
	 * 		See {@link TerminatableFileVisitor#always_call_post_visit}.
	 * 
	 * @return	a {@link FileVisitor} that will terminate when {@link
	 * 		#stopDirectoryCrawl()} is called.
	 */
	public <TDir> TerminatableFileVisitor<TDir> createManagedFileVisitor (
			FileVisitor<TDir> delegate,
			boolean call_post_visit_before_terminating)
	{
		return new TerminatableFileVisitor<>(
				continue_directory_traversal,
				delegate,
				call_post_visit_before_terminating);
	}
}
