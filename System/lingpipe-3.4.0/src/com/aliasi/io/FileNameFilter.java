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

package com.aliasi.io;

import java.io.File;
import java.io.FileFilter;

/**
 * An abstract file filter that accepts files based on the filename.
 * May be configured to accept all directories.  Concrete subclasses
 * must implement {@link #accept(String)}.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public abstract class FileNameFilter implements FileFilter {

    /**
     * <code>true</code> if all directories are accepted by the filter.
     */
    private final boolean mAcceptDirectories;

    /**
     * Construct a file name filter that accepts directories.
     */
    public FileNameFilter() {
        this(true);
    }

    /**
     * Construct a file name filter that accepts directories as
     * specified.
     *
     * @param acceptDirectories <code>true</code> if this filter
     * should accept all directories.
     */
    public FileNameFilter(boolean acceptDirectories) {
        mAcceptDirectories = acceptDirectories;
    }

    /**
     * Return <code>true</code> if a file is acceptable to this
     * filter.  A file is accepted if it is a directory and this
     * filter accepts all directories, or if it is a regular file that
     * is accepted by {@link #accept(String)}.
     *
     * @param file File to test for acceptance.
     * @return <code>true</code> if file name is acceptable.
     */
    public final boolean accept(File file) {
        return (mAcceptDirectories && file.isDirectory())
            || accept(file.getName());
    }

    /**
     * Return <code>true</code> if files with the specified
     * name should be accepted.
     *
     * @param fileName Name of file to test for acceptance.
     * @return <code>true</code> if file name is acceptable.
     */
    protected abstract boolean accept(String fileName);

}
