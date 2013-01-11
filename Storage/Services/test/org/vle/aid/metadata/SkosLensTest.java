/*
 * SkosLens Test.
 */

package org.vle.aid.metadata;

import java.util.Vector;
import junit.framework.TestCase;

/**
 *
 * @author wibisono
 */
public class SkosLensTest extends TestCase {
    
    public SkosLensTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getAvailableLensesAsString method, of class SkosLens.
     */
    public void testGetAvailableLensesAsString() {
        System.out.println("getAvailableLensesAsString");
        SkosLens instance = new SkosLens();
        Vector<String[]> expResult = null;
        Vector<String[]> result = instance.getAvailableLensesAsString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAvailableLenses method, of class SkosLens.
     */
    public void testGetAvailableLenses() {
        System.out.println("getAvailableLenses");
        SkosLens instance = new SkosLens();
        Vector<SkosLensType> expResult = null;
        Vector<SkosLensType> result = instance.getAvailableLenses();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLensType method, of class SkosLens.
     */
    public void testSetLensType() {
        System.out.println("setLensType");
        String topConcepts = "";
        String narrowerPredicate = "";
        String skosVersion = "";
        String virtuosoNamedGraph = "";
        SkosLens instance = new SkosLens();
        instance.setLensType(topConcepts, narrowerPredicate, skosVersion, virtuosoNamedGraph);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLensConcept method, of class SkosLens.
     */
    public void testGetLensConcept() {
        System.out.println("getLensConcept");
        SkosLens instance = new SkosLens();
        String expResult = "";
        String result = instance.getLensConcept();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLensNarrowerPredicate method, of class SkosLens.
     */
    public void testGetLensNarrowerPredicate() {
        System.out.println("getLensNarrowerPredicate");
        SkosLens instance = new SkosLens();
        String expResult = "";
        String result = instance.getLensNarrowerPredicate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRepository method, of class SkosLens.
     */
    public void testSetRepository() {
        System.out.println("setRepository");
        Repository r = null;
        SkosLens instance = new SkosLens();
        instance.setRepository(r);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRepository method, of class SkosLens.
     */
    public void testGetRepository() {
        System.out.println("getRepository");
        SkosLens instance = new SkosLens();
        Repository expResult = null;
        Repository result = instance.getRepository();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNarrowerTerms method, of class SkosLens.
     */
    public void testGetNarrowerTerms() throws Exception {
        System.out.println("getNarrowerTerms");
        String term = "";
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getNarrowerTerms(term);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBroaderTerms method, of class SkosLens.
     */
    public void testGetBroaderTerms() throws Exception {
        System.out.println("getBroaderTerms");
        String term = "";
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getBroaderTerms(term);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRelatedTerms method, of class SkosLens.
     */
    public void testGetRelatedTerms() throws Exception {
        System.out.println("getRelatedTerms");
        String term = "";
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getRelatedTerms(term);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRDFSLabels method, of class SkosLens.
     */
    public void testGetRDFSLabels() throws Exception {
        System.out.println("getRDFSLabels");
        String term = "";
        SkosLens instance = new SkosLens();
        String[] expResult = null;
        String[] result = instance.getRDFSLabels(term);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPreferedTerms method, of class SkosLens.
     */
    public void testGetPreferedTerms() throws Exception {
        System.out.println("getPreferedTerms");
        String term = "";
        SkosLens instance = new SkosLens();
        String[] expResult = null;
        String[] result = instance.getPreferedTerms(term);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTermCompletion method, of class SkosLens.
     */
    public void testGetTermCompletion() throws Exception {
        System.out.println("getTermCompletion");
        String term = "";
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getTermCompletion(term);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAlternativeTerms method, of class SkosLens.
     */
    public void testGetAlternativeTerms() throws Exception {
        System.out.println("getAlternativeTerms");
        String term = "";
        SkosLens instance = new SkosLens();
        String[] expResult = null;
        String[] result = instance.getAlternativeTerms(term);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTopConcepts method, of class SkosLens.
     */
    public void testGetTopConcepts() throws Exception {
        System.out.println("getTopConcepts");
        String scheme = "";
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getTopConcepts(scheme);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConceptSchemes method, of class SkosLens.
     */
    public void testGetConceptSchemes_0args() throws Exception {
        System.out.println("getConceptSchemes");
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getConceptSchemes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConceptSchemes method, of class SkosLens.
     */
    public void testGetConceptSchemes_String() throws Exception {
        System.out.println("getConceptSchemes");
        String ns = "";
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getConceptSchemes(ns);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumberOfNarrowerTerms method, of class SkosLens.
     */
    public void testGetNumberOfNarrowerTerms_String() throws Exception {
        System.out.println("getNumberOfNarrowerTerms");
        String term = "";
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getNumberOfNarrowerTerms(term);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumberOfNarrowerTerms method, of class SkosLens.
     */
    public void testGetNumberOfNarrowerTerms_StringArr() throws Exception {
        System.out.println("getNumberOfNarrowerTerms");
        String[] terms = null;
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getNumberOfNarrowerTerms(terms);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTermUri method, of class SkosLens.
     */
    public void testGetTermUri() throws Exception {
        System.out.println("getTermUri");
        String label = "";
        SkosLens instance = new SkosLens();
        String[][] expResult = null;
        String[][] result = instance.getTermUri(label);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
