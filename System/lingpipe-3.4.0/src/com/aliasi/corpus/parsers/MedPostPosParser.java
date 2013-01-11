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

/**
 * The <code>MedPostPosParser</code> class provides a parser for
 * MedPost part-of-speech corpus.  MedPost was created at the United
 * States National Center for Biotechnology Information (NCBI) and
 * is a part of their MedTag distribution, which also includes a 
 * gene-chunked corpus.
 *
 * <P>NCBI distributes the MedPost corpus freely for public use as a
 * &quot;United States Government Work&quot; (see included
 * <code>README</code> file for full licensing information):
 *
 * <UL>
 * <LI> <a href="ftp://ftp.ncbi.nlm.nih.gov/pub/lsmith/MedTag/medtag.tar.gz"
 *      >MedTag Corpus FTP</a> (tar.gz 14.7MB)
 * </UL>
 *
 * The labeled part-of-speech files match the pattern
 * <code>/medtag/medpost/*.ioc</code> relative to the directory from
 * which the distribution was unpacked.
 *
 * <P>The beginning of the first training file,
 * <code>medtag/medpost/tag_mb01.ioc</code>, is:
 * 
 * <blockquote>
 * <table border='1' cellpadding='3'><tr><td>
 * <pre>
 * P07504457A03
 * A_DD MAP_NN kinase_NN activator_NN recently_RR purified_VVN and_CC cloned_VVN has_VHZ been_VBN shown_VVN to_TO be_VBI a_DD protein_NN kinase_NN (_( MAP_NN kinase_NN kinase_NN )_) that_PNR is_VBZ able_JJ to_TO induce_VVI the_DD dual_JJ phosphorylation_NN of_II MAP_NN kinase_NN on_II both_CC the_DD regulatory_JJ tyrosine_NN and_CC threonine_NN sites_NNS in_RR+ vitro_RR ._.
 * P07535768A04
 * Here_RR we_PN report_VVB the_DD cloning_VVGN and_CC characterization_NN of_II a_DD novel_JJ dual_JJ specific_JJ phosphatase_NN ,_, HVH2_NN ,_, which_PNR may_VM function_VVB in_RR+ vivo_RR as_II a_DD MAP_NN kinase_NN phosphatase_NN ._.
* </pre>
* </td></tr></table>
 * </blockquote>
 *
 * Note that sentences are marked with identifiers on their own line
 * and followed by the text of the sentence with underscores
 * separating words from their tags.  
 *
 * <P>The primary citation for MedPost is available freely from BioMedCentral:
 *
 * <UL>
 * <LI>Larry Smith, Tom Rindflesch, and W. John Wilbur. 2004. 
 * <a href="http://bioinformatics.oxfordjournals.org/cgi/reprint/20/14/2320"
 * >MedPost:
 * a part-of-speech tagger for biomedical text</a>.
 * <i>Bioinformatics</i> <b>20</b>(14):2320-1.
 * </UL>
 * 
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public class MedPostPosParser extends AbstractMedTagParser {

    /**
     * Construct a MedPost corpus part-of-speech tag parser with
     * no handler specified.  
     */
    public MedPostPosParser() {
    super();
    }

    /**
     * Construct a MedPost corpus part-of-speech tag parser with
     * the specified tag handler.
     *
     * @param handler Tag handler.
     */
    public MedPostPosParser(TagHandler handler) {
    super(handler);
    }

    /**
     * Passes the specified tokens, whitespaces and tags to the
     * contained handler.  This implementation simply passes the
     * tokens, tags and a null whitespace array to the contained
     * handler.
     *
     * @param tokens Tokens to handle.
     * @param whitespaces Whitespaces to handle (ignored).
     * @param tags Tags to handle.
     */
    protected void parseTokensTags(String[] tokens, String[] whitespaces, 
                   String[] tags) {
    tagHandler().handle(tokens,null,tags);
    }

}
