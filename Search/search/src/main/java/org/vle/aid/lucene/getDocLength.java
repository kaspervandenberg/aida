/*
 *
 * Created on February 8, 2006, 3:06 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.vle.aid.lucene;


import org.apache.lucene.index.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import java.util.*;
import java.io.*;
import java.util.Iterator;
import java.util.logging.Logger;
import java.io.IOException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author emeij
 */

public class getDocLength {
    
    /** logger for Commons logging. */
    private static transient Logger log =
	 Logger.getLogger("getDocLength.class.getName()"); 
    
    private static int numDocs = -1;
    
    // getMaxDocLength
    // getMinDocLength

    /**
    * Calculates the standard deviation of an array
    * of numbers.
    * see http://davidmlane.com/hyperstat/A16252.html
    *
    * @param data Numbers to compute the standard deviation of.
    * Array must contain two or more numbers.
    * @return standard deviation estimate of population
    * ( to get estimate of sample, use n instead of n-1 in last line )
    */
    public static double sdFast ( Float[] data ) {
        double mean = 0;
        final int n = data.length;
        
        if ( n < 2 ) {
            return Double.NaN;
        }
        
        for ( int i=0; i<n; i++ ) {
            mean += data[i].floatValue();
        }
        mean /= n;
        
        // calculate the sum of squares
        double sum = 0;
        for ( int i=0; i<n; i++ ) {
            final double v = data[i].floatValue() - mean;
            sum += v * v;
        }
        
        return Math.sqrt( sum / ( n ) ); 
    }
    
    public static void main (String[] args) {
        try {
            int length = getAvgDocLength(args[0], args[1]);
            //System.out.println(" - Fields: ");
            //for (int i=0;i<fields.length;i++) {
              //  System.out.println(fields[i]);
            //}
            //System.out.println(" - Docs: " + numDocs);
            System.err.println();
        } catch (ArrayIndexOutOfBoundsException aoe) {
            System.err.println("Define location of indexdir and field");
        } 			
    }
	
	private static int getAvgDocLength(String indexLocation, String field) {
		
            Vector values = new Vector();
            IndexReader reader = null;
            int total = 0;
            int min = 1000000;
            int max = 0;
            float median = 0;
            double sd = 0;
		
            try {
				Directory indexDir = FSDirectory.open(new File(indexLocation));
                reader = DirectoryReader.open(indexDir);
                numDocs = reader.numDocs();
                
                for (int i=0; i<numDocs; i++) {
                    
                    Document doc = reader.document(i);
                    String content = doc.get(field);
                    //TermFreqVector vect = reader.getTermFreqVector(i, field);
                    //int[] content = vect.getTermFrequencies();
                    
                    int length = 0;
                    try {        
                        length = new StringTokenizer(doc.get(field)).countTokens();
                    } catch (java.lang.NullPointerException e) {}
                    
                    //calc min/max
                    if (length > max)
                        max = length;
                    if (length < min)
                        min = length;
                    
                    values.add(new Float(length));
                    
                    // Don't update every iteration
                    if ((i+1) % 100000 == 0) {
                        // calc SD
                        Float[] result = new Float[values.size()];
                        values.copyInto(result);
                        sd = sdFast(result);

                        // calc median
                        Arrays.sort(result);
                        median = result[((result.length/2))].floatValue();                        
                    }
                    
                    // Print only every 100th iteration
                    if ((i+1) % 100 == 0) {
                        System.err.print("Processing " + i + "/" + numDocs + 
                                ", avg. " + (float)(total/(i+1)) + 
                                ", median " + median + 
                                ", min. " + min + 
                                ", max. " + max + 
                                ", SD. " + sd + 
                                "\r");

                        total += length;
                    }
                    
                    System.out.println(doc.get("id") + " " + length);
                    
                }
                
                Float[] result = new Float[values.size()];
                values.copyInto(result);
                sd = sdFast(result);

                // calc median
                Arrays.sort(result);
                median = result[((result.length/2))].floatValue();                

                System.err.println("Done processing. Avg. " + (float)(total/(numDocs)) + 
                        ", median " + median + 
                        ", min. " + min + 
                        ", max. " + max + 
                        ", SD. " + sd + 
                        "\n");

                reader.close();
                
            } catch (ArrayIndexOutOfBoundsException aoe) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                aoe.printStackTrace(pw);
                log.severe(sw.toString());
                pw.close();             
            } catch(IOException e) {
                log.info("IOError: " + e.getMessage());
            }	
                return total;
            }
    
}
