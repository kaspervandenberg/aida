// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.filename_lookup;

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
	 * Command_Line uses commons-cli to parse the command line options.
	 */
	private static class Command_Line {
		private static final Options supported_commandline_options =
				new Options();
		static {
			supported_commandline_options.addOption(
					OptionBuilder
						.withLongOpt("help")
						.withDescription("print this message.")
						.create('h'));
		}

		private final CommandLine parsed_commandline;

		
		public Command_Line(String args[])
		{
			parsed_commandline = parse_commandline_args(args);
		}

		
		public List<PatisNumber> get_patients ()
		{
			List<String> remaining_args = Arrays.asList(parsed_commandline.getArgs());
			List<PatisNumber> result = new ArrayList<>(remaining_args.size());
			for (String arg : remaining_args) {
				result.add(PatisNumber.create(arg));
			}
			
			if (result.isEmpty()) {
				String msg = "Expected at least one patisnumber argument.";
				System.err.println("ERROR: " + msg);
				print_usage(System.err);
				throw new Error(msg);
			}
			return result;
		}


		private static CommandLine parse_commandline_args(String args[])
		{
			CommandLineParser cmd_parser = new org.apache.commons.cli.GnuParser();
			try
			{
				CommandLine commandline = cmd_parser.parse(supported_commandline_options, args);

				if(commandline.hasOption("help")) {
					print_usage(System.out);
					System.exit(0);
				}
				return commandline;
			} 
			catch (ParseException ex) {
				final String msg = "Cannot parse command line.";
				
				System.err.println(msg + "\n");
				System.err.println("reason: " + ex.getMessage());
				print_usage(System.err);

				throw new Error("Cannot parse command line.", ex);
			}
		}


		private static void print_usage(PrintStream output_target)
		{
				final String invocation = "java " + Main.class.getName() + " [OPTION]... {PATISNUMBER}...";
				final String purpose = "Lookup filenames of documents of a patient";
				final int line_width = 70;
				final int left_margin = 4;
				final int description_indent = 8;
		
				PrintWriter writer = new PrintWriter(output_target);
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(
						writer, line_width, invocation,
						purpose, supported_commandline_options,
						left_margin, description_indent, "");
				writer.flush();
		}
		
	}

	/**
	 * The relevant information extracted from the lucene index.
	 */
	public class Patient_Document_tuple {
		public final PatisNumber patient;
		public final @Nullable String metadata_location;
		public final @Nullable String data_location;


		/**
		 * Construct a {@code Patient_Document_tuple} from {@code doc_id},
		 * which contains an index into {@link Main#lucene_searcher}.
		 */
		public Patient_Document_tuple(PatisNumber patient_, ScoreDoc doc_id) throws IOException {
			this.patient = patient_;
			Document doc = lucene_searcher.doc(doc_id.doc, lucene_fields_to_load);
			this.data_location = doc.get(data_location_field);
			this.metadata_location = doc.get(metadata_location_field);
		}
	}
	

	/**
	 * We use spring to configure filename_lookup.
	 * The configuration file is expected in the classpath in 'META-INF/beans.xml'; maven generates
	 * the classpath version from the file 'src/main/resources/META-INF/beans.xml'.
	 */
	private final ApplicationContext spring_configuration_context;

	private final Command_Line command_line;

	/**
	 * Lucene object used for searching patient documents.
	 * @see #create_searcher(org.springframework.context.ApplicationContext) 
	 */
	private final IndexSearcher lucene_searcher;

	/**
	 * Lucene querying requires a limit to the number returned results.
	 * Configure this setting via the bean with id "max_hits_per_query".
	 */
	@Resource(name = "max_hits_per_query")
	private Integer max_hits_per_query;

	/**
	 * Name of the field in the index that contains the location of the
	 * meta data file.
	 * Configure this setting via the bean with id "index.fields.metadata_uri" 
	 */
	@Resource(name = "index.fields.metadata_uri")
	private String metadata_location_field;
	
	/**
	 * Name of the field in the index that contains the location of the
	 * data file.
	 * Configure this setting via the bean with id "index.fields.data_uri" 
	 */
	@Resource(name = "index.fields.data_uri")
	private String data_location_field;

	/**
	 * The lucene API allows selecting which fields to read for an 
	 * indexed document.  
	 */
	private final Set<String> lucene_fields_to_load;


	/**
	 * Constructor
	 */
	public Main(Command_Line cmd)
	{
		/* Use spring framework for configuration. */
		spring_configuration_context = new ClassPathXmlApplicationContext("META-INF/beans.xml");

		/* Configure fields annotated with '@Resource' */
		spring_configuration_context.getAutowireCapableBeanFactory().autowireBean(this);
		
		command_line = cmd;
		lucene_searcher = create_searcher(spring_configuration_context);
		lucene_fields_to_load = new HashSet<>(Arrays.asList(metadata_location_field, data_location_field));
	}


	/**
	 * Program entry point
	 */
	public static void main(String args[]) throws IOException
	{
		Main instance = new Main(new Command_Line(args));
		List<Main.Patient_Document_tuple> search_results = instance.search_commandline_patients();
		
		// Once we have the results, do something with them.
		// Here the results are printed separating the fields with tabs.
		System.out.printf("%-10s\t%-46s\t%s\n\n", "patisnr", "metadata", "data");
		for (Patient_Document_tuple doc : search_results) {
			System.out.printf("%-10s\t%-46s\t%s\n", 
					doc.patient.getValue(), doc.metadata_location, doc.data_location);
		}
		
	}


	/**
	 * Search the patisnumbers specified on the command line.
	 * 
	 * @return	a List of {@link Patient_Document_tuple}s containing all
	 * 		documents of the given patients.
	 * 
	 * @throws IOException {@link IndexSearcher#search} and 
	 * 		{@link IndexSearcher#doc} can throw exceptions; these exceptions
	 * 		propagate back to the caller.
	 */
	public List<Patient_Document_tuple> search_commandline_patients() throws IOException
	{
		return search(command_line.get_patients());
	}
	

	/**
	 * Search the patisnumbers in {@code patients}.
	 * 
	 * @param patients	a List of {@link PatisNumber} to search 
	 * 
	 * @return	a List of {@link Patient_Document_tuple}s containing all
	 * 		documents of the given patients.
	 * 
	 * @throws IOException {@link IndexSearcher#search} and 
	 * 		{@link IndexSearcher#doc} can throw exceptions; these exceptions
	 * 		propagate back to the caller.
	 */
	public List<Patient_Document_tuple> search(List<PatisNumber> patients) throws IOException
	{
		List<Patient_Document_tuple> result = new LinkedList<>();
		for (PatisNumber patient : patients) {
			result.addAll(search(patient));
		}
		return result;
	}


	/**
	 * Search the documents about {@code patient}.
	 * 
	 * @param patient	the patient to search
	 * @return	a List of {@link Patient_Document_tuple}s containing all
	 * 		documents of the given patient (upto a maximum of 
	 * 		{@link #max_hits_per_query}).
	 * 
	 * @throws IOException {@link IndexSearcher#search} and 
	 * 		{@link IndexSearcher#doc} can throw exceptions; these exceptions
	 * 		propagate back to the caller.
	 */
	public List<Patient_Document_tuple> search(PatisNumber patient) throws IOException
	{
		List<Patient_Document_tuple> result = new LinkedList<>();
		TopDocs results = lucene_searcher.search(patient.as_lucene_query(), max_hits_per_query);
		result.addAll(interpret_search_result(patient, results));
		return result;
	}

	
	/**
	 * Instantiate and IndexSearcher using the configuration in {@code context}.
	 * 
	 * @param context	the spring configuration
	 * 
	 * @return 	an {@link IndexSearcher}
	 */
	private static IndexSearcher create_searcher(ApplicationContext context)
	{
		File index = context.getBean("localindex", File.class);
		try 
		{
			Directory index_directory = FSDirectory.open(index);
			IndexSearcher result = new IndexSearcher(
					DirectoryReader.open(index_directory));

			return result;
		}
		catch (IOException ex)
		{
			throw new Error ("Cannot open index: " + index, ex);
		}
	}

	
	/**
	 * Construct a List of {@link Patient_Document_tuple} using the results
	 * from {@link IndexSearcher#search}.
	 * 
	 * @param patient	{@link PatisNumber} of the queried patient.
	 * @param results	{@link TopDocs} as returned by {@code IndexSearcher.search}
	 * @return	a List of {@link Patient_Document_tuple}s containing all
	 * 		documents of the given patient from {@code results}.
	 * 
	 * @throws IOException {@link IndexSearcher#doc} can throw exceptions;
	 * 		these exceptions propagate back to the caller.
	 */
	private List<Patient_Document_tuple> interpret_search_result(
				PatisNumber patient,
				TopDocs results)
	throws IOException
	{
		List<Patient_Document_tuple> result = new ArrayList<>(results.scoreDocs.length);
		for (int i = 0; i < results.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = results.scoreDocs[i];
			result.add(new Patient_Document_tuple(patient, scoreDoc));
		}
		return result;
	}
}
