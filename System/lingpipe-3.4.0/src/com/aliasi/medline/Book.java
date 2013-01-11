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

import org.xml.sax.SAXException;

/**
 * A <code>Book</code> contains a representation of a book in which an
 * article is published.  Information on title, authors collection
 * title, volume, and publication date is available.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class Book {

    private final PubDate mPubDate;
    private final String mPublisher;
    private final String mTitle;
    private final AuthorList mAuthorList;
    private final String mCollectionTitle;
    private final String mVolume;

    Book(PubDate pubDate,
         String publisher,
         String title,
         AuthorList authorList,
         String collectionTitle,
         String volume) {

        mPubDate = pubDate;
        mPublisher = publisher;
        mTitle = title;
        mAuthorList = authorList;
        mCollectionTitle = collectionTitle;
        mVolume = volume;
    }

    /**
     * Returns the publication date for this book, or <code>null</code>
     * if none was provided in the record.
     *
     * @return The publication date for this book.
     */
    public PubDate pubDate() {
        return mPubDate;
    }

    /**
     * Returns the publisher of this book.  The publisher is a
     * required element of a record, so this method should always
     * return a meaningful value.
     *
     * @return The publisher of this book.
     */
    public String publisher() {
        return mPublisher;
    }

    /**
     * Returns the title of this book.  The title is a required
     * element, so this method should always return a meaningful
     * value.
     *
     * @return The title of this book.
     */
    public String title() {
        return mTitle;
    }

    /**
     * Return the list of authors for this book or <code>null</code>
     * if none was supplied in the record.
     *
     * @return The list of authors for this book.
     */
    public AuthorList authorList() {
        return mAuthorList;
    }

    /**
     * Return the collection title for this book, or the empty
     * string if none was supplied.
     *
     * @return The collection title for this book.
     */
    public String collectionTitle() {
        return mCollectionTitle;
    }

    /**
     * Returns a string-based representation of the volume number
     * for this book, or the empty string if none was supplied.
     *
     * @return The volume information for this book.
     */
    public String volume() {
        return mVolume;
    }

    // <!ELEMENT Book (%PubDate.Ref;, Publisher, Title, AuthorList?,
    //                CollectionTitle?, Volume?)>
    // <!ENTITY % PubDate.Ref "PubDate?">
    static class Handler extends DelegateHandler {
        private boolean mFound = false;
        private final PubDate.Handler mPubDateHandler;
        private final TextAccumulatorHandler mPublisherHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mTitleHandler
            = new TextAccumulatorHandler();
        private final AuthorList.Handler mAuthorListHandler;
        private final TextAccumulatorHandler mCollectionTitleHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mVolumeHandler
            = new TextAccumulatorHandler();
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mPubDateHandler = new PubDate.Handler(delegator);
            mAuthorListHandler = new AuthorList.Handler(delegator);
            setDelegate(MedlineCitationSet.PUB_DATE_ELT,
                        mPubDateHandler);
            setDelegate(MedlineCitationSet.PUBLISHER_ELT,
                        mPublisherHandler);
            setDelegate(MedlineCitationSet.TITLE_ELT,
                        mTitleHandler);
            setDelegate(MedlineCitationSet.AUTHOR_LIST_ELT,
                        mAuthorListHandler);
            setDelegate(MedlineCitationSet.COLLECTION_TITLE_ELT,
                        mCollectionTitleHandler);
            setDelegate(MedlineCitationSet.VOLUME_ELT,
                        mVolumeHandler);
        }
        public Book getBook() {
            if (!mFound) return null;
            return new Book(mPubDateHandler.getPubDate(),
                            mPublisherHandler.getText(),
                            mTitleHandler.getText(),
                            mAuthorListHandler.getAuthorList(),
                            mCollectionTitleHandler.getText(),
                            mVolumeHandler.getText());
        }
        public void reset() {
            mFound = false;
            mPubDateHandler.reset();
            mPublisherHandler.reset();
            mTitleHandler.reset();
            mCollectionTitleHandler.reset();
            mVolumeHandler.reset();
            mAuthorListHandler.reset();
        }
        public boolean found() {
            return mFound;
        }
        public void startDocument() throws SAXException {
            reset();
            super.startDocument();
        }
        public void endDocument() throws SAXException {
            super.endDocument();
            mFound = true;
        }
    }

}
