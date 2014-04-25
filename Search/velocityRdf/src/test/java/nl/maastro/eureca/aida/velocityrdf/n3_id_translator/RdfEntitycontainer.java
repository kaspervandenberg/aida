// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

class RdfEntityContainer<T> {
	private final T entity;

	public RdfEntityContainer(T entity_)
	{
		this.entity = entity_;
	}

	public T getValue()
	{
		return entity;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

