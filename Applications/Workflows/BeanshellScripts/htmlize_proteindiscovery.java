/* beanshell script to compile lists of text mining results into a table */
/* 
variables:
structuredList //  list of (query protein, discovered protein, publication) lists
*/
String html_top="<html>\n<head>\n<title>Results of text mining workflow</title>\n<link href='http://www.adaptivedisclosure.org/workflows/AIDA_workflows.css' rel='stylesheet' type='text/css'/>\n</head>\n<body>\n<div id='wrapper'>\n<div id='header'>\n<span id='aida_logo' title='Adaptive Information Disclosure Application'><a href='http://adaptivedisclosure.org'>Adaptive Information Disclosure Application</a></span>\n<span id='vle_logo' title='Virtual Laboratory for e-Science'><a href='http://www.vl-e.nl/'>Virtual Laboratory for e-Science</a></span>\n</div><!-- header -->\n<div id='page_title'>\n<h1><span title='Adaptive Information Disclosure Application'>AIDA</span> workflow results</h1>\n</div><!-- page_title -->\n";

String html_top=html_top+"<table align='center' summary='this table gives the results of the text mining workflow'>\n<caption><em>Results of text mining workflow</em></caption>\n<tr>\n<th>Query<br/>protein</th>\n<th>Associated<br/>with</th>\n<th>Published in<br/><small>(PubMed ID)</small></th>\n</tr>\n";

String html_bottom="</table>\n\n<div id='footer'>\n<div id='AIDA'/>\n</div><!-- footer -->\n</div><!-- wrapper --></body>\n</html>\n";

String qry;
String prot;
String uniprot;
String pub_id;

String prev_qry="";
String prev_prot="";
String prev_uniprot="";
String prev_pub_id="";

String tablebody="";

Iterator item_iterator;
Iterator iterator = structuredList.iterator();
while ( iterator.hasNext() ) 
{
	item_iterator = iterator.next().iterator();
	qry=(String) item_iterator.next().toString();
	prot=(String) item_iterator.next().toString();
	uniprot=(String) item_iterator.next().toString();
	pub_id=(String) item_iterator.next().toString();
	
	if (!qry.equals(prot)) {
		if (qry.equals(prev_qry)) { qry=",,"; } else { prev_qry=qry; }
		if (prot.equals(prev_prot)) { prot=",,"; } else { prev_prot = prot; }
		if (uniprot.equals(prev_uniprot)) { uniprot=",,"; iHopString=""; } else { prev_uniprot = uniprot;  iHopString="<small><sup><a href='http://www.ihop-net.org/UniPub/iHOP/?search="+uniprot+"&field=UNIPROT__AC&ncbi_tax_id=9606&organism_syn='>iHop</a></sup></small>"; }
		if (pub_id.equals(prev_pub_id)) { pub_id=",,"; } else {prev_pub_id = pub_id; }
		
		tablebody=tablebody+"<tr>\n<td align='center'>"+qry+"</td>\n<td align='center'><a href='http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&id="+uniprot+"' title='uniprot id: "+uniprot+"'>"+prot+"</a>"+iHopString+"</td><td align='center' ><a href='http://www.ncbi.nlm.nih.gov/sites/entrez?cmd=Retrieve&amp;db=PubMed&amp;list_uids="+pub_id+"'>"+pub_id+"</a></td>\n</tr>\n";
	}
}

html_table=html_top+tablebody+html_bottom;