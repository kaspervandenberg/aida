/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

/**
 *
 * @author kasper2
 */
public class IdentityAdapter<T extends Query> implements QueryAdapterBuilder<T, T> {

	@Override
	public T adapt(T adapted) {
		return adapted;
	}
	
}
