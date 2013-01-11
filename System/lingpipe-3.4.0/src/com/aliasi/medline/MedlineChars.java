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

import java.util.Arrays;

/**
 * The <code>MedlineChars</code> class contains static methods for
 * handling characters in MEDLINE.  All valid characters may be
 * retrieved as an array, or characters may be tested for validity
 * using the {@link #isValid(char)} method.
 *
 * <P>The list of valid characters is as defined at:
 *
 * <blockquote>
 *  <a href="http://www.nlm.nih.gov/databases/dtd/medline_character_database.utf8">NLM MEDLINE Character Database</a>
 * </blockquote>
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class MedlineChars {

    // disallow instances
    private MedlineChars() { 
        /* does nothing */
    }

    /**
     * Array containing the complete set of characters used in text
     * content in MEDLINE.  See the class documentation for source
     * information.
     */
    public static final char[] VALID_CHARS
        = new char[] {
            (char) 0x020, // SPACE
            (char) 0x021, // EXCLAMATION MARK
            (char) 0x022, // QUOTATION MARK
            (char) 0x023, // NUMBER SIGN
            (char) 0x024, // DOLLAR SIGN
            (char) 0x025, // PERCENT SIGN
            (char) 0x026, // AMPERSAND
            (char) 0x027, //APOSTROPHE
            (char) 0x028, // LEFT PARENTHESIS
            (char) 0x029, // RIGHT PARENTHESIS
            (char) 0x02A, // ASTERISK
            (char) 0x02B, // PLUS SIGN
            (char) 0x02C, // COMMA
            (char) 0x02D, // HYPHEN-MINUS
            (char) 0x02E, // FULL STOP
            (char) 0x02F, // SOLIDUS
            (char) 0x030, // DIGIT ZERO
            (char) 0x031, // DIGIT ONE
            (char) 0x032, // DIGIT TWO
            (char) 0x033, // DIGIT THREE
            (char) 0x034, // DIGIT FOUR
            (char) 0x035, // DIGIT FIVE
            (char) 0x036, // DIGIT SIX
            (char) 0x037, // DIGIT SEVEN
            (char) 0x038, // DIGIT EIGHT
            (char) 0x039, // DIGIT NINE
            (char) 0x03A, // COLON
            (char) 0x03B, // SEMICOLON
            (char) 0x03C, // LESS-THAN SIGN
            (char) 0x03D, // EQUALS SIGN
            (char) 0x03E, // GREATER-THAN SIGN
            (char) 0x03F, // QUESTION MARK
            (char) 0x040, // COMMERCIAL AT
            (char) 0x041, // LATIN CAPITAL LETTER A
            (char) 0x042, // LATIN CAPITAL LETTER B
            (char) 0x043, // LATIN CAPITAL LETTER C
            (char) 0x044, // LATIN CAPITAL LETTER D
            (char) 0x045, // LATIN CAPITAL LETTER E
            (char) 0x046, // LATIN CAPITAL LETTER F
            (char) 0x047, // LATIN CAPITAL LETTER G
            (char) 0x048, // LATIN CAPITAL LETTER H
            (char) 0x049, // LATIN CAPITAL LETTER I
            (char) 0x04A, // LATIN CAPITAL LETTER J
            (char) 0x04B, // LATIN CAPITAL LETTER K
            (char) 0x04C, // LATIN CAPITAL LETTER L
            (char) 0x04D, // LATIN CAPITAL LETTER M
            (char) 0x04E, // LATIN CAPITAL LETTER N
            (char) 0x04F, // LATIN CAPITAL LETTER O
            (char) 0x050, // LATIN CAPITAL LETTER P
            (char) 0x051, // LATIN CAPITAL LETTER Q
            (char) 0x052, // LATIN CAPITAL LETTER R
            (char) 0x053, // LATIN CAPITAL LETTER S
            (char) 0x054, // LATIN CAPITAL LETTER T
            (char) 0x055, // LATIN CAPITAL LETTER U
            (char) 0x056, // LATIN CAPITAL LETTER V
            (char) 0x057, // LATIN CAPITAL LETTER W
            (char) 0x058, // LATIN CAPITAL LETTER X
            (char) 0x059, // LATIN CAPITAL LETTER Y
            (char) 0x05A, // LATIN CAPITAL LETTER Z
            (char) 0x05B, // LEFT SQUARE BRACKET
            (char) 0x05C, // REVERSE SOLIDUS
            (char) 0x05D, // RIGHT SQUARE BRACKET
            (char) 0x05F, // LOW LINE
            (char) 0x061, // LATIN SMALL LETTER A
            (char) 0x062, // LATIN SMALL LETTER B
            (char) 0x063, // LATIN SMALL LETTER C
            (char) 0x064, // LATIN SMALL LETTER D
            (char) 0x065, // LATIN SMALL LETTER E
            (char) 0x066, // LATIN SMALL LETTER F
            (char) 0x067, // LATIN SMALL LETTER G
            (char) 0x068, // LATIN SMALL LETTER H
            (char) 0x069, // LATIN SMALL LETTER I
            (char) 0x06A, // LATIN SMALL LETTER J
            (char) 0x06B, // LATIN SMALL LETTER K
            (char) 0x06C, // LATIN SMALL LETTER L
            (char) 0x06D, // LATIN SMALL LETTER M
            (char) 0x06E, // LATIN SMALL LETTER N
            (char) 0x06F, // LATIN SMALL LETTER O
            (char) 0x070, // LATIN SMALL LETTER P
            (char) 0x071, // LATIN SMALL LETTER Q
            (char) 0x072, // LATIN SMALL LETTER R
            (char) 0x073, // LATIN SMALL LETTER S
            (char) 0x074, // LATIN SMALL LETTER T
            (char) 0x075, // LATIN SMALL LETTER U
            (char) 0x076, // LATIN SMALL LETTER V
            (char) 0x077, // LATIN SMALL LETTER W
            (char) 0x078, // LATIN SMALL LETTER X
            (char) 0x079, // LATIN SMALL LETTER Y
            (char) 0x07A, // LATIN SMALL LETTER Z
            (char) 0x07C, // VERTICAL BAR (FILL) / VERTICAL LINE
            (char) 0x07E, // TILDE
            (char) 0x0BF, // INVERTED QUESTION MARK
            (char) 0x0D8, // LATIN CAPITAL LETTER O WITH STROKE
            (char) 0x0E0, // LATIN SMALL LETTER A WITH GRAVE
            (char) 0x0E1, // LATIN SMALL LETTER A WITH ACUTE
            (char) 0x0E2, // LATIN SMALL LETTER A WITH CIRCUMFLEX
            (char) 0x0E3, // LATIN SMALL LETTER A WITH TILDE
            (char) 0x0E4, // LATIN SMALL LETTER A WITH DIAERESIS
            (char) 0x0E5, // LATIN SMALL LETTER A WITH RING ABOVE
            (char) 0x0E7, // LATIN SMALL LETTER C WITH CEDILLA
            (char) 0x0E8, // LATIN SMALL LETTER E WITH GRAVE
            (char) 0x0E9, // LATIN SMALL LETTER E WITH ACUTE
            (char) 0x0EA, // LATIN SMALL LETTER E WITH CIRCUMFLEX
            (char) 0x0EB, // LATIN SMALL LETTER E WITH DIAERESIS
            (char) 0x0EC, // LATIN SMALL LETTER I WITH GRAVE
            (char) 0x0ED, // LATIN SMALL LETTER I WITH ACUTE
            (char) 0x0EE, // LATIN SMALL LETTER I WITH CIRCUMFLEX
            (char) 0x0EF, // LATIN SMALL LETTER I WITH DIAERESIS
            (char) 0x0F1, // LATIN SMALL LETTER N WITH TILDE
            (char) 0x0F2, // LATIN SMALL LETTER O WITH GRAVE
            (char) 0x0F3, // LATIN SMALL LETTER O WITH ACUTE
            (char) 0x0F4, // LATIN SMALL LETTER O WITH CIRCUMFLEX
            (char) 0x0F5, // LATIN SMALL LETTER O WITH TILDE
            (char) 0x0F6, // LATIN SMALL LETTER O WITH DIAERESIS
            (char) 0x0F8, // LATIN SMALL LETTER O WITH STROKE
            (char) 0x0F9, // LATIN SMALL LETTER U WITH GRAVE
            (char) 0x0FA, // LATIN SMALL LETTER U WITH ACUTE
            (char) 0x0FB, // LATIN SMALL LETTER U WITH CIRCUMFLEX
            (char) 0x0FC, // LATIN SMALL LETTER U WITH DIAERESIS
            (char) 0x0FD, // LATIN SMALL LETTER Y WITH ACUTE
            (char) 0x0FF, // LATIN SMALL LETTER Y WITH DIAERESIS
            (char) 0x0101, // LATIN SMALL LETTER A WITH MACRON
            (char) 0x0103, // LATIN SMALL LETTER A WITH BREVE
            (char) 0x0107, // LATIN SMALL LETTER C WITH ACUTE
            (char) 0x0109, // LATIN SMALL LETTER C WITH CIRCUMFLEX
            (char) 0x0113, // LATIN SMALL LETTER E WITH MACRON
            (char) 0x0115, // LATIN SMALL LETTER E WITH BREVE
            (char) 0x011D, // LATIN SMALL LETTER G WITH CIRCUMFLEX
            (char) 0x011F, // LATIN SMALL LETTER G WITH BREVE
            (char) 0x0123, // LATIN SMALL LETTER G WITH CEDILLA
            (char) 0x0125, // LATIN SMALL LETTER H WITH CIRCUMFLEX
            (char) 0x0129, // LATIN SMALL LETTER I WITH TILDE
            (char) 0x012B, // LATIN SMALL LETTER I WITH MACRON
            (char) 0x012D, // LATIN SMALL LETTER I WITH BREVE
            (char) 0x0135, // LATIN SMALL LETTER J WITH CIRCUMFLEX
            (char) 0x0137, // LATIN SMALL LETTER K WITH CEDILLA
            (char) 0x013A, // LATIN SMALL LETTER L WITH ACUTE
            (char) 0x013C, // LATIN SMALL LETTER L WITH CEDILLA
            (char) 0x0141, // LATIN CAPITAL LETTER L WITH STROKE
            (char) 0x0142, // LATIN SMALL LETTER L WITH STROKE
            (char) 0x0144, // LATIN SMALL LETTER N WITH ACUTE
            (char) 0x0146, // LATIN SMALL LETTER N WITH CEDILLA
            (char) 0x014D, // LATIN SMALL LETTER O WITH MACRON
            (char) 0x014F, // LATIN SMALL LETTER O WITH BREVE
            (char) 0x0155, // LATIN SMALL LETTER R WITH ACUTE
            (char) 0x0157, // LATIN SMALL LETTER R WITH CEDILLA
            (char) 0x015B, // LATIN SMALL LETTER S WITH ACUTE
            (char) 0x015D, // LATIN SMALL LETTER S WITH CIRCUMFLEX
            (char) 0x015F, // LATIN SMALL LETTER S WITH CEDILLA
            (char) 0x0163, // LATIN SMALL LETTER T WITH CEDILLA
            (char) 0x0169, // LATIN SMALL LETTER U WITH TILDE
            (char) 0x016B, // LATIN SMALL LETTER U WITH MACRON
            (char) 0x016D, // LATIN SMALL LETTER U WITH BREVE
            (char) 0x016F, // LATIN SMALL LETTER U WITH RING ABOVE
            (char) 0x0175, // LATIN SMALL LETTER W WITH CIRCUMFLEX
            (char) 0x0177, // LATIN SMALL LETTER Y WITH CIRCUMFLEX
            (char) 0x017A, // LATIN SMALL LETTER Z WITH ACUTE
            (char) 0x01E81, // LATIN SMALL LETTER W WITH GRAVE
            (char) 0x01E83, // LATIN SMALL LETTER W WITH ACUTE
            (char) 0x01E85, // LATIN SMALL LETTER W WITH DIAERESIS
            (char) 0x01EF3, // LATIN SMALL LETTER Y WITH GRAVE
        };

    /**
     * The number of characters used in MEDLINE text content.
     */
    public static final int NUM_CHARS = VALID_CHARS.length;


    /**
     * Returns <code>true</code> if the specified character may
     * appear as part of text content in a MEDLINE record.
     *
     * @param c Character to test for validity.
     * @return <code>true</code> if the specified character may
     * appear as part of text content in a MEDLINE record.
     */
    public static boolean isValid(char c) {
        return Arrays.binarySearch(VALID_CHARS,c) >= 0;
    }

}
