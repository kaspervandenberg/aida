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

package com.aliasi.corpus.parsers;

import com.aliasi.corpus.TagHandler;
import com.aliasi.corpus.ChunkTagHandlerAdapter;

/**
 * The <code>GeneTagParser</code> class provides a tag parser for the
 * GeneTag named-entity corpus.  GeneTag was created at the United States
 * national Center for Biotechnology Information (NCBI) and is a part
 * of their MedTag distribution, which also includes a part-of-speech
 * corpus (see {@link MedPostPosParser}).
 *
 * <P>NCBI distributes the GeneTag corpus freely for public use as a
 * &quot;United States Government Work&quot; (see included
 * <code>README</code> file for more information):
 *
 * <UL>
 * <LI> <a href="ftp://ftp.ncbi.nlm.nih.gov/pub/lsmith/MedTag/medtag.tar.gz"
 *      >MedTag Corpus FTP</a> (tar.gz 14.7MB)
 * </UL>
 *
 * The GeneTag corpus is in the single file
 * <code>/medtag/genetag/genetag.tag</code> relative to the directory
 * into which the distribution is unpacked.
 * 
 * <P>An excerpt of two training sentences in the file is:
 *
 * <blockquote><table border='1'><tr><td><pre>
 * P00073344A0367
 * In_TAG 2_TAG subjects_TAG the_TAG phytomitogen_TAG reactivity_TAG of_TAG the_TAG lymphocytes_TAG was_TAG improved_TAG after_TAG treatment_TAG ._TAG
 * P00083846T0000
 * Albumin_GENE2 and_TAG cyclic_TAG AMP_TAG levels_TAG in_TAG peritoneal_TAG fluids_TAG in_TAG the_TAG child_TAG
 * P00088391A0181
 * On_TAG the_TAG other_TAG hand_TAG factor_GENE1 IX_GENE1 activity_TAG is_TAG decreased_TAG in_TAG coumarin_TAG treatment_TAG with_TAG factor_GENE2 IX_GENE2 antigen_TAG remaining_TAG normal_TAG ._TAG
 * </pre></td></tr></table></blockquote>
 * 
 * GeneTag marks up individual sentences with a combination of
 * <code>GENE1</code>, <code>GENE2</code> and <code>TAG</code> tags.
 * A chunk is a contiguous sequence of <code>GENE1</code> or
 * <code>GENE2</code> tags.  The indices <code>1</code> and
 * <code>2</code> are not to differentiate types, but to allow two
 * genes in a row.  In fact, the corpus is annotateed such that the
 * gene references alternate even across sentences.
 *
 * <P>The output tagging is in the standard LingPipe BIO format.
 *
 * <P>The primary reference for GeneTag is:
 *
 * <UL>
 * <LI> Lorraine Tanabe, Natalie Xie, Lynne H. Thom, Wayne Matten and W.
 * John Wilbur. 2005. <a
 * href="http://www.biomedcentral.com/1471-2105/6/S1/S3">GENETAG: a
 * tagged corpus for gene/protein named entity recognition</a>.
 * <i>BMC Bioinformatics</i> 2005, <b>6</b>(Suppl 1):S3.
 * </UL>
 * 
 * @author  Bob Carpenter
 * @version 2.1.2
 * @since   LingPipe2.1
 */
public class GeneTagParser extends AbstractMedTagParser {

    /**
     * Construct a GeneTag corpus parser with no handler specified.
     */
    public GeneTagParser() {
    super();
    }

    /**
     * Construct a GeneTag corpus parser with the specified handler.
     *
     * @param handler Tag handler for taggings.
     */
    public GeneTagParser(TagHandler handler) {
    super(handler);
    }

    /**
     * Implementation of the tag normalizer for the GeneTag corpus.
     * This method converts the tags in the corpus-specific format
     * into LingPipe's BIO format: <code>B-GENE</code>,
     * <code>I-GENE</code> and <code>O</code>).
     */
    protected void parseTokensTags(String[] tokens, String[] whitespaces, 
                   String[] tags) {
    String[] normalTags = normalize(tags);
    tagHandler().handle(tokens,null,normalTags);
    }
    
    /**
     * The type of gene chunks, namely <code>&quot;GENE&quot;</code>.
     */
    public static String GENE_TYPE = "GENE";

    /**
     * The tag used to start gene spans,
     * <code>&quot;B-GENE&quot;</code>.
     */
    public static final String B_GENE_TAG 
    = ChunkTagHandlerAdapter.toBeginTag(GENE_TYPE);

    /**
     * The tag used to continue gene spans,
     * <code>&quot;I-GENE&quot;</code>.
     */
    public static final String I_GENE_TAG 
    = ChunkTagHandlerAdapter.toInTag(GENE_TYPE);

    static String[] normalize(String[] tags) {
    String[] result = new String[tags.length];
    for (int i = 0; i < tags.length; ) {
        if (tags[i].startsWith("GENE")) {
        String tag = tags[i];
        result[i] = B_GENE_TAG;
        ++i;
        while (i < tags.length && tags[i].equals(tag))
            result[i++] = I_GENE_TAG;
        } else { // (tags[i].equals("TAG")) {
        result[i++] = ChunkTagHandlerAdapter.OUT_TAG;
        }
    }
    return result;
    }

}
