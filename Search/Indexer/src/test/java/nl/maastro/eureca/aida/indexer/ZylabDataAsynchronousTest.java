/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer;

import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.Blocking;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static nl.maastro.eureca.aida.indexer.ZylabDataAsynchronousTest.ConcurrentActions.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author kasper
 */
@RunWith(Theories.class)
public class ZylabDataAsynchronousTest extends ZylabDataTest {
	
	public enum ConcurrentActions {
		SUBMIT_DATA {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				context.blockableDataParser = new Blocking<>(context.dataParser);
				Future<?> parseDataTask = context.parsetaskExecutor.submit(context.blockableDataParser);
				context.testee.setParseData(DocumentParts.DATA, parseDataTask);
			} },
		SUBMIT_METADATA {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				context.blockableMetadataParser = new Blocking(context.metadataParser);
				Future<?> parseMetadataTask = context.parsetaskExecutor.submit(context.blockableMetadataParser);
				context.testee.setParseData(DocumentParts.METADATA, parseMetadataTask);
			} },
		START_DATA {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				context.blockableDataParser.enableStart();
			} },
		START_METADATA {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				context.blockableMetadataParser.enableEnd();
			} },
		END_DATA {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				context.blockableDataParser.enableEnd();
			} },
		END_METADATA {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				context.blockableMetadataParser.enableStart();
			} },
		TAKE_COMPLETED_TASK {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				try {
					context.parsetaskExecutor.take();
				} catch (InterruptedException ex) {
					assumeNoException(ex);
				}
			} },
		NO_FIN {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				assertThat("not finished", context.testee.isAllTasksFinished(), is(false));
			} },
		FIN {
			@Override
			public void exec(ZylabDataAsynchronousTest context) {
				assertThat("not finished", context.testee.isAllTasksFinished(), is(true));
			} };

		public abstract void exec(ZylabDataAsynchronousTest context);
	}
	@DataPoints
	public static final ConcurrentActions[][] ACTION_SEQUENCES = new ConcurrentActions[][] {
		new ConcurrentActions[] {
			SUBMIT_DATA, NO_FIN, SUBMIT_METADATA, NO_FIN, START_DATA, NO_FIN, START_METADATA, NO_FIN, 
			END_DATA, NO_FIN, END_METADATA, TAKE_COMPLETED_TASK, TAKE_COMPLETED_TASK, FIN},
		new ConcurrentActions[] {
			SUBMIT_DATA, NO_FIN, START_DATA, NO_FIN, END_DATA, NO_FIN, TAKE_COMPLETED_TASK, NO_FIN, 
			SUBMIT_METADATA, NO_FIN, START_METADATA, NO_FIN, END_METADATA, TAKE_COMPLETED_TASK, FIN },
		new ConcurrentActions[] {
			SUBMIT_METADATA, NO_FIN, START_METADATA, NO_FIN, END_METADATA, NO_FIN, TAKE_COMPLETED_TASK, NO_FIN,
			SUBMIT_DATA, NO_FIN, START_DATA, NO_FIN, END_DATA, TAKE_COMPLETED_TASK, FIN} 
	};
	

	private Blocking<ZylabData> blockableDataParser;
	private Blocking<ZylabData> blockableMetadataParser;
	private CompletionService<ZylabData> parsetaskExecutor;
	
	public ZylabDataAsynchronousTest(MetadataResources metadata_, DataResources data_) {
		super(metadata_, data_);
	}
	
	@Before
	public void setup() {
		super.setup();
		parsetaskExecutor = new ExecutorCompletionService<>(Executors.newCachedThreadPool());
	}
	
	
	@Theory
	public void testAsynchronousParsing_isComplete() {
		submitAllTasks();
		releaseAllSemaphores();
		retrieveAllTasks();
		assertTrue("complete", testee.isAllTasksFinished());
	}

	@Theory
	public void testAsynchronousParsing(ConcurrentActions[] actionSequence) {
		for (ConcurrentActions action : actionSequence) {
			action.exec(this);
		}
	}

	private void submitAllTasks() {
		blockableDataParser = new Blocking<>(dataParser);
		Future<?> parseDataTask = parsetaskExecutor.submit(blockableDataParser);
		testee.setParseData(DocumentParts.DATA, parseDataTask);
		
		blockableMetadataParser = new Blocking<>(metadataParser);
		Future<?> parseMetadataTask = parsetaskExecutor.submit(blockableMetadataParser);
		testee.setParseData(DocumentParts.METADATA, parseMetadataTask);
	}
	
	private void releaseAllSemaphores() {
		blockableDataParser.enableStart();
		blockableDataParser.enableEnd();
		blockableMetadataParser.enableStart();
		blockableMetadataParser.enableEnd();
	}

	private void retrieveAllTasks() {
		try {
			parsetaskExecutor.take();
			parsetaskExecutor.take();
		} catch (InterruptedException ex) {
			assumeNoException(ex);
		}
	}
}