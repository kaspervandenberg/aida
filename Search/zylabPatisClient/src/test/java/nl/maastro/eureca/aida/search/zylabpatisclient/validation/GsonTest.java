// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test whether Gson does what {@link ExpectedPreviousResults} and its test require.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class GsonTest {
	private static class Data {
		private int intVal;
		private String stringVal;
	}
	
	@Mock Data mockedData;
	
	public GsonTest() {
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@Ignore
	public void testWriteMocked() {
		new Gson().toJson(mockedData);
	}
}