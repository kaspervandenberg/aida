// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Baseclass for {@link FileVisitor}s that forward the visited file to a delagate 
 * {@code FileVisitor} when a condition is met.
 * 
 * {@code ConditionalFileVisitor} provides a {@code isConditionMet_*}-method 
 * for each {@code visit-*}-method of {@link FileVisitor}, deriving classes 
 * must implement these methods.
 * <ul><li>When {@code isConditionMet_*} returns {@code true}, the call is 
 * 		forwarded to delegate;</li>
 * <li>when {@code isConditionMet_*} returns the file or directory (including 
 * 		all files and subdirectories it contains) is skipped ({@link 
 * 		FileVisitResult#CONTINUE} and {@link FileVisitResult#SKIP_SUBTREE}).</li></ul>
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */

public abstract class ConditionalFileVisitor<TDir> implements FileVisitor<TDir> {
	/**
	 * The {@link FileVisitor} to forward calls to when the condition is met.
	 */
	private final FileVisitor<TDir> delegate;

	protected ConditionalFileVisitor(FileVisitor<TDir> delegate_)
	{
		delegate = delegate_; 
	}

	
	/**
	 * Should {@link delegate}.{@link FileVisitor#preVisitDirectory(java.lang.Object, 
	 * java.nio.file.attribute.BasicFileAttributes)} be called?
	 * 
	 * Parameters are as in {@code preVisitDirectory(…)}.
	 * 
	 * @return	<ul><li>{@code true}, the condition is met: call {@code delegate.preVisitDirectory(…)}; or</li>
	 * 			<li>{@code false}, the condition is not met: <em>do not</em> call 
	 * 					{@code delegate.preVisitDirectory(…)} and skip this subtree (i.e. 
	 * 					{@link FileVisitResult#SKIP_SUBTREE}).</li></ul>
	 * 
	 * @see FileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes) 
	 */
	public abstract boolean isConditionMet_preVisitDirectory (TDir dir, BasicFileAttributes attrs) throws IOException; 
	

	/**
	 * Should {@link delegate}.{@link FileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)}
	 * be called?
	 * 
	 * Parameters are as in {@code  visitFile(…)}
	 * 
	 * @return	<ul><li>{@code true}, the condition is met: call {@code delegate.visitFile(…)}; or</li>
	 * 			<li>{@code false}, the condition is not met: <em>do not</em> call 
	 * 				{@code delegate.visitFile(…)}.</li></ul>
	 * 
	 * @see FileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes) 
	 */
	public abstract boolean isConditionMet_visitFile(TDir file, BasicFileAttributes attrs) throws IOException;


	/**
	 * Should {@link delegate}.{@link FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)}
	 * be called?
	 * 
	 * Parameters are as in {@code  visitFileFailed(…)}
	 * 
	 * @return	<ul><li>{@code true}, the condition is met: call {@code delegate.visitFileFailed(…)}; or</li>
	 * 			<li>{@code false}, the condition is not met: <em>do not</em> call 
	 * 				{@code delegate.visitFileFailed(…)}.</li></ul>
	 * 
	 * @see FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException) 
	 */
	public abstract boolean isConditionMet_visitFileFailed(TDir file, IOException exc) throws IOException;

	
	/**
	 * Should {@link delegate}.{@link FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)}
	 * be called?
	 * 
	 * Parameters are as in {@code  postVisitDirectory(…)}
	 * 
	 * @return	<ul><li>{@code true}, the condition is met: call {@code delegate.postVisitDirectory(…)}; or</li>
	 * 			<li>{@code false}, the condition is not met: <em>do not</em> call 
	 * 				{@code delegate.postVisitDirectory(…)}.</li></ul>
	 * 
	 * @see FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException) 
	 */
	public abstract boolean isConditionMet_postVisitDirectory(TDir dir, IOException exc) throws IOException;

	
	@Override
	public FileVisitResult preVisitDirectory(TDir dir, BasicFileAttributes attrs) throws IOException
	{
		if (isConditionMet_preVisitDirectory(dir, attrs)) {
			return delegate.preVisitDirectory(dir, attrs);
		} else {
			return FileVisitResult.SKIP_SUBTREE;
		}
	}

	
	@Override
	public FileVisitResult visitFile(TDir file, BasicFileAttributes attrs) throws IOException
	{
		if (isConditionMet_visitFile(file, attrs)) {
			return delegate.visitFile(file, attrs);
		} else {
			return FileVisitResult.CONTINUE;
		}
	}

	
	@Override
	public FileVisitResult visitFileFailed(TDir file, IOException exc) throws IOException
	{
		if (isConditionMet_visitFileFailed(file, exc)) {
			return delegate.visitFileFailed(file, exc);
		} else {
			return FileVisitResult.CONTINUE;
		}
	}

	
	@Override
	public FileVisitResult postVisitDirectory(TDir dir, IOException exc) throws IOException
	{
		if (isConditionMet_postVisitDirectory(dir, exc)) {
			return delegate.postVisitDirectory(dir, exc);
		} else {
			return FileVisitResult.CONTINUE;
		}
	}

}
