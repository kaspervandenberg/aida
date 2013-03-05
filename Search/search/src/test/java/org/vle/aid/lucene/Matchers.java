/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.lucene;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.vle.aid.lucene.IndexedDocuments.Documents;
import org.vle.aid.lucene.IndexedDocuments.Fields;
import org.vle.aid.lucene.IndexedDocuments.FieldContents;
import org.vle.aid.lucene.IndexedDocuments.Queries;

/**
 * Matchers that operate on {@link IndexedDocuments}
 * 
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
public class Matchers {
	private final IndexedDocuments index;

	public Matchers(final IndexedDocuments index_) {
		index = index_;
	}

	/**
	 * Return a {@link Iterable} of {@link Matcher} that matches if
	 * {@code item} contains all elements of {@code docs} and that can be
	 * used in {@link CoreMatchers#allOf(java.lang.Iterable)} and
	 * {@link CoreMatchers#anyOf(java.lang.Iterable) }.
	 *
	 * @param <T>	the class of items the matchers can operate on.  See
	 * 		{@link Documents#containedIn(java.lang.Class) } for supported
	 * 		values.
	 * @param itemType idem to {@code <T>}
	 * @param docs	the set of documents to match
	 *
	 * @return 	an Iterable that calls {@link Documents#containedIn(java.lang.Class) }
	 * 		on each element of {@code docs}
	 */
	public static <T> Iterable<Matcher<? extends T>> containingAll(final Iterable<Documents> docs, final Class<T> itemType) {
		return new Iterable<Matcher<? extends T>>() {
			@Override
			public Iterator<Matcher<? extends T>> iterator() {
				return new Iterator<Matcher<? extends T>>() {
					private final Iterator<Documents> iDocs = docs.iterator();

					@Override
					public boolean hasNext() {
						return iDocs.hasNext();
					}

					@Override
					public Matcher<T> next() {
						return iDocs.next().containedIn(itemType);
					}

					@Override
					public void remove() {
						iDocs.remove();
					}
				};
			}
		};
	}

	/**
	 * Return a {@link Matcher} that matches when {@code item} contains
	 * all {@link Documents} expected as result for {@code query}.
	 *
	 * @param <T>	the class of items the matchers can operate on.  See
	 * 		{@link Documents#containedIn(java.lang.Class) } for supported
	 * 		values.
	 * @param itemType idem to {@code <T>}
	 * @param query	the {@link Queries lucene query} whose
	 * 		{@link #hittingDocs(org.vle.aid.lucene.SearcherWSTest.Queries, org.vle.aid.lucene.SearcherWSTest.Fields, org.vle.aid.lucene.SearcherWSTest.Queries.MatchStrategy) }
	 * 		to expect.
	 *
	 * @return a matcher for items of type {@code <T>}
	 */
	public <T> Matcher<T> containsAllExpectedResultsOf(Class<T> itemtype, Queries query) {
		return CoreMatchers.allOf(containingAll(
				hittingDocs(query, query.field, IndexedDocuments.Queries.MatchStrategy.ANY), itemtype));
	}

	/**
	 * Return a {@link Matcher} that matches when {@code item} does not
	 * contain any{@link Documents} not expected as result for {@code query}.
	 *
	 * @param <T>	the class of items the matchers can operate on.  See
	 * 		{@link Documents#containedIn(java.lang.Class) } for supported
	 * 		values.
	 * @param itemType idem to {@code <T>}
	 * @param query	the {@link Queries lucene query} whose complement of
	 * 		{@link #hittingDocs(org.vle.aid.lucene.SearcherWSTest.Queries, org.vle.aid.lucene.SearcherWSTest.Fields, org.vle.aid.lucene.SearcherWSTest.Queries.MatchStrategy) }
	 * 		not to expect.
	 * @param index	a {@link IndexedDocuments} used to test {@code query} on.
	 *
	 * @return a matcher for items of type {@code <T>}
	 */
	public <T> Matcher<T> containsNoUnexpectedResultsOf(Class<T> itemtype, IndexedDocuments.Queries query) {
		return CoreMatchers.not(CoreMatchers.anyOf(containingAll(EnumSet.complementOf(
				hittingDocs(query, query.field, IndexedDocuments.Queries.MatchStrategy.ANY)),
				itemtype)));
	}
	
	/**
	 * The set of {@link Documents} that have at least one
	 * {@link Fields fields} that matches {@code query}.
	 *
	 * @param query	{@link Queries} id
	 * @param matchStrat	strategy that defines whether any matching
	 * 		{@link FieldContents} is sufficient for the query to match or
	 * 		the document's field must have all the query's
	 * 		{@code FieldContents}.
	 *
	 * @return a {@link EnumSet} of matching documents.
	 */
	public EnumSet<Documents> hittingDocs(final Queries query, final Queries.MatchStrategy matchStrat) {
		EnumSet<Documents> result = EnumSet.noneOf(Documents.class);
		for (Documents doc : index.allDocuments()) {
			if (documentMatcher(doc, matchStrat).matches(query)) {
				result.add(doc);
			}
		}
		return result;
	}

	/**
	 * The set of {@link Documents} that have {@code field} that matches
	 * {@code query}.
	 *
	 * @param query	{@link Queries} id
	 * @param matchStrat	strategy that defines whether any matching
	 * 		{@link FieldContents} is sufficient for the query to match or
	 * 		the document's field must have all the query's
	 * 		{@code FieldContents}.
	 *
	 * @return a {@link EnumSet} of matching documents.
	 */
	public EnumSet<Documents> hittingDocs(final Queries query, final Fields field, final Queries.MatchStrategy matchStrat) {
		EnumSet<Documents> result = EnumSet.noneOf(Documents.class);
		for (Documents doc : index.allDocuments()) {
			if (documentMatcher(doc, field, matchStrat).matches(query)) {
				result.add(doc);
			}
		}
		return result;
	}

	/**
	 * Return a Hamcrest {@link Matcher} that matches when a query 'hits'
	 * the {@code field} of document {@code doc}.
	 *
	 * @param doc	the document id
	 * @param field	the field id
	 * @param strategy	{@link Queries.MatchStrategy} used to determine
	 * 		whether a query hits.
	 *
	 * @return {@link Matcher} that can be used in unit tests.
	 */
	public Matcher<Queries> documentMatcher(final Documents doc, final Fields field, final Queries.MatchStrategy strategy) {
		if (index.contains(doc,field)) {
			Set<FieldContents> con = EnumSet.noneOf(FieldContents.class);
			for (FieldContents value : index.contentsOf(doc, field)) {
				con.add(value);
			}
			Set<FieldContents> docContents = Collections.unmodifiableSet(con);
			return Queries.matchFieldContents(docContents, strategy);
		} else {
			return CoreMatchers.<Queries>not(CoreMatchers.<Queries>anything());
		}
	}

	/**
	 * Return a Hamcrest {@link Matcher} that matches when a query 'hits'
	 * the document {@code doc}.
	 *
	 * @param doc	the document id
	 * @param strategy	{@link Queries.MatchStrategy} used to determine
	 * 		whether a query hits.
	 *
	 * @return {@link Matcher} that can be used in unit tests.
	 */
	public Matcher<Queries> documentMatcher(final Documents doc, final Queries.MatchStrategy strategy) {
		if (index.contains(doc)) {
			Set<FieldContents> con = EnumSet.noneOf(FieldContents.class);
			for (FieldContents value : index.contentsOf(doc)) {
				con.add(value);
			}
			Set<FieldContents> docContents = Collections.unmodifiableSet(con);
			return Queries.matchFieldContents(docContents, strategy);
		} else {
			return CoreMatchers.<Queries>not(CoreMatchers.<Queries>anything());
		}
	}

	/**
	 * Return a Hamcrest {@link Matcher} that matches when a query 'hits'
	 * any document stored in this {@link IndexedDocuments}.
	 *
	 * @param strategy	{@link Queries.MatchStrategy} used to determine
	 * 		whether a query hits.
	 *
	 * @return {@link Matcher} that can be used in unit tests.
	 */
	public Matcher<Queries> allDocsMatcher(final Queries.MatchStrategy strategy) {
		Set<FieldContents> con = EnumSet.noneOf(FieldContents.class);
		for (FieldContents value : index.allContents()) {
			con.add(value);
		}
		Set<FieldContents> docContents = Collections.unmodifiableSet(con);
		return Queries.matchFieldContents(docContents, strategy);
	}

	/**
	 * Return a Hamcrest {@link Matcher} that matches when a query 'hits'
	 * {@code field} of any document stored in this {@link IndexedDocuments}.
	 *
	 * @param field 	{@link Fields} identifier of field to match on
	 * @param strategy	{@link Queries.MatchStrategy} used to determine
	 * 		whether a query hits.
	 *
	 * @return {@link Matcher} that can be used in unit tests.
	 */
	public Matcher<Queries> allDocsMatcher(final Fields field, final Queries.MatchStrategy strategy) {
		Set<FieldContents> con = EnumSet.noneOf(FieldContents.class);
		for (Documents doc : index.allDocuments()) {
			if (index.contains(doc,field)) {
				for(FieldContents c : index.contentsOf(doc, field)) {
					con.add(c);
				}
			}
		}
		Set<FieldContents> docContents = Collections.unmodifiableSet(con);
		return Queries.matchFieldContents(docContents, strategy);
	}

}
