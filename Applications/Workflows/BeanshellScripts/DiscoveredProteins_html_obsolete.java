import java.lang.String;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;

public class DiscoveredProteins_html {
/* derived from beanshell script to compile lists of text mining results into a html table */
/* 
variables:
structuredList //  list of (query protein, discovered protein, publication) lists
*/
	private String pubmed_url_stub = "http://www.ncbi.nlm.nih.gov/sites/entrez?cmd=Retrieve&amp;db=PubMed&amp;list_uids=";
	private String iHop_url_stub_before = "http://www.ihop-net.org/UniPub/iHOP/?search=";
	private String iHop_url_stub_after = "&field=UNIPROT__AC&ncbi_tax_id=9606&organism_syn=";
	private String html_top="<html>\n<head>\n<title>Results of text mining workflow</title>\n<link href='http://www.adaptivedisclosure.org/workflows/AIDA_workflows.css' rel='stylesheet' type='text/css'/>\n</head>\n<body>\n<div id='wrapper'>\n<div id='header'>\n<span id='aida_logo' title='Adaptive Information Disclosure Application'><a href='http://adaptivedisclosure.org'>Adaptive Information Disclosure Application</a></span>\n<span id='vle_logo' title='Virtual Laboratory for e-Science'><a href='http://www.vl-e.nl/'>Virtual Laboratory for e-Science</a></span>\n</div><!-- header -->\n<div id='page_title'>\n<h1><span title='Adaptive Information Disclosure Application'>AIDA</span> workflow results</h1>\n</div><!-- page_title -->\n"+"<table align='center' summary='this table gives the results of the text mining workflow'>\n<caption><em>Results of text mining workflow</em></caption>\n<tr>\n<th>Query<br/>protein</th>\n<th>Associated<br/>with</th>\n<th>Published in<br/><small>(PubMed ID)</small></th>\n</tr>\n";
	private String html_bottom="</table>\n\n<div id='footer'>\n<div id='AIDA'/>\n</div><!-- footer -->\n</div><!-- wrapper --></body>\n</html>\n";

	/* Skip highest list level below the root */
	public List SliceOutListLevel (List listOfNestedLists) {
		Iterator iterator=(Iterator) listOfNestedLists.iterator(); // iterator for the list (of lists) below the root, the level to skip
		/* Iterator iterator2; // iterator for the lower level */
		
		List tmpList = new ArrayList(); // for the new list
		
		while (iterator.hasNext()) {
			tmpList.addAll((List) iterator.next());
			/*
			iterator2=(Iterator) ((List) iterator.next()).iterator(); // iterator for the lower level lists of lists
			while (iterator2.hasNext()) {
				tmpList.add((List) new ArrayList((List) iterator2.next())); add a copy of the lower list of lists
			}
			*/
		}

		return(tmpList);
	}

	/* Concatenate lists */
	public List StructureLists (String query_protein, String discovered_protein, String discovered_uniprot_id, String pubmed_id, String ranking_score) {
		List tmpList = new ArrayList();

		tmpList.add(query_protein+ranking_score+discovered_protein+pubmed_id); // key
		tmpList.add(query_protein);
		tmpList.add(discovered_protein);
		tmpList.add(discovered_uniprot_id);
		tmpList.add(pubmed_id);
		tmpList.add(ranking_score);
		
		return(tmpList);
	}
	
	private class ListKeyComparator implements Comparator
   {
	   // Compare key field in two lists. Callback for sort.
	   // effectively returns a-b;
	   // e.g. +1 (or any +ve number) if a > b
	   // 0 if a == b
	   // -1 (or any -ve number) if a < b
	   public final int compare ( Object a, Object b )
	   {
			return( (String) ((List) a).iterator().next().toString() ).compareTo( (String) ((List) b).iterator().next().toString() ); // first element in list is key
	   } // end compare
   } // end class StringComparator
	
	public String DiscoveredProteinsToHtmlTable(List structuredList) {
		String key;
		String qry;
		String prot;
		String uniprot;
		String pub_id;
		String iHopString = new String();

		String prev_qry="";
		String prev_prot="";
		String prev_uniprot="";
		String prev_pub_id="";

		String tablebody="";

		Collections.sort(structuredList, new ListKeyComparator());
		
		Iterator item_iterator;
		Iterator iterator = (Iterator) structuredList.iterator();
		while ( iterator.hasNext() ) 
		{
			item_iterator =(Iterator) ((List) iterator.next()).iterator();
			key=(String) item_iterator.next().toString();
			qry=(String) item_iterator.next().toString();
			prot=(String) item_iterator.next().toString();
			uniprot=(String) item_iterator.next().toString();
			pub_id=(String) item_iterator.next().toString();
			
			if (!qry.equals(prot)) {
				if (qry.equals(prev_qry)) { qry=",,"; } else { prev_qry=qry; }
				if (prot.equals(prev_prot)) { prot=",,"; } else { prev_prot = prot; }
				if (uniprot.equals(prev_uniprot)) { uniprot=",,"; iHopString=""; } else { prev_uniprot = uniprot;  iHopString="<small><sup><a href='"+iHop_url_stub_before+uniprot+iHop_url_stub_after+"'>iHop</a></sup></small>"; }
				if (pub_id.equals(prev_pub_id)) { pub_id=",,"; } else {prev_pub_id = pub_id; }
				
				tablebody=tablebody+"<tr>\n<td align='center'>"+qry+"</td>\n<td align='center'><a href='http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&id="+uniprot+"' title='uniprot id: "+uniprot+"'>"+prot+"</a>"+iHopString+"</td><td align='center' ><a href='"+pubmed_url_stub+pub_id+"'>"+pub_id+"</a></td>\n</tr>\n";
			}
		}

		return(html_top+tablebody+html_bottom);
	}
}

