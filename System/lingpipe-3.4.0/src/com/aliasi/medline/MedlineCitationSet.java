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

import java.util.HashSet;
import java.util.Set;


/**
 * The <code>MedlineCitationSet</code> provides static constants for
 * the XML elements, attributes and constant values used in MEDLINE.
 * The current version is for the 2008 MEDLINE baseline and update
 * releases.
 *
 * <P>For more information, see the LingPipe tutorial on parsing MEDLINE.
 *
 * <P>Thorough documentation for the content of a
 * <code>MedlineCitationSet</code> XML document is provided by NLM at:
 *
 * <blockquote>
 * <a
 * href="http://www.nlm.nih.gov/bsd/licensee/data_elements_doc.html"
 * >MEDLINE XML Element Descriptions and Their Attributes</a>.
 * </blockquote>
 *
 * The DTDs for MEDLINE citations are available in the same directory
 * as this file, and the small sample file is available in LingPipe's
 * <code>demos/data</code> directory.  The DTDs and further sample files
 * are available from:
 *
 * <blockquote>
 *  <a href="http://www.nlm.nih.gov/bsd/licensee.html"
 *    >MEDLINE DTDs and Sample Data</a>
 * </blockquote>
 *
 * @author  Bob Carpenter
 * @version 3.3
 * @since   LingPipe2.0
 */
public class MedlineCitationSet {

    // do not allow instances
    private MedlineCitationSet() {
        /* do nothing */
    }

    /**
     * The <code>MedlineCitationSet</code> element.
     */
    public static final String MEDLINE_CITATION_SET_ELT = "MedlineCitationSet";

    /**
     * The <code>MedlineCitation</code> element.
     */
    public static final String MEDLINE_CITATION_ELT = "MedlineCitation";

    /**
     * The <code>PMID</code> element.
     */
    public static final String PMID_ELT = "PMID";

    /**
     * The <code>DateCreated</code> element.
     */
    public static final String DATE_CREATED_ELT = "DateCreated";

    /**
     * The <code>DateCompleted</code> element.
     */
    public static final String DATE_COMPLETED_ELT = "DateCompleted";

    /**
     * The <code>DateRevised</code> element.
     */
    public static final String DATE_REVISED_ELT = "DateRevised";

    /**
     * The <code>Year</code> element.
     */
    public static final String YEAR_ELT = "Year";

    /**
     * The <code>Season</code> element.
     */
    public static final String SEASON_ELT = "Season";

    /**
     * The <code>Month</code> element.
     */
    public static final String MONTH_ELT = "Month";

    /**
     * The <code>Day</code> element.
     */
    public static final String DAY_ELT = "Day";

    /**
     * The <code>MeshHeadingList</code> element.
     */
    public static final String MESH_HEADING_LIST_ELT = "MeshHeadingList";

    /**
     * The <code>MeshHeading</code> element.
     */
    public static final String MESH_HEADING_ELT = "MeshHeading";

    /**
     * The <code>DescriptorName</code> element.
     */
    public static final String DESCRIPTOR_NAME_ELT = "DescriptorName";

    /**
     * The <code>QualifierName</code> element.
     */
    public static final String QUALIFIER_NAME_ELT = "QualifierName";

    /**
     * The <code>CitationSubset</code> element.
     */
    public static final String CITATION_SUBSET_ELT = "CitationSubset";

    /**
     * The <code>Chemical</code> element.
     */
    public static final String CHEMICAL_ELT = "Chemical";

    /**
     * The <code>ChemicalList</code> element.
     */
    public static final String CHEMICAL_LIST_ELT = "ChemicalList";

    /**
     * The <code>RegistryNumber</code> element.
     */
    public static final String REGISTRY_NUMBER_ELT = "RegistryNumber";

    /**
     * The <code>NameOfSubstance</code> element.
     */
    public static final String NAME_OF_SUBSTANCE_ELT = "NameOfSubstance";

    /**
     * The <code>KeywordList</code> element.
     */
    public static final String KEYWORD_LIST_ELT = "KeywordList";

    /**
     * The <code>Keyword</code> element.
     */
    public static final String KEYWORD_ELT = "Keyword";

    /**
     * The <code>GeneralNote</code> element.
     */
    public static final String GENERAL_NOTE_ELT = "GeneralNote";

    /**
     * The <code>GeneSymbol</code> element.
     */
    public static final String GENE_SYMBOL_ELT = "GeneSymbol";

