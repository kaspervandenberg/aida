// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * A FileVisitor that only visits files which were recently created or modified.
 *
 * Combine different {@link FileVisitor}-adapters to create the required 
 * behaviour:
 * <ul><li>{@link RecentlyChanged_ConditionalFileVisitor},</li>
 * <li>{@link nl.maastro.eureca.aida.indexer.jmx.TerminatableFileVisitor},
 * 		and</li>
 * <li>{@link TaskSubmittingFileVisitor}.</li></ul>
 * <em>Advise:</em> when the file crawling part of the  indexing process has 
 * to change, create new {@code FileVisitor}-adapters with a single function
 * and use it in the {@code FileVisitor} composition.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class RecentlyChanged_ConditionalFileVisitor<TDir> 
		extends ConditionalFileVisitor<TDir> {
	
	/**
	 * The {@link FileTime} to compare the visited files to.
	 */
	private final FileTime targetTimestamp;

	
	/**
	 * Directory recursion strategy:
	 * <ul><li>{@code true}, always visit a the files and subdirectories in a 
	 * 		directory, regardless or the directory's {@link 
	 * 		BasicFileAttributes#creationTime()} and 
	 * 		{@link BasicFileAttributes#lastModifiedTime()}; or</li>
	 * <li>{@code false}, only enter directories more recent than 
	 * 		{@link #targetTimestamp} and skip directories that are older.
	 * </li><ul>
	 */
	private final boolean alwaysRecurseIntoDirectories;


	/**
	 * Construct a RecentlyChanged_ConditionalFileVisitor which forwards 
	 * {@code visit*}-calls to {@code delegate_} when visiting files more
	 * recent than {@code targetTimestamp_}.
	 * 
	 * @param delegate_		the {@link FileVisitor} to forward {@code 
	 * 		visit*}-calls to.
	 * @param targetTimestamp_		the {@link FileTime} to compare file 
	 * 		attributes to.
	 */
	public RecentlyChanged_ConditionalFileVisitor(
			FileVisitor<TDir> delegate_,
			FileTime targetTimestamp_)
	{
		this(delegate_, targetTimestamp_, true);
	}


	/**
	 * Construct a RecentlyChanged_ConditionalFileVisitor which forwards 
	 * {@code visit*}-calls to {@code delegate_} when visiting files more
	 * recent than {@code targetTimestamp_}.
	 * 
	 * @param delegate_		the {@link FileVisitor} to forward {@code 
	 * 		visit*}-calls to.
	 * @param targetTimestamp_		the {@link FileTime} to compare file 
	 * 		attributes to.
	 * @param alwaysRecurseIntoDirectories_		directory recursion strategy:
	 *		<ul><li>{@code true}, always visit a the files and subdirectories
	 * 			in a directory, regardless or the directory's {@link 
	 * 			BasicFileAttributes#creationTime()} and 
	 * 			{@link BasicFileAttributes#lastModifiedTime()}; or</li>
	 * 		<li>{@code false}, only enter directories more recent than 
	 * 			{@link #targetTimestamp} and skip directories that are older.
	 * 		</li><ul>
	 */
	public RecentlyChanged_ConditionalFileVisitor(
			FileVisitor<TDir> delegate_,
			FileTime targetTimestamp_,
			boolean alwaysRecurseIntoDirectories_)
	{
		super (delegate_);
		this.targetTimestamp = targetTimestamp_;
		this.alwaysRecurseIntoDirectories = alwaysRecurseIntoDirectories_;
	}

	/**
	 * Is {@code toCheck} more recent than {@code target} or from the same time
	 * as {@code target}?
	 * 
	 * @param toCheck	the {@link FileTime} of the visited file.
	 * @param target	the {2link FileTime} to compare {@code toCheck} to
	 * 					(e.g. {@link #targetTimestamp}).
	 * 
	 * @return 	<ul><li>{@code true}, {@code toCheck} is at least as recent as 
	 * 				{@code target}; or</li>
	 * 			<li>{@code false}, {@code toCheck} is older than {@code target}.
	 * 		</li></ul>
	 */
	private static boolean isSameTime_Or_LaterThan(FileTime toCheck, FileTime target)
	{
		return toCheck.compareTo(target) >= 0;
	}
			

	/**
	 * Compare {@code attrs}.{@link BasicFileAttributes#lastModifiedTime()} and
	 * -{@link BasicFileAttributes#creationTime()} to {@link #targetTimestamp}.
	 * 
	 * @param attrs		{@link BasicFileAttributes} of the visited file.
	 * 
	 * @return	<ul><li>{@code true}, the file of {@code attrs} is created or 
	 * 				modified at the same time or more recent than 
	 * 				{@code targetTimestamp}; or</li>
	 * 		<li>{code false}, the file is older than {@code targetTimestamp}.
	 * 			</li></ul>
	 */
	public boolean isMoreRecentThanTarget(BasicFileAttributes attrs)
	{
		return isSameTime_Or_LaterThan(attrs.creationTime(), targetTimestamp) ||
				isSameTime_Or_LaterThan(attrs.lastModifiedTime(), targetTimestamp);
	}

	@Override
	public boolean isConditionMet_preVisitDirectory(TDir dir, BasicFileAttributes attrs)
			throws IOException
	{
		return alwaysRecurseIntoDirectories ||
				isMoreRecentThanTarget(attrs);
	}

	@Override
	public boolean isConditionMet_visitFile(TDir file, BasicFileAttributes attrs)
			throws IOException
	{
		return isMoreRecentThanTarget(attrs);
	}

	@Override
	public boolean isConditionMet_visitFileFailed(TDir file, IOException exc)
			throws IOException
	{
		return true; // always notify delegate when 'visitFileFailed'
	}

	@Override
	public boolean isConditionMet_postVisitDirectory(TDir dir, IOException exc)
			throws IOException
	{
		return true; // always call delegate's postVisitDirectory.
	}
	
	
}
