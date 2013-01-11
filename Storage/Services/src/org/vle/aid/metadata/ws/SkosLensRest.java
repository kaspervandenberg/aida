package org.vle.aid.metadata.ws;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.vle.aid.metadata.SkosLens;
import org.vle.aid.metadata.SkosLensType;
import org.vle.aid.metadata.ThesaurusRepository;
import org.vle.aid.metadata.exception.UnknownException;
import org.vle.aid.metadata.ws.ThesaurusRepositoryWS.Topterms;
import org.vle.aid.metadata.ws.ThesaurusRepositoryWS.Topterms.Topterm;
import org.vle.aid.thesaurus.client.ThesaurusConceptNode;

@Path("/skoslens")
public class SkosLensRest {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	
	/**
	 * 
	 * Class to hold the topconcepts
	 * 
	 * @author emeij
	 * 
	 */
	public class Topterms {

		/**
		 * A single topconcept
		 * 
		 * @author emeij
		 * 
		 */
		public class Topterm {

			/**
			 * ID of the concept
			 */
			public String id;

			/**
			 * Label of the concept
			 */
			public String term;

			/**
			 * Creates a new Topterm
			 * 
			 * @param id
			 *            the ID
			 * @param term
			 *            the label
			 */
			public Topterm(String id, String term) {
				this.id = id;
				this.term = term;
			}

		}

		/**
		 * The topterms
		 */
		public final Collection<Topterm> topterms = new Vector<Topterm>();

		/**
		 * Construct a new Topterms object with the terms in the argument
		 * 
		 * @param terms
		 *            the topconcepts to use
		 */
		public Topterms(AbstractMap<String, String> terms) {
			for (String k : terms.keySet()) {
				this.topterms.add(new Topterm(terms.get(k), k));
			}
		}
	}

	static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(SkosLensRest.class);

	/*
	 * Semantic Relations
	 */

	

	@SuppressWarnings("unchecked")
	private static <T> T[] arrayMerge(T[]... arrays) {

		int count = 0;
		for (T[] array : arrays) {
			count += array.length;
		}

		T[] mergedArray = (T[]) Array.newInstance(arrays[0][0].getClass(),
				count);

		int start = 0;
		for (T[] array : arrays) {
			System.arraycopy(array, 0, mergedArray, start, array.length);
			start += array.length;
		}
		return (T[]) mergedArray;
	}