    /**
     * The <code>SpaceFlightMission</code> element.
     */
    public static final String SPACE_FLIGHT_MISSION_ELT = "SpaceFlightMission";

    /**
     * The <code>OtherID</code> element.
     */
    public static final String OTHER_ID_ELT = "OtherID";

    /**
     * The <code>Investigator</code> element.
     */
    public static final String INVESTIGATOR_ELT = "Investigator";

    /**
     * The <code>ForeName</code> element.
     */
    public static final String FORE_NAME_ELT = "ForeName";

    /**
     * The <code>MiddleName</code> element.
     */
    public static final String MIDDLE_NAME_ELT = "MiddleName";

    /**
     * The <code>LastName</code> element.
     */
    public static final String LAST_NAME_ELT = "LastName";

    /**
     * The <code>Initials</code> element.
     */
    public static final String INITIALS_ELT = "Initials";

    /**
     * The <code>Affiliation</code> element.
     */
    public static final String AFFILIATION_ELT = "Affiliation";

    /**
     * The <code>OtherAbstract</code> element.
     */
    public static final String OTHER_ABSTRACT_ELT = "OtherAbstract";

    /**
     * The <code>AbstractText</code> element.
     */
    public static final String ABSTRACT_TEXT_ELT = "AbstractText";

    /**
     * The <code>Note</code> element.
     */
    public static final String NOTE_ELT = "Note";

    /**
     * The <code>RefSource</code> element.
     */
    public static final String REF_SOURCE_ELT = "RefSource";

    /**
     * The <code>MedlineJournalInfo</code> element.
     */
    public static final String MEDLINE_JOURNAL_INFO_ELT = "MedlineJournalInfo";

    /**
     * The <code>MedlineTA</code> element.
     */
    public static final String MEDLINE_TA_ELT = "MedlineTA";

    /**
     * The <code>Country</code> element.
     */
    public static final String COUNTRY_ELT = "Country";

    /**
     * The <code>NlmUniqueID</code> element.
     */
    public static final String NLM_UNIQUE_ID_ELT = "NlmUniqueID";

    /**
     * The <code>Article</code> element.
     */
    public static final String ARTICLE_ELT = "Article";

    /**
     * The <code>MedlinePgn</code> element.
     */
    public static final String MEDLINE_PAGINATION_ELT = "MedlinePgn";

    /**
     * The <code>ELocationId</code> element.
     */
    public static final String E_LOCATION_ID_ELT = "ELocationId";

    /**
     * The <code>StartPage</code> element.
     */
    public static final String START_PAGE_ELT = "StartPage";

    /**
     * The <code>EndPage</code> element.
     */
    public static final String END_PAGE_ELT = "EndPage";

    /**
     * The <code>Journal</code> element.
     */
    public static final String JOURNAL_ELT = "Journal";

    /**
     * The <code>ISSN</code> element.
     */
    public static final String ISSN_ELT = "ISSN";

    /**
     * The <code>ISSNLinking</code> element.
     */
    public static final String ISSN_LINKING_ELT = "ISSNLinking";

    /**
     * The <code>JournalIssue</code> element.
     */
    public static final String JOURNAL_ISSUE_ELT = "JournalIssue";

    /**
     * The <code>Volume</code> element.
     */
    public static final String VOLUME_ELT = "Volume";

    /**
     * The <code>Issue</code> element.
     */
    public static final String ISSUE_ELT = "Issue";

    /**
     * The <code>Book</code> element.
     */
    public static final String BOOK_ELT = "Book";

    /**
     * The <code>Author</code> element.
     */
    public static final String AUTHOR_ELT = "Author";

    /**
     * The <code>Publisher</code> element.
     */
    public static final String PUBLISHER_ELT = "Publisher";

    /**
     * The <code>AuthorList</code> element.
     */
    public static final String AUTHOR_LIST_ELT = "AuthorList";

    /**
     * The <code>CollectionTitle</code> element.
     */
    public static final String COLLECTION_TITLE_ELT = "CollectionTitle";

    /**
     * The <code>MedlineDate</code> element.
     */
    public static final String MEDLINE_DATE_ELT = "MedlineDate";

    /**
     * The <code>CommentOn</code> element.
     */
    public static final String COMMENT_ON_ELT = "CommentOn";

    /**
     * The <code>CommentIn</code> element.
     */
    public static final String COMMENT_IN_ELT = "CommentIn";

    /**
     * The <code>ErratumIn</code> element.
     */
    public static final String ERRATUM_IN_ELT = "ErratumIn";

