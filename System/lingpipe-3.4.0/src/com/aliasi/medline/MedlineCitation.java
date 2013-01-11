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

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>MedlineCitation</code> represents the content of a single
 * record in the 2008 MEDLINE database for the citation of an individual
 * article.
 *
 * <P>A citation contains a richly structured set of information.
 * Each citation contains an {@link Article}, which contains most of
 * the information drawn from the article itself.  The other
 * information has been added by the creators of MEDLINE and comes in
 * two flavors.  The first type is information about the MEDLINE
 * record itself, such as who &quot;owns&quot; it, when it was
 * created, completed and revised, etc.  The second type of
 * information is metadata about the article itself, such as names of
 * chemicals and database links, keywords, lists of MeSH terms, gene
 * names and database links, cross-references to comments or
 * corrections in other articles, etc.
 *
 * <P>In addition to the structured information in a citation the raw
 * XML is available as a string through the {@link #xmlString()}
 * method.
 *
 * @author  Bob Carpenter
 * @version 3.3
 * @since   LingPipe2.0
 */
public class MedlineCitation {

    private final String mNlmDcmsID;
    private final String mPMID;
    private final String mOwner;
    private final String mStatus;
    private final Date mDateCreated;
    private final Date mDateCompleted;
    private final Date mDateRevised;
    private final Chemical[] mChemicals;
    private final String[] mCitationSubsets;
    private final MeshHeading[] mMeshHeadings;
    private final String mNumberOfReferences;
    private final PersonalNameSubject[] mPersonalNameSubjects;
    private final KeywordList[] mKeywordLists;
    private final GeneralNote[] mGeneralNotes;
    private final String[] mGeneSymbols;
    private final String[] mSpaceFlightMissions;
    private final OtherID[] mOtherIDs;
    private final Investigator[] mInvestigators;
    private final OtherAbstract[] mOtherAbstracts;
    private final CommentOrCorrection[] mCommentOrCorrections;
    private final JournalInfo mJournalInfo;
    private final Article mArticle;
    private String mXMLString;

    MedlineCitation(String owner,
                    String status,
                    String nlmDcmsID,
                    String pmid,
                    Date dateCreated,
                    Date dateCompleted,
                    Date dateRevised,
                    Chemical[] chemicals,
                    String[] citationSubsets,
                    MeshHeading[] meshHeadings,
                    String numberOfReferences,
                    PersonalNameSubject[] personalNameSubjects,
                    KeywordList[] keywordLists,
                    GeneralNote[] generalNotes,
                    String[] geneSymbols,
                    String[] spaceFlightMissions,
                    OtherID[] otherIDs,
                    Investigator[] investigators,
                    OtherAbstract[] otherAbstracts,
                    CommentOrCorrection[] commentOrCorrections,
                    JournalInfo journalInfo,
                    Article article) {
        mOwner = owner;
        mStatus = status;
        mNlmDcmsID = nlmDcmsID;
        mPMID = pmid;
        mDateCreated = dateCreated;
        mDateCompleted = dateCompleted;
        mDateRevised = dateRevised;
        mChemicals = chemicals;
        mCitationSubsets = citationSubsets;
        mMeshHeadings = meshHeadings;
        mNumberOfReferences = numberOfReferences;
        mPersonalNameSubjects = personalNameSubjects;
        mKeywordLists = keywordLists;
        mGeneralNotes = generalNotes;
        mGeneSymbols = geneSymbols;
        mSpaceFlightMissions = spaceFlightMissions;
        mOtherIDs = otherIDs;
        mInvestigators = investigators;
        mOtherAbstracts = otherAbstracts;
        mCommentOrCorrections = commentOrCorrections;
        mJournalInfo = journalInfo;
        mArticle = article;
    }

    /**
     * Returns the XML underlying this citation as a string.  Note
     * that there will be no XML declaration in this string, nor
     * will there be a DTD reference.  All entities and defaults
     * provided by the DTD will be expanded into the string.
     *
     * @return The XML underlying this citation as a string.
     */
    public String xmlString() {
        return mXMLString;
    }

    /**
     * The owner is the group that created and validated this
     * citation.  Each citation has a unique owner, which takes on one
     * of the following possible values, shown with their
     * corresponding constants in {@link MedlineCitationSet}.
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><i>Value</i></td>
     *     <td><i>Constant in</i> <code>MedlineCitationSet</code></td>
     *     <td><i>Description</i></td><td><i>Notes</i></tr>
     * <tr><td>NLM</td><td>NLM_VALUE</td>
     *     <td>(U.S.) National Library of Medicine, Index Section</td>
     *     <td>Value for majority of citations</td></tr>
     * <tr><td>NASA</td><td>NASA_VALUE</td>
     *     <td>(U.S.) National Aeronautics and Space Administration</td>
     *     <td>&nbsp;</td></tr>
     * <tr><td>PIP</td><td>PIP_VALUE</td>
     *     <td>Population Information Program, Johns Hopkins</td>
     *     <td>Not current; only on older citations</td></tr>
     * <tr><td>KIE</td><td>KIE_VALUE</td>
     *     <td>Kennedy Institute of Ethics, Georgetown University</td>
     *     <td>&nbsp;</td></tr>
     * <tr><td>HSR</td><td>HSR_VALUE</td>
     *     <td>(U.S.) National Infomration Center on Health Services Reserach
     *         and Health Care Technology, National Library of Medicine</td>
     *     <td>&nbsp;</td></tr>
     * <tr><td>HMD</td><td>HMD_VALUE</td>
     *     <td>History of Medicine Division, National Library of Medicine</td>
     *     <td>&nbsp;</td></tr>
     * <tr><td>SIS</td><td>SIS_VALUE</td>
     *     <td>Specialized Information Services Division,
     *         National Library of Medicine</td>
     *     <td>Not yet used; reserved for future use.</td></tr>
     * <tr><td>NOTNLM</td><td>NOT_NLM_VALUE</td>
     *     <td>Not from NLM</td>
     *     <td>Will never be used for any distributed MEDLINE record</td></tr>
     * </table>
     * </blockquote>
     *
     * <P>These values may also appear as owners of general notes and
     * keyword lists.
     *
     * @return The owner of the group that created and validated this
     * citation.
     */
    public String owner() {
        return mOwner;
    }

    /**
     * Returns the status of this citation. The status is always
     * defined, and takes one of the values listed below.  As articles
     * progress through the pipeline, they are rereleased with new
     * status values; the later versions should replace the earlier
     * ones based on PubMed identifier.
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><i>Value</i></td>
     *     <td><i>Constant in</i> <code>MedlineCitationSet</code></td>
     *     <td><i>Notes</i></td></tr>
     * <tr><td>In-Data-Review</td><td>IN_DATA_REVIEW_VALUE</td>
     *     <td>Reference citation elements at the journal issue level have
     *         been checked against print or online versions.  Citations
     *         with this status lack completion dates.  They have not
     *         undergrone quality assurance nor had MeSH terms added.</td></tr>
     * <tr><td>In-Process</td><td>IN_PROCESS_VALUE</td>
     *     <td>Citation elements at the article level have been
     *         reviewed, including author names, title and pagination.
     *         Not every in-process article is promoted to MEDLINE; some
     *         are classified out of scope and receive PubMed-not-Medline
     *         as their final status</td></tr>
     * <tr><td>Completed</td><td>COMPLETED_VALUE</td>
     *     <td>These are 'true' MEDLINE records with
     *         date completed and typically with MeSH headings.
     *         This status remains under revisions.</td></tr>
     * <tr><td>PubMed-not-MEDLINE</td><td>PUBMED_NOT_MEDLINE_VALUE
     *     <td>These are articles have gone through data review,
     *         but were not chosen for inclusion.  They fall into
     *         the following categories:
     *         (1) citations that precede the data a journal was
     *         selected for MEDLINE, (2) citations that are out of scope,
     *         and (3) analytical summaries of articles published
     *         elsewhere.</td></tr>
     * <tr><td>Publisher</td><td>PUBLISHER_VALUE</td>
     *     <td>These are not distributed to licensees, and include
     *         a number of categories of yet-to-be processed, out of scope,
     *         and out of date articles.</td></tr>
     * <tr><td>MEDLINE</td><td>MEDLINE_VALUE</td>
     *     <td>2005 addition not yet documented by the NLM.</td></tr>
     * <tr><td>OLDMEDLINE</td><td>MEDLINE_VALUE</td>
     *     <td>2005 addition not yet documented by the NLM.</td></tr>
     * </table>
     * </blockquote>
     *
     * @return The status of this citation.
     */
    public String status() {
        return mStatus;
    }

    /**
     * Returns the NLM DCMS identifier for this document.
     * May return <code>null</code> if there is no identifier.
     *
     * <P>The PubMed identifier, available through the method {@link
     * #pmid()}, is the official identifier for MEDLINE documents.
     *
     * <P>Note that this identifier was introduced in 2005 and
     * was previously known as the MEDLINE ID.
     *
     * @return The NLM DCMS identifier for this document.
     */
    public String nlmDcmsID() {
        return mNlmDcmsID;
    }

    /**
     * Returns the PubMed identifier for this document.  This is now
     * the official identifier for MEDLINE documents.  Each document
     * has a unique PubMed identifier, which will be a one to eight
     * digit accession number without leading zeros.  The identifiers
     * of deleted records will not be reused.
     *
     * @return The PubMed identifier for this document.
     */
    public String pmid() {
        return mPMID;
    }

    /**
     * Returns the date on which this citation was created.  Note that
     * this is <i>not</i> the date on which the article was created,
     * but is the date on which the MEDLINE citation for the article
     * was created.  The value will be non-null because every citation
     * is required to have a creation date.  The date will only be
     * resolved to the level of day, month and year.
     *
     * <P>Note that this is also not the same as the PubMed Entrez
     * date, which is not distributed as part of MEDLINE.
     *
     * @return The date on which this citation was created.
     */
    public Date dateCreated() {
        return mDateCreated;
    }

    /**
     * Returns the date that the processing of this citation ended or
     * <code>null</code> if it is still in process.  Specifically,
     * citations are completed when all extra information has been
     * added and quality assurance has been completed.  If a record's
     * status is in-process, as indicated by method {@link #status()},
     * it lacks a date completed, so that this method will return
     * <code>null</code>.  The date will only be resolved to the level
     * of day, month and year.
     *
     * <P>For ciations before about the year 2000, the date created
     * and date completed were set to the same value.
     *
     * @return The date that this citation was completed.
     */
    public Date dateCompleted() {
        return mDateCompleted;
    }

    /**
     * Returns the latest date on which a change was made to this
     * citation as a result of maintenance.  Note that no information
     * is provided in MEDLINE as to the nature of the change, and only
     * the latest revision date is included.  The date will only be
     * resolved to the level of day, month and year.  The value may be
     * <code>null</code> if this citation has not been revised.
     *
     * <P>Note that every record that existed at the time was revised
     * on 18 December 2000, and many of these have been revised
     * subsequently.
     *
     * @return The latest date on which a change was made to this
     * citation.
     */
    public Date dateRevised() {
        return mDateCreated;
    }

    /**
     * Returns the article for this citation, containing the
     * information derived from the cited article itself.
     *
     * @return The article for this citation.
     */
    public Article article() {
        return mArticle;
    }

    /**
     * Returns information about the journal in which this
     * citation appears.
     *
     * @return Information about the journal in which this
     * citation appears.
     */
    public JournalInfo journalInfo() {
        return mJournalInfo;
    }

    /**
     * Returns the array of chemical substances mentioned in this
     * journal.  The array may be of length zero if no chemicals were
     * included.
     *
     * @return The substances mentioned in this journal.
     */
    public Chemical[] chemicals() {
        return mChemicals;
    }

    /**
     * Returns the array of citation subsets for this journal.  These
     * values indicate specialized subsets of the collection of journals.
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><code>AIM</code></td>
     *     <td>citations from Abridged Index Medicus journals,
     *         a list of about 120 core clinical, English language
     *         journals.</td></tr>
     * <tr><td><code>B</code></td>
     *     <td>citations from non-Index Medicus journals in the field of
     *         biotechnology (not currently used).</td></tr>
     * <tr><td><code>C</code></td>
     *     <td>citations from non-Index Medicus journals in the field of
     *         communication disorders (not currently used).</td></tr>
     * <tr><td><code>D</code></td>
     *     <td>citations from non-Index Medicus journals in the field of
     *         dentistry; these citations appeared in Index to Dental
     *         Literature.</td></tr>
     * <tr><td><code>E</code></td>
     *     <td>citations in the field of bioethics. (includes records
     *         from the former BIOETHICS database)</td></tr>
     * <tr><td><code>F</code></td>
     *     <td>older citations from one journal prior to its selection for
     *         Index Medicus; used to augment the database for NLM's
     *         International MEDLARS Centers (not currently used)</td></tr>
     * <tr><td><code>H</code></td>
     *     <td>citations from non-Index Medicus journals in the field of
     *         health administration. (includes records from the former
     *         HealthSTAR database)</td></tr>
     * <tr><td><code>IM</code></td>
     *     <td>citations from Index Medicus journals.</td></tr>
     * <tr><td><code>J</code></td>
     *     <td>citations from non-Index Medicus journals in the field of
     *         population information. (not currently used; on records from the
     *         former POPLINE database)</td></tr>
     * <tr><td><code>K</code></td>
     *     <td>citations from non-Index Medicus journals relating to
     *         consumer health.</td></tr>
     * <tr><td><code>N</code></td>
     *     <td>citations from non-Index Medicus journals in the field of
     *         nursing; these citations appeared in the International
     *         Nursing Index.</td></tr>
     * <tr><td><code>R</code></td>
     *     <td>citations from non-Index Medicus journals in the field of
     *         population and reproduction; these citations appeared in
     *         Population Sciences (not currently used).</td></tr>
     * <tr><td><code>Q</code></td>
     *     <td>citations in the field of the history of medicine. (includes
     *         records from the former HISTLINE database)</td></tr>
     * <tr><td><code>QO</code></td>
     *     <td>is subset of Q - indicates older history of medicine journal
     *         citations that were created before the former HISTLINE file
     *         was converted to a MEDLINE-like format. (For NLM use because
     *         they require special handling at NLM).</td></tr>
     * <tr><td><code>S</code></td>
     *     <td>citations in the field of space life sciences. (includes
     *         records from the former SPACELINE database)</td></tr>
     * <tr><td><code>T</code></td>
     *     <td>citations from non-Index Medicus journals in the field of
     *         health technology assessment. (includes records from the
     *         former HealthSTAR database)</td></tr>
     * <tr><td><code>X</code></td>
     *     <td>citations in the field of AIDS/HIV. (includes records from
     *         the former AIDSLINE database)</td></tr>
     * </table>
     * </blockquote>
     *
     * @return An array of zero or more citation subsets.
     */
    public String[] citationSubsets() {
        return mCitationSubsets;
    }

    /**
     * Returns an array of the comments or corrections for
     * this citation.
     *
     * @return The comments or corrections for this citaiton.
     */
    public CommentOrCorrection[] commentOrCorrections() {
        return mCommentOrCorrections;
    }

    /**
     * Return an array of the gene symbols for this citation.  This
     * item was only provided for records processed at NLM between
     * 1991 and 1995.  Up to 25 symbols per article may appear.  Each
     * symbol is limited to 72 characters.  There was no
     * standardization of the naming conventions.  SGML escapes appear
     * in the text used for Greek characters.  Superscripts are
     * enclosed in <code>&lt;up&gt;</code> and <code>&lt;/up&gt;</code>
     * delimeters, whereas subscripts use <code>down</code>.
     *
     * <P>The Greek character entities used are listed in:
     * <blockquote>
     * <a href="http://www.nlm.nih.gov/bsd/license/greek_symbol.html"
     *   >NLM Technical Bulletin: Greek Symbols</a>
     * </blockquote>
     *
     * @return An array of gene symbols for this citation.
     */
    public String[] geneSymbols() {
        return mGeneSymbols;
    }

    /**
     * Returns an array of MeSH headings for this citation.
     *
     * @return An array of MeSH headings for this citation.
     */
    public MeshHeading[] meshHeadings() {
        return mMeshHeadings;
    }

    /**
     * Returns the number of bibliographic references in a review
     * article, or the empty (zero length) string if none was
     * provided. Note that other information concerning reference
     * counts are provided by NLM's partners as part of the general
     * notes.
     *
     * @return Number of bibliographic references in a review article.
     */
    public String numberOfReferences() {
        return mNumberOfReferences;
    }

    /**
     * Returns the array of personal names of subjects for articles
     * that contain information about the person named.  This is
     * typically a biographical note or an obituary.
     *
     * @return An array of personal names of subjects in this article.
     */
    public PersonalNameSubject[] personalNameSubjects() {
        return mPersonalNameSubjects;
    }

    /**
     * Returns the array of other identifiers for this citation.
     * These identifiers are drawn from other sources as described
     * in the documentation for <code>OtherID</code>.
     *
     * @return The array of other identifiers for this citation.
     */
    public OtherID[] otherIDs() {
        return mOtherIDs;
    }

    /**
     * Returns the array of other abstracts for this citation.
     *
     * @return The array of other abstracts for this citation.
     */
    public OtherAbstract[] otherAbstracts() {
        return mOtherAbstracts;
    }

    /**
     * Returns the keyword lists for this citation.  Each
     * keyword list in the returned array will have an
     * owner specified, and each keyword is specified as being
     * a major or minor keyword.
     *
     * @return The keyword lists for this citation.
     */
    public KeywordList[] keywordLists() {
        return mKeywordLists;
    }

    /**
     * Returns a list of space flight names or mission numbers for
     * articles on research carried out in space.  Results may
     * be as simple as <code>manned</code> or <code>short duration</code>
     * or may indicate more specific projects such as <code>Biosatellite
     * 2 Project</code>.
     *
     * <P>This space flight data is added by the (United States)
     * National Aeronautics and Space Administration (NASA) and more
     * information is availabe from:
     *
     * <blockquote>
     * <a href="http://www.nlm.nih.gov/bsd/space_flight.html"
     *   >Space Flight Mission Summary Table</a>
     * </blockquote>
     *
     * @return An array of space flight names, descriptions or mission
     * numbers.
     */
    public String[] spaceFlightMissions() {
        return mSpaceFlightMissions;
    }

    /**
     * Returns the investigators created by the (United States)
     * National Aeronautics and Space Administration (NASA).
     * Investigators identify NASA-funded principal investigators.
     * Investigator lists are always complete.
     *
     * @return The inve
     */
    public Investigator[] investigators() {
        return mInvestigators;
    }

    /**
     * Returns supplemental or descriptive information for this
     * citation that does not fit elsewhere.  Each general note
     * has an owner and text.
     *
     * @return The general notes for this citation.
     */
    public GeneralNote[] generalNotes() {
        return mGeneralNotes;
    }

    /**
     * Returns a string-based representation of this citation.
     *
     * @return A string-based representation of this citation.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (mNlmDcmsID != null && mNlmDcmsID.length() > 0) {
            sb.append("Medline ID=" + mNlmDcmsID);
        }
        sb.append("PMID=" + mPMID);
        sb.append("\n");
        sb.append("     JOURNAL INFO=" + mJournalInfo);
        sb.append("\n");
        sb.append("     ARTICLE=" + mArticle);
        sb.append("\n");
        sb.append("     OWNER=" + mOwner);
        sb.append("\n");
        sb.append("     STATUS=" + mStatus);
        sb.append("\n");
        sb.append("     CREATED=" + mDateCreated);
        sb.append("\n");
        sb.append("     COMPLETED=" + mDateCompleted);
        sb.append("\n");
        sb.append("     REVISED=" + mDateRevised);
        sb.append("\n");
        sb.append("     CHEMICALS=" + Arrays.asList(mChemicals));
        sb.append("\n");
        sb.append("     CITATION SUBSETS="
                  + Arrays.asList(mCitationSubsets));
        sb.append("\n");
        sb.append("     MESH HEADINGS=" + Arrays.asList(mMeshHeadings));
        sb.append("\n");
        if (numberOfReferences() != null
            && numberOfReferences().length() > 0) {

            sb.append ("     NUMBER OF REFERENCES=");
            sb.append(numberOfReferences());
            sb.append("\n");
        }
        if (personalNameSubjects().length > 0) {
            sb.append("     PERSONAL NAME SUBJECTS=");
            sb.append(Arrays.asList(personalNameSubjects()));
            sb.append("\n");
        }
        if (mKeywordLists != null && mKeywordLists.length > 0) {
            sb.append("     KEYWORD LISTS=" + mKeywordLists);
        }
        if (mGeneralNotes.length > 0) {
            sb.append("     GENERAL NOTES="
                      + Arrays.asList(mGeneralNotes));
            sb.append("\n");
        }
        if (mGeneSymbols.length > 0) {
            sb.append("     GENE SYMBOLS="
                      + Arrays.asList(mGeneSymbols));
            sb.append("\n");
        }
        if (mSpaceFlightMissions.length > 0) {
            sb.append("     SPACEFLIGHT MISSIONS="
                      + Arrays.asList(mSpaceFlightMissions));
            sb.append("\n");
        }
        if (mOtherIDs.length > 0) {
            sb.append("     OTHER IDS="
                      + Arrays.asList(mOtherIDs));
            sb.append("\n");
        }
        if (mInvestigators.length > 0) {
            sb.append("     INVESTIGATORS="
                      + Arrays.asList(mInvestigators));
            sb.append("\n");
        }
        if (mOtherAbstracts.length > 0) {
            sb.append("     OTHER ABSTRACTS="
                      + Arrays.asList(mOtherAbstracts));
            sb.append("\n");
        }
        if (mCommentOrCorrections.length > 0) {
            sb.append("     COMMENTS OR CORRECTIONS="
                      + Arrays.asList(mCommentOrCorrections));
            sb.append("\n");
        }
        // sb.append(" XML=");
        // sb.append(xmlString());
        return sb.toString();
    }

    void setXMLString(String xmlString) {
        mXMLString = xmlString;
    }


    public static MedlineCitation parse(InputSource inSource,
                                        XMLReader xmlReader)
        throws IOException, SAXException {

        DelegatingHandler handler = new DelegatingHandler();
        Handler citationHandler = new Handler(handler);
        handler.setDelegate(MedlineCitationSet.MEDLINE_CITATION_ELT,
                            citationHandler);
        xmlReader.setContentHandler(handler);
        xmlReader.parse(inSource);
        return citationHandler.getCitation();
    }

    // <!ELEMENT MedlineCitation (%NlmDcmsID.Ref;, %PMID.Ref;,
    //                            %DateCreated.Ref;, DateCompleted?,
    //                            DateRevised?, Article, MedlineJournalInfo,
    //                            ChemicalList?,
    //                            CitationSubset*, CommentsCorrections?,
    //                            GeneSymbolList?,
    //                            MeshHeadingList?, NumberOfReferences?,
    //                            PersonalNameSubjectList?,
    //                            OtherID*, OtherAbstract*, KeywordList*,
    //                            SpaceFlightMission*,
    //                            InvestigatorList?, GeneralNote*)>
    // <!ATTLIST MedlineCitation
    //           Owner %Owner; "NLM"
    //           Status %Status; >
    // <!ENTITY % Owner "(NLM | NASA | PIP | KIE | HSR | HMD | SIS | NOTNLM)">
    // <!ENTITY % Status "(Completed | In-Process | PubMed-not-MEDLINE |
    //                    In-Data-Review | Publisher) #REQUIRED">
    // <!ENTITY % NlmDcmsID.Ref "NlmDcmsID?">
    // <!ENTITY % PMID.Ref "PMID">
    // <!ENTITY % PubDate.Ref "PubDate">
    // <!ENTITY % DateCreated.Ref "DateCreated">
    // <!ELEMENT PersonalNameSubjectList (PersonalNameSubject+)>
    // returned citation will have null XML bytes, which is OK
    // <!ELEMENT GeneSymbol (#PCDATA)>
    // <!ELEMENT GeneSymbolList (GeneSymbol+)>
    static class Handler extends DelegateHandler {
        private String mOwner;
        private String mStatus;
        private final TextAccumulatorHandler mNlmDcmsIDHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mPMIDHandler
            = new TextAccumulatorHandler();
        private final DateHandler mDateCreatedHandler;
        private final DateHandler mDateCompletedHandler;
        private final DateHandler mDateRevisedHandler;
        private final Article.Handler mArticleHandler;
        private final JournalInfo.Handler mJournalInfoHandler;
        private final ArrayList mChemicalList = new ArrayList();
        private final Chemical.Handler mChemicalHandler;
        private final ArrayList mCitationSubsetList = new ArrayList();
        private final TextAccumulatorHandler mCitationSubsetHandler
            = new TextAccumulatorHandler();
        private final ArrayList mCommentOrCorrectionList = new ArrayList();
        private final ArrayList mGeneSymbolList = new ArrayList();
        private final TextAccumulatorHandler mGeneSymbolHandler
            = new TextAccumulatorHandler();
        private final MeshHeading.ListHandler mMeshHeadingListHandler;
        private final TextAccumulatorHandler mNumberOfReferencesHandler
            = new TextAccumulatorHandler();
        private final ArrayList mPersonalNameSubjectList = new ArrayList();
        private final PersonalNameSubject.Handler mPersonalNameSubjectHandler;
        private final ArrayList mOtherIDList = new ArrayList();
        private final OtherID.Handler mOtherIDHandler;
        private final ArrayList mOtherAbstractList = new ArrayList();
        private final OtherAbstract.Handler mOtherAbstractHandler;
        private final ArrayList mKeywordListList = new ArrayList();
        private final KeywordList.Handler mKeywordListHandler;
        private final ArrayList mSpaceFlightMissionList = new ArrayList();
        private final TextAccumulatorHandler mSpaceFlightMissionHandler
            = new TextAccumulatorHandler();
        private final ArrayList mInvestigatorList = new ArrayList();
        private final Investigator.Handler mInvestigatorHandler;
        private final ArrayList mGeneralNoteList = new ArrayList();
        private final GeneralNote.Handler mGeneralNoteHandler;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mDateCreatedHandler = new DateHandler(delegator);
            mDateCompletedHandler = new DateHandler(delegator);
            mDateRevisedHandler = new DateHandler(delegator);
            mArticleHandler = new Article.Handler(delegator);
            mJournalInfoHandler = new JournalInfo.Handler(delegator);
            mChemicalHandler = new Chemical.Handler(delegator);
            mMeshHeadingListHandler
                = new MeshHeading.ListHandler(delegator);
            mPersonalNameSubjectHandler
                = new PersonalNameSubject.Handler(delegator);
            mOtherIDHandler = new OtherID.Handler();
            mOtherAbstractHandler = new OtherAbstract.Handler(delegator);
            mKeywordListHandler = new KeywordList.Handler(delegator);
            mInvestigatorHandler = new Investigator.Handler(delegator);
            mGeneralNoteHandler = new GeneralNote.Handler();

            setDelegate(MedlineCitationSet.NLM_DCMS_ID_ELT,
                        mNlmDcmsIDHandler);
            setDelegate(MedlineCitationSet.PMID_ELT,mPMIDHandler);
            setDelegate(MedlineCitationSet.DATE_CREATED_ELT,
                        mDateCreatedHandler);
            setDelegate(MedlineCitationSet.DATE_COMPLETED_ELT,
                        mDateCompletedHandler);
            setDelegate(MedlineCitationSet.DATE_REVISED_ELT,
                        mDateRevisedHandler);
            setDelegate(MedlineCitationSet.CHEMICAL_ELT,
                        mChemicalHandler);
            setDelegate(MedlineCitationSet.CITATION_SUBSET_ELT,
                        mCitationSubsetHandler);
            setDelegate(MedlineCitationSet.MESH_HEADING_LIST_ELT,
                        mMeshHeadingListHandler);
            setDelegate(MedlineCitationSet.NUMBER_OF_REFERENCES_ELT,
                        mNumberOfReferencesHandler);
            setDelegate(MedlineCitationSet.PERSONAL_NAME_SUBJECT_ELT,
                        mPersonalNameSubjectHandler);
            setDelegate(MedlineCitationSet.KEYWORD_LIST_ELT,
                        mKeywordListHandler);
            setDelegate(MedlineCitationSet.GENERAL_NOTE_ELT,
                        mGeneralNoteHandler);
            setDelegate(MedlineCitationSet.GENE_SYMBOL_ELT,
                        mGeneSymbolHandler);
            setDelegate(MedlineCitationSet.SPACE_FLIGHT_MISSION_ELT,
                        mSpaceFlightMissionHandler);
            setDelegate(MedlineCitationSet.OTHER_ID_ELT,
                        mOtherIDHandler);
            setDelegate(MedlineCitationSet.INVESTIGATOR_ELT,
                        mInvestigatorHandler);
            setDelegate(MedlineCitationSet.OTHER_ABSTRACT_ELT,
                        mOtherAbstractHandler);
            for (int i = 0;
                 i < MedlineCitationSet.COMMENT_OR_CORRECTIONS.length; ++i) {
                String elt = MedlineCitationSet.COMMENT_OR_CORRECTIONS[i];
                setDelegate(elt,new CommentOrCorrection.Handler(elt,
                                                                delegator));
            }
            setDelegate(MedlineCitationSet.MEDLINE_JOURNAL_INFO_ELT,
                        mJournalInfoHandler);
            setDelegate(MedlineCitationSet.ARTICLE_ELT,mArticleHandler);
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.CITATION_SUBSET_ELT)) {
                mCitationSubsetList.add(mCitationSubsetHandler.getText());
            } else if (qName.equals(MedlineCitationSet.CHEMICAL_ELT)) {
                mChemicalList.add(mChemicalHandler.getChemical());
            } else if (qName.equals(MedlineCitationSet.KEYWORD_LIST_ELT)) {
                mKeywordListList.add(mKeywordListHandler.getKeywordList());
            } else if (qName.equals(MedlineCitationSet.GENERAL_NOTE_ELT)) {
                mGeneralNoteList.add(mGeneralNoteHandler.getNote());
            } else if (qName.equals(MedlineCitationSet.GENE_SYMBOL_ELT)) {
                mGeneSymbolList.add(mGeneSymbolHandler.getText());
            } else if (qName.equals(MedlineCitationSet
                                    .SPACE_FLIGHT_MISSION_ELT)) {
                mSpaceFlightMissionList.add(mSpaceFlightMissionHandler
                                            .getText());
            } else if (qName.equals(MedlineCitationSet.OTHER_ID_ELT)) {
                mOtherIDList.add(mOtherIDHandler.getOtherID());
            } else if (qName.equals(MedlineCitationSet.INVESTIGATOR_ELT)) {
                mInvestigatorList.add(mInvestigatorHandler.getInvestigator());
            } else if (qName.equals(MedlineCitationSet.OTHER_ABSTRACT_ELT)) {
                mOtherAbstractList.add(mOtherAbstractHandler
                                       .getOtherAbstract());
            } else if (MedlineCitationSet.COMMENT_OR_CORRECTION_SET
                       .contains(qName)) {
                CommentOrCorrection.Handler ccHandler
                    = (CommentOrCorrection.Handler) handler;
                mCommentOrCorrectionList.add(ccHandler
                                             .getCommentOrCorrection());
            } else if (qName.equals(MedlineCitationSet
                                    .PERSONAL_NAME_SUBJECT_ELT)) {
                mPersonalNameSubjectList
                    .add(mPersonalNameSubjectHandler
                         .getPersonalNameSubject());
            }
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            mNlmDcmsIDHandler.reset();
            mPMIDHandler.reset();
            mDateCreatedHandler.reset();
            mDateCompletedHandler.reset();
            mDateRevisedHandler.reset();
            mCitationSubsetList.clear();
            mChemicalList.clear();
            mKeywordListList.clear();
            mKeywordListHandler.reset();
            mGeneralNoteList.clear();
            mGeneSymbolList.clear();
            mSpaceFlightMissionList.clear();
            mOtherIDList.clear();
            mInvestigatorList.clear();
            mOtherAbstractList.clear();
            mCommentOrCorrectionList.clear();
            mMeshHeadingListHandler.reset();
            mNumberOfReferencesHandler.reset();
            mPersonalNameSubjectList.clear();
        }
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            if (qName.equals(MedlineCitationSet.MEDLINE_CITATION_ELT)) {
                mOwner = atts.getValue(MedlineCitationSet.OWNER_ATT);
                mStatus = atts.getValue(MedlineCitationSet.STATUS_ATT);
            } else {
                super.startElement(namespaceURI,localName,qName,atts);
            }
        }
        public MedlineCitation getCitation() {
            String[] citationSubsets = new String[mCitationSubsetList.size()];
            mCitationSubsetList.toArray(citationSubsets);
            Chemical[] chemicals = new Chemical[mChemicalList.size()];
            mChemicalList.toArray(chemicals);
            KeywordList[] keywordLists
                = new KeywordList[mKeywordListList.size()];
            mKeywordListList.toArray(keywordLists);
            GeneralNote[] generalNotes
                = new GeneralNote[mGeneralNoteList.size()];
            mGeneralNoteList.toArray(generalNotes);
            String[] geneSymbols = new String[mGeneSymbolList.size()];
            mGeneSymbolList.toArray(geneSymbols);
            String[] spaceFlightMissions
                = new String[mSpaceFlightMissionList.size()];
            mSpaceFlightMissionList.toArray(spaceFlightMissions);
            OtherID[] otherIDs = new OtherID[mOtherIDList.size()];
            mOtherIDList.toArray(otherIDs);
            Investigator[] investigators
                = new Investigator[mInvestigatorList.size()];
            mInvestigatorList.toArray(investigators);
            OtherAbstract[] otherAbstracts
                = new OtherAbstract[mOtherAbstractList.size()];
            mOtherAbstractList.toArray(otherAbstracts);
            CommentOrCorrection[] commentOrCorrections
                = new CommentOrCorrection[mCommentOrCorrectionList.size()];
            mCommentOrCorrectionList.toArray(commentOrCorrections);
            PersonalNameSubject[] personalNameSubjects
                = new PersonalNameSubject[mPersonalNameSubjectList.size()];
            mPersonalNameSubjectList.toArray(personalNameSubjects);
            return new MedlineCitation(mOwner,
                                       mStatus,
                                       mNlmDcmsIDHandler.getText(),
                                       mPMIDHandler.getText(),
                                       mDateCreatedHandler.getDate(),
                                       mDateCompletedHandler.getDate(),
                                       mDateRevisedHandler.getDate(),
                                       chemicals,
                                       citationSubsets,
                                       mMeshHeadingListHandler
                                       .getMeshHeadings(),
                                       mNumberOfReferencesHandler.getText(),
                                       personalNameSubjects,
                                       keywordLists,
                                       generalNotes,
                                       geneSymbols,
                                       spaceFlightMissions,
                                       otherIDs,
                                       investigators,
                                       otherAbstracts,
                                       commentOrCorrections,
                                       mJournalInfoHandler.getJournalInfo(),
                                       mArticleHandler.getArticle());
        }
    }


    // <!ENTITY % normal.date "(Year,Month,Day,(Hour,(Minute,Second?)?)?)">
    static class DateHandler extends DelegateHandler {
        private final DateFormat mDateFormat
            = new SimpleDateFormat("yyyy.MM.dd",Locale.ENGLISH);
        private final TextAccumulatorHandler mYearHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mMonthHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mDayHandler
            = new TextAccumulatorHandler();
        public DateHandler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.YEAR_ELT,mYearHandler);
            setDelegate(MedlineCitationSet.MONTH_ELT,mMonthHandler);
            setDelegate(MedlineCitationSet.DAY_ELT,mDayHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mYearHandler.reset();
            mMonthHandler.reset();
            mDayHandler.reset();
        }
        public Date getDate() {
            String yyyy = mYearHandler.getText();
            String mm = mMonthHandler.getText();
            String dd = mDayHandler.getText();
            String date = yyyy + '.' + mm + '.' + dd;
            if (date.length() < 3) return null;
            try {
                return mDateFormat.parse(date);
            } catch (ParseException e) {
                // log
                return null;
            }
        }
    }


}
