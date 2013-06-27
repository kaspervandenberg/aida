// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

/**
 * Interface for builders of adapters that adapt from {@code TIn} to 
 * {@code TOut} {@link Query}s.
 * 
 * @param <TIn>	type of {@link Query} to accept
 * @param <TOut>	type of {@link Query} to output
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface QueryAdapterBuilder<TIn extends Query, TOut extends Query> {
	/**
	 * Transform {@code adapted} to {@code TOut}
	 * @param adapted
	 * 
	 * @return {@code adapted} transformed into {@code <TOut>}
	 */
	TOut adapt(TIn adapted);	
}
