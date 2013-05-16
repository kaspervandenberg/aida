// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

/**
 * Common implementation for the wrappers … and … allowing {@link ZylabPatisClient} to 
 * search via the SearcherWS webservice and via a process local Lucene instance.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
abstract class SearcherBase implements Searcher {
	private static final Logger log = Logger.getLogger(SearcherBase.class.getName());
	private final ForkJoinPool taskPool;
	private final String defaultField;
	private final int maxResults;

	protected SearcherBase(final String defaultField_, final int maxResults_) {
		taskPool = new ForkJoinPool();
		defaultField = defaultField_;
		maxResults = maxResults_;
	}

	protected SearcherBase(final String defaultField_, final int maxResults_,
			final ForkJoinPool taskPool_) {
		taskPool = taskPool_;
		defaultField = defaultField_;
		maxResults = maxResults_;
	}

	@Override
	public Iterable<SearchResult> searchForAll(final String query, final Iterable<PatisNumber> patients) {
		return taskPool.invoke(new SearchAllTask<String>(query, patients) {
			@Override
			protected RecursiveTask<SearchResult> searchForOneTask(final String query, final PatisNumber patient) {
				return new RecursiveTask<SearchResult>() {
					@Override
					protected SearchResult compute() {
						try {
							return searchFor(query, patient);
						} catch (QueryNodeException ex) {
							log.log(Level.SEVERE, String.format("syntax error in query: %s", query), ex);
							completeExceptionally(ex);
							return null;
						}
					}
				};
			}
		});
	}

	@Override
	public Iterable<SearchResult> searchForAll(final Query query, final Iterable<PatisNumber> patients) {
		return taskPool.invoke(new SearchAllTask<Query>(query, patients) {
			@Override
			protected RecursiveTask<SearchResult> searchForOneTask(final Query query, final PatisNumber patient) {
				return new RecursiveTask<SearchResult>() {
					@Override
					protected SearchResult compute() {
						return searchFor(query, patient);
					}
				};
			}
		});
	}

	protected abstract class SearchAllTask<TQuery> extends RecursiveTask<Iterable<SearchResult>> {

		private final TQuery query;
		private final Iterable<PatisNumber> patients;
		private final Queue<RecursiveTask<SearchResult>> subtasks = new ConcurrentLinkedQueue<>();
		private final Deque<SearchResult> result = new ConcurrentLinkedDeque<>();

		protected SearchAllTask(final TQuery query_, final Iterable<PatisNumber> patients_) {
			query = query_;
			patients = patients_;
		}

		protected abstract RecursiveTask<SearchResult> searchForOneTask(final TQuery query, final PatisNumber patient);

		@Override
		protected Iterable<SearchResult> compute() {
			forkAll();
			joinAll();
			return result;
		}

		private void forkAll() {
			for (PatisNumber patient : patients) {
				if (!isCancelled()) {
					RecursiveTask<SearchResult> task = searchForOneTask(query, patient);
					subtasks.offer(task);
					task.fork();
				} else {
					return;
				}
			}
		}

		private void joinAll() {
			while (!subtasks.isEmpty()) {
				RecursiveTask<SearchResult> task = subtasks.poll();
				if (!isCancelled()) {
					result.add(task.join());
				} else {
					task.cancel(true);
				}
			}
		}
	}
	
	protected String getDefaultField() {
		return defaultField;
	}

	protected int getMaxResults() {
		return maxResults;
	}
}
