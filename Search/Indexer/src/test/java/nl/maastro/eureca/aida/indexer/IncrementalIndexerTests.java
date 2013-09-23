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
	nl.maastro.eureca.aida.indexer.ParseZylabMetadataTest.class,
//	nl.maastro.eureca.aida.indexer.DocumentParseTaskCombinationTest.class,
	nl.maastro.eureca.aida.indexer.ZylabDataTest.class,
	nl.maastro.eureca.aida.indexer.ParseDataTest.class,
	nl.maastro.eureca.aida.indexer.ZylabDataAsynchronousTest.class})
public class IncrementalIndexerTests {
	
}