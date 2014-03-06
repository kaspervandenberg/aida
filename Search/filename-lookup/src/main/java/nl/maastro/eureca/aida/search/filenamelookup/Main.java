// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.filenamelookup;

import checkers.nullness.quals.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Drives the filename lookup (demo) utility. 
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Main {
	/**
	 * CommandLine uses commons-cli to parse the command line options.
	 */
	private static class CommandLine {
		private static final Options supportedCommandlineOptions =
				new Options();
		static {
			supportedCommandlineOptions.addOption(
					OptionBuilder
						.withLongOpt("help")
						.withDescription("print this message.")
						.create('h'));
		}

		private final org.apache.commons.cli.CommandLine parsedcommandline;

		
		public CommandLine(String args[])
		{
			parsedcommandline = parseCommandlineArgs(args);
		}

		
		public List<PatisNumber> getPatients ()
		{
			List<String> remainingArgs = Arrays.asList(parsedcommandline.getArgs());
			List<PatisNumber> result = new ArrayList<>(remainingArgs.size());
			for (String arg : remainingArgs) {
				result.add(PatisNumber.create(arg));
			}
			
			if (result.isEmpty()) {
				String msg = "Expected at least one patisnumber argument.";
				System.err.println("ERROR: " + msg);
				printUsage(System.err);
				throw new Error(msg);
			}
			return result;
		}


		private static org.apache.commons.cli.CommandLine parseCommandlineArgs(String args[])
		{
			CommandLineParser cmdParser = new org.apache.commons.cli.GnuParser();
			try
			{
				org.apache.commons.cli.CommandLine commandline = cmdParser.parse(supportedCommandlineOptions, args);

				if(commandline.hasOption("help")) {
					printUsage(System.out);
					System.exit(0);
				}
				return commandline;
			} 
			catch (ParseException ex) {
				final String msg = "Cannot parse command line.";
				
				System.err.println(msg + "\n");
				System.err.println("reason: " + ex.getMessage());
				printUsage(System.err);

				throw new Error("Cannot parse command line.", ex);
			}
		}


		private static void printUsage(PrintStream outputTarget)
		{
				final String invocation = "java " + Main.class.getName() + " [OPTION]... {PATISNUMBER}...";
				final String purpose = "Lookup filenames of documents of a patient";
				final int lineWidth = 70;
				final int leftMargin = 4;
				final int descriptionIndent = 8;
		
				PrintWriter writer = new PrintWriter(outputTarget);
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(
						writer, lineWidth, invocation,
						purpose, supportedCommandlineOptions,
						leftMargin, descriptionIndent, "");
				writer.flush();
		}
		
	}

	/**
	 * The relevant information extracted from the lucene index.
	 */
	public class PatientDocumentTuple {
		public final PatisNumber patient;
		public final @Nullable String metadataLocation;
		public final @Nullable String dataLocation;


		/**
		 * Construct a {@code PatientDocumentTuple} from {@code docId},
		 * which contains an index into {@link Main#luceneSearcher}.
		 */
		public PatientDocumentTuple(PatisNumber patient_, ScoreDoc docId) throws IOException {
			this.patient = patient_;
			Document doc = luceneSearcher.doc(docId.doc, luceneFieldsToLoad);
			this.dataLocation = doc.get(dataLocationField);
			this.metadataLocation = doc.get(metadataLocationField);
		}
	}
	

	/**
	 * We use spring to configure filenamelookup.
	 * The configuration file is expected in the classpath in 'META-INF/beans.xml'; maven generates
	 * the classpath version from the file 'src/main/resources/META-INF/beans.xml'.
	 */
	private final ApplicationContext springConfigurationContext;

	private final CommandLine commandLine;

	/**
	 * Lucene object used for searching patient documents.
	 * @see #createSearcher(org.springframework.context.ApplicationContext) 
	 */
	private final IndexSearcher luceneSearcher;

	/**
	 * Lucene querying requires a limit to the number returned results.
	 * Configure this setting via the bean with id "maxHitsPerQuery".
	 */
	@Resource(name = "maxHitsPerQuery")
	private Integer maxHitsPerQuery;

	/**
	 * Name of the field in the index that contains the location of the
	 * meta data file.
	 * Configure this setting via the bean with id "index.fields.metadataUri" 
	 */
	@Resource(name = "index.fields.metadataUri")
	private String metadataLocationField;
	
	/**
	 * Name of the field in the index that contains the location of the
	 * data file.
	 * Configure this setting via the bean with id "index.fields.dataUri" 
	 */
	@Resource(name = "index.fields.dataUri")
	private String dataLocationField;

	/**
	 * The lucene API allows selecting which fields to read for an 
	 * indexed document.  
	 */
	private final Set<String> luceneFieldsToLoad;


	/**
	 * Constructor
	 */
	public Main(CommandLine cmd)
	{
		/* Use spring framework for configuration. */
		springConfigurationContext = new ClassPathXmlApplicationContext("META-INF/beans.xml");

		/* Configure fields annotated with '@Resource' */
		springConfigurationContext.getAutowireCapableBeanFactory().autowireBean(this);
		
		commandLine = cmd;
		luceneSearcher = createSearcher(springConfigurationContext);
		luceneFieldsToLoad = new HashSet<>(Arrays.asList(metadataLocationField, dataLocationField));
	}


	/**
	 * Program entry point
	 */
	public static void main(String args[]) throws IOException
	{
		Main instance = new Main(new CommandLine(args));
		List<Main.PatientDocumentTuple> searchResults = instance.searchCommandlinePatients();
		
		// Once we have the results, do something with them.
		// Here the results are printed separating the fields with tabs.
		System.out.printf("%-10s\t%-46s\t%s\n\n", "patisnr", "metadata", "data");
		for (PatientDocumentTuple doc : searchResults) {
			System.out.printf("%-10s\t%-46s\t%s\n", 
					doc.patient.getValue(), doc.metadataLocation, doc.dataLocation);
		}
		
	}


	/**
	 * Search the patisnumbers specified on the command line.
	 * 
	 * @return	a List of {@link PatientDocumentTuple}s containing all
	 * 		documents of the given patients.
	 * 
	 * @throws IOException {@link IndexSearcher#search} and 
	 * 		{@link IndexSearcher#doc} can throw exceptions; these exceptions
	 * 		propagate back to the caller.
	 */
	public List<PatientDocumentTuple> searchCommandlinePatients() throws IOException
	{
		return search(commandLine.getPatients());
	}
	

	/**
	 * Search the patisnumbers in {@code patients}.
	 * 
	 * @param patients	a List of {@link PatisNumber} to search 
	 * 
	 * @return	a List of {@link PatientDocumentTuple}s containing all
	 * 		documents of the given patients.
	 * 
	 * @throws IOException {@link IndexSearcher#search} and 
	 * 		{@link IndexSearcher#doc} can throw exceptions; these exceptions
	 * 		propagate back to the caller.
	 */
	public List<PatientDocumentTuple> search(List<PatisNumber> patients) throws IOException
	{
		List<PatientDocumentTuple> result = new LinkedList<>();
		for (PatisNumber patient : patients) {
			result.addAll(search(patient));
		}
		return result;
	}


	/**
	 * Search the documents about {@code patient}.
	 * 
	 * @param patient	the patient to search
	 * @return	a List of {@link PatientDocumentTuple}s containing all
	 * 		documents of the given patient (upto a maximum of 
	 * 		{@link #maxHitsPerQuery}).
	 * 
	 * @throws IOException {@link IndexSearcher#search} and 
	 * 		{@link IndexSearcher#doc} can throw exceptions; these exceptions
	 * 		propagate back to the caller.
	 */
	public List<PatientDocumentTuple> search(PatisNumber patient) throws IOException
	{
		List<PatientDocumentTuple> result = new LinkedList<>();
		TopDocs results = luceneSearcher.search(patient.asLuceneQuery(), maxHitsPerQuery);
		result.addAll(interpretSearchResult(patient, results));
		return result;
	}

	
	/**
	 * Instantiate and IndexSearcher using the configuration in {@code context}.
	 * 
	 * @param context	the spring configuration
	 * 
	 * @return 	an {@link IndexSearcher}
	 */
	private static IndexSearcher createSearcher(ApplicationContext context)
	{
		File index = context.getBean("localindex", File.class);
		try 
		{
			Directory indexDirectory = FSDirectory.open(index);
			IndexSearcher result = new IndexSearcher(
					DirectoryReader.open(indexDirectory));

			return result;
		}
		catch (IOException ex)
		{
			throw new Error ("Cannot open index: " + index, ex);
		}
	}

	
	/**
	 * Construct a List of {@link PatientDocumentTuple} using the results
	 * from {@link IndexSearcher#search}.
	 * 
	 * @param patient	{@link PatisNumber} of the queried patient.
	 * @param results	{@link TopDocs} as returned by {@code IndexSearcher.search}
	 * @return	a List of {@link PatientDocumentTuple}s containing all
	 * 		documents of the given patient from {@code results}.
	 * 
	 * @throws IOException {@link IndexSearcher#doc} can throw exceptions;
	 * 		these exceptions propagate back to the caller.
	 */
	private List<PatientDocumentTuple> interpretSearchResult(
				PatisNumber patient,
				TopDocs results)
	throws IOException
	{
		List<PatientDocumentTuple> result = new ArrayList<>(results.scoreDocs.length);
		for (int i = 0; i < results.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = results.scoreDocs[i];
			result.add(new PatientDocumentTuple(patient, scoreDoc));
		}
		return result;
	}
}
