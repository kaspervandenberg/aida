// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.output;

import java.awt.Color;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.UnknownFormatConversionException;
import javax.xml.bind.DatatypeConverter;
import nl.maastro.eureca.aida.search.zylabpatisclient.DocumentId;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.ResultDocument;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.Snippet;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.Classifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;

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
			private String classExpr="class";

			@Override
			public String open() {
				return open("");
			}
			
			@Override
			public String open(String params) {
				String modParams;
				if(params.contains(classExpr)) {
					modParams = params.replaceFirst(
							classExpr + "=\"",
							classExpr +"=\"" + cssClass + " ");
				} else {
					modParams = params + " " + classExpr + "=\"" + cssClass + "\"";
				}
				return super.open(modParams);
			} },
		LABEL("label", "class=\"showSnippetChoice\""),
		DISPLAY_CHECKBOX("input", "type=\"checkbox\""),
		TABLE("table"),
		TABLE_ROW("tr"),
		TABLE_CELL("td"),
		TABLE_HEADER_CELL("th"),
		TABLE_HEADER("thead"),
		STYLE("style", "type=\"text/css\""),
		LINK("a");

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
				String id = result.patient.getValue() + "-" + QNameUtil.instance().tinySemiUnique();
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
		
		public void writeSnippet(Appendable out, SearchResult result) throws IOException {
			if(!result.getMatchingDocuments().isEmpty()) {
				out.append(String.format("%s Matching documents:\n",
						Tags.LIST.open()));
				for (ResultDocument doc: result.getMatchingDocuments()) {
					String docName;
					if(doc.isAvailable()) {
						String urlDec = URLDecoder.decode(doc.getUrl().toASCIIString(), StandardCharsets.UTF_8.name());
						String open = Tags.LINK.open(String.format("href=\"%s\"", urlDec));
						docName = String.format("%s%s%s",
								open, doc.getId().getValue(), Tags.LINK.close());
					} else {
						docName = doc.getId().getValue();
					}
					
					String docType = !doc.getType().isEmpty() ?
							String.format("(type: %s)", doc.getType()) :
							"";
									
					Set<Snippet> perDocSnippets = doc.getSnippets();
					String innerPattern = !perDocSnippets.isEmpty() ?
							"Document: %s %s %%s, snippets:\n" :
							"Document: %s %s %%s";
					String outerPattern = String.format(innerPattern, docName, docType);
					
					out.append(Tags.LIST_ITEM.open());
					writeEligibility(out, outerPattern, doc.getClassifiers());
					if(!perDocSnippets.isEmpty()) {
						out.append(Tags.LIST.open());
						for (SemanticModifier semMod : doc.getModifiers()) {
							out.append(Tags.LIST_ITEM.open());
							writeEligibility(out, "%s", Collections.singleton(semMod.getClassification()));
							out.append(Tags.LIST.open());
							for (Snippet snippet : perDocSnippets) {
								out.append(Tags.LIST_ITEM.format(snippet.getValue()));
							}
							out.append(Tags.LIST.close());
							out.append(Tags.LIST_ITEM.close());
						}
						out.append(Tags.LIST.close());
					}
					out.append(Tags.LIST_ITEM.close());
				}
			} else {
				out.append("empty");
			}
		}
		
		@Override
		public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException {
			throw new UnsupportedOperationException("Not supported.");
		}

		@Override
		public void writeTable(Appendable out, LinkedHashMap<String, Iterable<SearchResult>> results) throws IOException {
			throw new UnsupportedOperationException("Not supported.");
		}
		
	}

	private static final EnumMap<EligibilityClassification, Color> eligibilityColours;
	static {
		eligibilityColours = new EnumMap<>(EligibilityClassification.class);
		eligibilityColours.put(EligibilityClassification.NO_EXCLUSION_CRITERION_FOUND, Color.GREEN);
		eligibilityColours.put(EligibilityClassification.NOT_ELIGIBLE, Color.RED);
		eligibilityColours.put(EligibilityClassification.UNCERTAIN, Color.ORANGE);
		eligibilityColours.put(EligibilityClassification.UNKNOWN, Color.RED.darker());
	}

	private static final String eligCssPrefix = "eligibility-";
	
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
				+ "}\n");
		out.append(Tags.SCRIPT.close());
	}

	public static void writeStyle(Appendable out) throws IOException {
		out.append(Tags.STYLE.open());
		out.append(
			"table th {\n"
			+ "\t" +	"border:1px solid black;\n"
			+ "}\n\n"

			+ "table td {\n"
			+ "\t" +	"border:#ccc 1px solid;\n"
			+ "}\n\n"

			+ "table tr:nth-child(even) {\n"
			+ "\t" +	"background: #F4EFEF;\n"
			+ "}\n\n"

			+ "table tr:nth-child(odd) {"
			+ "\t" +	"background: #FAFAFA;\n"
			+ "}\n\n"
			 
			+ ".showSnippetChoice {\n"
			+ "\t" +	"color:blue;\n"
			+ "\t" + 	"font-size:8pt;\n"
			+ "\t" +	"float: right;\n"
			+ "}\n\n"

			+ ".searchHit {\n"
			+ "\t" +	"background-color:yellow;\n"
			+ "}\n\n");

		for (Map.Entry<EligibilityClassification, Color> entry : eligibilityColours.entrySet()) {
			out.append(
					"." + eligCssPrefix + entry.getKey().name().toLowerCase() + " {\n"
					+ "\t" +	"background-color:#" + Integer.toHexString(entry.getValue().getRGB()).substring(2) + ";\n"
					+ "}\n\n");
		}
		out.append(Tags.STYLE.close());
	}
	
	@Override
	public void write(Appendable out, SearchResult result) throws IOException {
		if(result.nHits > 0) {
			String innerPattern = String.format("patient %s: %%s (#hits: %d)<br/>\n",
					result.patient.getValue(),
					result.nHits);
			writeEligibility(out, innerPattern, result.getClassification());
			getSnippetStrategy().write(out, result);
		} else {
			String innerPattern = String.format("patient %s: %%s",
					result.patient.getValue());
			writeEligibility(out, innerPattern, result.getClassification());
		}
	}

	@Override
	public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException {
		new SearchResultFormatterBase() {
			@Override
			public void write(Appendable out, SearchResult result) throws IOException {
				out.append(HtmlFormatter.Tags.LIST_ITEM.open());
				HtmlFormatter.this.write(out, result);
				out.append(HtmlFormatter.Tags.LIST_ITEM.close());
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
	public void writeTable(Appendable out, LinkedHashMap<String, Iterable<SearchResult>> results) throws IOException {
		out.append(Tags.TABLE.open());
		out.append(Tags.TABLE_HEADER.open());
		out.append(Tags.TABLE_ROW.open());
		out.append(Tags.TABLE_HEADER_CELL.format("PatisNr"));
		for (String dataSetId : results.keySet()) {
			out.append(Tags.TABLE_HEADER_CELL.format(dataSetId));
		}
		out.append(Tags.TABLE_ROW.close());
		out.append(Tags.TABLE_HEADER.close());
		
		super.writeTable(out, results); 

		out.append(Tags.TABLE.close());
	}

	@Override
	protected void writeTableRow(Appendable out, Table data, PatisNumber row) throws IOException {
		out.append(Tags.TABLE_ROW.open());
		out.append("\t" + Tags.TABLE_HEADER_CELL.format(row.getValue()) + "\n");
		super.writeTableRow(out, data, row);
		out.append(Tags.TABLE_ROW.close() + "\n");
	}

	@Override
	protected void writeTableCell(Appendable out, Table data, PatisNumber row, String col) throws IOException {
		out.append("\t" + Tags.TABLE_CELL.open());
		if(data.containsKey(row) 
				? (data.get(row).containsKey(col)
					? (data.get(row).get(col).nHits > 0) : false) : false) {
			SearchResult r = data.get(row).get(col);
			String innerPattern = String.format("%%s (%d hits)", r.nHits);
			writeEligibility(out, innerPattern, r.getClassification());
			getSnippetStrategy().write(out, r);
		} else {
			writeEligibility(out, "%s", Collections.singleton(EligibilityClassification.NO_EXCLUSION_CRITERION_FOUND));
		}
		out.append(Tags.TABLE_CELL.close() + "\n");
	}

	private static void writeEligibility(Appendable out, String pattern,
			Set<EligibilityClassification> classifications) throws IOException {
		Set<EligibilityClassification> tmpClassifications = classifications;
		if(classifications.isEmpty()) {
			tmpClassifications = Collections.singleton(EligibilityClassification.NO_EXCLUSION_CRITERION_FOUND);
		}
			
		if(tmpClassifications.size()==1) {
			out.append(Tags.ELIGIBILITY_CLASS.open(String.format(
					"class=\"%s%s\"",
					eligCssPrefix,
					tmpClassifications.iterator().next().name().toLowerCase())) +
					"\n");
		} else {
			out.append(Tags.ELIGIBILITY_CLASS.open());
		}
		out.append(String.format(pattern, tmpClassifications.toString()));
		out.append(Tags.ELIGIBILITY_CLASS.close() + "\n");
	}
}
