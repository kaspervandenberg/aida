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
	
	
	public RdfEntityContainer<Literal> stringLiteral()
	{
		return newLit("lit1");
	}

	
	public RdfEntityContainer<Literal> string4Literal()
	{
		return newLit("4");
	}

	
	public RdfEntityContainer<Literal> emptyLiteral()
	{
		return newLit("");
	}

	
	public RdfEntityContainer<Literal> number4Literal()
	{
		return newNumLit(4);
	}

	
	public RdfEntityContainer<Literal> number2Literal()
	{
		return newNumLit(2);
	}


	public RdfEntityContainer<URI> prefixedNs1A()
	{
		return newQName(NAMESPACES[0], "a");
	}


	public RdfEntityContainer<URI> prefixedNs1B()
	{
		return newQName(NAMESPACES[0], "b");
	}


	public RdfEntityContainer<URI> prefixedNs2A()
	{
		return newQName(NAMESPACES[1], "a");
	}


	public RdfEntityContainer<URI> prefixedNs3A()
	{
		return newQName(NAMESPACES[2], "a");
	}

	
	public RdfEntityContainer<URI> fullNsA()
	{
		return newFullUri("http://test.dummy.org/fullnamespace/a");
	}

	
	public RdfEntityContainer<URI> fullNsB()
	{
		return newFullUri("http://test.dummy.org/fullnamespace/b");
	}

	
	public RdfEntityContainer<URI> fullOtherA()
	{
		return newFullUri("http://other.org/fullnamespace/a");
	}


	public RdfEntityContainer<BNode> annonBNode1()
	{
		return newBNode();
	}


	public RdfEntityContainer<BNode> annonBNode2()
	{
		return newBNode();
	}


	public RdfEntityContainer<BNode> namedBNode1()
	{
		return newBNode("n1");
	}


	public RdfEntityContainer<BNode> namedBNode2()
	{
		return newBNode("a");
	}


	public RdfEntityContainer<BNode> duplBNode1()
	{
		return newBNode("n1");
	}


	public Set<RdfEntityContainer<Literal>> literals()
	{
		return new HashSet<RdfEntityContainer<Literal>>(asList(
				stringLiteral(), string4Literal(), emptyLiteral(),
				number4Literal(), number2Literal()));
	}


	public Set<RdfEntityContainer<Literal>> literalSelection()
	{
		return new HashSet<RdfEntityContainer<Literal>>(asList(
				stringLiteral(), emptyLiteral()));
	}


	
	public Set<RdfEntityContainer<URI>> qnames()
	{
		return new HashSet<RdfEntityContainer<URI>>(asList(
				prefixedNs1A(), prefixedNs1B(),
				prefixedNs2A(), prefixedNs3A()));
	}


	public Set<RdfEntityContainer<URI>> fullUris()
	{
		return new HashSet<RdfEntityContainer<URI>>(asList(
				fullNsA(), fullNsB(), fullOtherA()));
	}


	public Set<RdfEntityContainer<URI>> uris()
	{
		Set<RdfEntityContainer<URI>> result = 
				new HashSet<RdfEntityContainer<URI>>(qnames());
		result.addAll(fullUris());
		return result;
	}

	public Set<RdfEntityContainer<URI>> uriSelection()
	{
		return new HashSet<RdfEntityContainer<URI>>(asList(
				prefixedNs1A(), fullNsA()));
	}


	public Set<RdfEntityContainer<BNode>> annonBNodes()
	{
		return new HashSet<RdfEntityContainer<BNode>>(asList(
			annonBNode1(), annonBNode2()));
	}


	public Set<RdfEntityContainer<BNode>> namedBNodes()
	{
		return new HashSet<RdfEntityContainer<BNode>>(asList(
			namedBNode1(), namedBNode2(), duplBNode1()));
	}


	public Set<RdfEntityContainer<BNode>> bnodes()
	{
		Set<RdfEntityContainer<BNode>> result =
				new HashSet<RdfEntityContainer<BNode>>(annonBNodes());
		result.addAll(namedBNodes());
		return result;
	}


	public Set<RdfEntityContainer<BNode>> bnodeSelection()
	{
		return new HashSet<RdfEntityContainer<BNode>>(asList(
				annonBNode1(), namedBNode1()));
	}


	public Set<RdfEntityContainer<? extends Resource>> resources()
	{
		Set<RdfEntityContainer<? extends Resource>> result =
				new HashSet<RdfEntityContainer<? extends Resource>>(uris());
		result.addAll(bnodes());
		return result;
	}


	public Set<RdfEntityContainer<? extends Resource>> resourceSelection()
	{
		Set<RdfEntityContainer<? extends Resource>> result =
				new HashSet<RdfEntityContainer<? extends Resource>>(uriSelection());
		result.addAll(bnodeSelection());
		return result;
	}


	public Set<RdfEntityContainer<? extends Value>> values()
	{
		Set<RdfEntityContainer<? extends Value>> result = 
				new HashSet<RdfEntityContainer<? extends Value>>(resources());
		result.addAll(literals());
		return result;
	}


	public Set<RdfEntityContainer<? extends Value>> valueSelection()
	{
		Set<RdfEntityContainer<? extends Value>> result = 
				new HashSet<RdfEntityContainer<? extends Value>>(resourceSelection());
		result.addAll(literalSelection());
		return result;
	}


	public Set<RdfEntityContainer<Statement>> statements()
	{
		return generateAllStatementCombinations(
				resources(), uris(), values());
	}


	public Set<RdfEntityContainer<Statement>> statementSelection()
	{
		return generateAllStatementCombinations(
				resourceSelection(), uriSelection(), valueSelection());
	}


	private Set<RdfEntityContainer<Statement>> generateAllStatementCombinations(
			Set<RdfEntityContainer<? extends Resource>> subjects,
			Set<RdfEntityContainer<URI>> predicates,
			Set<RdfEntityContainer<? extends Value>> objects)
	{
		Set<RdfEntityContainer<Statement>> result = new HashSet<>();
		for (RdfEntityContainer<? extends Resource> subj: subjects)
		{
			for (RdfEntityContainer<? extends URI> pred: predicates)
			{
				for (RdfEntityContainer<? extends Value> obj: objects)
				{
					Statement stat = valueFactory.createStatement(
							subj.getValue(), pred.getValue(), obj.getValue());
					result.add(new RdfEntityContainer<>(stat));
				}
			}
		}
		return result;
	}


	private RdfEntityContainer<Literal> newLit(String value)
	{
		return new RdfEntityContainer<Literal>(new LiteralImpl(value));
	}


	private RdfEntityContainer<Literal> newNumLit(int value)
	{
		return new RdfEntityContainer<Literal>(new NumericLiteralImpl(value));
	}
		

	private RdfEntityContainer<URI> newQName(String prefix, String local)
	{
		return new RdfEntityContainer<URI>(
				valueFactory.createURI(prefix, local));
	}
	

	private RdfEntityContainer<URI> newFullUri(String fullUri)
	{
		return new RdfEntityContainer<URI>(
				valueFactory.createURI(fullUri));
	}


	private RdfEntityContainer<BNode> newBNode()
	{
		return new RdfEntityContainer<BNode>(
				valueFactory.createBNode());
	}


	private RdfEntityContainer<BNode> newBNode(String id)
	{
		return new RdfEntityContainer<BNode>(
				valueFactory.createBNode(id));
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

