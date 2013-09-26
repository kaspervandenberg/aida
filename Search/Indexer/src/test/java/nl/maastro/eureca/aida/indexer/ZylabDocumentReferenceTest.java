// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ZylabDocumentReferenceTest {
	@Mock private ZylabDocument initiallyReferenced;
	@Mock private ZylabDocument newReferenced;
	private ZylabDocumentReference testee;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testee = new ZylabDocumentReference(initiallyReferenced);
	}

	@Test
	public void testSwitch_forwardsToNew() {
		testee.switchTo(newReferenced);

		testee.getFields();
		verify (newReferenced).getFields();
	}
	
	@Test
	public void testSwitch_notForwardToInitial() {
		testee.switchTo(newReferenced);

		testee.getFields();
		verify (initiallyReferenced, never()).getFields();
	}

	@Test
	public void testSwitchAndMerge_forwardsToNew() {
		testee.switchToAndMerge(newReferenced);

		testee.getFields();
		verify (newReferenced).getFields();
	}
	
	@Test
	public void testSwitchAndMerge_notForwardToInitial() {
		testee.switchToAndMerge(newReferenced);

		testee.getFields();
		verify (initiallyReferenced, never()).getFields();
	}

	@Test
	public void testSwitchAndMerge_newContainsUnion() {
		testee.switchToAndMerge(newReferenced);
		
		verify (newReferenced).merge(initiallyReferenced);
	}
}