    /**
     * The <code>ErratumFor</code> element.
     */
    public static final String ERRATUM_FOR_ELT = "ErratumFor";

    /**
     * The <code>PartialRetractionOf</code> element.
     */
    public static final String PARTIAL_RETRACTION_OF_ELT = "PartialRetractionOf";

    /**
     * The <code>PartialRetractionIn</code> element.
     */
    public static final String PARTIAL_RETRACTION_IN_ELT = "PartialRetractionIn";

    /**
     * The <code>RepublishedFrom</code> element.
     */
    public static final String REPUBLISHED_FROM_ELT = "RepublishedFrom";

    /**
     * The <code>RepublishedIn</code> element.
     */
    public static final String REPUBLISHED_IN_ELT = "RepublishedIn";

    /**
     * The <code>RetractionOf</code> element.
     */
    public static final String RETRACTION_OF_ELT = "RetractionOf";

    /**
     * The <code>RetractionIn</code> element.
     */
    public static final String RETRACTION_IN_ELT = "RetractionIn";


    /**
     * The <code>UpdateIn</code> element.
     */
    public static final String UPDATE_IN_ELT = "UpdateIn";

    /**
     * The <code>UpdateOf</code> element.
     */
    public static final String UPDATE_OF_ELT = "UpdateOf";

    /**
     * The <code>SummaryForPatientsIn</code> element.
     */
    public static final String SUMMARY_FOR_PATIENTS_IN_ELT
        = "SummaryForPatientsIn";

    /**
     * The <code>OriginalReportIn</code> element.
     */
    public static final String ORIGINAL_REPORT_IN_ELT = "OriginalReportIn";

    /**
     * The <code>ReprintIn</code> element.
     */
    public static final String REPRINT_IN_ELT = "ReprintIn";

    /**
     * The <code>ReprintOf</code> element.
     */
    public static final String REPRINT_OF_ELT = "ReprintOf";

    /**
     * The <code>PubDate</code> element.
     */
    public static final String PUB_DATE_ELT = "PubDate";

    /**
     * The <code>Coden</code> element.
     */
    public static final String CODEN_ELT = "Coden";

    /**
     * The <code>ISOAbbreviation</code> element.
     */
    public static final String ISO_ABBREVIATION_ELT = "ISOAbbreviation";

    /**
     * The <code>Title</code> element.
     */
    public static final String TITLE_ELT = "Title";

    /**
     * The <code>Abstract</code> element.
     */
    public static final String ABSTRACT_ELT = "Abstract";

    /**
     * The <code>CopyrightInformation</code> element.
     */
    public static final String COPYRIGHT_INFORMATION_ELT = "CopyrightInformation";

    /**
     * The <code>ArticleTitle</code> element.
     */
    public static final String ARTICLE_TITLE_ELT = "ArticleTitle";

    /**
     * The <code>Suffix</code> element.
     */
    public static final String SUFFIX_ELT = "Suffix";

    /**
     * The <code>CollectiveName</code> element.
     */
    public static final String COLLECTIVE_NAME_ELT = "CollectiveName";

    /**
     * The <code>DatesAssociatedWithName</code> element.
     */
    public static final String DATES_ASSOCIATED_WITH_NAME_ELT
        = "DatesAssociatedWithName";

    /**
     * The <code>NameQualifier</code> element.
     */
    public static final String NAME_QUALIFIER_ELT = "NameQualifier";

    /**
     * The <code>OtherInformation</code> element.
     */
    public static final String OTHER_INFORMATION_ELT = "OtherInformation";

    /**
     * The <code>TitleAssociatedWithName</code> element.
     */
    public static final String TITLE_ASSOCIATED_WITH_NAME_HANDLER
        = "TitleAssociatedWithName";

    /**
     * The <code>Language</code> element.
     */
    public static final String LANGUAGE_ELT = "Language";

    /**
     * The <code>DataBank</code> element.
     */
    public static final String DATA_BANK_ELT = "DataBank";

    /**
     * The <code>DataBankList</code> element.
     */
    public static final String DATA_BANK_LIST_ELT = "DataBankList";

    /**
     * The <code>DataBankName</code> element.
     */
    public static final String DATA_BANK_NAME_ELT = "DataBankName";

    /**
     * The <code>AccessionNumber</code> element.
     */
    public static final String ACCESSION_NUMBER_ELT = "AccessionNumber";

    /**
     * The <code>GrantList</code> element.
     */
    public static final String GRANT_LIST_ELT = "GrantList";

