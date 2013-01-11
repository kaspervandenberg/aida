/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.test.time;

import com.aliasi.util.Reflection;

/**
 * The <code>Stopwatch</code> class supports testing the execution
 * time of a runnable.  A single static method {@link
 * #test(Runnable,int,int)} allows the specification of a runnable,
 * the number of warmup iterations, and the number of test iterations.
 * It returns a result in the form of the inner class {@link
 * Results}. A stopwatch may be employed either by using this class's
 * {@link #main(String[])} method from the command line, or using the
 * static {@link #test(Runnable,int,int)} method.
 *
 * <P>Care should be taken in the use of stopwatch timing.  With the
 * combination of static and run-time code optimization,
 * micro-benchmarks may be highly misleading.  For instance, loops
 * with arithmetic operations assigning to unused local variables may
 * be optimized away to no operation.  One way to protect against this
 * kind of optimization is to store cumulative results of computations
 * in a variable that outlives the stopwatch timing run; that way,
 * computations involved in creating it can't be optimized away.
 *
 * @author  Bob Carpenter
 * @version 1.0.7
 * @since   LingPipe1.0
 */
public class Stopwatch {
    
    /**
     * Invokes a test as a command, with specified number of warmup runs,
     * test runs, class name, and string arguments.  The command-line
     * syntax is:
     * 
     * <blockquote>
     * <code>
     *   java com.aliasi.test.time.Stopwatch 
     *   <br>&nbsp;&nbsp;&nbsp;
     *   <i>numWarmups</i> <i>numTests</i> <i>className</i> 
     *                  <i>arg1</i> ... <i>argN</i>  [<i>N</i> &gt;= 0]
     * </code>
     * </blockquote>
     *
     * The arguments are strings, and there must be an
     * <i>N</i>-argument constructor with string argument types for
     * the class with the name <i>className</i>.  In particular, if
     * there are no arguments, there must be a no-argument
     * constructor.  
     * 
     * @param args Command-line arguments.
     */
    public static void main(String[] args) throws Exception {
	if (args.length < 3) {
	    String msg = "Must specify name of class as argument.";
	    throw new IllegalArgumentException(msg);
	}
	int numWarmups = Integer.parseInt(args[0]);
	int numTests = Integer.parseInt(args[1]);
	String className = args[2];
	String[] consArgs = new String[args.length-3];
	System.arraycopy(args,3,consArgs,0,consArgs.length);
	Runnable runnable 
	    = (Runnable) Reflection.newInstance(className,consArgs);
	if (runnable == null) {
	    String msg = "Could not construct runnable.";
	    throw new IllegalArgumentException(msg);
	}
	Results results = test(runnable,numWarmups,numTests);
	System.out.println();
	System.out.println("RESULTS");
	System.out.println(results.toString());
    }



    /**
     * Tests the specified runnable, using the specified number
     * of warmup and test runs.
     *
     * @param runnable Runnable to test.
     * @param numWarmupRuns Number of warmup runs before testing.
     * @param numTestRuns Number of test runs.
     * @return Results object.
     */
    public static Results test(Runnable runnable,
			       int numWarmupRuns, 
			       int numTestRuns) {
	for (int i = 0; i < numWarmupRuns; ++i) {
	    runnable.run();
	}
	Results results = new Results(numWarmupRuns,numTestRuns);
	for (int i = 0; i < numTestRuns; ++i) {
	    results.start();
	    runnable.run();
	    results.end();
	}
	return results;
    }

    /**
     * Results of a timing run.  Single results can be accessed
     * directly through {@link #runTimeMillis(int)}.  Collective
     * results in the form of mean (average), deviation and variance
     * are returned through methods.  A report on the run is generated
     * by {@link #toString()}.
     *
     * @author  Bob Carpenter
     * @version 1.0.7
     * @since   LingPipe1.0
     */
    public static class Results {
	private final int mNumWarmupRuns;
	private final long[] mRunTimes;
	private int mRun;
	private long mStartTime;

