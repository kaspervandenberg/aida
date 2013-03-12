/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.lucene;

/**
 * Functional programming concepts
 * 
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
public interface Transformer<TIn, TOut> {

	public TOut transform(TIn input);
	
}
