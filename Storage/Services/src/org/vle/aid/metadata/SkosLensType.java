/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

import java.util.Vector;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;




public class SkosLensType {

	public String label;

	public String concept;

	public String predicate;

    public SkosLensType(String label, String  concept, String  predicate){
        this.label = label;
        this.concept = concept;
        this.predicate = predicate;
    }
    
}