	/**
	 * Construct a results object with the specified number
	 * of warmup runs and test runs.
	 *
	 * @param numWarmupRuns Number of warmup runs.  
	 * @param numTestRuns Number of test runs.
	 */
	private Results(int numWarmupRuns, int numTestRuns) {
	    mNumWarmupRuns = numWarmupRuns;
	    mRunTimes = new long[numTestRuns];
	}

	/**
	 * Called by the {@link #test(Runnable,int,int)} method to
	 * start a timing run.
	 */
	private void start() {
	    mStartTime = System.currentTimeMillis();
	}
    
	/**
	 * Called by the {@link #test(Runnable,int,int)} method to
	 * end a timing run and record the time.
	 */
	private void end() {
	    long endTime = System.currentTimeMillis();
	    mRunTimes[mRun++] = endTime - mStartTime;
	}

	/**
	 * Returns the run time, in milliseconds, for the specified
	 * test run.  Note that runs are numbered from <code>0</code>
	 * to <code>numRuns()-1</code>.
	 *
	 * @param i Test run whose time is returned.
	 * @return The run time, in milliseconds, for the specified
	 * test run.
	 */
	public long runTimeMillis(int i) {
	    return mRunTimes[i];
	}

	/**
	 * Returns the number of runs for this test.
	 *
	 * @return The number of runs for this test.
	 */
	public int numRuns() {
	    return mRunTimes.length;
	}

	/**
	 * Returns the number of warmup runs for this test.
	 *
	 * @return The number of warmup runs for this test.
	 */
	public int numWarmups() {
	    return mNumWarmupRuns;
	}

	/**
	 * Returns the total time of all test runs in milliseconds.
	 *
	 * @return The total time of all test runs in milliseconds.
	 */
	public long totalTimeMillis() {
	    long total = 0;
	    for (int i = 0; i < numRuns(); ++i)
		total += runTimeMillis(i);
	    return total;
	}

	/**
	 * Returns the variance of the timing of the test runs, in
	 * milliseconds squared.  Variance is the sum of the squared
	 * differences between test run times and the mean test run
	 * time.
	 *
	 * <blockquote>
	 * <code>
	 * SUM<sub>0 &lt;= <i>n</i> &lt; numRuns()</sub>
	 *   (runTimeMillis(<i>n</i>) - meanTimeMillis())<sup>2</sup>
	 *       
	 * </code>
	 * </blockquote>
	 *
	 * @return The variance of the timing of the test runs, in
	 * milliseconds squared.
	 */
	public double varianceTimeMillisSquared() {
	    double mean = meanTimeMillis();
	    double totalVariance = 0.0;
	    for (int i = 0; i < numRuns(); ++i) {
		double difference = mean - (double) runTimeMillis(i);
		totalVariance += difference * difference;
	    }
	    return totalVariance / numRuns();

	}

	/**
	 * Returns the deviation of run times, in milliseconds.  Deviation
	 * is simply the square root of the variance.
	 *
	 * @return The deviation of run times, in milliseconds.  
	 */
	public double deviationTimeMillis() {
	    return Math.sqrt(varianceTimeMillisSquared());
	}

	/**
	 * Returns the mean (average) run time in milliseconds.
	 *
	 * @return The mean (average) run time in milliseconds.
	 */
	public double meanTimeMillis() {
	    return ((double) totalTimeMillis())
		/ ((double) numRuns());
	}
    
	/**
	 * Returns a report in the form of string, indicating the number
	 * of warmups, runs, total time, mean time, and deviation.
	 *
	 * @return Report of the run in the form of a string.
	 */
	public String toString() {
	    return "Warmups     " + numWarmups() + "\n"
		+ "Runs        " + numRuns() + "\n"
		+ "Total Time  " + totalTimeMillis() + "ms\n"
		+ "Mean Time   " + meanTimeMillis() + "ms\n"
		+ "Deviation   " + deviationTimeMillis() + "ms\n";
        
	}
    }
}