	/**
	 * Returns the narrower and alternative concepts for a concept
	 * 
	 * @param form
	 *            the parameters
	 * @return narrower and alternative concepts
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/narroweralts")
	public ThesaurusConceptNode[] narrowerAndAlts(
			MultivaluedMap<String, String> param) throws ServletException,
			RemoteException {

		ThesaurusConceptNode[] narrower = narrower(param);
		ThesaurusConceptNode[] alts = alternatives(param);
		if (alts.length > 0 && narrower.length > 0) {
			return arrayMerge(alts, narrower);
		} else if (alts.length == 0) {
			return narrower;
		} else if (narrower.length == 0) {
			return alts;
		} else {
			return new ThesaurusConceptNode[0];
		}
	}

	/**
	 * Returns the narrower and alternative concepts for a concept
	 * 
	 * @param ui
	 *            the parameters
	 * @return narrower and alternative concepts
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@GET
	@Produces("application/json")
	@Path("/narroweralts")
	public ThesaurusConceptNode[] narrowerAndAlts(@Context UriInfo ui)
			throws ServletException, RemoteException {
		return narrowerAndAlts(ui.getQueryParameters());
	}

	/**
	 * Returns the narrower concepts for a concept
	 * 
	 * @param form
	 *            the parameters
	 * @return children
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/narrower")
	public ThesaurusConceptNode[] narrower(MultivaluedMap<String, String> param)
			throws ServletException, RemoteException {

		SkosLens tr = new SkosLens(param);

		String focus_term = null;
		if (!param.containsKey("term")) {
			focus_term = null;
		} else {
			focus_term = param.getFirst("term");
		}

		// extjs specific
		if (param.containsKey("node"))
			focus_term = param.getFirst("node");

		// if it contains an explicit id, use that
		if (param.containsKey("uri"))
			focus_term = param.getFirst("uri");

		if (focus_term != null) {
			// extjs
			if (focus_term.startsWith("extjs_s_"))
				focus_term = focus_term.replaceFirst("extjs_s_", "");

			// Check if we're dealing with a term or a uri
			if (!focus_term.startsWith("http://")) {
				return new ThesaurusConceptNode[0];
				// String terms[][] = tr.getTermUri(focus_term);
				// if (terms.length > 0)
				// focus_term = terms[0][0];
				// else
				// throw new ServletException(focus_term + " not found!");

			}
		}

		String[][] _narrower = tr.getNarrowerTerms(focus_term);

		if (_narrower == null)
			return new ThesaurusConceptNode[0];
		;

		ThesaurusConceptNode[] out = new ThesaurusConceptNode[_narrower.length];
		for (int j = 0; j < _narrower.length; j++) {
			out[j] = new ThesaurusConceptNode(_narrower[j][0], // URI
					_narrower[j][1], // label
					"Narrower Term: " + _narrower[j][0], // qtip
					"narrower-icon" // iconCls
			);
		}

		return out;
	}

	/**
	 * Returns the narrower concepts for a concept
	 * 
	 * @param ui
	 *            the parameters
	 * @return children
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@GET
	@Produces("application/json")
	@Path("/narrower")
	public ThesaurusConceptNode[] narrower(@Context UriInfo ui)
			throws ServletException, RemoteException {
		return narrower(ui.getQueryParameters());
	}

	/**
	 * Returns the alternative labels for a concept
	 * 
	 * @param form
	 *            the parameters
	 * @return labels
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/alternatives")
	public ThesaurusConceptNode[] alternatives(
			MultivaluedMap<String, String> param) throws ServletException,
			RemoteException {

		SkosLens tr = new SkosLens(param);

		String focus_term = null;
		if (!param.containsKey("term")) {
			focus_term = null;
		} else {
			focus_term = param.getFirst("term");
		}

		// extjs specific
		if (param.containsKey("node"))
			focus_term = param.getFirst("node");

		// if it contains an explicit id, use that
		if (param.containsKey("uri"))
			focus_term = param.getFirst("uri");

		if (focus_term != null) {
			// extjs
			if (focus_term.startsWith("extjs_s_"))
				focus_term = focus_term.replaceFirst("extjs_s_", "");

			// Check if we're dealing with a term or a uri
			if (!focus_term.startsWith("http://")) {
				return new ThesaurusConceptNode[0];
				// String terms[][] = tr.getTermUri(focus_term);
				// if (terms.length > 0)
				// focus_term = terms[0][0];
				// else
				// throw new ServletException(focus_term + " not found!");

			}
		}

		String[] alts = tr.getAlternativeTerms(focus_term);

		if (alts == null)
			return new ThesaurusConceptNode[0];
		;

		ArrayList<ThesaurusConceptNode> temp = new ArrayList<ThesaurusConceptNode>();

		for (int j = 0; j < alts.length; j++) {

			if (focus_term.equalsIgnoreCase(alts[j]))
				continue;

			temp.add(new ThesaurusConceptNode(alts[j], // URI
					alts[j], // label
					"Alternative Term: " + alts[j], // qtip
					"alt-icon" // iconCls
			));

		}

		return temp.toArray(new ThesaurusConceptNode[0]);
	}

	/**
	 * Returns the alternative terms for a concept
	 * 
	 * @param ui
	 *            the parameters
	 * @return alternative labels
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@GET
	@Produces("application/json")
	@Path("/alternatives")
	public ThesaurusConceptNode[] alternatives(@Context UriInfo ui)
			throws ServletException, RemoteException {
		return alternatives(ui.getQueryParameters());
	}

	/**
	 * Returns the topconcepts for a given repository
	 * 
	 * @param form
	 *            the parameters
	 * @return top concepts
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/rootnodes")
	public Topterms rootnodes( MultivaluedMap<String, String> param) throws ServletException, RemoteException {

		String ns = "null";
		SkosLens tr = new SkosLens(param);
		if (param.containsKey("ns")) {
			ns = param.getFirst("ns");
		}

		Vector<String> done = new Vector<String>();
		String[][] cschemes = tr.getConceptSchemes(ns);

		// sort here
		java.util.TreeMap<String, String> out = new java.util.TreeMap<String, String>();

		// Even when we have no concept schemes, try to get topconcepts without any scheme
		if (cschemes == null){
				String[][] top = tr.getTopConcepts("");

				if (top == null) return null;

				for (int k = 0; k < top.length; k++) {
					out.put(top[k][1], top[k][0]);
				}
	
			return out == null ? null : new Topterms(out);
		}


		for (int i = 0; i < cschemes.length; i++) {
			for (int j = 0; j < cschemes[i].length; j++) {
				String _scheme = cschemes[i][j];

				// logger.info("scheme: " + _scheme);

				if (!_scheme.startsWith("http://"))
					continue;

				if (done.contains(_scheme))
					continue;

				done.add(_scheme);
				String[][] top = tr.getTopConcepts(_scheme);

				if (top == null)
					continue;

				for (int k = 0; k < top.length; k++) {
					out.put(top[k][1], top[k][0]);
				}
			}
		}

		return out == null ? null : new Topterms(out);
	}

	/**
	 * Returns the topconcepts for a given repository
	 * 
	 * @param ui
	 *            the parameters
	 * @return top concepts
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@GET
	@Produces("application/json")
	@Path("/rootnodes")
	public Topterms rootnodes(@Context UriInfo ui) throws ServletException,
			RemoteException {
		return rootnodes(ui.getQueryParameters());
	}

	/**
	 * Looks up concepts
	 * 
	 * @param form
	 *            the parameters
	 * @return suggestions
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/suggest")
	public ThesaurusConceptNode[] suggest(MultivaluedMap<String, String> param)
			throws ServletException, RemoteException {

		String focus_term = null;
		if (!param.containsKey("term")) {
			focus_term = null;
		} else {
			focus_term = param.getFirst("term");
		}

		// extjs specific
		if (param.containsKey("node"))
			focus_term = param.getFirst("node");

		// if it contains an explicit id, use that
		if (param.containsKey("uri"))
			focus_term = param.getFirst("uri");

		SkosLens tr = new SkosLens(param);
		String[][] suggestions = tr.getTermCompletion(focus_term);

		if (suggestions == null || suggestions.length == 0)
			return null;

		ThesaurusConceptNode[] out = new ThesaurusConceptNode[suggestions.length];
		for (int j = 0; j < suggestions.length; j++) {
			out[j] = new ThesaurusConceptNode(suggestions[j][0], // URI
					suggestions[j][1] // label
			);
		}

		return out;
	}

	/**
	 * Looks up concepts
	 * 
	 * @param ui
	 *            the parameters
	 * @return suggestions
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@GET
	@Produces("application/json")
	@Path("/suggest")
	public ThesaurusConceptNode[] suggest(@Context UriInfo ui)
			throws ServletException, RemoteException {
		return suggest(ui.getQueryParameters());
	}

	/**
	 * Looks up for available skos lenses.
	 * 
	 * @param form
	 *            the parameters
	 * @return getLenses
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/getLenses")
	public SkosLensType[] getLenses(MultivaluedMap<String, String> param)
			throws ServletException, RemoteException {
		Vector<SkosLensType> lenses = SkosLens.getAvailableLenses();
		SkosLensType [] resArray = lenses.toArray(new SkosLensType[0]);
		return resArray;
	}

	/**
	 * Looks up concepts
	 * 
	 * @param ui
	 *            the parameters
	 * @return getLensesions
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@GET
	@Produces("application/json")
	@Path("/getLenses")
	public SkosLensType[] getLenses(@Context UriInfo ui)
			throws ServletException, RemoteException {
		return getLenses(ui.getQueryParameters());
	}
}
