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
 * A <code>PubDate</code> represents a publication date in a
 * semi-structured or unstructured format.  Publication dates may
 * appear in structured form if they can be broken down into year with
 * an optional season, optional month, and optional day.  Some journal
 * publication dates cover date ranges, etc.  In these cases, dates
 * are presented as plain text.  Examples that are not structured in
 * MEDLINE are (<i>e.g.</i> <code>1998 Dec-1999 Jan</code> <i>or</i>
 * <code>2000 Nov-Dec</code> <i>or</i> <code>2000
 * Spring-Summer</code>).
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class PubDate {

    private final String mYear;
    private final String mMonth;
    private final String mDay;
    private final String mSeason;
    private final String mMedlineDate;

    PubDate(String year, String season,
            String month, String day,
            String medlineDate) {
        mYear = year;
        mSeason = season;
        mMonth = month;
        mDay = day;
        mMedlineDate = medlineDate;
    }

    /**
     * Returns <code>true</code> if this date was provided with year,
     * season, month and/or day structure.  If the date is not
     * structured, the method {@link #medlineDate()} will return the
     * unstructured text.  Otherwise, the year, season, month and day
     * accessor methods should be queried.
     *
     * @return <code>true</code> if this date is structured.
     */
    public boolean isStructured() {
        return mMedlineDate == null
            || mMedlineDate.length() == 0;
    }

    /**
     * Returns a string representing an unstructured date if
     * the date is not structured and returns an empty (zero length)
     * string otherwise. This method will return a non-empty
     * value if {@link #isStructured()} returns <code>true</code>.
     *
     * @return The unstructured date as a string.
     */
    public String medlineDate() {
        return mMedlineDate;
    }

    /**
     * Returns the year for structured dates, and the empty (zero
     * length) string otherwise.  This method will return a non-empty
     * result for structured dates. The method {@link #isStructured()}
     * can be used to test if this date is structured.
     *
     * @return The year for this date.
     */
    public String year() {
        return mYear;
    }

    /**
     * Returns the season for this date, or the empty (zero length)
     * string if none was specified.  A structured date optionally
     * specifies a season.
     *
     * @return The season for this date.
     */
    public String season() {
        return mSeason;
    }

    /**
     * Returns the month for this date, or the empty (zero length)
     * string if none was specified.  A structured date optionally
     * specifies a month.
     *
     * @return The month for this date.
     */
    public String month() {
        return mMonth;
    }


    /**
     * Returns the day for this date, or the empty (zero length)
     * string if none was specified.  A structured date optionally
     * specifies a day.
     *
     * @return The day for this date.
     */
    public String day() {
        return mDay;
    }

    /**
     * Returns a plain string representation of this publication date
     * without field information.  The format returned is either a
     * MEDLINE date or <code>(Day ' ') (Month ' ') (Season ' ')
     * Year</code>.
     *
     * @return Plain string representation of date.
     */
    public String toPlainString() {
        if (mMedlineDate.length() > 0)
            return mMedlineDate;
        StringBuffer sb = new StringBuffer();
        if (mDay.length() > 0)
            sb.append(mDay + " ");
        if (mMonth.length() > 0)
            sb.append(mMonth + " ");
        if (mSeason.length() > 0)
            sb.append(mSeason + " ");
        sb.append(mYear);
        return sb.toString();
    }

    /**
     * Returns a string-based representation of this publication
     * date.
     *
     * @return A string-based representation of this publication date.
     */
    public String toString() {
        if (mMedlineDate.length() > 0) {
            return '{'
                + "MedlineDate=" + mMedlineDate
                +'}';
        }
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Year=");
        sb.append(mYear);
        if (mSeason.length() > 0) {
            sb.append(" Season=");
            sb.append(mSeason);
        }
        if (mMonth.length() > 0) {
            sb.append(" Month=");
            sb.append(mMonth);
        }
        if (mDay.length() > 0) {
            sb.append(" Day=");
            sb.append(mDay);
        }
        sb.append('}');
        return sb.toString();
    }

    // <!ENTITY % pub.date "((Year, ((Month, Day?) | Season)?)
    //                      | MedlineDate)">
    static class Handler extends DelegateHandler {
        private final TextAccumulatorHandler mMedlineDateHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mYearHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mSeasonHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mMonthHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mDayHandler
            = new TextAccumulatorHandler();
        private boolean mVisited = false;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.YEAR_ELT,mYearHandler);
            setDelegate(MedlineCitationSet.SEASON_ELT,mSeasonHandler);
            setDelegate(MedlineCitationSet.MONTH_ELT,mMonthHandler);
            setDelegate(MedlineCitationSet.DAY_ELT,mDayHandler);
            setDelegate(MedlineCitationSet.MEDLINE_DATE_ELT,mMedlineDateHandler);
        }
        public void reset() {
            mYearHandler.reset();
            mSeasonHandler.reset();
            mMonthHandler.reset();
            mDayHandler.reset();
            mMedlineDateHandler.reset();
            mVisited = false;
        }
        public void startDocument() throws SAXException {
            reset();
            mVisited = true;
            super.startDocument();
        }
        public PubDate getPubDate() {
            if (!mVisited) return null;
            return new PubDate(mYearHandler.getText(),
                               mSeasonHandler.getText(),
                               mMonthHandler.getText(),
                               mDayHandler.getText(),
                               mMedlineDateHandler.getText());
        }
    }
}
