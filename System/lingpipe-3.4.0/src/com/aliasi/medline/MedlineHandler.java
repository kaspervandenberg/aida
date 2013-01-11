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

package com.aliasi.medline;

import com.aliasi.corpus.Handler;

/**
 * The <code>MedlineHandler</code> interface specifies a single method
 * that applies to a MEDLINE citation.  The standard usage for a
 * handler is to handle citations generated by a {@link
 * MedlineParser}.  
 *
 * <P>Each year, the NLM publishes a baseline version of MEDLINE, with
 * a single entry per citation.  Parsing the baseline, each citation
 * will result in a single call to the method {@link
 * #handle(MedlineCitation)}.  
 *
 * <P>On an ongoing basis (5 times weekly), the NLM publishes updates
 * for MEDLINE.  These updates contain new citations, replacement
 * citations, and deletions.  New and replacement citations result in
 * a call to {@link #handle(MedlineCitation)} just as when parsing the
 * baseline.  It is up to the handler implementation to detect
 * replacements through duplicated PubMed identifiers in the citation.
 * The deletions in the MEDLINE updates result in calls to the method
 * {@link #delete(String)} which indicate the PubMed identifier of
 * the citation to delete.
 *
 * <P>For more information, see the LingPipe tutorial on parsing MEDLINE.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public interface MedlineHandler extends Handler {

    /**
     * Handle the specified citation.  When parsing the MEDLINE
     * baseline, this method will be called exactly once for each
     * baseline citation.  When parsing the MEDLINE updates, this
     * method may be called more than once for a given citation, with
     * later calls taking precedence over earlier calls.
     * 
     *
     * @param citation MEDLINE citation that is visited.
     */
    public void handle(MedlineCitation citation);

    /**
     * Delete the citation with the specified PubMed identifier.
     * These events will be called only when parsing the MEDLINE
     * updates; there are no deletions in the yearly baselines.
     * This method will <i>not</i> be called for citations that
     * are being replaced; {@link #handle(MedlineCitation)} will
     * be called a second time instead.
     *
     * @param pmid Identifier of citation to delete.
     */
    public void delete(String pmid);
}