    /**
     * The <code>Grant</code> element.
     */
    public static final String GRANT_ELT = "Grant";

    /**
     * The <code>GrantID</code> element.
     */
    public static final String GRANT_ID_ELT = "GrantID";

    /**
     * The <code>Agency</code> element.
     */
    public static final String AGENCY_ELT = "Agency";

    /**
     * The <code>Acronym</code> element.
     */
    public static final String ACRONYM_ELT = "Acronym";

    /**
     * The <code>PublicationType</code> element.
     */
    public static final String PUBLICATION_TYPE_ELT = "PublicationType";

    /**
     * The <code>VernacularTitle</code> element.
     */
    public static final String VERNACULAR_TITLE_ELT = "VernacularTitle";

    /**
     * The <code>ElectronicPubDate</code> element.
     */
    public static final String ELECTRONIC_PUB_DATE_ELT = "ElectronicPubDate";

    /**
     * The <code>NlmDcmsID</code> element.
     */
    public static final String NLM_DCMS_ID_ELT = "NlmDcmsID";

    /**
     * The <code>NumberOfReferences</code> element.
     */
    public static final String NUMBER_OF_REFERENCES_ELT = "NumberOfReferences";

    /**
     * The <code>PersonalNameSubject</code> element.
     */
    public static final String PERSONAL_NAME_SUBJECT_ELT = "PersonalNameSubject";

    /**
     * The <code>TitleAssociatedWithName</code> element.
     */
    public static final String TITLE_ASSOCIATED_WITH_NAME_ELT
        = "TitleAssociatedWithName";

    /**
     * The <code>DeleteCitation</code> element.
     */
    public static final String DELETE_CITATION_ELT
        = "DeleteCitation";

    /**
     * The <code>EIdType</code> attribute.
     */
    public static final String E_ID_TYPE_ATT = "EIdType";

    /**
     * The <code>Owner</code> attribute.
     */
    public static final String OWNER_ATT = "Owner";

    /**
     * The <code>Status</code> attribute.
     */
    public static final String STATUS_ATT = "Status";

    /**
     * The <code>MajorTopicYN</code> attribute.
     */
    public static final String MAJOR_TOPIC_YN_ATT = "MajorTopicYN";

    /**
     * The <code>Source</code> attribute.
     */
    public static final String SOURCE_ATT = "Source";

    /**
     * The <code>Prefix</code> attribute for other identifiers.
     */
    public static final String PREFIX_ATT = "Prefix";


    /**
     * The <code>Type</code> attribute.
     */
    public static final String TYPE_ATT = "Type";

    /**
     * The <code>PrintYN</code> attribute.
     */
    public static final String PRINT_YN_ATT = "PrintYN";

    /**
     * The <code>CompleteYN</code> attribute.
     */
    public static final String COMPLETE_YN_ATT = "CompleteYN";

    /**
     * The <code>ValidYN</code> attribute for author names.
     */
    public static final String VALID_YN_ATT = "ValidYN";

    /**
     * The <code>OfficialDateYN</code> attribute.
     */
    public static final String OFFICIAL_DATE_YN_ATT = "OfficialDateYN";

    /**
     * The <code>PubModel</code> attribute for articles.
     */
    public static final String PUB_MODEL_ATT = "PubModel";

    /**
     * The <code>Y</code> value.
     */
    public static final String YES_VALUE = "Y";

    /**
     * The <code>N</code> value.
     */
    public static final String NO_VALUE = "N";

    /**
     * The <code>Completed</code> value.
     */
    public static final String COMPLETED_VALUE = "Completed";

    /**
     * The <code>In-Process</code> value.
     */
    public static final String IN_PROCESS_VALUE = "In-Process";

    /**
     * The <code>PubMed-not-MEDLINE</code> value.
     */
    public static final String PUBMED_NOT_MEDLINE_VALUE = "PubMed-not-MEDLINE";

    /**
     * The <code>MEDLINE</code> value (for citation status).
     */
    public static final String MEDLINE_VALUE = "MEDLINE";

    /**
     * The <code>OLDMEDLINE</code> value (for citation status).
     */
    public static final String OLDMEDLINE_VALUE = "OLDMEDLINE";

    /**
     * The <code>In-Data-Review</code> value.
     */
    public static final String IN_DATA_REVIEW_VALUE = "In-Data-Review";

    /**
     * The <code>Publisher</code> value.
     */
    public static final String PUBLISHER_VALUE = "Publisher";


    /**
     * The <code>NLM</code> value.
     */
    public static final String NLM_VALUE = "NLM";

