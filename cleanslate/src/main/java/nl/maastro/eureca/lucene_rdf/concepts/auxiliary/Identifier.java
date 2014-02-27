// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucene_rdf.concepts.auxiliary;

/**
 * (Tagging) interface for classes used as identifier.
 */
public interface Identifier {

	@Override
	public boolean equals(Object other);

	@Override
	public int hashCode();
}


/* vim:set tabstop=4 shiftwidth=4 autoindent : */


