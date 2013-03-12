/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.lucene;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.junit.After;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Reproduce {@link http://dev-issues.ad.maastro.nl/browse/AIDA-3}
 * 
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class BugAida3Test {
	static final private String PROP_SEARCH_KEY="target.search.url";
	static final private String PROP_SEARCH_VALUE=System.getProperty(PROP_SEARCH_KEY);
	static private URI searchService;
	static private URI jsonSearchService;
	
	private final Queries query;
	private HttpClient httpclient;
	private HttpPost post;

	public enum Indexes {
		ZYLAB_TEST("Zylab_test"),
		UNEXISTING(String.format("UNEXISTING%s", UUID.randomUUID().toString()));
		
		final public String indexName;
		
		private Indexes(String indexName_) {
			indexName = indexName_;
		}
	}

	public enum SearchString {
		PROSTATE("prostate");

		final public String text;
		
		private SearchString(String text_) {
			text =text_;
		}
	}

	public enum Queries {
		ZYLABTEST_PROSTATE(Indexes.ZYLAB_TEST, SearchString.PROSTATE),
		UNEXISTING_INDEX(Indexes.UNEXISTING, SearchString.PROSTATE);

		final public Indexes index;
		final public SearchString searchString;
		private HttpEntity entity = null;
		
		private Queries(Indexes index_, SearchString searchString_) {
			index = index_;
			searchString = searchString_;
		}

		public HttpEntity getEntity() {
			if(entity == null) {
				entity = createEntity();
			}
			return entity;
		}

		private HttpEntity createEntity() {
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("start", "0"));
			params.add(new BasicNameValuePair("count", "10"));
			params.add(new BasicNameValuePair("target", "search"));
			params.add(new BasicNameValuePair("index", this.index.indexName));
			params.add(new BasicNameValuePair("field", "content"));
			params.add(new BasicNameValuePair("query", this.searchString.text));
			try {
				return new UrlEncodedFormEntity(params);
			} catch (UnsupportedEncodingException ex) {
				throw new Error(ex);
			}
		}
	}

	@DataPoints
	public static Queries all_queries[] = Queries.values();

	public BugAida3Test(Queries query_) {
		query = query_;
	}

	
	
	@BeforeClass
	public static void setupClass() {
		try {
			if(PROP_SEARCH_VALUE.isEmpty()) {
				fail(String.format(
						"Define the property %s to specify the search webservice to call for testing.",
						PROP_SEARCH_KEY));
			}
			searchService = new URI(PROP_SEARCH_VALUE);
			jsonSearchService =  new URL(searchService.toURL(), "jason").toURI();
		} catch (MalformedURLException | URISyntaxException ex) {
			fail(String.format("Search service URL ('%s') is invallid.",
					PROP_SEARCH_VALUE));
		}
	}

	@Before
	public void setup() {
		httpclient = new DefaultHttpClient();
		post = new HttpPost(jsonSearchService);
		post.setEntity(query.getEntity());
	}

	@After
	public void tearDown() {
		post.releaseConnection();
		httpclient.getConnectionManager().shutdown();
	}
	
	/**
	 * Test whether the search service answers when {@link #query} is posted.
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws IOException 
	 */
	@Theory
	public void testServiceAnswers() 
			throws UnsupportedEncodingException, IOException {
		
		HttpResponse result = httpclient.execute(post);
		assertThat("Expecting HTTP response OK (200)", 
				result.getStatusLine().getStatusCode(), is(200));
		assertThat("Expecting some characters in result contents", 
				getContents(result).length(), greaterThan(0));
	}

	/**
	 * Test whether the search service responds with a valid JSON
	 * 
	 * @throws IOException
	 * @throws JSONException 
	 */
	@Theory
	public void testServiceVallidJSON() throws IOException, JSONException {
		HttpResponse result = httpclient.execute(post);
		assumeThat(result.getStatusLine().getStatusCode(), is(200));
		
		String resultContents = getContents(result);
		assumeThat(resultContents.length(), greaterThan(0));

		JSONObject o = new JSONObject(resultContents);
	}


	/**
	 * Test whether the service response contains no results that do not contain
	 * 
	 * 
	 * @throws IOException
	 * @throws JSONException 
	 */
	@Theory
	public void testServicePrecision() throws IOException, JSONException {
		HttpResponse response = httpclient.execute(post);
		assumeThat(response.getStatusLine().getStatusCode(), is(200));
		
		String contents = getContents(response);
		assumeThat(contents.length(), greaterThan(0));

		JSONObject result = new JSONObject(contents);
		System.out.println(result.toString());
		
		assumeTrue(result.has("items"));
		JSONArray items = result.getJSONArray("items");
		for (int i = 0; i < items.length(); i++) {
			JSONObject item = items.getJSONObject(i);
			assumeTrue(item.has("description"));
			assertThat(String.format("Result item (%s) contains searched string",
					(item.has("id") ? item.getString("id") :"¿unknown?" )),
					item.getString("description"), containsString(query.searchString.text));
		}
	}
	
	private static String getContents(HttpResponse response)
			throws IOException {
		HttpEntity contents = response.getEntity();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	
		IOUtils.copy(contents.getContent(), out);
		assertThat("Expecting some characters in result contents", 
				out.size(), greaterThan(0));
	
		EntityUtils.consume(contents);
		return out.toString();
	}
}
