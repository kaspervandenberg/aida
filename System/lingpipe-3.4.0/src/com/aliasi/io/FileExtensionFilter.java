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

/**
 * A file filter that accepts all base files with a suffix with a
 * specified value or one of a set of specified values.  The
 * extensions are specified in the constructor, and the filter may be
 * set to accept or reject directories.  A file is tested by name
 * against {@link String#endsWith(String)}.
 *
 * @author Bob Carpenter
 * @version 2.0
 * @since   LingPipe1.0
 */
public class FileExtensionFilter extends FileNameFilter {

    /** 
     * The extensions matched by this filter.
     */
    private final String[] mExtensions;

    /**
     * Create a file filter that accepts files with the specified
     * extensions, optionally accepting directories.
     *
     * @param extensions Suffixes of files to accept.
     * @param acceptDirectories <code>true</code> if all directories
     * should be accepted by this filter.
     */
    public FileExtensionFilter(String[] extensions,
                               boolean acceptDirectories) {
        super(acceptDirectories);
        mExtensions = new String[extensions.length];
	for (int i = 0; i < extensions.length; ++i)
	    mExtensions[i] = extensions[i];
    }

    /**
     * Create a file filter that accepts files with the specified
     * extension, optionally accepting directories.
     *
     * @param extension Suffix of files to accept.
     * @param acceptDirectories <code>true</code> if all directories
     * should be accepted by this filter.
     */
    public FileExtensionFilter(String extension,
                               boolean acceptDirectories) {
        this(new String[] { extension },
             acceptDirectories);
    }


    /**
     * Create a file filter that accepts files with the specified
     * extension and accepts all directories.
     *
     * @param extension Suffix of files to accept.
     */
    public FileExtensionFilter(String extension) {
        this(extension,true);
    }

    /**
     * Create a file filter that accepts files with the specified
     * extensions and accepts all directories.
     *
     * @param extensions Suffixes of files to accept.
     */
    public FileExtensionFilter(String[] extensions) {
        this(extensions,true);
    }

    /**
     * Returns <code>true</code> if the specified file name has
     * an acceptable suffix as specified in the constructor.
     *
     * @param fileName Name of file to test for acceptance.
     * @return <code>true</code> If file name is acceptable.
     */
    protected final boolean accept(String fileName) {
	for (int i = 0; i < mExtensions.length; ++i)
	    if (fileName.endsWith(mExtensions[i])) return true;
	return false;
    }

}