    /**
     * The <code>NASA</code> value.
     */
    public static final String NASA_VALUE = "NASA";

    /**
     * The <code>PIP</code> value.
     */
    public static final String PIP_VALUE = "PIP";

    /**
     * The <code>KIE</code> value.
     */
    public static final String KIE_VALUE = "KIE";

    /**
     * The <code>HSR</code> value.
     */
    public static final String HSR_VALUE = "HSR";

    /**
     * The <code>HMD</code> value.
     */
    public static final String HMD_VALUE = "HMD";

    /**
     * The <code>SIS</code> value.
     */
    public static final String SIS_VALUE = "SIS";

    /**
     * The <code>NOTNLM</code> value.
     */
    public static final String NOT_NLM_VALUE = "NOTNLM";

    /**
     * The <code>Print</code> value for publication model.
     */
    public static final String PRINT_VALUE = "Print";

    /**
     * The <code>Print-Electronic</code> value for publication model.
     */
    public static final String PRINT_ELECTRONIC_VALUE = "Print-Electronic";

    /**
     * The <code>Electronic</code> value for publication model.
     */
    public static final String ELECTRONIC_VALUE = "Electronic";

    /**
     * The <code>Electronic-Print</code> value for publication model.
     */
    public static final String ELECTRONIC_PRINT_VALUE = "Electronic-Print";

    static final String[] COMMENT_OR_CORRECTIONS = new String[] {
        COMMENT_ON_ELT,
        COMMENT_IN_ELT,
        ERRATUM_IN_ELT,
        ERRATUM_FOR_ELT,
        PARTIAL_RETRACTION_IN_ELT,
        PARTIAL_RETRACTION_OF_ELT,
        REPUBLISHED_FROM_ELT,
        REPUBLISHED_IN_ELT,
        RETRACTION_OF_ELT,
        RETRACTION_IN_ELT,
        UPDATE_IN_ELT,
        UPDATE_OF_ELT,
        SUMMARY_FOR_PATIENTS_IN_ELT,
        ORIGINAL_REPORT_IN_ELT,
        REPRINT_IN_ELT,
        REPRINT_OF_ELT
    };
    static final Set COMMENT_OR_CORRECTION_SET = new HashSet();
    static {
        for (int i = 0; i < COMMENT_OR_CORRECTIONS.length; ++i)
            COMMENT_OR_CORRECTION_SET.add(COMMENT_OR_CORRECTIONS[i]);
    }


    // these are actually used in MedlineParser

    /**
     * The name of the NLM Medline DTD, namely <code>&quot;-//NLM//DTD
     * NLM Medline//EN&quot;</code>.  This DTD is available on the
     * LingPipe classpath in this package
     * (<code>com.aliasi.medline</code>) as file
     * <code>nlmmedline_080101.dtd</code>.
     */
    public static final String NLM_MEDLINE_DTD_NAME
        = "-//NLM//DTD Medline Citation, 1st January 2008//EN";

    /**
     * The name of the NLM Medline DTD, namely
     * <code>&quot;-//NLM//DTD MedlineCitation, 1st January 2008//EN&quot;</code>.
     * This DTD is
     * available on the LingPipe classpath in this package
     * (<code>com.aliasi.medline</code>) as file
     * <code>nlmmedlinecitation_080101.dtd</code>.
     */
    public static final String NLM_MEDLINE_CITATION_DTD_NAME
        = "-//NLM//DTD MedlineCitation, 1st January 2008//EN";

    /**
     * The name of the NLM Medline DTD, namely
     * <code>&quot;-//NLM//DTD SharedCatCit, 1st January 2008//EN&quot;</code>.
     * This DTD is
     * available on the LingPipe classpath in this package
     * (<code>com.aliasi.medline</code>) as file
     * <code>nlmsharedcatcit_080101.dtd</code>.
     */
    public static final String NLM_SHARED_CAT_DTD_NAME
        = "-//NLM//DTD SharedCatCit, 1st January 2008//EN";

    /**
     * The name of the NLM Medline DTD, namely
     * <code>&quot;-//NLM//DTD Common, 1st January 2008//EN&quot;</code>.
     * This DTD is
     * available on the LingPipe classpath in this package
     * (<code>com.aliasi.medline</code>) as file
     * <code>nlmcommon_080101.dtd</code>.
     */
    public static final String NLM_COMMON_DTD_NAME
        = "-//NLM//DTD Common, 1st January 2008//EN";

}

