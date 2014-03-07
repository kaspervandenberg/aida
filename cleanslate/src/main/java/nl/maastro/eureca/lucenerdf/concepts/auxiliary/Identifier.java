// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.auxiliary;

import checkers.nullness.quals.Nullable;
import dataflow.quals.Pure;

/**
 * (Tagging) interface for classes used as identifier.
 */
public interface Identifier {

	@Override
	@Pure
	public boolean equals(@Nullable Object other);

	@Override
	@Pure
	public int hashCode();
}


/* vim:set tabstop=4 shiftwidth=4 autoindent : */


