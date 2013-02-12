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

package org.vle.aid.lucene.tools;

/**
 * A math utility class with static methods.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe1.0
 */
public class Math {

    // forbid instances
    private Math() { }

    
    /**
     * An array of the Fibonacci sequence.  The array is defined
     * as follows:
     *
     * <blockquote><pre>
     * FIBONACCI_SEQUENCE[0] = 1
     * FIBONACCI_SEQUENCE[1] = 2
     * FIBONACCI_SEQUENCE[n+2] = FIBONACCI_SEQUENCE[n+1] + FIBONACCI_SEQUENCE[n]
     * </pre></blockquote>
     *
     * So <code>FIBONACCI_SEQUENCE[0]</code> represents the second
     * Fibonacci number in the traditional numbering.  The inital entries
     * are:
     *
     * <blockquote><code>
     * 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 
     * 2584, ...
     * </code></blockquote>
     *
     * The length of the array is 91, and the largest value is:
     *
     * <blockquote><code>
     * FIBONACCI_SEQUENCE[90] = 7540113804746346429
     * 
     * </code></blockquote>
     * 
     * <P>See the following references for more information on
     * the fascinating properties of Fibonacci numbers:
     *
     * <UL>
     * <LI> <a href="http://en.wikipedia.org/wiki/Fibonacci_number">Wikipedia: Fibonacci Number</a>
     * <LI> <a href="http://mathworld.wolfram.com/FibonacciNumber.html">Mathworld: Fibonacci Number</a>
     */
    public static final long[] FIBONACCI_SEQUENCE = new long[] {
	1l,
	2l,
	3l,
	5l,
	8l,
	13l,
	21l,
	34l,
	55l,
	89l,
	144l,
	233l,
	377l,
	610l,
	987l,
	1597l,
	2584l,
	4181l,
	6765l,
	10946l,
	17711l,
	28657l,
	46368l,
	75025l,
	121393l,
	196418l,
	317811l,
	514229l,
	832040l,
	1346269l,
	2178309l,
	3524578l,
	5702887l,
	9227465l,
	14930352l,
	24157817l,
	39088169l,
	63245986l,
	102334155l,
	165580141l,
	267914296l,
	433494437l,
	701408733l,
	1134903170l,
	1836311903l,
	2971215073l,
	4807526976l,
	7778742049l,
	12586269025l,
	20365011074l,
	32951280099l,
	53316291173l,
	86267571272l,
	139583862445l,
	225851433717l,
	365435296162l,
	591286729879l,
	956722026041l,
	1548008755920l,
	2504730781961l,
	4052739537881l,
	6557470319842l,
	10610209857723l,
	17167680177565l,
	27777890035288l,
	44945570212853l,
	72723460248141l,
	117669030460994l,
	190392490709135l,
	308061521170129l,
	498454011879264l,
	806515533049393l,
	1304969544928657l,
	2111485077978050l,
	3416454622906707l,
	5527939700884757l,
	8944394323791464l,
	14472334024676221l,
	23416728348467685l,
	37889062373143906l,
	61305790721611591l,
	99194853094755497l,
	160500643816367088l,
	259695496911122585l,
	420196140727489673l,
	679891637638612258l,
	1100087778366101931l,
	1779979416004714189l,
	2880067194370816120l,
	4660046610375530309l,
	7540113804746346429l
    };

    /**
     * Returns <code>true</code> if the specified number is prime.  A
     * prime is a positive number greater than <code>1</code> with no
     * divisors other than <code>1</code> and itself, thus
     * <code>{2,3,5,7,11,13,...}</code>.
     *
     * @param num Number to test for primality.
     * @return <code>true</code> if the specified number is prime.
     */
    public static boolean isPrime(int num) {
        if (num < 2) return false;
        for (int i = 2; i <= num/2; ++i)
            if (num % i == 0) return false;
        return true;
    }

    /**
     * Returns the smallest prime number that is strictly larger than
     * the specified integer.  See {@link #isPrime(int)} for the
     * definition of primality.
     *
     * @param num Base from which to look for the next prime.
     * @return Smallest prime number strictly larget than specified
     * number.
     */
    public static int nextPrime(int num) {
        if (num < 2) return 2;
        for (int i = num + 1; ; ++i)
            if (isPrime(i)) return i;
    }

    /**
     * Converts a natural logarithm to a base 2 logarithm.
     * That is, if the input is <code><i>x</i> = ln <i>z</i></code>, then
     * the return value is <code>log<sub>2</sub> <i>z</i></code>.
     * Recall that <code>log<sub>2</sub> <i>z</i> = ln <i>z</i> / ln 2.
     *
     * @param x Natural log of value.
     * @return Log base 2 of value.
     */
    public static double naturalLogToBase2Log(double x) {
        return x / LN_2;
    }

