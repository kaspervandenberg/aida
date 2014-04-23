// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Namespace;
import org.openrdf.model.BNode;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
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
	
	
	public Literal stringLiteral()
	{
		return new LiteralImpl("lit1");
	}

	
	public Literal string4Literal()
	{
		return new LiteralImpl("4");
	}

	
	public Literal emptyLiteral()
	{
		return new LiteralImpl("");
	}

	
	public Literal number4Literal()
	{
		return new NumericLiteralImpl(4);
	}

	
	public Literal number2Literal()
	{
		return new NumericLiteralImpl(2);
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
				"http://test.dummy.org/fullnamespace/a");
	}

	
	public URI fullNsB()
	{
		return valueFactory.createURI(
				"http://test.dummy.org/fullnamespace/b");
	}

	
	public URI fullOtherA()
	{
		return valueFactory.createURI(
				"http://other.org/fullnamespace/a");
	}


	public BNode annonBNode1()
	{
		return valueFactory.createBNode();
	}


	public BNode annonBNode2()
	{
		return valueFactory.createBNode();
	}


	public BNode namedBNode1()
	{
		return valueFactory.createBNode("n1");
	}


	public BNode namedBNode2()
	{
		return valueFactory.createBNode("a");
	}


	public BNode duplBNode1()
	{
		return valueFactory.createBNode("n1");
	}

}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

