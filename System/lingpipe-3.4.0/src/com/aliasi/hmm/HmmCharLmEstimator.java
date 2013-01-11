package com.aliasi.hmm;

import com.aliasi.lm.NGramBoundaryLM;

import com.aliasi.symbol.MapSymbolTable;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Exceptions;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Tuple;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An <code>HmmCharLmEstimator</code> employs a maximum a posteriori
 * transition estimator and a bounded character language model
 * emission estimator.
 *
 * <h3>Emission Language Models</h3>
 *
 * <p>The emission language models are instances {@link
 * NGramBoundaryLM}.  As such, they explicitly model start-of-token
 * (prefix) and end-of-token (suffix) and basic token-shape features.
 * The language model parameters are the usual ones: n-gram length,
 * interpolation ratio (controls amount of smoothing), and number of
 * characters (controls final smoothing).
 *
 * <h3>Transition Estimates and Smoothing</h3>
 *
 * <p>The initial state and final state estimators are <a
 * href="http://en.wikipedia.org/wiki/Multinomial_distribution">multinomial
 * distributions</a>, as is the conditional estimator of the next
 * state given a previous state.  The default behavior is to use <a
 * href="http://en.wikipedia.org/wiki/Maximum_likelihood">maximum
 * likelihood</a> estimates with no smoothing for initial state, final
 * state, and transition likelihoods in the model.  That is, the
 * estimated likelihood of a state being an initial state is
 * proportional its training data frequency, with the actual
 * likelihood being the training data frequency divided by the total
 * training data frequency across tags.
 *
 * <p>With the constructor {@link
 * #HmmCharLmEstimator(int,int,double,boolean)}, a flag may be
 * specified to use smoothing for states.  The smoothing used is
 * add-one smoothing, also called Laplace smoothing.  For each state,
 * it adds one to the count for that state being an initial state and
 * for that state being a final state.  For each pair of states, it
 * adds one to the count of the transitions (including the self
 * transition, which is only counted once.)  This smoothing is
 * equivalent to putting an alpha=1 uniform <a
 * href="http://en.wikipedia.org/wiki/Dirichlet_distribution">Dirichlet
 * prior</a> on the initial state, final state, and conditional next
 * state estimators, with the resulting estimates being the maximum a
 * posteriori estimates.
 *
 * @author  Bob Carpenter
 * @version 2.4.1
 * @since   LingPipe2.1
 */
public class HmmCharLmEstimator extends AbstractHmmEstimator {

    private final MapSymbolTable mStateMapSymbolTable;

    private final ObjectToCounterMap mStateExtensionCounter
        = new ObjectToCounterMap();

    private final ObjectToCounterMap mStatePairCounter
        = new ObjectToCounterMap();

    private final HashMap mStateToLm = new HashMap();
    private final double mCharLmInterpolation;
    private final int mCharLmMaxNGram;
    private final int mMaxCharacters;

    private int mNumStarts = 0;
    private final ObjectToCounterMap mStartCounter
        = new ObjectToCounterMap();

    private int mNumEnds = 0;
    private final ObjectToCounterMap mEndCounter
        = new ObjectToCounterMap();

    private final boolean mSmootheStates;
    final Set mStateSet = new HashSet();


    /**
     * Construct an HMM estimator with default parameter settings.
     * The defaults are <code>6</code> for the maximum character
     * n-gram and 6.0, {@link Character#MAX_VALUE}<code>-1</code> for
     * the maximum number of characters, <code>6.0</code> for the
     * character n-gram interpolation factor, and no state likelihood
     * smoothing.
     */
    public HmmCharLmEstimator() {
        this(6, Character.MAX_VALUE-1, 6.0);
    }

    /**
     * Construct an HMM estimator with the specified maximum character
     * n-gram size, maximum number of characters in the data, and
     * character n-gram interpolation parameter, with no state
     * smoothing.  For more information on these parameters, see
     * {@link NGramBoundaryLM#NGramBoundaryLM(int,int,double,char)}.
     *
     * @param charLmMaxNGram Maximum n-gram for emission character
     * language models.
     * @param maxCharacters Maximum number of unique characters in
     * the training and test data.
     * @param charLmInterpolation Interpolation parameter for character
     * language models.
     * @throws IllegalArgumentException If the max n-gram is less
     * than one, the max characters is less than 1 or greater than
     * {@link Character#MAX_VALUE}<code>-1</code>, or if the interpolation
     * parameter is negative or greater than 1.0.
     */
    public HmmCharLmEstimator(int charLmMaxNGram,
                              int maxCharacters,
                              double charLmInterpolation) {
        this(charLmMaxNGram,maxCharacters,charLmInterpolation,false);

    }

    /**
     * Construct an HMM estimator with the specified maximum character
     * n-gram size, maximum number of characters in the data,
     * character n-gram interpolation parameter, and state
     * smoothing.  For more information on these parameters, see
     * {@link NGramBoundaryLM#NGramBoundaryLM(int,int,double,char)}.
     * For information on state smoothing, see the class documentation
     * above.
     *
     * @param charLmMaxNGram Maximum n-gram for emission character
     * language models.
     * @param maxCharacters Maximum number of unique characters in
     * the training and test data.
     * @param charLmInterpolation Interpolation parameter for character
     * language models.
     * @param smootheStates Flag indicating if add one smoothing is
     * carried out for HMM states.
     * @throws IllegalArgumentException If the max n-gram is less
     * than one, the max characters is less than 1 or greater than
     * {@link Character#MAX_VALUE}<code>-1</code>, or if the interpolation
     * parameter is negative or greater than 1.0.
     */
    public HmmCharLmEstimator(int charLmMaxNGram,
                              int maxCharacters,
                              double charLmInterpolation,
                              boolean smootheStates) {
        super(new MapSymbolTable());
        mSmootheStates = smootheStates;
        if (charLmMaxNGram < 1) {
            String msg = "Max n-gram must be greater than 0."
                + " Found charLmMaxNGram=" + charLmMaxNGram;
            throw new IllegalArgumentException(msg);
        }
        if (maxCharacters < 1 || maxCharacters > (Character.MAX_VALUE-1)) {
            String msg = "Require between 1 and "
                + (Character.MAX_VALUE-1) + " max characters."
                + " Found maxCharacters=" + maxCharacters;
            throw new IllegalArgumentException(msg);
        }
        if (charLmInterpolation < 0.0) {
            String msg = "Char interpolation param must be between "
                + " 0.0 and 1.0 inclusive."
                + " Found charLmInterpolation=" + charLmInterpolation;
            throw new IllegalArgumentException(msg);
        }
        mStateMapSymbolTable = (MapSymbolTable) stateSymbolTable();
        mCharLmInterpolation = charLmInterpolation;
        mCharLmMaxNGram = charLmMaxNGram;
        mMaxCharacters = maxCharacters;
    }

    void addStateSmoothe(String state) {
        if (!mStateSet.add(state)) return;
        mStateMapSymbolTable.getOrAddSymbol(state);
        if (!mSmootheStates) return;
        trainStart(state);
        trainEnd(state);
        Iterator it = mStateSet.iterator();
        while (it.hasNext()) {
            String state2 = it.next().toString();
            trainTransit(state,state2);
            if (state.equals(state2)) continue;
            trainTransit(state2,state);
        }

    }

    public void trainStart(String state) {
        // System.out.println("trainStart(" + state + ")");
        addStateSmoothe(state);
        ++mNumStarts;
        mStartCounter.increment(state);
    }

    /**
     * Convenience method equivalent in behavior to
     * calling {@link #trainStart(String)} the specified
     * number of times.
     *
     * @param state State to train as starting state.
     * @param count Count of training events.
     * @throws IllegalArgumentException If the count is negative.
     */
    public void trainStart(String state, int count) {
	verifyNonNegativeCount(count);
	if (count == 0) return;
	addStateSmoothe(state);
	
	
    }

    static void verifyNonNegativeCount(int count) {
	if (count >= 0) return;
	String msg = "Counts must be positve."
	    + " Found count=" + count;
	throw new IllegalArgumentException(msg);
    }


    public void trainEnd(String state) {
        // System.out.println("trainEnd(" + state + ")");
        addStateSmoothe(state);
        mStateExtensionCounter.increment(state);
        ++mNumEnds;
        mEndCounter.increment(state);
    }

    public void trainEmit(String state, CharSequence emission) {
        // System.out.println("trainEmit(" + state + ", " + emission + ")");
        addStateSmoothe(state);
        emissionLm(state).train(emission);
    }

    public void trainTransit(String sourceState, String targetState) {
        // System.out.println("trainTransit(" + sourceState
        // + ", " + targetState + ")");
        addStateSmoothe(sourceState);
        addStateSmoothe(targetState);
        mStateExtensionCounter.increment(sourceState);
        mStatePairCounter.increment(Tuple.create(sourceState,targetState));
    }


    public double startProb(String state) {
        double count = mStartCounter.getCount(state);
        double total = mNumStarts;
        return count / total;
    }

    public double endProb(String state) {
        double count = mEndCounter.getCount(state);
        double total = mNumEnds;
        return count / total;
    }

    /**
     * Returns the transition estimate from the specified source state
     * to the specified target state.  For this estimator, this is
     * just the maximum likelihood estimate.  If all transitions
     * should be allowed, then each pair of states should be presented
     * in both orders to {@link #trainTransit(String,String)}, in
     * order to produce add-one smoothing.  Typically, maximum
     * likelihood estimates of state transitions are fine for HMMs
     * trained with large sets of supervised data.
     *
     * @param source Originating state for the transition.
     * @param target Resulting state after the transition.
     * @return Maximum likelihood estimate of transition probability
     * given training data.
     */
    public double transitProb(String source, String target) {
        double extCount = mStateExtensionCounter.getCount(source);
        double pairCount
            = mStatePairCounter.getCount(Tuple.create(source,target));
        return pairCount / extCount;
    }

    /**
     * Returns the estimate of the probability of the specified string
     * being emitted from the specified state.  For a character
     * language-model based HMM, this is just the language model
     * estimate of the string likelihood of the emission for the
     * particular state.
     *
     * @param state State of HMM.
     * @param emission String emitted by state.
     * @return Estimate of probability of state emitting string.
     */
    public double emitProb(String state, CharSequence emission) {
        return Math.pow(2.0,emitLog2Prob(state,emission));
    }

    public double emitLog2Prob(String state, CharSequence emission) {
        return emissionLm(state).log2Estimate(emission);
    }

    /**
     * Returns the language model used for emission probabilities for
     * the specified state.  By grabbing the models directly in
     * this way, they may be pruned, etc., before being compiled
     *
     * @param state State of the HMM.
     * @return The language model for the specified state.
     */
    public NGramBoundaryLM emissionLm(String state) {
        NGramBoundaryLM lm = (NGramBoundaryLM) mStateToLm.get(state);
        if (lm == null) {
            lm = new NGramBoundaryLM(mCharLmMaxNGram,
                                     mMaxCharacters,
                                     mCharLmInterpolation,
                                     '\uFFFF');
            mStateToLm.put(state,lm);
        }
        return lm;
    }

    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 8463739963673120677L;
        final HmmCharLmEstimator mEstimator;
        public Externalizer() {
            this(null);
        }
        public Externalizer(HmmCharLmEstimator handler) {
            mEstimator = handler;
        }
        public Object read(ObjectInput in) throws IOException {
            try {
                return new CompiledHmmCharLm(in);
            } catch (ClassNotFoundException e) {
                throw Exceptions.toIO("HmmCharLmEstimator.compileTo()",e);
            }
        }
        public void writeExternal(ObjectOutput objOut) throws IOException {
            // state sym table
            mEstimator.mStateMapSymbolTable.writeTo(objOut);

            int numStates = mEstimator.mStateMapSymbolTable.numSymbols();

            // float matrix: #state x #state
            for (int i = 0; i < numStates; ++i)
                for (int j = 0; j < numStates; ++j)
                    objOut.writeDouble((float) mEstimator.transitProb(i,j));

            // LM^(#state)
            for (int i = 0; i < numStates; ++i) {
                String state = mEstimator.mStateMapSymbolTable.idToSymbol(i);
                mEstimator.emissionLm(state).compileTo(objOut);
            }

            // start prob vector
            for (int i = 0; i < numStates; ++i)
                objOut.writeDouble(mEstimator.startProb(i));

            // end prob vector
            for (int i = 0; i < numStates; ++i)
                objOut.writeDouble(mEstimator.endProb(i));
        }
    }

}
