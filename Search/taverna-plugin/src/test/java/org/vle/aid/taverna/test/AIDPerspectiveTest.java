package org.vle.aid.taverna.test;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;
import org.vle.aid.taverna.AIDPerspective;
import org.vle.aid.taverna.remote.AIDRemote;


public class AIDPerspectiveTest {
	private AIDPerspective perspective;
	
	@Before
	public void setUp() throws Exception {
		perspective = new AIDPerspective();

	}

	@Test
	public void testGetText() {
		assertEquals("AID Plugin", perspective.getText());
	}


}
