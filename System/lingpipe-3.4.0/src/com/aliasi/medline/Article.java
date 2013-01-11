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

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * An <code>Article</code> represents the content of the
 * <code>Article</code> element of a MEDLINE citation.
 *
 * <P>An article contains information about the book or journal in
 * which it is published.  Either the method {@link #book()} or {@link
 * #journal()} will return a non-<code>null</code> result, but not
 * both.  Similarly, either {@link #inBook()} or {@link #inJournal()}
 * will return <code>true</code> but not both.
 *
 * <P>Each article is required to have a title, and its text
 * is returned by the method {@link #articleTitle()}.  Each article
 * also has information about its page numbers within the book or
 * journal in which it was published, which is returned by
 * the methods {@link #pagination()} and {@link #eLocationIds()}.
 * There will be a pagination and zero or more e-locations,
 * or there will be one or more e-locations for each article.
 *
 * <P>An article optionally contains an abstract, which will be a
 * non-<code>null</code> return value from {@link #abstrct()}; note the
 * unusual argument naming to avoid conflict with the keyword
 * <code>abstract</code>.
 *
 * <P>Articles also optionally contain an author list and affiliation
 * for the first author, which will be available as a non-null return
 * values from {@link #authorList()} and {@link #affiliation()}.
 *
 * <P>One or more languages in which the text of the article appeared
 * is available from {@link #languages()}.  Note that abstracts, if
 * available, are always in English.  For articles not in English,
 * the {@link #vernacularTitle()} method returns the original title
 * transliterated into the Roman alphabet; for English articles, it
 * returns the empty string.
 *
 * <P>Some articles contain linkages to sequence databanks.  The
 * method {@link #dataBankList()} returns a list of zero or more
 * such linkages, organized by database.
 *
 * <P>Some articles contain information about grant funding for
 * the research reported.  The method {@link #grantList()} returns
 * zero or more grant objects.
 *
 * <P>Each article contains a non-empty list of publication types,
 * available through {@link #publicationTypes()}.
 *
 * <P>Articles that were published electronically as well as in print
 * also specify the electronic publication date as a
 * non-<code>null</code> value for {@link #articleDate()}.
 *
 * @author  Bob Carpenter
 * @version 3.3
 * @since   LingPipe2.0
 */
public class Article {
    private final String mPublicationModel;
    private final Journal mJournal;
    private final Book mBook;
    private final String mArticleTitle;
    private final String mPagination;
    private final ELocationId[] mELocationIds;
    private final Abstract mAbstract;
    private final String mAffiliation;
    private final AuthorList mAuthorList;
    private final String[] mLanguages;
    private final DataBankList mDataBankList;
    private final GrantList mGrantList;
    private final String[] mPublicationTypes;
    private final String mVernacularTitle;
    private final ArticleDate mArticleDate;
    Article(String publicationModel,
            Journal journal,
            Book book,
            String articleTitle,
            String pagination,
            ELocationId[] eLocationIds,
            Abstract abstrct,
            String affiliation,
            AuthorList authorList,
            String[] languages,
            DataBankList dataBankList,
            GrantList grantList,
            String[] publicationTypes,
            String vernacularTitle,
            ArticleDate articleDate) {
        mPublicationModel = publicationModel;
        mJournal = journal;
        mBook = book;
        mArticleTitle = articleTitle;
        mPagination = pagination;
        mELocationIds = eLocationIds;
        mAbstract = abstrct;
        mAffiliation = affiliation;
        mAuthorList = authorList;
        mLanguages = languages;
        mDataBankList = dataBankList;
        mGrantList = grantList;
        mPublicationTypes = publicationTypes;
        mVernacularTitle = vernacularTitle;
        mArticleDate = articleDate;
    }

    /**
     * Returns the publication model for this article.  All articles
     * have one of the following models:
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><i>Publication Type</i></td><td><i>Constant</i></td></tr>
     * <tr><td>Print</td><td><code>MedlineCitationSet.PRINT_VALUE</td></tr>
     * <tr><td>Print-Electronic</td><td><code>MedlineCitationSet.PRINT_ELECTRONIC_VALUE</td></tr>
     * <tr><td>Electronic</td><td><code>MedlineCitationSet.ELECTRONIC_VALUE</td></tr>
     * <tr><td>Electronic-Print</td><td><code>MedlineCitationSet.ELECTRONIC_PRINT_VALUE</td></tr>
     * </table>
     * </blockquote>
     */
    public String publicationModel() {
        return mPublicationModel;
    }

    /**
     * Returns the page numbering for this article as a string, or <code>null</code>
     * if there is no page numbering for this article if it is electronic only.
     * Articles without pagination must have at least one electronic location
     * identifier, returned by method {@link #eLocationIds()}.
     *
     * <p>The
     * pagination is presented without redundant prefixes; for example
     * <code>212-27</code> indicates pages 212 to 227.  MEDLINE
     * presents a wide range of possible pagination schemes.  There
     * may be be alphanumeric page numbering (<i>e.g.</i>
     * <code>P32-4</code>), split pages (<i>e.g.</i> <code>24-32,
     * 64</code>), pages plus sections (<i>e.g.</i> <code>176-8
     * concl</code> <i>or</i> <code>suppl 111-2</code>), Roman
     * numerals (<i>e.g.</i> <code>iii-viii</code> <i>or</i>
     * <code>XC-CIII</code>), or other descriptive content
     * (<i>e.g.</i> <code>1 p preceding table of contents</code>
     * <i>or</i> <code>[6021 words; 81 paragraphs]</code>).
     *
     * @return The page numbering for this article as a string.
     */
    public String pagination() {
        return mPagination;
    }

    /**
     * Returns zero or more electronic location identifiers for this
     * article.  If the return value of {@link #pagination()} is
     * <code>null</code>, then the list returned by this method will
     * contain at least one element.  If pagination is not null, the
     * array of electronic locations may have zero or more elements.
     *
     * @return The electronic location identifiers for this article.
     */
    public ELocationId[] eLocationIds() {
        return mELocationIds;
    }

    /**
     * Returns a representation of the journal in which this article
     * appeared, or <code>null</code> if it appeared in a book rather
     * than a journal.  The method <code>inJournal()</code> may be
     * used to test whether this article is in a journal.
     *
     * @return The journal in which this article was published, or
     * <code>null</code> if it appeared in a book.
     */
    public Journal journal() {
        return mJournal;
    }

    /**
     * Returns a representation of the book in which this article
     * appeared, or <code>null</code> if it appeared in a journal
     * rather than a book.  The method <code>inBooK()</code> may
     * be used to test whether this article appeared in a book.
     *
     * @return The book in which this article was published, or
     * <code>null</code> if it appeared in a journal.
     */
    public Book book() {
        return mBook;
    }

    /**
     * Returns <code>true</code> if this article appeared in
     * a book.  Every article is drawn from either a book or
     * a journal, so this method will return the negation of
     * the method {@link #inJournal()}.
     *
     * @return <code>true</code> if this article appeared in a book.
     */
    public boolean inBook() {
        return mBook != null;
    }

    /**
     * Returns <code>true</code> if this article appeared in
     * a journal.  Every article is drawn from either a book or
     * a journal, so this method will return the negation of
     * the method {@link #inBook()}.
     *
     * @return <code>true</code> if this article appeared in a book.
     */
    public boolean inJournal() {
        return mJournal != null;
    }

    /**
     * Returns a representation of the book or journal in which this
     * article was published.  If it was published in a book, then the
     * result is the same as {@link #book()}, oteherwise it is the
     * same as {@link #journal()}.
     *
     * @return A representation of the book or journal in which this
     * article was published.
     */
    public Object in() {
        if (inBook()) return book();
        else return journal();
    }


    /**
     * Returns the title of this article.  Article titles are always
     * returned in English.  A translated title appears wrapped in
     * square brackets (<i>e.g.</i>. <code>[Biological rhythms and
     * human disease]</code> <i>or</i> <code>[Anterior
     * panhypopituitarism after sella turcica fracture (author's
     * trans)]</code>).  Note that if the citation is in process, the
     * title will appear as <code>[In Process Citation]</code>.
     * Untranslated titles end with a period unless the article title
     * itself ends with another punctuation mark (<i>e.g.</i>
     * <code>Why is xenon not more widely used for anaesthesia?</code>
     * <i>or</i> <code>The Kleine-Levin syndrome as a neuropsychiatric
     * disorder: a case report.</code>).
     *
     * @return The title of this article.
     */
    public String articleTitle() {
        return mArticleTitle;
    }

    /**
     * Returns <code>true</code> if the article has been translated
     * from a language other than English.  The method determines this
     * by inspecting the article title for marking that it has been
     * translated.
     *
     * @return <code>true</code> if the article has been
     * translated from a language other than English.
     */
    public boolean articleTranslated() {
        return articleTitle().length() > 1
            && articleTitle().charAt(0) == '['
            && articleTitle().charAt(articleTitle().length()-1) == ']';
    }

    /**
     * Return the article title without any brackets that may have
     * been added to indicate translation and without any indication
     * of the author having translated it.  Note that sentence-final
     * periods may have also been inserted by NLM, but there is no
     * reliable way to remove them without potentially removing
     * periods that ended original titles, such as those ending
     * acronyms.
     *
     * @return The text of the title of th is article.
     */
    public String articleTitleText() {
        if (!articleTranslated()) return articleTitle();
        String baseText
            = articleTitle().substring(1,articleTitle().length()-1);
        if (baseText.endsWith(AUTHORS_TRANS_MARKER))
            return baseText.substring(0,
                                      baseText.length()
                                      - AUTHORS_TRANS_MARKER.length());
        return baseText;
    }

    /**
     * Returns the abstract for this article, or <code>null</code> if
     * there is no abstract.
     *
     * @return The abstract for this article.
     */
    public Abstract abstrct() {
        return mAbstract;
    }

    /**
     * Returns the author list for this article. Note that this
     * list will not be null, but may be empty.  This varies
     * slightly from the XML, which simply elides the author list
     * when it has no members, as in the case of an anonymous
     * article.
     *
     * @return The author list for this article.
     */
    public AuthorList authorList() {
        return mAuthorList;
    }

    /**
     * Returns the affiliation for the the first author of this
     * article.  The way in which affiliation is recorded has evolved
     * from its introduction in 1988.  At one point, it included city
     * information along with state and zip code information for the
     * United States and country information for elsewhere.  Later it
     * added the <code>USA</code> for United States-based
     * institutiones. In 1996, the email was added as it appears in
     * the article. As of 2003, first author address is included as it
     * appears in the article.
     *
     * @return Affiliation information for first author.
     */
    public String affiliation() {
        return mAffiliation;
    }

    /**
     * Returns an array of three-letter language abbreviations
     * indicating the language(s) in which the article was published.
     * A complete list of language abbreviations cna be found at:
     *
     * <blockquote>
     * <a href="http://www.nlm.nih.gov/bsd/language_table.html"
     >MEDLINE Language Table</a>
     * </blockquote>
     *
     * @return The abbreviations of languages in which the article appeared.
     */
    public String[] languages() {
        return mLanguages;
    }

    /**
     * Returns an object representing a list of linkages of
     * molecular sequences mentioned in the paper to their
     * accession numbers in a given data bank.  This method
     * may return <code>null</code> if there are no linkages.
     *
     * @return The data bank linkage list for this article.
     */
    public DataBankList dataBankList() {
        return mDataBankList;
    }

    /**
     * Returns the list of agencies and grant identifiers sponsoring
     * the work reporting in this article.  This method may return
     * <code>null</code> if no grants are provided.
     *
     * @return The grant list for this
     */
    public GrantList grantList() {
        return mGrantList;
    }

    /**
     * Returns a complete list of publication types for this article
     * in alphabetical order.  A full list of publication types
     * may be found at:
     *
     * <blockquote>
     * <a href="http://www.ncbi.nlm.nih.gov/entrez/query/static/help/pmhelp.html#PublicationTypes">PubMed Help: Publication Types</a>
     * </blockquote>
     *
     * @return The complete list of publication types for this article.
     */
    public String[] publicationTypes() {
        return mPublicationTypes;
    }

    /**
     * Returns the title of the article if it was originally published
     * in a language other than English.  All vernacular titles are
     * transliterated into Roman characters.  May be the empty string
     * if there is no vernacular title.
     *
     * @return The original language title for this article.
     */
    public String vernacularTitle() {
        return mVernacularTitle;
    }

    /**
     * Returns the date on which the publisher released an electronic
     * version of this article.  This method may return <code>null</code>
     * if there was no electronic publication date supplied by the
     * publisher or if the article was never published electronically.
     *
     * @return The date on which the publisher produced an electronic
     * version of this article.
     */
    public ArticleDate articleDate() {
        return mArticleDate;
    }

    /**
     * Returns a string representation of this article.
     *
     * @return A string representation of this article.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Title=");
        sb.append(articleTitle());
        sb.append(" Authors=");
        sb.append(authorList());
        sb.append(" Publication Model=");
        sb.append(publicationModel());
        if (inJournal()) {
            sb.append(" Journal=" + journal());
        } else {
            sb.append(" Book=" + book());
        }
        if (pagination().length() > 0) {
            sb.append(" Pagination=" + pagination());
        }
        if (abstrct() != null && abstrct().text().length() > 0) {
            sb.append(" Abstract=");
            sb.append(abstrct());
        }
        if (affiliation().length() > 0) {
            sb.append(" Affiliation=");
            sb.append(affiliation());
        }
        sb.append(" Languages=");
        sb.append(Arrays.asList(languages()));
        if (dataBankList() != null) {
            sb.append(" Data Banks=");
            sb.append(dataBankList());
        }
        if (grantList() != null) {
            sb.append(" Grant List=");
            sb.append(grantList());
        }
        if (publicationTypes().length > 0) {
            sb.append(" Publication Types=");
            sb.append(Arrays.asList(publicationTypes()));
        }
        if (vernacularTitle() != null && vernacularTitle().length() > 0) {
            sb.append(" Vernacular Title=");
            sb.append(vernacularTitle());
        }
        if (articleDate() != null) {
            sb.append(" Electronic Article Pub Date=");
            sb.append(articleDate());
        }
        sb.append('}');
        return sb.toString();
    }

    private static final String AUTHORS_TRANS_MARKER = " (author's trans)";

    // <!ELEMENT Article ((Journal | Book), %ArticleTitle.Ref;,
    //                   Pagination, Abstract?,
    //                   Affiliation?, AuthorList?, Language+, DataBankList?,
    //                   GrantList?, PublicationTypeList, VernacularTitle?,
    //                   ArticleDate?)>
    // <!ENTITY % ArticleTitle.Ref "ArticleTitle">
    // <!ELEMENT PublicationType (#PCDATA)>
    // <!ELEMENT PublicationTypeList (PublicationType+)>
    // <!ELEMENT VernacularTitle (#PCDATA)>
    static class Handler extends DelegateHandler {
        private String mPublicationModel;
        private final Journal.Handler mJournalHandler;
        private final Book.Handler mBookHandler;
        private final TextAccumulatorHandler mArticleTitleHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mMedlinePaginationHandler
            = new TextAccumulatorHandler();
        private final Abstract.Handler mAbstractHandler;
        private final TextAccumulatorHandler mAffiliationHandler
            = new TextAccumulatorHandler();
        String mPagination;
        private final ELocationId.Handler mELocationIdHandler
            =  new ELocationId.Handler();
        private final List<ELocationId> mELocationIdList
            = new ArrayList<ELocationId>();
        private final TextAccumulatorHandler mStartPageHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mEndPageHandler
            = new TextAccumulatorHandler();
        private final AuthorList.Handler mAuthorListHandler;
        private final ArrayList mLanguageList = new ArrayList();
        private final TextAccumulatorHandler mLanguageHandler
            = new TextAccumulatorHandler();
        private final DataBankList.Handler mDataBankListHandler;
        private final GrantList.Handler mGrantListHandler;
        private final ArrayList mPublicationTypeList = new ArrayList();
        private final TextAccumulatorHandler mPublicationTypeHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mVernacularTitleHandler
            = new TextAccumulatorHandler();
        private final ArticleDate.Handler mArticleDateHandler;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mJournalHandler = new Journal.Handler(delegator);
            mBookHandler = new Book.Handler(delegator);
            mAbstractHandler = new Abstract.Handler(delegator);
            mAuthorListHandler = new AuthorList.Handler(delegator);
            mDataBankListHandler = new DataBankList.Handler(delegator);
            mGrantListHandler = new GrantList.Handler(delegator);
            mArticleDateHandler = new ArticleDate.Handler(delegator);
            setDelegate(MedlineCitationSet.JOURNAL_ELT,
                        mJournalHandler);
            setDelegate(MedlineCitationSet.BOOK_ELT,
                        mBookHandler);
            setDelegate(MedlineCitationSet.ARTICLE_TITLE_ELT,
                        mArticleTitleHandler);
            setDelegate(MedlineCitationSet.MEDLINE_PAGINATION_ELT,
                        mMedlinePaginationHandler);
            setDelegate(MedlineCitationSet.E_LOCATION_ID_ELT,
                        mELocationIdHandler);
            setDelegate(MedlineCitationSet.START_PAGE_ELT,
                        mStartPageHandler);
            setDelegate(MedlineCitationSet.END_PAGE_ELT,
                        mEndPageHandler);
            setDelegate(MedlineCitationSet.ABSTRACT_ELT,
                        mAbstractHandler);
            setDelegate(MedlineCitationSet.AFFILIATION_ELT,
                        mAffiliationHandler);
            setDelegate(MedlineCitationSet.AUTHOR_LIST_ELT,
                        mAuthorListHandler);
            setDelegate(MedlineCitationSet.LANGUAGE_ELT,
                        mLanguageHandler);
            setDelegate(MedlineCitationSet.DATA_BANK_LIST_ELT,
                        mDataBankListHandler);
            setDelegate(MedlineCitationSet.GRANT_LIST_ELT,
                        mGrantListHandler);
            setDelegate(MedlineCitationSet.PUBLICATION_TYPE_ELT,
                        mPublicationTypeHandler);
            setDelegate(MedlineCitationSet.VERNACULAR_TITLE_ELT,
                        mVernacularTitleHandler);
            setDelegate(MedlineCitationSet.ELECTRONIC_PUB_DATE_ELT,
                        mArticleDateHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            mJournalHandler.reset();
            mBookHandler.reset();
            mArticleTitleHandler.reset();
            mMedlinePaginationHandler.reset();
            mPagination = null;
            mStartPageHandler.reset();
            mEndPageHandler.reset();
            mAbstractHandler.reset();
            mAffiliationHandler.reset();
            mAuthorListHandler.reset();
            mLanguageList.clear();
            mDataBankListHandler.reset();
            mGrantListHandler.reset();
            mPublicationTypeList.clear();
            mVernacularTitleHandler.reset();
            mArticleDateHandler.reset();
            mELocationIdList.clear();
            super.startDocument();
        }
        public void startElement(String x, String y, String qName,
                                 Attributes atts)
            throws SAXException {
            if (qName.equals(MedlineCitationSet.ARTICLE_ELT)) {
                mPublicationModel
                    = atts.getValue(MedlineCitationSet.PUB_MODEL_ATT);
            }
            super.startElement(x,y,qName,atts);
        }
        public Article getArticle() {
            String[] languages = new String[mLanguageList.size()];
            mLanguageList.toArray(languages);
            String[] publicationTypes
                = new String[mPublicationTypeList.size()];
            mPublicationTypeList.toArray(publicationTypes);
            ELocationId[] eLocationIds = new ELocationId[mELocationIdList.size()];
            mELocationIdList.<ELocationId>toArray(eLocationIds);
            return new Article(mPublicationModel,
                               mJournalHandler.getJournal(),
                               mBookHandler.getBook(),
                               mArticleTitleHandler.getText(),
                               mPagination,
                               eLocationIds,
                               mAbstractHandler.getAbstract(),
                               mAffiliationHandler.getText(),
                               mAuthorListHandler.getAuthorList(),
                               languages,
                               mDataBankListHandler.getDataBankList(),
                               mGrantListHandler.getGrantList(),
                               publicationTypes,
                               mVernacularTitleHandler.getText(),
                               mArticleDateHandler.getArticleDate());
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.MEDLINE_PAGINATION_ELT)) {
                mPagination = mMedlinePaginationHandler.getText();
            } else if (qName.equals(MedlineCitationSet.START_PAGE_ELT)) {
                mPagination = mStartPageHandler.getText();
            } else if (qName.equals(MedlineCitationSet.END_PAGE_ELT)) {
                if (mPagination.length() > 0)
                    mPagination = mPagination + "-" + mEndPageHandler.getText();
                else
                    mPagination = mEndPageHandler.getText();
            } else if (qName.equals(MedlineCitationSet.LANGUAGE_ELT)) {
                mLanguageList.add(mLanguageHandler.getText());
            } else if (qName.equals(MedlineCitationSet.PUBLICATION_TYPE_ELT)) {
                mPublicationTypeList.add(mPublicationTypeHandler.getText());
            } else if (qName.equals(MedlineCitationSet.E_LOCATION_ID_ELT)) {
                mELocationIdList.add(mELocationIdHandler.getELocationId());
            }
        }
    }

}
