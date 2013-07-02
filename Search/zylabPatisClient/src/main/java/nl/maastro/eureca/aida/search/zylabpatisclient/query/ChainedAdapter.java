// © Maastro Clinic, 2013 
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

/**
 * Combine two adapters to convert a {@link Query} of type {@code <TIn>} to
 * {@code <TOut>} using {@code <TIntermediate>}
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ChainedAdapter<TIn extends Query, TOut extends Query, TIntermediate extends Query> 
	implements QueryAdapterBuilder<TIn, TOut> {

	private final QueryAdapterBuilder<TIn, TIntermediate> first;
	private final QueryAdapterBuilder<TIntermediate, TOut> second;

	public ChainedAdapter(
			QueryAdapterBuilder<TIn, TIntermediate> first_, 
			QueryAdapterBuilder<TIntermediate, TOut> second_) {
		this.first = first_;
		this.second = second_;
	}
	
	@Override
	public TOut adapt(TIn adapted) {
		return second.adapt(first.adapt(adapted));
	}
}

// vim:  set tabstab=4 shiftwidth=4 : 

