// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.search.zylabpatisclient.ReportBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class CommandLineParser {
	private enum SupportedOptions {
		HELP("h") {
			@Override
			protected Option create() {
				OptionBuilder.withLongOpt("help");
				OptionBuilder.withDescription("Print this message.");
				return OptionBuilder.create(this.shortOpt);
			}
		},

		VALIDATION("e") {
			@Override
			protected Option create() {
				OptionBuilder.withLongOpt("expected");
				OptionBuilder.withDescription("Include validation columns in generated report.");
				return OptionBuilder.create(this.shortOpt);
			}
		},

		PRODUCTION("n") {
			@Override
			protected Option create() {
				OptionBuilder.withLongOpt("noexpected");
				OptionBuilder.withDescription("(default) Omit validation columns");
				return OptionBuilder.create(this.shortOpt);
			}
		},

		EMD_USERNAME("u") {
			@Override
			protected Option create() {
				OptionBuilder.withLongOpt("emdUsername");
				OptionBuilder.hasArg();
				OptionBuilder.withArgName("username");
				OptionBuilder.withDescription("Account to use when connecting to EMD database.");
				return OptionBuilder.create(this.shortOpt);
			}
		},

		EMD_PASSWORD("p") {
			@Override
			protected Option create() {
				OptionBuilder.withLongOpt("emdPassword");
				OptionBuilder.hasArg();
				OptionBuilder.withArgName("password");
				OptionBuilder.withDescription("Password to authenticate the emdUsername account when connection to EMD database.");
				return OptionBuilder.create(this.shortOpt);
			}
		},

		;

		public final String shortOpt;
		
		protected abstract Option create();


		private SupportedOptions(String shortOpt_)
		{
			this.shortOpt = shortOpt_;
		}
		
		public static Options createAll() {
			final Options opts = new Options();
		
			opts.addOption(HELP.create());
			
			OptionGroup validationSwitch = new OptionGroup();
			validationSwitch.addOption(PRODUCTION.create());
			validationSwitch.addOption(VALIDATION.create());
			opts.addOptionGroup(validationSwitch);

			opts.addOption(EMD_USERNAME.create());
			opts.addOption(EMD_PASSWORD.create());

			return opts;
		}
	}

	
	private final Options commandlineOptions = SupportedOptions.createAll();
	private final CommandLine commandline;

	
	public CommandLineParser(String[] args) throws ParseException
	{
		org.apache.commons.cli.CommandLineParser parser = new GnuParser();
		
		try {
			commandline = parser.parse(commandlineOptions, args);
		} catch (ParseException ex) {
			Logger.getLogger(CommandLineParser.class.getName()).log(
					Level.SEVERE,
					"Error parsing commandline: " + args, ex);
			printUseage(commandlineOptions);
			throw ex;
		}
	}
	

	public final void printUseage()
	{
		CommandLineParser.printUseage(commandlineOptions);
	}


	private static void printUseage(Options commandlineOptions_)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				"zylabpatisclient",
				"Search zylab documents for information about patients' trial eligibility.",
				commandlineOptions_,
				"Use https://dev-issues.maastro.nl/browse/AIDA/ to report issues.",
				true);
	}


	public ReportBuilder.Purpose getReportPurpose()
	{
		if (commandline.hasOption(SupportedOptions.PRODUCTION.shortOpt)) {
			return ReportBuilder.Purpose.PATIENT_INFO;
		} else if (commandline.hasOption(SupportedOptions.VALIDATION.shortOpt)) {
			return ReportBuilder.Purpose.VALIDATION;
		} else {
			return ReportBuilder.Purpose.PATIENT_INFO;
		}
	}


	public boolean isHelpRequested()
	{
		return commandline.hasOption(SupportedOptions.HELP.shortOpt); 
	}

	public boolean isEmdUsernameSpecified()
	{
		return commandline.hasOption(SupportedOptions.EMD_USERNAME.shortOpt) &&
				(commandline.getOptionValue(SupportedOptions.EMD_USERNAME.shortOpt) != null);
	}

	public String getEmdUsername() throws IllegalStateException
	{
		String value = commandline.getOptionValue(SupportedOptions.EMD_USERNAME.shortOpt);
		if (value == null) {
			throw new IllegalStateException(String.format(
					"Option %s not set.",
					SupportedOptions.EMD_USERNAME));
		} 
		return value;
	}

	public boolean isEmdPasswordSpecified()
	{
		return commandline.hasOption(SupportedOptions.EMD_PASSWORD.shortOpt) &&
				(commandline.getOptionValue(SupportedOptions.EMD_PASSWORD.shortOpt) != null);
	}

	public char[] getEmdPassword() throws IllegalStateException
	{
		String value = commandline.getOptionValue(SupportedOptions.EMD_PASSWORD.shortOpt);
		if (value == null) {
			throw new IllegalStateException(String.format(
					"Option %s not set.",
					SupportedOptions.EMD_PASSWORD));
		}
		return value.toCharArray();
	}
			

	
	
}
