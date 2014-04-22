// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.URI;
import org.openrdf.model.Namespace;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.NamespaceImpl;

import static java.util.Arrays.asList;

/**
 * Contains common {@link org.junit.experimental.theories.DataPoint} used in
 * several tests.
 */
class Data {
	private final ValueFactory valueFactory =
			new ValueFactoryImpl();

	private static final String PREFIXES[] = {
			"ns1", "ns2", "ns3"};
	private static final String NAMESPACES[] = {
			"http://test.dummy.org/namespace/",
			"http://test.dummy.org/",
			"http://test.dummy.org/file.html#"};

	public NamespaceContainer namespaces()
	{
		return new NamespaceContainer( asList(
				((Namespace) new NamespaceImpl(PREFIXES[0], NAMESPACES[0])),
				((Namespace) new NamespaceImpl(PREFIXES[1], NAMESPACES[1])),
				((Namespace) new NamespaceImpl(PREFIXES[2], NAMESPACES[2]))));
	}
	

	public URI prefixedNs1A()
	{
		return valueFactory.createURI(NAMESPACES[0], "a");
	}


	public URI prefixedNs1B()
	{
		return valueFactory.createURI(NAMESPACES[0], "b");
	}


	public URI prefixedNs2A()
	{
		return valueFactory.createURI(NAMESPACES[1], "a");
	}


	public URI prefixedNs3A()
	{
		return valueFactory.createURI(NAMESPACES[2], "a");
	}

	
	public URI fullNsA()
	{
		return valueFactory.createURI(
				"http://test.dummy.org/namespace/a");
	}

	
	public URI fullNsB()
	{
		return valueFactory.createURI(
				"http://test.dummy.org/namespace/b");
	}

	
	public URI fullOtherA()
	{
		return valueFactory.createURI(
				"http://other.org/namespace/a");
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

