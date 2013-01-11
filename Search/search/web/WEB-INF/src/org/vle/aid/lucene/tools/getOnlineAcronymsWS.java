/*
 * getOnlineAcronymsWS.java
 *
 * Reads acronym definitions from acronymfinder.com, using screenscraper approach.
 * 
 */

package org.vle.aid.lucene.tools;

import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.*;
 
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.util.logging.Logger;

import java.util.regex.*;


public class getOnlineAcronymsWS {
    
    /** logger for Commons logging. */
    private static transient Logger log =
	 Logger.getLogger(getOnlineAcronymsWS.class.getName());
    
    static final StringWriter sw = new StringWriter();
    static PrintWriter out;
    
    private static final String REGEX = 
            "(  <td valign=\"middle\" width=\"70%\"([a-z,A-Z,0-9,-, , =,\",>,#,(,)]{2,}))";
    private static final Pattern pattern = Pattern.compile(REGEX);
    private static Matcher matcher;
    
    public String[] getOnlineAcronyms (String term) {
        
        out = new PrintWriter(sw);
                
        try {
            URLConnection url = (new URL("http://acronymfinder.com/af-query.asp?Acronym=" + term)).openConnection();
            readHttpURL((HttpURLConnection) url);        
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            log.severe("I/O Error " + e.toString());
            return null;
        }
    
        out.close();       
        
        String[] item = pattern.split(sw.toString());
        matcher = pattern.matcher(sw.toString());

        String[] result = new String[item.length - 1];
        
        int k = 0;

        while (matcher.find()) {
            String content = matcher.group();    
            String temp[] = content.split(">");
            result[k] = temp[1];
            k++;
        }
        
        return result;
    }    
    
    private static final void readHttpURL(HttpURLConnection url) 
            throws IOException {

        url.setAllowUserInteraction (false);
        url.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.10) Gecko/20050716 Firefox/1.0.6");
        url.connect();

        DataInputStream in = new DataInputStream(url.getInputStream());

        try {
          if (url.getResponseCode() != HttpURLConnection.HTTP_OK) {
            log.severe(url.getResponseMessage());
          } else {
            while (true) {
                out.print((char) in.readUnsignedByte());
            }
          }
        } catch (EOFException e) {
          url.disconnect();
        } catch (IOException e) {
          log.severe("I/O Error " + url.getResponseMessage());
        }
    }    
}
