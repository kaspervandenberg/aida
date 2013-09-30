/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author kasper
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	nl.maastro.eureca.aida.indexer.ZylabDocumentReferenceTest.class,
	nl.maastro.eureca.aida.indexer.DocumentPartTypeDetectorTest.class,
	nl.maastro.eureca.aida.indexer.DocumentParseTaskSynchronizerTest.class,
	nl.maastro.eureca.aida.indexer.ParseDataTaskTest.class,
	nl.maastro.eureca.aida.indexer.ParseZylabMetadataTaskTest.class,
	nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorServiceTestBasic.class,
	nl.maastro.eureca.aida.indexer.ZylabDataTest.class
})
public class IncrementalIndexerTests {
	
}