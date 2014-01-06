// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Submit a task to an executorservice for each visited file.
 * 
 * For a directory no task is submitted but their contents is visited.
 * For example:
 * <pre>
 * ./			→ no task
 * ./file1		→ generate task
 * ./file2		→ generate task
 * ./subdir		→ no task
 * ./subdir/file		→ generate task
 * ./subdir/emptydir/	→ no task
 * ./file3		→ generate task
 * ./other_emptydir/	→ no task
 * </pre>
 *
 * Combine different {@link FileVisitor}-adapters, with this {@code 
 * TaskSubmittingFileVisitor} (non-adapting) {@code FileVisitor} as the final
 * one, to create the required behaviour:
 * <ul><li>{@link RecentlyChanged_ConditionalFileVisitor},</li> 
 * <li>{@link nl.maastro.eureca.aida.indexer.jmx.TerminatableFileVisitor},
 * 		and</li>
 * <li>{@link TaskSubmittingFileVisitor}.</li></ul>
 * <em>Advise:</em> when the file crawling part of the 
 * indexing process has to change, create new {@code FileVisitor}-adapters with
 * a single function and use it in the filevisitor composition.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class TaskSubmittingFileVisitor<TDir>
		extends SimpleFileVisitor<TDir>
{
	/**
	 * {@link TaskSubmitter} used to create and submit tasks.
	 */
	private final TaskSubmitter<TDir, ?> taskSubmitter;

	
	/**
	 * Constructor
	 * 
	 * @param taskSubmitter_	used to create and submit tasks. 
	 */
	public TaskSubmittingFileVisitor(TaskSubmitter<TDir, ?> taskSubmitter_) {
		this.taskSubmitter = taskSubmitter_;
	}
	
	
	/**
	 * {@link TaskSubmitter#createAndSubmit(java.lang.Object) Create and 
	 * submit} a task for {@code file}.
	 * 
	 * @param file		supplied to {@link #taskSubmitter}
	 * @param ignored	not used
	 * 
	 * @return		always {@link FileVisitResult#CONTINUE}
	 */
	@Override
	public FileVisitResult visitFile(TDir file, BasicFileAttributes ignored) {
		taskSubmitter.createAndSubmit(file);
		return FileVisitResult.CONTINUE;
	}
}
