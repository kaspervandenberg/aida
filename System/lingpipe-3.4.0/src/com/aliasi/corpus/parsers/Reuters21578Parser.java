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

package com.aliasi.corpus.parsers;

import com.aliasi.classify.BinaryLMClassifier;
import com.aliasi.classify.Classification;

import com.aliasi.corpus.ClassificationHandler;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.StringParser;

import com.aliasi.io.FileExtensionFilter;

import java.io.File;
import java.io.IOException;

/**
 * A <code>Reuters21578Parser</code> provides a parser for the
 * Reuters-21578 text categorization test collection.  The Reuters
 * collection consists of business stories from Reuters published in
 * the 1980s.  There are a total of 123 topics, all having to do with
 * business (e.g. &quot;money-supply&quot; and &quot;earn&quot;), but
 * the count of their training documents ranges from almost 4000 down
 * to 1;  there are only 26 topics with 100 or more training documents.
 *
 * <p>The parser produces classifications for the handler that are
 * binary, relative to a specified topic.  That is, a topic such as
 * <code>earn</code> is fixed, and the classifications are binary,
 * assigning the accept category to a document that is tagged as
 * belonging to the <code>earn</code> topic in the corpus, and
 * assigning a reject category to documents not assigned to the
 * <code>earn</code> topic.  The categories are the default accept and
 * reject categories in {@link BinaryLMClassifier}, namely {@link
 * BinaryLMClassifier#DEFAULT_ACCEPT_CATEGORY} and {@link
 * BinaryLMClassifier#DEFAULT_REJECT_CATEGORY}.
 *
 *
 * <h4>Corpus Factory</h4>
 *
 * <p>With a parser, the typical usage scenario would involve setting
 * up a parser, then parsing all of the SGML documents making up the
 * corpus.  It's also possible to encapsulate this logic using a
 * higher-order interface, that of {@link Corpus}.  There is a static
 * factory method {@link #corpus(String,File)} that constructs an
 * implementation of {@link Corpus} from the Reuters collection for a
 * specified topic.  This higher-order interface is necessary useful
 * for batch learning algorithms that require corpora input, like the
 * perceptron classifier.
 *
 * <h4>Available Topics</h4>
 *
 * <p>Here is the complete list of topics found in the Reuters corpus.
 * This is just the result of scraping the topics, not necessarily the
 * count of topics in the Mod-Apte split.  The topics are drawn from
 * general subjects, economic indicators, corporate reports,
 * currencies, and commodities (including energy).  The topics are
 * described further in the following file distributed
 * with the corpus:
 *</p>
 *
 * <ul>
 * <li><code>cat-descriptions_120396.txt</code></li>
 * </ul>
 *
 * <table border="0" cellpadding="5">
 * <tr>
 *
 * <td><table style="border: 1px solid #AAA" cellpadding="2">
 * <tr><th colspan="2" align="left">Topic</th><th>Count</th></tr>
 * <tr><td>1</td><td>earn</td><td>3987</td></tr>
 * <tr><td>2</td><td>acq</td><td>2448</td></tr>
 * <tr><td>3</td><td>money</td><td>991</td></tr>
 * <tr><td>4</td><td>fx</td><td>801</td></tr>
 * <tr><td>5</td><td>crude</td><td>634</td></tr>
 * <tr><td>6</td><td>grain</td><td>628</td></tr>
 * <tr><td>7</td><td>trade</td><td>552</td></tr>
 * <tr><td>8</td><td>interest</td><td>513</td></tr>
 * <tr><td>9</td><td>wheat</td><td>306</td></tr>
 * <tr><td>10</td><td>ship</td><td>305</td></tr>
 * <tr><td>11</td><td>corn</td><td>255</td></tr>
 * <tr><td>12</td><td>oil</td><td>238</td></tr>
 * <tr><td>13</td><td>dlr</td><td>217</td></tr>
 * <tr><td>14</td><td>gas</td><td>195</td></tr>
 * <tr><td>15</td><td>oilseed</td><td>192</td></tr>
 * <tr><td>16</td><td>supply</td><td>190</td></tr>
 * <tr><td>17</td><td>sugar</td><td>184</td></tr>
 * <tr><td>18</td><td>gnp</td><td>163</td></tr>
 * <tr><td>19</td><td>coffee</td><td>145</td></tr>
 * <tr><td>20</td><td>veg</td><td>137</td></tr>
 * <tr><td>21</td><td>gold</td><td>135</td></tr>
 * <tr><td>22</td><td>nat</td><td>130</td></tr>
 * <tr><td>23</td><td>soybean</td><td>120</td></tr>
 * <tr><td>24</td><td>bop</td><td>116</td></tr>
 * <tr><td>25</td><td>livestock</td><td>114</td></tr>
 * </table></td>

 * <td><table style="border: 1px solid #AAA" cellpadding="2">
 * <tr><th colspan="2" align="left">Topic</th><th>Count</th></tr>
 * <tr><td>26</td><td>cpi</td><td>112</td></tr>
 * <tr><td>27</td><td>reserves</td><td>84</td></tr>
 * <tr><td>28</td><td>meal</td><td>82</td></tr>
 * <tr><td>29</td><td>copper</td><td>78</td></tr>
 * <tr><td>30</td><td>cocoa</td><td>76</td></tr>
 * <tr><td>31</td><td>jobs</td><td>76</td></tr>
 * <tr><td>32</td><td>carcass</td><td>75</td></tr>
 * <tr><td>33</td><td>yen</td><td>69</td></tr>
 * <tr><td>34</td><td>iron</td><td>67</td></tr>
 * <tr><td>35</td><td>rice</td><td>67</td></tr>
 * <tr><td>36</td><td>steel</td><td>67</td></tr>
 * <tr><td>37</td><td>cotton</td><td>66</td></tr>
 * <tr><td>38</td><td>ipi</td><td>65</td></tr>
 * <tr><td>39</td><td>alum</td><td>63</td></tr>
 * <tr><td>40</td><td>barley</td><td>54</td></tr>
 * <tr><td>41</td><td>soy</td><td>52</td></tr>
 * <tr><td>42</td><td>feed</td><td>51</td></tr>
 * <tr><td>43</td><td>rubber</td><td>51</td></tr>
 * <tr><td>44</td><td>zinc</td><td>44</td></tr>
 * <tr><td>45</td><td>palm</td><td>43</td></tr>
 * <tr><td>46</td><td>chem</td><td>41</td></tr>
 * <tr><td>47</td><td>pet</td><td>41</td></tr>
 * <tr><td>48</td><td>silver</td><td>37</td></tr>
 * <tr><td>49</td><td>lead</td><td>35</td></tr>
 * <tr><td>50</td><td>rapeseed</td><td>35</td></tr>
 * </table></td>

 * <td><table style="border: 1px solid #AAA" cellpadding="2">
 * <tr><th colspan="2" align="left">Topic</th><th>Count</th></tr>
 * <tr><td>51</td><td>sorghum</td><td>35</td></tr>
 * <tr><td>52</td><td>tin</td><td>33</td></tr>
 * <tr><td>53</td><td>metal</td><td>32</td></tr>
 * <tr><td>54</td><td>strategic</td><td>32</td></tr>
 * <tr><td>55</td><td>wpi</td><td>32</td></tr>
 * <tr><td>56</td><td>orange</td><td>29</td></tr>
 * <tr><td>57</td><td>fuel</td><td>28</td></tr>
 * <tr><td>58</td><td>hog</td><td>27</td></tr>
 * <tr><td>59</td><td>retail</td><td>27</td></tr>
 * <tr><td>60</td><td>heat</td><td>25</td></tr>
 * <tr><td>61</td><td>housing</td><td>21</td></tr>
 * <tr><td>62</td><td>stg</td><td>21</td></tr>
 * <tr><td>63</td><td>income</td><td>18</td></tr>
 * <tr><td>64</td><td>lei</td><td>17</td></tr>
 * <tr><td>65</td><td>lumber</td><td>17</td></tr>
 * <tr><td>66</td><td>sunseed</td><td>17</td></tr>
 * <tr><td>67</td><td>dmk</td><td>15</td></tr>
 * <tr><td>68</td><td>tea</td><td>15</td></tr>
 * <tr><td>69</td><td>oat</td><td>14</td></tr>
 * <tr><td>70</td><td>coconut</td><td>13</td></tr>
 * <tr><td>71</td><td>cattle</td><td>12</td></tr>
 * <tr><td>72</td><td>groundnut</td><td>12</td></tr>
 * <tr><td>73</td><td>platinum</td><td>12</td></tr>
 * <tr><td>74</td><td>nickel</td><td>11</td></tr>
 * <tr><td>75</td><td>sun</td><td>10</td></tr>
 * </table></td>

 * <td><table style="border: 1px solid #AAA" cellpadding="2">
 * <tr><th colspan="2" align="left">Topic</th><th>Count</th></tr>
 * <tr><td>76</td><td>l</td><td>9</td></tr>
 * <tr><td>77</td><td>rape</td><td>9</td></tr>
 * <tr><td>78</td><td>jet</td><td>8</td></tr>
 * <tr><td>79</td><td>debt</td><td>7</td></tr>
 * <tr><td>80</td><td>instal</td><td>7</td></tr>
 * <tr><td>81</td><td>inventories</td><td>7</td></tr>
 * <tr><td>82</td><td>naphtha</td><td>7</td></tr>
 * <tr><td>83</td><td>potato</td><td>6</td></tr>
 * <tr><td>84</td><td>propane</td><td>6</td></tr>
 * <tr><td>85</td><td>austdlr</td><td>4</td></tr>
 * <tr><td>86</td><td>belly</td><td>4</td></tr>
 * <tr><td>87</td><td>cpu</td><td>4</td></tr>
 * <tr><td>88</td><td>nzdlr</td><td>4</td></tr>
 * <tr><td>89</td><td>plywood</td><td>4</td></tr>
 * <tr><td>90</td><td>pork</td><td>4</td></tr>
 * <tr><td>91</td><td>tapioca</td><td>4</td></tr>
 * <tr><td>92</td><td>cake</td><td>3</td></tr>
 * <tr><td>93</td><td>can</td><td>3</td></tr>
 * <tr><td>94</td><td>copra</td><td>3</td></tr>
 * <tr><td>95</td><td>dfl</td><td>3</td></tr>
 * <tr><td>96</td><td>f</td><td>3</td></tr>
 * <tr><td>97</td><td>lin</td><td>3</td></tr>
 * <tr><td>98</td><td>lit</td><td>3</td></tr>
 * <tr><td>99</td><td>nkr</td><td>3</td></tr>
 * <tr><td>100</td><td>palladium</td><td>3</td></tr>
 * </table></td>

 * <td valign="top"><table style="border: 1px solid #AAA" cellpadding="2">
 * <tr><th colspan="2" align="left">Topic</th><th>Count</th></tr>
 * <tr><td>101</td><td>palmkernel</td><td>3</td></tr>
 * <tr><td>102</td><td>rand</td><td>3</td></tr>
 * <tr><td>103</td><td>saudriyal</td><td>3</td></tr>
 * <tr><td>104</td><td>sfr</td><td>3</td></tr>
 * <tr><td>105</td><td>castor</td><td>2</td></tr>
 * <tr><td>106</td><td>cornglutenfeed</td><td>2</td></tr>
 * <tr><td>107</td><td>fishmeal</td><td>2</td></tr>
 * <tr><td>108</td><td>linseed</td><td>2</td></tr>
 * <tr><td>109</td><td>rye</td><td>2</td></tr>
 * <tr><td>110</td><td>wool</td><td>2</td></tr>
 * <tr><td>111</td><td>bean</td><td>1</td></tr>
 * <tr><td>112</td><td>bfr</td><td>1</td></tr>
 * <tr><td>113</td><td>castorseed</td><td>1</td></tr>
 * <tr><td>114</td><td>citruspulp</td><td>1</td></tr>
 * <tr><td>115</td><td>cottonseed</td><td>1</td></tr>
 * <tr><td>116</td><td>cruzado</td><td>1</td></tr>
 * <tr><td>117</td><td>dkr</td><td>1</td></tr>
 * <tr><td>118</td><td>hk</td><td>1</td></tr>
 * <tr><td>119</td><td>peseta</td><td>1</td></tr>
 * <tr><td>120</td><td>red</td><td>1</td></tr>
 * <tr><td>121</td><td>ringgit</td><td>1</td></tr>
 * <tr><td>122</td><td>rupiah</td><td>1</td></tr>
 * <tr><td>123</td><td>skr</td><td>1</td></tr>
 * </table></td>
 *
 * </tr></table>

 *
 * <h4>Modified Apte Split</h4>
 *
 * <p>This class distinguishes between training and test data based
 * on the encoding in the corpus itself.  Each document in the corpus
 * is marked as being either a test or training document (or neither).
 * This class parses either or both of the test and training documents
 * from the corpus based on the boolean flags provided to the constructor.
 *
 * <p>This class uses the &quot;Modified Apte&quot; (ModApte) split of
 * the corpus into training and test segments, which is defined as follows
 * (see the README cited below for more details):
 *
 * <table border="1" cellpadding="5">
 * <tr><th>Category</th><th>Number of Documents</th><th>SGML Pattern</th></tr>
 * <tr><td>Training</td>
 *     <td>9,603</td>
 *     <td><code>LEWISSPLIT="TRAIN";  TOPICS="YES"</code></td>
 * </tr>
 * <tr><td>Test</td>
 *     <td>3,299</td>
 *     <td><code>LEWISSPLIT="TEST"; TOPICS="YES"</code></td>
 * </tr>
 * <tr><td>Unused</td>
 *     <td>8,676</td>
 *     <td><code>LEWISSPLIT="NOT-USED"; TOPICS="YES"</code>
           <br /> <i>or</i> &nbsp; <code>TOPICS="NO"</code>
           <br /> <i>or</i> &nbsp; <code>TOPICS="BYPASS"</code></td>
 * </tr>
 * </table>
 *
 * <p>Note that some of the listed topics occur only in the unused
 * portion of the corpus; see the README cited below for more information.
 *
 * <h4>Corpus Organization</h4>
 *
 * <p>The corpus is distributed as 22 SGML files encoded in ASCII
 * (<code>reut2-000.sgm</code> through <code>reut2-021.sgm</code>).  It is
 * these SGML files which are parsed by this parser.
 *
 * <h4>Obtaining the Corpus</h4>
 *
 * <p>The Reuters-21578 collection may be downloaded for research
 * purposes from:
 *
 * <ul>
 * <li>David D. Lewis. <a href="http://www.daviddlewis.com/resources/testcollections/reuters21578/">Reuters-21578 Home Page</a>.</li>
 * </ul>
 *
 * It is distributed with the following read-me file, which provides
 * the (1) the exact licensing terms, (2) the format of the corpus,
 * and (3) a set of references.
 *
 * <ul>
 * <li>David D. Lewis. 2004. <a href="http://www.daviddlewis.com/resources/testcollections/reuters21578/readme.txt">Reuters-21578 README v1.3</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.2.1
 * @since   LingPipe3.2.1
 */
