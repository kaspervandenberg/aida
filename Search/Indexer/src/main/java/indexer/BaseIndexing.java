/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
package indexer;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParsingReader;

public class BaseIndexing implements AutoCloseable {
	public enum FixedFields {
		ID,
		CONTENT,
		MEDIA_TYPE("mediaType");

		public final String fieldName;

		private FixedFields() {
			fieldName = this.name().toLowerCase();
		}

		private FixedFields(final String fieldName_) {
			fieldName = fieldName_;
		}
	}
  
  public File datadir;
	private final TikaConfig tc = TikaConfig.getDefaultConfig();
	private final AnalyzerFactory analyzerFactory;

  private IndexWriterUtil indexWriterUtil;
  private IndexWriter indexWriter;
//  public FSDirectory indexdir;
//  public final File cachedir;
  private ConfigurationHandler cfg;
  private HandlerFactory hf;
  public int added = 0;
  public int failed = 0;
  
  /** logger for Commons logging. */
  private transient Logger log =
    Logger.getLogger(BaseIndexing.class.getName());

  /** Creates a new instance of BaseIndexing
   * @param       configFile      path to the configurationfile
   * @param       name            Name of the index to use
   * @param       dataPath        path to the files
   */
  public BaseIndexing(String configFile, String name, String dataPath) {
    this(new ConfigurationHandler(configFile), name, dataPath);
  }
  
  public BaseIndexing(ConfigurationHandler cfg, String name, String dataPath) {
    
    this.cfg = cfg;
    
    hf = new HandlerFactory(cfg);
	analyzerFactory = new AnalyzerFactory(cfg);

	indexWriterUtil = (name == null || name.length() == 0) ?
			new IndexWriterUtil(cfg) :
			new IndexWriterUtil(cfg, name);
	
    datadir = (dataPath == null || dataPath.length() == 0) ?
			new File(cfg.getDataPath()) :
			new File(dataPath);
	
		if (log.isLoggable(Level.FINE)) {
		  log.fine("Indexdir: " + indexWriterUtil.getIndexdir());
		  log.fine("Datadir: " + datadir);
		}
		
  }

	@Override
	public void close() {
		indexWriterUtil.closeIndexWriter();
	}

  /** Adds Documents to the index */
  public boolean addDocuments() throws IOException{
	assertIsDirectory(indexWriterUtil.getCacheDir());

    indexDocs(datadir);
    indexWriterUtil.getIndexWriter().commit();
    return true;
  }

  /**
   * Add the visited file to the Lucene Index of {@link #indexWriter}.
   * 
   * Use {@link Tika} to detect the file type an parse the file into a stream. 
   * Use an {@link Analyzer} as returned by 
   * {@link AnalyzerFactory#getAnalyzer(String)}.
   *
   * @author Kasper van den Berg <kasper@kaspervandenberg.net>
   */
  private class TikaIndexAdder extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Tika tikaFacade = new  Tika(); 
			Parser parser = tikaFacade.getParser();
			ParseContext context = new ParseContext();
			context.set(ZylabMetadataXml.FileRefResolver.class, cfg.getReferenceResolver());
			
			VisitedDocument doc = new VisitedDocument(file);
			
			try {
				Analyzer analyzer = doc.storeContent(parser, context);
				doc.storeMetadata();
				try {
					indexWriterUtil.getIndexWriter().addDocument(doc.getDocument(), analyzer);
					indexWriterUtil.getIndexWriter().commit();

					ZylabMetadataXml.FileRef ref_about = context.get(ZylabMetadataXml.FileRef.class);
					if(ref_about != null) {
						String s_about= doc.metadata.get(ZylabMetadataXml.FixedProperties.ABOUT_RESOLVED.get());
						try {
							URI about = new URI(s_about);
							indexWriterUtil.copyToCache(new File(about).toPath(), file.getFileName().toString());
						} catch (URISyntaxException ex) {
							Logger.getLogger(BaseIndexing.class.getName()).log(
									Level.SEVERE,
									null, ex);
						}
					} else {
						indexWriterUtil.copyToCache(file);
					}
					
				} catch (OutOfMemoryError ex) {
					indexWriterUtil.handleOutOfMememoryError(ex);
					
					failed++;
					throw ex;
				} catch (CorruptIndexException ex) {
					String msg =  String.format(
							"Lucene index %s corrupt, rebuild index",
							indexWriterUtil.getIndexdir().getName());
					log.log(Level.SEVERE, msg, ex);
					
					failed++;
					throw ex;
				} catch (IOException ex) {
					// Skip this file continue, with next
					String msg = String.format(
							"Error indexing: while writing to index %s file %s",
							indexWriterUtil.getIndexdir().getName(),
							file.toString());
					log.log(Level.SEVERE, msg, ex);
					
					failed++;
					return FileVisitResult.CONTINUE;
				}
			} catch (IOException ex) {
				// Skip this file
				String msg = String.format(
						"Error indexing: while parsing file %s",
						file.toString());
				log.log(Level.WARNING, msg, ex);

				failed++;
				return FileVisitResult.CONTINUE;
			}
			
			added++;
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			log.log(Level.WARNING, String.format(
					"Error indexing file %s into index %s",
					file,
					indexWriterUtil.getIndexdir().getName()), exc);
			return FileVisitResult.CONTINUE;
		}

		private class VisitedDocument {
			private final Document doc = new Document();
			private final Metadata metadata = new Metadata();
			private final Path file;

			public VisitedDocument(Path file_) {
				file = file_;
			}
			
			public Analyzer storeContent(Parser parser, ParseContext context)
					throws IOException {
				Reader content = new ParsingReader(parser, new FileInputStream(file.toFile()), metadata, context);

				Analyzer analyzer = analyzerFactory.getAnalyzer(metadata.get(Metadata.CONTENT_TYPE));
				TokenStream tokenStream = analyzer.tokenStream(
						FixedFields.CONTENT.fieldName, content);
				doc.add(new TextField(FixedFields.CONTENT.fieldName, tokenStream));
				return analyzer;
			}

			public void storeMetadata() {
				doc.add(new StringField(
						FixedFields.ID.fieldName, file.toFile().getName(), Store.YES));
				
				doc.add(new StringField(
						FixedFields.MEDIA_TYPE.fieldName,
						metadata.get(Metadata.CONTENT_TYPE),
						Store.YES));
				
				for (String property : metadata.names()) {
					for (String value : metadata.getValues(property)) {
						doc.add(new StringField(property, value, Store.YES));
					}
				}
			}

			public Document getDocument() {
				return doc;
			}
		}
	}

	/** Does the actual indexing
	 * @param       file            File to index
	 */
	private void indexDocs(File file) {
		// TODO Integrate Tica config with 'indexconfig.xml'
		try {
			Files.walkFileTree(file.toPath(), new TikaIndexAdder());
		} catch (IOException ex) {
			String msg = String.format("Failed to index %s into index %s",
					file.getPath(),
					indexWriterUtil.getIndexdir().getName());
			throw new Error(msg, ex);
		}
	}

  public File getIndexdir() {
	  return indexWriterUtil.getIndexdir();
  }

  private void assertIsDirectory(File f) {
	if(!f.exists()) {
		f.mkdirs();
	}
	
	if((!f.exists()) || (!f.isDirectory())) {
		throw new RuntimeException(String.format("Error creating directory %s", f.toString()));
	}
  }
}

/* vim: set shiftwidth=4 tabstop=4 noexpandtab fo=ctwan ai : */

