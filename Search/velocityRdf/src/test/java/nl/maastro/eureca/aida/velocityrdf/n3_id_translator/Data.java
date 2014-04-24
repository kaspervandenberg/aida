// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Namespace;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.impl.NamespaceImpl;

import static java.util.Arrays.asList;
import java.util.Set;
import java.util.HashSet;

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


	public Set<Literal> literals()
	{
		return new HashSet<Literal>(asList(
				stringLiteral(), string4Literal(), emptyLiteral(),
				number4Literal(), number2Literal()));
	}


	public Set<Literal> literalSelection()
	{
		return new HashSet<Literal>(asList(
				stringLiteral(), emptyLiteral()));
	}


	
	public Set<URI> qnames()
	{
		return new HashSet<URI>(asList(
				prefixedNs1A(), prefixedNs1B(),
				prefixedNs2A(), prefixedNs3A()));
	}


	public Set<URI> fullUris()
	{
		return new HashSet<URI>(asList(
				fullNsA(), fullNsB(), fullOtherA()));
	}


	public Set<URI> uris()
	{
		Set<URI> result = new HashSet<URI>(qnames());
		result.addAll(fullUris());
		return result;
	}

	public Set<URI> uriSelection()
	{
		return new HashSet<URI>(asList(
				prefixedNs1A(), fullNsA()));
	}


	public Set<BNode> annonBNodes()
	{
		return new HashSet<BNode>(asList(
			annonBNode1(), annonBNode2()));
	}


	public Set<BNode> namedBNodes()
	{
		return new HashSet<BNode>(asList(
			namedBNode1(), namedBNode2(), duplBNode1()));
	}


	public Set<BNode> bnodes()
	{
		Set<BNode> result = new HashSet<BNode>(annonBNodes());
		result.addAll(namedBNodes());
		return result;
	}


	public Set<BNode> bnodeSelection()
	{
		return new HashSet<BNode>(asList(
				annonBNode1(), namedBNode1()));
	}


	public Set<Resource> resources()
	{
		Set<Resource> result = new HashSet<Resource>(uris());
		result.addAll(bnodes());
		return result;
	}


	public Set<Resource> resourceSelection()
	{
		Set<Resource> result = new HashSet<Resource>(uriSelection());
		result.addAll(bnodeSelection());
		return result;
	}


	public Set<Value> values()
	{
		Set<Value> result = new HashSet<Value>(resources());
		result.addAll(literals());
		return result;
	}


	public Set<Value> valueSelection()
	{
		Set<Value> result = new HashSet<Value>(resourceSelection());
		result.addAll(literalSelection());
		return result;
	}


	public Set<Statement> statements()
	{
		return generateAllStatementCombinations(
				resources(), uris(), values());
	}


	public Set<Statement> statementSelection()
	{
		return generateAllStatementCombinations(
				resourceSelection(), uriSelection(), valueSelection());
	}


	private Set<Statement> generateAllStatementCombinations(
			Set<Resource> subjects, Set<URI> predicates, Set<Value> objects)
	{
		Set<Statement> result = new HashSet<Statement>();
		for (Resource subj: subjects)
		{
			for (URI pred: predicates)
			{
				for (Value obj: objects)
				{
					Statement stat = valueFactory.createStatement(
							subj, pred, obj);
					result.add(stat);
				}
			}
		}
		return result;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

