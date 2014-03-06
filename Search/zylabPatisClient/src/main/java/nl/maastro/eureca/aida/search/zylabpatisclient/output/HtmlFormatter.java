// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.output;

import java.awt.Color;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.ResultDocument;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResultTable;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.Snippet;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparison;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparisonTable;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class HtmlFormatter extends SearchResultFormatterBase {
	private enum Tags {
		LIST("ul"),
		LIST_ITEM("li"),
		SCRIPT("script", "language=\"javascript\""),
		SNIPPET_DIV("span", "class=\"snippet\" style=\"display:none\""),
		ELIGIBILITY_CLASS("span") {
			private String cssClass="eligibily";

			@Override
			public String open() {
				return open("");
			}
			
			@Override
			public String open(String params) {
				String modParams = combineClass(cssClass, params);
				return super.open(modParams);
			} },
		LABEL("label", "class=\"showSnippetChoice\""),
		DISPLAY_CHECKBOX("input", "type=\"checkbox\""),
		TABLE("table"),
		TABLE_ROW("tr"),
		TABLE_CELL("td"),
		TABLE_HEADER_CELL("th"),
		ROTATED_TABLE_HEADER_CELL("th><div><span") {
			private final String cssClass="rotate-45";

			@Override
			public String open() {
				return "<th class=\"" + cssClass + "\"><div><span>";
			}

			@Override
			public String open(String params) {
				String modParams = combineClass(cssClass, params);
				return String.format("<th %s><div<span>", modParams);
			}

			@Override
			public String close() {
				return "</span></div></th>";
			}

			@Override
			public String format(String arg) {
				return String.format("%s%s%s\n", open(), arg, close());
			}
			
		},
		TABLE_HEADER("thead"),
		STYLE("style", "type=\"text/css\""),
		LINK("a"),
		DOC_HTML("html"),
		DOC_HEAD("head"),
		DOC_BODY("body"),
		DOC_TITLE("title"),
		META_CHARSET("meta",
				String.format("charset=\"%s\"", StandardCharsets.UTF_8.name())),
		TITLE("h1"),
		SUBTITLE("h2");

		private final String tag;
		private final String tag_o;
		private final String tag_c;
		private final String pattern;

		private Tags(final String tag_) {
			this(tag_, "");
		}

		private Tags(final String tag_, final String params_) {
			this.tag = combine(tag_, params_);
			this.tag_o = String.format("<%s>", this.tag);
			this.tag_c = String.format("</%s>", tag_);
			pattern = this.tag_o + "%s" + this.tag_c + "\n";
		}

		public String open() {
			return tag_o;
		}

		public String open(final String params) {
			return String.format("<%s>", combine(tag, params));
		}

		public String close() {
			return tag_c;
		}

		public String format(String arg) {
			return String.format(this.pattern, arg);
		}

		private static String combine(final String tag_, final String params_) {
			return tag_ +
					((params_.isEmpty() || params_.startsWith(" "))
						? params_
						: " " + params_);
		}

		protected String combineClass(String existingClass, String params) {
			final String classExpr="class";
			
			String modParams;
			if(params.contains(classExpr)) {
				modParams = params.replaceFirst(
						classExpr + "=\"",
						classExpr +"=\"" + existingClass + " ");
			} else {
				modParams = params + " " + classExpr + "=\"" + existingClass + "\"";
			}
			return modParams;
		}
	}
	
	public enum SnippetDisplayStrategy implements SearchResultFormatter {
		SHOW_ALWAYS {
			@Override
			public void write(Appendable out, SearchResult result) throws IOException {
				writeSnippet(out, result);
			} },
		SHOW_NEVER {
			@Override
			public void write(Appendable out, SearchResult result) throws IOException {
				// No code needed
			} },
		DYNAMIC_SHOW {

			@Override
			public void write(Appendable out, SearchResult result) throws IOException {
				String id = result.getPatient().getValue() + "-" + QNameUtil.instance().tinySemiUnique();
				out.append(Tags.LABEL.open());
				out.append(Tags.DISPLAY_CHECKBOX.open(
						String.format(
							"id=\"cb-%1$s\" onclick=\"toggleShow(\'cb-%1$s\',\'%1$s\')\"",
							id)));
				out.append(Tags.DISPLAY_CHECKBOX.close());
				out.append("details");
				out.append(Tags.LABEL.close() + "\n");
				
				out.append(Tags.SNIPPET_DIV.open(String.format("id=\"%s\"", id)));
				writeSnippet(out, result);
				out.append(Tags.SNIPPET_DIV.close());
			}

		}
		;
		
		private static void writeSnippet(Appendable out, SearchResult result) throws IOException {
			writeDocumentList(out, result.getMatchingDocuments());
		}

		private static void writeDocumentList(Appendable out, 
				Collection<ResultDocument> docs) throws IOException {
			if(!docs.isEmpty()) {
				out.append("\tMatching documents:\n");
				out.append(Tags.LIST.open());
				for (ResultDocument doc : docs) {
					out.append(Tags.LIST_ITEM.open());
					writeDocument(out, doc);
					out.append(Tags.LIST_ITEM.close());
				}
				out.append(Tags.LIST.close());
			} else {
				out.append("\tsearch results contain no snippets\n");		
			}
		}

		private static void writeDocument(Appendable out, ResultDocument doc)
				throws IOException {
			String docName;
			if(doc.isAvailable()) {
				String urlDec = URLDecoder.decode(doc.getUrl().toASCIIString(), StandardCharsets.UTF_8.name());
				String open = Tags.LINK.open(String.format("href=\"%s\"", urlDec));
				docName = String.format("%s%s%s",
						open, doc.getId().getValue(), Tags.LINK.close());
			} else {
				docName = doc.getId().getValue();
			}
			
			String docType = doc.getTypeOrDefault("");
			String docTypeMsg = (!docType.isEmpty()) ?	// if docType is available show it, else ignore it. 
					String.format("(type: %s)", docType) :
					"";
							
			Set<Snippet> perDocSnippets = doc.getSnippets();
			String innerPattern = !perDocSnippets.isEmpty() ?
					"Document: %s %s %%s, snippets:\n" :
					"Document: %s %s %%s";
			String outerPattern = String.format(innerPattern, docName, docTypeMsg);
			
			writeEligibility(out, outerPattern, doc.getClassifiers());
			writePerDocModifierList(out, doc, doc.getModifiers());
		}

		private static void writePerDocModifierList(Appendable out, 
				ResultDocument doc, Set<SemanticModifier> modifiers)
				throws IOException {
			out.append(Tags.LIST.open());
			for (SemanticModifier semMod : modifiers) {
				out.append(Tags.LIST_ITEM.open());
				writeModifier(out, doc, semMod);
				out.append(Tags.LIST_ITEM.close());
			}
			out.append(Tags.LIST.close());
		}

		private static void writeModifier(Appendable out,
				ResultDocument doc, SemanticModifier semMod)
				throws IOException {
			writeEligibility(out, "%s\n", 
					Collections.singleton(semMod.getClassification()));
			writeSnippetList(out, doc.getSnippets(semMod));
		}

		private static void writeSnippetList(Appendable out,
				Set<Snippet> snippets) throws IOException {
			out.append(Tags.LIST.open());
			for (Snippet snippet : snippets) {
				out.append(Tags.LIST_ITEM.open());
				writeSnippet(out, snippet);
				out.append(Tags.LIST_ITEM.close());
			}
			out.append(Tags.LIST.close());
		}

		private static void writeSnippet(Appendable out, Snippet snippet) throws IOException {
			out.append(snippet.getValue());
		}

		@Override
		public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException {
			throw new UnsupportedOperationException("Not supported.");
		}

		@Override
		public void writeTable(Appendable out, SearchResultTable results) throws IOException {
			throw new UnsupportedOperationException("Not supported.");
		}
		
	}

	private static final EnumMap<ConceptFoundStatus, Color> eligibilityColours;
	static {
		eligibilityColours = new EnumMap<>(ConceptFoundStatus.class);
		eligibilityColours.put(ConceptFoundStatus.NOT_FOUND, Color.GREEN);
		eligibilityColours.put(ConceptFoundStatus.FOUND, Color.RED);
		eligibilityColours.put(ConceptFoundStatus.UNCERTAIN, Color.ORANGE);
		eligibilityColours.put(ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN, Color.RED.darker());
	}

	private static final String eligCssPrefix = "eligibility-";

	public static void writeDocStart(Appendable out, String title) throws IOException {
		out.append(Tags.DOC_HTML.open() + "\n");
		out.append(Tags.DOC_HEAD.open() + "\n");
		out.append(Tags.DOC_TITLE.format(title));
		out.append(Tags.META_CHARSET.format(""));
		writeScript(out);
		writeStyle(out);
		out.append(Tags.DOC_HEAD.close() + "\n");
		out.append(Tags.DOC_BODY.open() + "\n");
		out.append(Tags.TITLE.format(title));
	}

	public static void writeDocEnd(Appendable out) throws IOException {
		out.append(Tags.DOC_BODY.close() + "\n");
		out.append(Tags.DOC_HTML.close());
	}
	
	public static void writeScript(Appendable out) throws IOException {
		out.append(Tags.SCRIPT.open() + "\n");
		out.append(
				"function toggleShow(cbId, id) {\n"
				+ "\t" + "el = document.getElementById(id);\n"
				+ "\t" + "cb = document.getElementById(cbId);\n"
				+ "\n"
				+ "\t" + "if(el && el.style && cb) {\n"
				+ "\t\t" + 	"el.style.display = cb.checked ? 'inline' : 'none';\n"
				+ "\t" + "}\n"
				+ "}\n\n");
		out.append(Tags.SCRIPT.close());
	}

	public static void writeStyle(Appendable out) throws IOException {
		final String MAASTRO_BLUE= "#0c4c90"; 
		final String MAASTRO_TURQUOISE= "#09979e";
		final String VARIANT_BLUE_LIGHT= "#EAF3FD";
		final String VARIANT_TURQUOISE_LIGHT = "#CFEDEE";
		final String GREY = "#CCC";
		
		out.append(Tags.STYLE.open());
		out.append("" 
			+ "h1 {\n"
			+ "\t" +	"font-size: 400%;\n"
			+ "\t" +	"color: " + MAASTRO_TURQUOISE + ";\n"
			+ "}\n\n"
				
			+ "h2 {\n"
			+ "\t" +	"font-size: 350%;\n"
			+ "\t" +	"color: " + MAASTRO_TURQUOISE +";\n"
			+ "\t" +	"font-style:italic;\n"
			+ "}\n\n"
				
			+ "table th {\n"
			+ "\t" +	"font-family: Sans-serif;\n"
			+ "\t" +	"font-size:125%;\n"
			+ "\t" +	"color: white;\n"
			+ "\t" +	"padding: 0.2em;\n"
			+ "}\n\n"

			+ "table th:not(.rotate-45) {\n"
			+ "\t" +	"background: " + MAASTRO_BLUE +";\n"
			+ "}\n\n"

			+ "table:not(.table-header-rotated) thead th {\n"
			+ "\t" +	"border-left-style: solid;\n"	
			+ "\t" +	"border-left-width: 0.1em;\n"	
			+ "\t" +	"border-left-color: white;\n\n"	

			+ "\t" +	"border-right-style: solid;\n"	
			+ "\t" +	"border-right-width: 0.1em;\n"	
			+ "\t" +	"border-right-color: white;\n\n"

			+ "\t" +	"border-top-left-radius: 0.8em\n"
			+ "\t" +	"border-top-right-radius: 0.8em\n\n"
			+ "}\n\n"

			+ "table.table-header-rotated th.rotate-45 {\n"
			+ "\t" +	"height: 240px;\n"
			+ "\t" +	"width: 80px;\n"
			+ "\t" +	"min-width: 80px;\n"
			+ "\t" +	"max-width: 80px;\n"
			+ "\t" +	"position: relative;\n"
			+ "\t" +	"vertical-align: bottom;\n"
			+ "\t" +	"padding: 0px\n"
			+ "\t" +	"line-height: 0.8;\n"
			+ "}\n\n"
				
			+ "table.table-header-rotated th.rotate-45 div {\n"
			+ "\t" +	"overflow: hidden;\n"
			+ "\t" +	"position: relative;\n"
			+ "\t" +	"top: 0px;\n"
			+ "\t" +	"left: 120px;\n"
			+ "\t" +	"height: 100%;\n\n"
				
		  	+ "\t" +	"-ms-transform:skew(-45deg,0deg);\n"
			+ "\t" +	"-moz-transform:skew(-45deg,0deg);\n"
			+ "\t" +	"-webkit-transform:skew(-45deg,0deg);\n"
			+ "\t" +	"-o-transform:skew(-45deg,0deg);\n"
			+ "\t" +	"transform:skew(-45deg,0deg);\n\n"

			+ "\t" +	"border-left-style: solid;\n"	
			+ "\t" +	"border-left-width: 0.1em;\n"	
			+ "\t" +	"border-left-color: white;\n\n"	

			+ "\t" +	"border-right-style: solid;\n"	
			+ "\t" +	"border-right-width: 0.1em;\n"	
			+ "\t" +	"border-right-color: white;\n\n"

			+ "\t" +	"border-top-left-radius: 0.8em;\n"
			+ "\t" +	"border-top-right-radius: 0.8em;\n\n"
			+ "\t" +	"background: " + MAASTRO_BLUE +";\n"
			+ "}\n\n"
	
			+ "table.table-header-rotated th.rotate-45 span {\n"
			+ "\t" +	"-ms-transform:skew(45deg,0deg) rotate(315deg);\n"
			+ "\t" +	"-moz-transform:skew(45deg,0deg) rotate(315deg);\n"
			+ "\t" +	"-webkit-transform:skew(45deg,0deg) rotate(315deg);\n"
			+ "\t" +	"-o-transform:skew(45deg,0deg) rotate(315deg);\n"
			+ "\t" +	"transform:skew(45deg,0deg) rotate(315deg);\n\n"

			+ "\t" +	"position: absolute;\n"
			+ "\t" +	"bottom: 30px;\n" 
			+ "\t" +	"left: -5px;\n"
			+ "\t" +	"display: inline-block;\n"
  			+ "\t" +	"width: 85px;\n"
			+ "\t" +	"text-align: left;\n"
			+ "}\n\n"

			+ "table td {\n"
			+ "\t" +	"font-family: Sans-serif;\n"
			+ "\t" +	"border-top-color: " + GREY + ";\n"
			+ "\t" + 	"border-top-width: 0.1em;\n"
			+ "\t" +	"border-top-style: solid;\n"
			+ "\t" +	"border-bottom-color: " + GREY + ";\n"
			+ "\t" + 	"border-bottom-width: 0.1em;\n"
			+ "\t" +	"border-bottom-style: solid;\n"
			+ "\t" +	"border-left-style: none;\n"
			+ "\t" +	"border-right-style: none;\n"
			+ "\t" +	"padding: 0.8em"
			+ "}\n\n"

			+ "table tr:nth-child(even) {\n"
			+ "\t" +	"background: " + VARIANT_TURQUOISE_LIGHT + ";\n"
			+ "}\n\n"

			+ "table tr:nth-child(odd) {"
			+ "\t" +	"background: " + VARIANT_BLUE_LIGHT + ";\n"
			+ "}\n\n"
			 
			+ ".showSnippetChoice {\n"
			+ "\t" +	"color:" + MAASTRO_BLUE + ";\n"
			+ "\t" + 	"font-size:8pt;\n"
			+ "\t" +	"float: right;\n"
			+ "}\n\n"

			+ ".searchHit {\n"
			+ "\t" +	"background-color:yellow;\n"
			+ "}\n\n");

		for (Map.Entry<ConceptFoundStatus, Color> entry : eligibilityColours.entrySet()) {
			out.append(
					"." + eligCssPrefix + entry.getKey().name().toLowerCase() + " {\n"
					+ "\t" +	"background-color:#" + Integer.toHexString(entry.getValue().getRGB()).substring(2) + ";\n"
					+ "}\n\n");
		}
		out.append(Tags.STYLE.close());
	}

	public static void writeValidationCounts(Appendable out, ResultComparisonTable table) throws IOException {
		out.append(HtmlFormatter.Tags.SUBTITLE.format("Summary"));
		out.append(HtmlFormatter.Tags.TABLE.open("class=\"table-header-rotated\""));
		writeTableHeaderSingleRow(out, 1, table.getQualifications(), true);
		
		for (ResultComparison comp : table.getComparisons()) {
			writeValidationCountRow(out, comp, table.getQualifications());
		}
		
		out.append(HtmlFormatter.Tags.TABLE.close());
	}

	
	@Override
	public void write(Appendable out, SearchResult result) throws IOException {
		if(result.getTotalHits() > 0) {
			String innerPattern = String.format("\tpatient %s: %%s (#hits: %d)<br/>\n",
					result.getPatient().getValue(), result.getTotalHits());
			writeEligibility(out, innerPattern, result.getClassification());
			getSnippetStrategy().write(out, result);
		} else {
			String innerPattern = String.format("\tpatient %s: %%s",
					result.getPatient().getValue());
			writeEligibility(out, innerPattern, result.getClassification());
		}
	}

	@Override
	public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException {
		new SearchResultFormatterBase() {
			@Override
			public void write(Appendable out, SearchResult result) throws IOException {
				out.append("\t" + HtmlFormatter.Tags.LIST_ITEM.open());
				HtmlFormatter.this.write(out, result);
				out.append("\t" + HtmlFormatter.Tags.LIST_ITEM.close());
			}

			@Override
			public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException {
				out.append(Tags.LIST.open() + "\n");
				super.writeList(out, results);
				out.append(Tags.LIST.close() + "\n");
			}
		}.writeList(out, results);
	}
	
	@Override
	public void writeTable(Appendable out, SearchResultTable table) throws IOException {
		out.append(HtmlFormatter.Tags.SUBTITLE.format("Results"));
		out.append(Tags.TABLE.open());
		out.append(Tags.TABLE_HEADER.open());
		out.append(Tags.TABLE_ROW.open());
		out.append(Tags.TABLE_HEADER_CELL.format("PatisNr"));
		for (String dataSetId : table.getColumnNames()) {
			out.append(Tags.TABLE_HEADER_CELL.format(dataSetId));
		}
		out.append(Tags.TABLE_ROW.close());
		out.append(Tags.TABLE_HEADER.close());
		
		super.writeTable(out, table); 

		out.append(Tags.TABLE.close());
	}

	@Override
	protected void writeTableRow(Appendable out, SearchResultTable data, PatisNumber row) throws IOException {
		out.append(Tags.TABLE_ROW.open());
		out.append("\t" + Tags.TABLE_HEADER_CELL.format(row.getValue()) + "\n");
		super.writeTableRow(out, data, row);
		out.append(Tags.TABLE_ROW.close() + "\n");
	}

	@Override
	protected void writeTableCell(Appendable out, SearchResultTable data, PatisNumber row, String col) throws IOException {
		out.append("\t" + Tags.TABLE_CELL.open());
		SearchResult r = data.getCell(row, col);
		String innerPattern = String.format("%%s (%d hits)", r.getTotalHits());
		writeEligibility(out, innerPattern, r.getClassification());
		getSnippetStrategy().write(out, r);
		out.append(Tags.TABLE_CELL.close() + "\n");
	}

	private static void writeEligibility(Appendable out, String pattern,
			Set<ConceptFoundStatus> classifications) throws IOException {
		Set<ConceptFoundStatus> tmpClassifications = classifications;
		if(classifications.isEmpty()) {
			tmpClassifications = Collections.singleton(ConceptFoundStatus.NOT_FOUND);
		}
			
		if(tmpClassifications.size()==1) {
			out.append("\t\t" + Tags.ELIGIBILITY_CLASS.open(String.format(
					"class=\"%s%s\"",
					eligCssPrefix,
					tmpClassifications.iterator().next().name().toLowerCase())) +
					"\n");
		} else {
			out.append("\t\t" + Tags.ELIGIBILITY_CLASS.open());
		}
		out.append("\t\t" + String.format(pattern, tmpClassifications.toString()));
		out.append("\t\t" + Tags.ELIGIBILITY_CLASS.close() + "\n");
	}

	private static <T extends Enum<T>> void writeTableHeaderSingleRow(
			Appendable out, int nEmptyCells, Iterable<T> headerItems, boolean rotated)
			throws IOException {
		final int maxLabelLength = 14;
		out.append(HtmlFormatter.Tags.TABLE_HEADER.open());
		out.append(HtmlFormatter.Tags.TABLE_ROW.open());
		
		for (int i = 0; i < nEmptyCells; i++) {
			out.append(HtmlFormatter.Tags.TABLE_HEADER_CELL.format(""));
		}
		for (T item : headerItems) {
			String itemName = item.name();
			if(itemName.length() >= maxLabelLength) {
				String prefix = itemName.substring(0, maxLabelLength);
				String rest = itemName.substring(maxLabelLength);
				String alteredRest = rest.replaceFirst("_", "_<br/>");
				itemName = prefix + alteredRest;
			}
			if(rotated) {
				out.append(HtmlFormatter.Tags.ROTATED_TABLE_HEADER_CELL.format(itemName));
			} else {
				out.append(HtmlFormatter.Tags.TABLE_HEADER_CELL.format(itemName));
			}
		}
		
		out.append(HtmlFormatter.Tags.TABLE_ROW.close());
		out.append(HtmlFormatter.Tags.TABLE_HEADER.close());
	}

	private static void writeValidationCountRow(Appendable out, ResultComparison comparison, 
			Iterable<ResultComparison.Qualifications> qualifications)
			throws IOException {
		out.append(HtmlFormatter.Tags.TABLE_ROW.open());
		out.append(HtmlFormatter.Tags.TABLE_HEADER_CELL.format(comparison.getConcept().getName().getLocalPart()));
		
		for (ResultComparison.Qualifications qual : qualifications) {
			out.append(HtmlFormatter.Tags.TABLE_CELL.format(Integer.toString(comparison.getCount(qual))));
		}

		out.append(HtmlFormatter.Tags.TABLE_ROW.close());
	}
}
