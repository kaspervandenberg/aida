// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.jmx;

import java.lang.management.ManagementFactory;
import java.nio.file.FileVisitor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * JMX implementation for the MXBean IndexerService; allows the application
 * administrator to manage the indexer.
 * 
 * JMX is Java Management eXtensions, see Oracle's<a 
 * href=http://www.oracle.com/technetwork/java/javase/tech/docs-jsp-135989.html>
 * JMX Documentation</a>.  It is a specification of a standard intertace to
 * manage applications.  An administrator can use JConsole, SNMP, or any other
 * program that can communicate with JMX to manage the indexer application
 * via the functionallity that {@code IndexerServiceMXBean} provides.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class IndexerService implements IndexerServiceMXBean {
	/**
	 * JMX beans are found via their {@link ObjectName}; {@link JmxObjectName}
	 * provides a default name for IndexerService.
	 */
	private enum JmxObjectName {
		/**
		 * Default {@link ObjectName}-generator for {@link IndexerService}:
		 * <ul><li>the package name, {@code nl.maastro.eureca.aida.indexer}, as 
		 * 		domain;</li>
		 * <li>{@code IndexerService}, as type name</li></ul>
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
	 * Creates a fresh {@link IndexerService}, named {@code name},
	 * and registers it with {@code server} 
	 * 
	 * @param server	{@link MBeanServer} with which to register
	 * 		the created {@code IndexerService}.  {@link	ManagementFactory}.
	 * 		{@link ManagementFactory#getPlatformMBeanServer() 
	 * 		getPlatformMBeanServer()} can be used as value for {@code server}.
	 * @param name		{@link ObjectName} under which the created
	 * 		{@code IndexerService} can be found.  {@link JmxObjectName#DEFAULT}.
	 * 		{@link JmxObjectName#createName() createName()} can be used as
	 * 		value for {@code name}.
	 * 
	 * @return a fresh and registered {@link IndexerService}
	 * 
	 * @throws InstanceAlreadyExistsException		when {@code server}
	 * 		aready contains an MBean with {@code name}; names must be unique.
	 */
	public static IndexerService createAndRegister(
			MBeanServer server,
			ObjectName name) 
				throws InstanceAlreadyExistsException
	{
		IndexerService freshService = new IndexerService(name);
		try {
			server.registerMBean(freshService, name);
			return freshService;
		} catch (MBeanRegistrationException ex) {
			throw new Error("The preRegister method has thrown an exception." +
					"IndexerService does not use preRegister, so this " +
					"exception should not occur; programmer has to check " +
					"what went wrong.", ex);
		} catch (NotCompliantMBeanException ex) {
			throw new Error("This is not a compliant MBean; programmer " +
					"has to fix it.", ex);
		}
	}


	/**
	 * Creates a fresh {@link IndexerService}, named {@code name},
	 * and registers it with the {@link 
	 * ManagementFactory#getPlatformMBeanServer() platform's} 
	 * {@link MBeanServer}.
	 * 
	 * @param name		{@link ObjectName} under which the created
	 * 		{@code IndexerService} can be found.  {@link JmxObjectName#DEFAULT}.
	 * 		{@link JmxObjectName#createName() createName()} can be used as
	 * 		value for {@code name}.
	 * 
	 * @return a fresh and registered {@link IndexerService}
	 * 
	 * @throws InstanceAlreadyExistsException		when {@code server}
	 * 		aready contains an MBean with {@code name}; names must be unique.
	 */
	public static IndexerService createAndRegister(
			ObjectName name) 
				throws InstanceAlreadyExistsException
	{
		MBeanServer platformServer = ManagementFactory.getPlatformMBeanServer();
		return createAndRegister(platformServer, name);
	}
	

	/**
	 * Creates a fresh {@link IndexerService}, named {@link 
	 * JmxObjectName#DEFAULT}, and registers it with the {@link 
	 * ManagementFactory#getPlatformMBeanServer() platform's} 
	 * {@link MBeanServer}.
	 * 
	 * @param extra_name_key_value_pairs 	supplied to {@link
	 * 		JmxObjectName#createName(java.util.Map)} to construct the name
	 * 		for the created {@code IndexerService}.
	 * 
	 * @return a fresh and registered {@link IndexerService}
	 * 
	 * @throws InstanceAlreadyExistsException		when the platform's
	 * 		MBean server aready contains an MBean with {@code name};
	 * 		names must be unique.
	 * 
	 * @throws MalformedObjectNameException		when {@link 
	 * 				ObjectName#construct(java.lang.String, java.util.Map)} 
	 * 				throws it.
	 */
	public static IndexerService createAndRegister(
			Map<String, String> extra_name_key_value_pairs) 
				throws MalformedObjectNameException, InstanceAlreadyExistsException
	{
		ObjectName name = JmxObjectName.DEFAULT.createName(extra_name_key_value_pairs);
		return createAndRegister(name);
	}


	/**
	 * Create a fresh instance of {@link IndexerService} with a {@link 
	 * JmxObjectName#DEFAULT} default name} and registered with the {@link 
	 * ManagementFactory#getPlatformMBeanServer() platform's} 
	 * {@link MBeanServer}.
	 * 
	 * @return a fresh and registered {@link IndexerService}
	 * 
	 * @throws InstanceAlreadyExistsException	when the platform's MBean
	 * 		server already contains an IndexerServer MBean with the default
	 * 		name.  In general only one IndexerService should be created.
	 * 		When you require more than one IndexerService MBean, register
	 * 		it with a different name ({@link #createAndRegister(java.util.Map)}
	 * 		or {@link #createAndRegister(javax.management.ObjectName)})
	 */
	public static IndexerService createAndregister()
				throws InstanceAlreadyExistsException
	{
		ObjectName name = JmxObjectName.DEFAULT.createName();
		return createAndRegister(name);
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
