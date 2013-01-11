package com.aliasi.test.unit.corpus.parsers;

import com.aliasi.corpus.ChunkTagHandlerAdapter;

import com.aliasi.corpus.parsers.GeneTagParser;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.test.unit.corpus.CollectingTagHandler;

import java.io.IOException;

public class GeneTagParserTest extends BaseTestCase {

    static final String CORPUS 
	= "P00073344A0367"
	+ "\n"
	+ "In_TAG 2_TAG subjects_TAG the_TAG phytomitogen_TAG reactivity_TAG of_TAG the_TAG lymphocytes_TAG was_TAG improved_TAG after_TAG treatment_TAG ._TAG"
	+ "\n"
	+ "P00083846T0000"
	+ "\n"
	+ "Albumin_GENE2 and_TAG cyclic_TAG AMP_TAG levels_TAG in_TAG peritoneal_TAG fluids_TAG in_TAG the_TAG child_TAG"
	+ "\n"
	+ "P00088391A0181"
	+ "\n"
	+ "On_TAG the_TAG other_TAG hand_TAG factor_GENE1 IX_GENE1 activity_TAG is_TAG decreased_TAG in_TAG coumarin_TAG treatment_TAG with_TAG factor_GENE2 IX_GENE2 antigen_TAG remaining_TAG normal_TAG ._TAG"
	+ "\n"
	;

    
    static final String CORPUS2 
	= "P00001606T0076"
	+ "\n" 
	+ "A_GENE1 B_GENE1 C_GENE1 D_GENE2 E_TAG F_GENE1 G_GENE2 ._TAG"
	+ "\n"
	+ "P00001406T0076"
	+ "\n" 
	+ "A_GENE1 B_TAG C_GENE2"
	+ "\n"
	+ "P00001406T0076"
	+ "\n" 
	+ "A_TAG B_GENE1 C_GENE1"
	+ "\n"
	+ "P00001406T0076"
	+ "\n" 
	+ "A_TAG B_GENE1 C_GENE1 D_GENE1"
	+ "\n"
	;
    

    static final String[] TAGS2_0
	= new String[] { GeneTagParser.B_GENE_TAG, GeneTagParser.I_GENE_TAG, GeneTagParser.I_GENE_TAG,
			 GeneTagParser.B_GENE_TAG, ChunkTagHandlerAdapter.OUT_TAG, GeneTagParser.B_GENE_TAG, GeneTagParser.B_GENE_TAG, ChunkTagHandlerAdapter.OUT_TAG };

    static final String[] TAGS2_1
	= new String[] { GeneTagParser.B_GENE_TAG, ChunkTagHandlerAdapter.OUT_TAG, GeneTagParser.B_GENE_TAG };

    static final String[] TAGS2_2
	= new String[] { ChunkTagHandlerAdapter.OUT_TAG, GeneTagParser.B_GENE_TAG, GeneTagParser.I_GENE_TAG };

    static final String[] TAGS2_3
	= new String[] { ChunkTagHandlerAdapter.OUT_TAG, GeneTagParser.B_GENE_TAG, GeneTagParser.I_GENE_TAG, GeneTagParser.I_GENE_TAG };


    static final String[] TOKS0 
	= new String[] {
        "In", "2", "subjects", "the", "phytomitogen", "reactivity", "of",
        "the", "lymphocytes", "was", "improved", "after", "treatment", "." 
    };

    static final String[] TOKS1
	= new String[] {
        "Albumin", "and", "cyclic", "AMP", "levels", "in", 
        "peritoneal", "fluids", "in", "the", "child" 
    };

    static final String[] TOKS2
	= new String[] {
        "On", "the", "other", "hand", "factor", "IX", "activity", "is", 
        "decreased", "in", "coumarin", "treatment", "with", "factor", 
        "IX", "antigen", "remaining", "normal", "."
    };
    
    static final String[] TAGS0
	= new String[] {
        ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, 
        ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, 
        ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, 
        ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG
    };

    static final String[] TAGS1
	= new String[] {
        GeneTagParser.B_GENE_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG
    };

    static final String[] TAGS2
	= new String[] {
        ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, GeneTagParser.B_GENE_TAG, GeneTagParser.I_GENE_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG,
        ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, GeneTagParser.B_GENE_TAG, GeneTagParser.I_GENE_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG, ChunkTagHandlerAdapter.OUT_TAG
    };

    public void testStatics() {
	assertEquals(TAGS0.length, TOKS0.length);
	assertEquals(TAGS1.length, TOKS1.length);
	assertEquals(TAGS2.length, TOKS2.length);
    }

    public void testOne() throws IOException {
	CollectingTagHandler handler = new CollectingTagHandler();
	GeneTagParser parser = new GeneTagParser(handler);
	parser.parseString(CORPUS);
	handler.assertTokens(new String[][] { TOKS0, TOKS1, TOKS2 });
	handler.assertTags(new String[][] { TAGS0, TAGS1, TAGS2 });

    }

    public void testTwo() throws IOException {
	CollectingTagHandler handler = new CollectingTagHandler();
	GeneTagParser parser = new GeneTagParser(handler);
	parser.parseString(CORPUS2);
	handler.assertTags(new String[][] { TAGS2_0, TAGS2_1, 
					    TAGS2_2, TAGS2_3 });

    }


}