    /**
     * Returns the log base 2 of the specivied value.
     *
     * @param x Value whose log is taken.
     * @return Log of specified value.
     */
    public static double log2(double x) {
        return naturalLogToBase2Log(java.lang.Math.log(x));
    }


    /**
     * Returns the integer value of reading the specified byte as an
     * unsigned value.  The computation is carried out by subtracting
     * the minimum value, as defined by the constant {@link
     * Byte#MIN_VALUE}.
     *
     * @param b Byte to convert.
     * @return Unsigned value of specified byte.
     */
    public static int byteAsUnsigned(byte b) {
        return (b >= 0) ? (int)b : (256+(int)b);
    }

    /**
     * Returns the log (base 2) of the factorial of the specified long
     * integer.  The factorial of <code>n</code> is defined for
     * <code>n > 0</code> by:
     *
     * <blockquote><code>
     *  n!
     *  = <big><big>&Pi;</big></big><sub><sub>i < 0 <= n</sub></sub> i
     * </code></blockquote>
     *
     * Taking logs of both sides gives:
     *
     * <blockquote><code>
     *  log<sub><sub>2</sub></sub> n!
     *  = <big><big>&Sigma;</big></big><sub><sub>i < 0 <= n</sub></sub>
     *    log<sub><sub>2</sub></sub> i
     * </code></blockquote>
     *
     * By convention, 0! is taken to be 1, and hence <code>ln 0! = 0</code>.
     *
     * @param n Specified long integer.
     * @return Log of factorial of specified integer.
     * @throws IllegalArgumentException If the argument is negative.
     */
    public static double log2Factorial(long n) {
        if (n < 0) {
            String msg = "Factorials only defined for non-negative arguments."
                + " Found argument=" + n;
            throw new IllegalArgumentException(msg);
        }
        double sum = 0.0;
        for (long i = 1; i <= n; ++i)
            sum += log2(i);
        return sum;
    }

    /**
     * Returns the sum of the specified array of double values.
     *
     * @param xs Array of values to sum.
     * @return The sum of the values.
     */
    public static double sum(double[] xs) {
        double sum = 0.0;
        for (int i = 0; i < xs.length; ++i)
            sum += xs[i];
        return sum;
    }

    /**
     * Returns the minimum of the specified array of double values.
     * If the length of the array is zero, the result is {@link
     * Double#NaN}.
     *
     * @param xs Array of values.
     * @return Minimum value in array.
     */
    public static double minimum(double[] xs) {
        if (xs.length == 0) return Double.NaN;
        double min = xs[0];
        for (int i = 1; i < xs.length; ++i)
            if (xs[i] < min) min = xs[i];
        return min;
    }

    /**
     * Returns the log (base 2) of the binomial coefficient of the
     * specified arguments.  The binomial coefficient is equal to the
     * number of ways to choose a subset of size <code>m</code> from a
     * set of <code>n</code> objects, which is pronounced "n choose
     * m", and is given by:
     *
     * <blockquote><code>
     *   choose(n,m) = n! / ( m! * (n-m)!)
     *   <br>
     *   log<sub>2</sub> choose(n,m)
     *    = log<sub>2</sub> n - log<sub>2</sub> m
     *      - log<sub>2</sub> (n-m)
     * </code></blockquote>
     *
     * @return The log (base 2) of the binomial coefficient of the
     * specified arguments.
     */
    public static double log2BinomialCoefficient(long n, long m) {
        return log2(n) - log2(m) - log2(n-m);
    }

    /**
     * Throws an illegal argument exception if the specified double
     * value is not a finite non-negative number.  The label is
     * included in the generated exception's error message if
     * necessary.
     *
     * @param label Label of variable for exception message.
     * @param x Double value to check.
     * @throws IllegalArgumentException If the specified double value
     * is infinite, is not a number, or is negative.
     */
    public static void assertFiniteNonNegative(String label, double x) {
        if (x < 0.0 ||
            Double.isNaN(x)
            || Double.isInfinite(x)) {
            String msg = label + " must be finite and non-negative."
                + " Found " + label + "=" + x;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * The natural logarithm of 2.
     */
    public static final double LN_2 = java.lang.Math.log(2.0);

    /**
     * The log base 2 of the constant <i>e</i>, which is the base of
     * the natural logarithm.  The constant <i>e</i> is determined by
     * the java constant {@link java.lang.Math#E}.
     */
    public static final double LOG2_E = log2(java.lang.Math.E);

}
