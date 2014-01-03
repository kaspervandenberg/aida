// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.jmx;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Decorate a {@link #delegate} {@link FileVisitor} to terminate when a flag
 * ({@link #continue_file_traversal} is {@code false}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class TerminatableFileVisitor<TDir>
		implements FileVisitor<TDir>
{
	/**
	 * Default for {@link #always_call_post_visit}.
	 */
	public static final boolean ALWAYS_CALL_POST_VISIT_DEFAULT = false;
	
	/**
	 * Flag set somewhere outside {@code TerminatableFileVisitor}; when:
	 * <ul><li>{@code true}, call {@link #delegate} {@code visit*}-methods; and
	 * 		when</li>
	 * <li>{@code false}, return {@link FileVisitResult#TERMINATE}.</li></ul>
	 */
	private final AtomicBoolean continue_file_traversal;
	

	/**
	 * Policy should {@link FileVisitor#postVisitDirectory(java.lang.Object,
	 * java.io.IOException)} and {@link FileVisitor#visitFileFailed(
	 * java.lang.Object, java.io.IOException)} be called one last time
	 * before terminating?.
	 * <ul><li>{@code true}, when {@link #continue_file_traversal} becomes
	 * 			{@code false} terminate <em>after</em> calling
	 * 			{@link FileVisitor#postVisitDirectory(java.lang.Object,
	 * 			java.io.IOException)} or 
	 * 			{@link FileVisitor#visitFileFailed(java.lang.Object, 
	 * 			java.io.IOException)}; or</li>
	 * <li>{@code false}, terminate <em>immediatly</em> when
	 * 			{@code continue_file_traversal} becomes {@code false}.</li></ul>
	 * 
	 * {@code always_call_post_visit} does not affect {@link #preVisitDirectory(
	 * java.lang.Object, java.nio.file.attribute.BasicFileAttributes) } and
	 * {@link #visitFile(java.lang.Object, 
	 * java.nio.file.attribute.BasicFileAttributes)}: these methods will always
	 * immediatly terminate when {@code continue_file_traversal} becomes
	 * {@code false}.
	 * 
	 * Setting {@code always_call_post_visit} to {@code true} allows the 
	 * decorated visitor to cleanly finish the visited file or directory.
	 */
	private final boolean always_call_post_visit;


	/**
	 * The decorated {@link FileVisitor}.
	 */
	private final FileVisitor<TDir> delegate;


	/**
	 * Constructor
	 * 
	 * @param continue_file_traversal_flag_		flag that the caller can change
	 * 			to {@code false} when this {@code TerminatableFileVisitor} has
	 * 			to terminate.
	 * 
	 * @param delegate_		the {@link FileVisitor} to decorate; all 
	 * 			{@code visit*}-calls are forwarded to {@code delegate_}
	 * 			(for as long as {@code continue_file_traversal_flag_} remains
	 * 			{@code true}).
	 */
	public TerminatableFileVisitor (
			AtomicBoolean continue_file_traversal_flag_,
			FileVisitor<TDir> delegate_)
	{
		this (continue_file_traversal_flag_, delegate_, ALWAYS_CALL_POST_VISIT_DEFAULT);
	}


	/**
	 * Constructor
	 * 
	 * @param continue_file_traversal_flag_		flag that the caller can change
	 * 			to {@code false} when this {@code TerminatableFileVisitor} has
	 * 			to terminate.
	 * 
	 * @param delegate_		the {@link FileVisitor} to decorate; all 
	 * 			{@code visit*}-calls are forwarded to {@code delegate_}
	 * 			(for as long as {@code continue_file_traversal_flag_} remains
	 * 			{@code true}).
	 * 
	 * @param always_call_post_visit_	<ul><li>{@code true}, when {@link 
	 * 			#continue_file_traversal} becomes {@code false} terminate 
	 * 			<em>after</em> calling {@link FileVisitor#postVisitDirectory(
	 * 			java.lang.Object, java.io.IOException)} or 
	 * 			{@link FileVisitor#visitFileFailed(java.lang.Object,
	 * 			java.io.IOException)}; or</li>
	 * 		<li>{@code false}, (default = {@value #ALWAYS_CALL_POST_VISIT_DEFAULT})
	 * 			terminate <em>immediatly</em> when {@code continue_file_traversal}
	 * 			becomes {@code false}.</li></ul>
	 */
	public TerminatableFileVisitor (
			AtomicBoolean continue_file_traversal_flag_,
			FileVisitor<TDir> delegate_,
			boolean always_call_post_visit_)
	{
		this.continue_file_traversal = continue_file_traversal_flag_;
		this.delegate = delegate_;
		this.always_call_post_visit =always_call_post_visit_;
	}
	
	
	@Override
	public FileVisitResult preVisitDirectory(TDir dir, BasicFileAttributes attrs)
			throws IOException
	{
		if (continue_file_traversal.get()) {
			return delegate.preVisitDirectory (dir, attrs);
		} else {
			return FileVisitResult.TERMINATE;
		}
	}

	
	@Override
	public FileVisitResult visitFile(TDir file, BasicFileAttributes attrs)
			throws IOException
	{
		if (continue_file_traversal.get()) {
			return delegate.visitFile (file, attrs);
		} else {
			return FileVisitResult.TERMINATE;
		}
	}

	
	@Override
	public FileVisitResult visitFileFailed(TDir file, IOException exc)
			throws IOException
	{
		if (continue_file_traversal.get()) {
			return delegate.visitFileFailed (file, exc);
		} else {
			if (always_call_post_visit) {
				delegate.visitFileFailed (file, exc);
			}
			return FileVisitResult.TERMINATE;
		}
	}

	@Override
	public FileVisitResult postVisitDirectory(TDir dir, IOException exc)
			throws IOException
	{
		if (always_call_post_visit || continue_file_traversal.get()) {
			return delegate.postVisitDirectory(dir, exc);
		} else {
			if (always_call_post_visit) {
				delegate.postVisitDirectory(dir, exc);
			}
			return FileVisitResult.TERMINATE;
		}
	}
	
}
