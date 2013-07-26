/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import nl.maastro.eureca.aida.search.zylabpatisclient.query.DualRepresentationQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;

/**
 *
 * @author kasper
 */
public interface Concept extends Query, DualRepresentationQuery, LuceneObject {
	
}