public class Reuters21578Parser
    extends StringParser<ClassificationHandler<CharSequence,Classification>> {

    private final boolean mIncludeTestDocuments;
    private final boolean mIncludeTrainingDocuments;
    private final String mTopic;

    /**
     * Construct a Reuters-21578 test collection parser for the
     * specified topic that includes test and/or training documents as
     * specified.  See the corpus documentation above for a description
     * of the corpus itself.
     *
     * <p>The topic specified must be available as part of the
     * Reuters classification.  If it isn't, the constructor will raise
     * an illegal argument exception.  The set of legal topics is
     * available through {@link #availableTopics()}, and a topic
     * may be tested through {@link #isAvailableTopic(String)}.
     *
     * @param topic One of the topics in the Reuters collection.
     * @param includeTrainingDocuments Set to <code>true</code> to handle
     * training documents.
     * @param includeTestDocuments Set to <code>true</code> to handle
     * test documents.
     * @throws IllegalArgumentException If the topic isn't an available
     * topic for the Reuters collection.
     */
    public Reuters21578Parser(String topic,
                              boolean includeTrainingDocuments,
                              boolean includeTestDocuments) {
        mIncludeTrainingDocuments = includeTrainingDocuments;
        mIncludeTestDocuments = includeTestDocuments;
        mTopic = topic;
        if (!isAvailableTopic(mTopic)) {
            String msg = "Require known topic."
                + " Found topic=" + topic;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Implements the parser for character array slices.  All other
     * parse methods eventually call this implementation.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in the slice.
     * @param end Index of the last character in the slice plus 1.
     */
    public void parseString(char[] cs, int start, int end) {
        String text = new String(cs,start,end-start);
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; ++i) {
            if (!lines[i].startsWith("<REUTERS")) continue;
            StringBuilder sb = new StringBuilder();
            while (!lines[i].startsWith("</REUTERS")) {
                sb.append(lines[i++]);
                sb.append("\n");
            }
            handleDocument(sb.toString());
        }
    }


    void handleDocument(String text) {
        if (!hasTopics(text)) return;
        if (isTrainingDocument(text) && !mIncludeTrainingDocuments) return;
        if (isTestDocument(text) && !mIncludeTestDocuments) return;
        String topics = extract("TOPICS",text,true);
        String title = extract("TITLE",text,true);
        String dateline = extract("DATELINE",text,true);
        String body = extract("BODY",text,true);
        if (body.endsWith(END_BOILERPLATE_1) || body.endsWith(END_BOILERPLATE_2))
            body = body.substring(0,body.length() - END_BOILERPLATE_1.length());
        StringBuilder sb = new StringBuilder();
        sb.append(title + "\n");
        sb.append(dateline + "\n");
        sb.append(body);
        boolean hasTopic = topics.indexOf(mTopic) >= 0;
        Classification classification = hasTopic ? ON_TOPIC : OFF_TOPIC;
        getHandler().handle(sb,classification);
    }

    static String extract(String elt, String text, boolean allowEmpty) {
        String startElt = "<" + elt + ">";
        String endElt = "</" + elt + ">";
        int startEltIndex = text.indexOf(startElt);
        if (startEltIndex < 0) {
            if (allowEmpty) return "";
            throw new IllegalArgumentException("no start, elt=" + elt + " text=" + text);
        }
        int start = startEltIndex + startElt.length();
        int end = text.indexOf(endElt,start);
        if (end < 0) throw new IllegalArgumentException("no end, elt=" + elt + " text=" + text);
        return text.substring(start,end);
    }


    static final Classification ON_TOPIC
        = new Classification(BinaryLMClassifier.DEFAULT_ACCEPT_CATEGORY);
    static final Classification OFF_TOPIC
        = new Classification(BinaryLMClassifier.DEFAULT_REJECT_CATEGORY);

    static final String END_BOILERPLATE_1 = "Reuter&#3;";
    static final String END_BOILERPLATE_2 = "REUTER&#3;";


    static final String[] TOPICS = {
        "acq",
        "alum",
        "austdlr",
        "barley",
        "bean",
        "belly",
        "bfr",
        "bop",
        "cake",
        "can",
        "carcass",
        "castor",
        "castorseed",
        "cattle",
        "chem",
        "citruspulp",
        "cocoa",
        "coconut",
        "coffee",
        "copper",
        "copra",
        "corn",
        "cornglutenfeed",
        "cotton",
        "cottonseed",
        "cpi",
        "cpu",
        "crude",
        "cruzado",
        "debt",
        "dfl",
        "dkr",
        "dlr",
        "dmk",
        "earn",
        "f",
        "feed",
        "fishmeal",
        "fuel",
        "fx",
        "gas",
        "gnp",
        "gold",
        "grain",
        "groundnut",
        "heat",
        "hk",
        "hog",
        "housing",
        "income",
        "instal",
        "interest",
        "inventories",
        "ipi",
        "iron",
        "jet",
        "jobs",
        "l",
        "lead",
        "lei",
        "lin",
        "linseed",
        "lit",
        "livestock",
        "lumber",
        "meal",
        "metal",
        "money",
        "naphtha",
        "nat",
        "nickel",
        "nkr",
        "nzdlr",
        "oat",
        "oil",
        "oilseed",
        "orange",
        "palladium",
        "palm",
        "palmkernel",
        "peseta",
        "pet",
        "platinum",
        "plywood",
        "pork",
        "potato",
        "propane",
        "rand",
        "rape",
        "rapeseed",
        "red",
        "reserves",
        "retail",
        "rice",
        "ringgit",
        "rubber",
        "rupiah",
        "rye",
        "saudriyal",
        "sfr",
        "ship",
        "silver",
        "skr",
        "sorghum",
        "soy",
        "soybean",
        "steel",
        "stg",
        "strategic",
        "sugar",
        "sun",
        "sunseed",
        "supply",
        "tapioca",
        "tea",
        "tin",
        "trade",
        "veg",
        "wheat",
        "wool",
        "wpi",
        "yen",
        "zinc"
    };


    /**
     * Returns an array consisting of all of the available topics in
     * the Reuters collection.  The complete list is shown in the
     * class javadoc above.
     *
     * <p>The list is a copy, so changing it has no effect on this class.
     *
     * @return The topics for the Reuters collection.
     */
    public static String[] availableTopics() {
        String[] topics = new String[TOPICS.length];
        for (int i = 0; i < topics.length; ++i)
            topics[i] = TOPICS[i];
        return topics;
    }

    /**
     * Returns <code>true</code> if the specified topic is
     * available in the Reuters collection.
     *
     * @param topic Topic to test.
     * @return <code>true</code> if it available for classification.
     */
    public static boolean isAvailableTopic(String topic) {
        for (String validTopic : TOPICS)
            if (validTopic.equals(topic))
                return true;
        return false;
    }


    /**
     * Returns the corpus representation of the Reuters collection,
     * for the specified topic, reading the SGML files from the specified
     * directory.
     *
     * <p>The directory specified is read each time the methods of the
     * returned corpus are called.  This streams the relevant parts of
     * the corpus as needed, which requires less memory, but more
     * time.  It also requires the directory to stick around until needed.
     *
     * @param topic Topic for the corpus.
     * @param directory Directory in which to find the corpus files.
     * @return The corpus for the specified topic.
     * @throws IOException If there is an underlying I/O error reading
     * the corpus data.
     * @throws IllegalArgumentException If the topic is not available
     * in the Reuters collection.
     */
    public static Corpus<ClassificationHandler<CharSequence,Classification>>
        corpus(String topic, File directory) throws IOException {

        return new ReutersCorpus(topic,directory);
    }

    static boolean hasTopics(String document) {
        return containsText(document,"TOPICS=\"Y");
    }

    static boolean isTrainingDocument(String document) {
        return containsText(document,"LEWISSPLIT=\"TR");
    }

    static boolean isTestDocument(String document) {
        return containsText(document,"LEWISSPLIT=\"TE");
    }

    static boolean containsText(String doc, String text) {
        return doc.indexOf(text) >= 0;
    }

    private static class ReutersCorpus
        extends Corpus<ClassificationHandler<CharSequence,Classification>> {

        private final String mTopic;
        private final File mDirectory;

        ReutersCorpus(String topic, File directory) {
            mTopic = topic;
            mDirectory = directory;
        }

        public void visitCorpus(ClassificationHandler<CharSequence,Classification> handler)
            throws IOException {

            visit(handler,true,true);
        }

        public void visitTest(ClassificationHandler<CharSequence,Classification> handler)
            throws IOException {

            visit(handler,false,true);
        }

        public void visitTrain(ClassificationHandler<CharSequence,Classification> handler)
            throws IOException {

            visit(handler,true,false);
        }

        void visit(ClassificationHandler<CharSequence,Classification> handler,
                   boolean includeTrain, boolean includeTest)
            throws IOException {

            Reuters21578Parser parser = new Reuters21578Parser(mTopic,includeTrain,includeTest);
            parser.setHandler(handler);
            for (File file : mDirectory.listFiles(new FileExtensionFilter(".sgm")))
                parser.parse(file);
        }
    }

}