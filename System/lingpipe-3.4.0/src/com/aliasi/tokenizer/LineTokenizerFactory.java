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

package com.aliasi.tokenizer;

/**
 * A <code>LineTokenizerFactory</code> treats each line of an input as
 * a token.  Whitespaces separating lines are simply newlines.  This
 * is useful for decoders that work at the line level.
 *
 * <p>Line terminators are as defined in {@link java.util.regex.Pattern},
 * and include all of the Windows, Unix, and Macintosh standards, as well
 * as some unicode extensions.
 *
 * <p>Whitespaces will be either empty strings or strings representing
 * one or more newlines.
 *
 * <p>Tokens may consist entirely of whitespace characters if
 * whitespace is the only thing on a line.  But tokens will never contain
 * sequences representing newlines.  Tokens will alwyas consist of at
 * least one character.
 *
 * <h3>Examples</h3>
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><th>Input String</th><th>Tokens</th><th>Whitespaces</th></tr>
 * <tr><td><code>&quot;&quot;</code></td><td><code>{}</code></td><td><code>{ &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot;abc&quot;</code></td><td><code>{ &quot;abc&quot; }</code></td><td><code>{ &quot;&quot;, &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot;abc\ndef&quot;</code></td><td><code>{ &quot;abc&quot;, &quot;def&quot; }</code></td><td><code>{ &quot;&quot;, &quot;\n&quot;, &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot;abc\r\ndef&quot;</code></td><td><code>{ &quot;abc&quot;, &quot;def&quot; }</code></td><td><code>{ &quot;&quot;, &quot;\r\n&quot;, &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot;abc\r\ndef&quot;</code></td><td><code>{ &quot;abc&quot;, &quot;def&quot; }</code></td><td><code>{ &quot;&quot;, &quot;\r\n&quot;, &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot; abc\n def \n&quot;</code></td><td><code>{ &quot; abc&quot;, &quot; def &quot; }</code></td><td><code>{ &quot;&quot;, &quot;\n&quot;, &quot;\n&quot; }</code></td></tr>
 * <tr><td><code>&quot;  \n&quot;</code></td><td><code>{ &quot;  &quot; }</code></td><td><code>{ &quot;&quot;, &quot;\n&quot; }</code></td></tr>
 * </table>
 * </blockquote>
 *
 * <h3>Compilation</h3>
 *
 * <p>A line tokenizer factory may be compiled.  Upon deserialization,
 * the resulting class will be an instance of
 * {@link RegExTokenizerFactory}.  In future versions, the
 * deserialized class may change, so it is safest to simply cast it
 * to the interface {@link TokenizerFactory}.
 *
 * <h3>Implementation Note</h3>
 *
 * <p>This tokenizer factory is nothing more than a convenience
 * wrapper around a very simple {@link RegExTokenizerFactory}, with
 * the simplest possible regular expression:
 *
 * <pre>
 *      RegExTokenizerFactory(&quot;.+&quot;)</pre>
 *
 * <p>Because the regular expression tokenizer factory takes the
 * default regular expression flags (see {@link java.util.regex.Pattern}),
 * the period (<code>.</code>) matches any character except a newline.
 *
 * @author  Bob Carpenter
 * @version 3.2
 * @since   LingPipe3.2
 */
public class LineTokenizerFactory extends RegExTokenizerFactory {

    /**
     * Construct a line-based tokenizer.  See the class documentation
     * above for a description of behavior.
     */
    public LineTokenizerFactory() {
        super(".+");
    }

}