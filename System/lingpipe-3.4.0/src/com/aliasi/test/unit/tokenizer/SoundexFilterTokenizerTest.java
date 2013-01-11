package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.SoundexFilterTokenizer;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.test.unit.BaseTestCase;

public class SoundexFilterTokenizerTest extends BaseTestCase {

    public void testSoundex() {
        assertSoundex("","0000");
        assertSoundex("1","0000");
        assertSoundex("34","0000");
        assertSoundex("#$%^&*(","0000");

        // from census
        assertSoundex("Gutierrez","G362");
        assertSoundex("Pfister","P236");
        assertSoundex("Jackson","J250");
        assertSoundex("Tymczak","T522");
        assertSoundex("VanDeusen","V532");
        assertSoundex("Ashcraft","A261");

        // from Wikipedia
        assertSoundex("Robert","R163");
        assertSoundex("Rupert","R163");
        assertSoundex("Rubin","R150");

        // from Knuth
        assertSoundex("Euler","E460");
        assertSoundex("Gauss","G200");
        assertSoundex("Hilbert","H416");
        assertSoundex("Knuth","K530");
        assertSoundex("Lloyd","L300");
        assertSoundex("Lukasiewicz","L222");  // Dark L out of Latin1!
        assertSoundex("Wachs","W200");

        assertSoundex("Ellery","E460");
        assertSoundex("Ghosh","G200");
        assertSoundex("Heilbronn","H416");
        assertSoundex("Kant","K530");
        assertSoundex("Liddy","L300");
        assertSoundex("Lissajous","L222");
        assertSoundex("Waugh","W200");
    }

    void assertSoundex(String in, String soundexToken) {
        assertEquals("in=" + in
                     + " soundex=" + soundexToken
                     + " found="
                     + SoundexFilterTokenizer.soundexEncoding(in),
                     soundexToken,
                     SoundexFilterTokenizer.soundexEncoding(in));
    }
    void assertSoundex(String in, String[] soundexTokens) {
        char[] cs = in.toCharArray();
        Tokenizer tokenizer
            = IndoEuropeanTokenizerFactory
            .FACTORY
            .tokenizer(cs,0,cs.length);
        SoundexFilterTokenizer soundexTokenizer
            = new SoundexFilterTokenizer(tokenizer);
        String[] tokens = soundexTokenizer.tokenize();
        assertEqualsArray(soundexTokens,tokens);
    }

}