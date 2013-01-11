package com.aliasi.test.unit.spell;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.test.unit.BaseTestCase;

public class TrainSpellCheckerTest extends BaseTestCase {

    public void testEx() {
        NGramProcessLM lm = new NGramProcessLM(5);
        WeightedEditDistance distance = new FixedWeightEditDistance(1,1,1,1,1);
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,distance,IndoEuropeanTokenizerFactory.FACTORY);
        try {
            trainer.train("tra la la",-1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

    }

    public void testSerialize() throws Exception {
        NGramProcessLM lm = new NGramProcessLM(5);
        WeightedEditDistance distance = new FixedWeightEditDistance(1,1,1,1,1);
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,distance,IndoEuropeanTokenizerFactory.FACTORY);
        trainer.train("tra la la");
        trainer.train("fa do");
        trainer.train("do do");
        TrainSpellChecker trainer2
            = (TrainSpellChecker) AbstractExternalizable.serializeDeserialize(trainer);

        assertEquals(trainer.numTrainingChars(), trainer2.numTrainingChars());
        assertEquals(trainer.tokenCounter().keySet(), trainer2.tokenCounter().keySet());
        for (String key : trainer.tokenCounter().keySet())
            assertEquals(trainer.tokenCounter().getCount(key),
                         trainer2.tokenCounter().getCount(key));

        WeightedEditDistance distance2 = trainer2.editDistance();
        assertEquals(distance.deleteWeight('a'), distance2.deleteWeight('a'));
        assertEquals(distance.transposeWeight('e','1'),
                     distance2.transposeWeight('e','1'));
        assertEquals(distance.substituteWeight('F','&'),
                     distance2.substituteWeight('F','&'));
        assertEquals(distance.matchWeight('-'),
                     distance2.matchWeight('-'));

        NGramProcessLM lm2 = trainer2.languageModel();
        assertEquals(lm.log2Estimate("foo bar"),
                     lm2.log2Estimate("foo bar"),
                     0.0001);
    }

}