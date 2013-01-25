/*
/*  © Maastro 2013
 *  Author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
package nl.maastro.eureca.aida.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.util.EnumSet;
import javax.xml.rpc.ServiceException;
import nl.maastro.vocab.axis.services.IndexWS.Indexer;
import nl.maastro.vocab.axis.services.IndexWS.IndexerServiceLocator;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
public class BasicClient {
	private final static String propertyFileName = "basicIndexClient.properties";

	private enum Properties {
		indexLocation(new IndexerServiceLocator().getIndexWSAddress()),
		indexConfigXmlFileName("indexconfig.xml"),
		indexName("vocabTest");
		
		private Properties(final Object defaultValue_) {
			defaultValue = defaultValue_;
		}
		
		public final Object defaultValue;
	}
	
	private final Configuration config;
	private final Indexer indexer;
	private final String indexConfigTxt;

	public BasicClient() {
		config = initConfig();
		indexer = initIndexer();
		indexConfigTxt = getIndexConfig();
	}

	public void Index(Path fileName) {
		try {
			BufferedReader reader = Files.newBufferedReader(fileName, Charset.defaultCharset());
			CharBuffer buffer = CharBuffer.allocate(1024);
			StringBuilder data = new StringBuilder();

			while (reader.read(buffer) != -1) {
				data.append(buffer);
			}
			
			try {
				indexer.addToIndexWithConfig(
						data.toString(), 
						fileName.getFileName().toString(), 
						getProperty(Properties.indexName),
						indexConfigTxt);
			} catch (RemoteException ex) {
				throw new Error(String.format(
						"Remote exception when adding %s to index", fileName.toString()));
			}
		} catch (IOException ex) {
			throw new Error(String.format(
					"Error while reading %s", fileName.toString()), ex);
		}
	}

	private Configuration initConfig() {
		try {
			return new PropertiesConfiguration(propertyFileName);
		} catch (ConfigurationException ex) {
			// TODO Log a warning "Unable to read configuration from %s; exception ex caught.";
			return new PropertiesConfiguration();
		}
	}

	private Indexer initIndexer() {
		try {
			if(config.containsKey(Properties.indexLocation.name())) {
				String indexStr = getProperty(Properties.indexLocation);
				try {
					URL indexLocation = new URL(indexStr);
					
					return new IndexerServiceLocator().getIndexWS(indexLocation);
				} catch (MalformedURLException ex) {
					throw new Error(
							String.format("URL, %s, (configured in %s:%s) is malformed",
							indexStr, propertyFileName, Properties.indexLocation.name()),
							ex);
				}
			} else {
				return new IndexerServiceLocator().getIndexWS();
			}
		} catch (ServiceException ex) {
			throw new Error(String.format("Error connecting to Indexer service"), ex);
		}
	}

	private String getIndexConfig() {
		String fileName = getProperty(Properties.indexConfigXmlFileName); 
		
		InputStream is = BasicClient.class.getResourceAsStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		CharBuffer buffer = CharBuffer.allocate(1024);
		StringBuilder result = new StringBuilder();
		
		try {
			while (reader.read(buffer) != -1) {
				result.append(buffer);
			}
		} catch (IOException ex) {
			throw new Error(String.format(
					"Error while reading %s config file", fileName), ex);
		}

		return result.toString();
	
	} 
	
	private static void checkArguments(String[] args) {
		if (args.length == 0) {
			printUseage();
			System.exit(-1);
		}
	}

	private static void printUseage() {
		System.err.append("Supply the files to index as commandline arguments");
	}

	private String getProperty(Properties prop) {
		if(config == null) {
			initConfig();
		}
		
		return config.getString(prop.name(), (String)prop.defaultValue);
	}
	
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		BasicClient.checkArguments(args);
		
		final BasicClient client = new BasicClient();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			Path path = Paths.get(arg);
			try {
				Files.walkFileTree(path, null, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						client.Index(file);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException ex) {
				throw new Error("Unexpected IOException thrown", ex);
			}
			
		}
	}
}
