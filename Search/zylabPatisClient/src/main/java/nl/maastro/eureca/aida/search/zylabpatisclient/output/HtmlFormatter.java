// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.output;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class HtmlFormatter extends SearchResultFormatterBase {
	private enum Tags {
		LIST("ul"),
		LIST_ITEM("li"),
		SCRIPT("script", "language=\"javascript\""),
		SNIPPET_DIV("span", "class=\"snippet\" style=\"display:none\""),
		LABEL("label", "class=\"showSnippetChoice\""),
		DISPLAY_CHECKBOX("input", "type=\"checkbox\""),
		TABLE("table"),
		TABLE_ROW("tr"),
		TABLE_CELL("td"),
		TABLE_HEADER_CELL("th"),
		TABLE_HEADER("thead"),
		STYLE("style", "type=\"text/css\"");

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
				String id = result.patient.value + "-" + tinySemiUnique();
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
			if(!result.snippets.isEmpty()) {
				out.append(String.format("%s Matching documents:\n",
						Tags.LIST.open()));
				for (String docId : result.snippets.keySet()) {
					Set<String> perDocSnippets = result.snippets.get(docId);
					if(!perDocSnippets.isEmpty()) {
						out.append(String.format("%s Document: %s, snippets:\n%s",
								Tags.LIST_ITEM.open(),
								docId,
								Tags.LIST.open()));
						for (String snippet : perDocSnippets) {
							out.append(Tags.LIST_ITEM.format(snippet));
						}
						out.append(String.format("%s%s\n",
								Tags.LIST.close(),
								Tags.LIST_ITEM.close()));
					} else {
						out.append(Tags.LIST_ITEM.format("Document: " + docId));
					}
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
			+ "}\n");
		out.append(Tags.STYLE.close());
	}
	
	@Override
	public void write(Appendable out, SearchResult result) throws IOException {
		if(result.nHits > 0) {
			out.append(String.format("patient %s: found (#hits: %d)<br/>\n",
					result.patient.value,
					result.nHits));
			getSnippetStrategy().write(out, result);
		} else {
			out.append(String.format("patient %s: <em>not</em> found\n",
					result.patient.value));
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
		out.append("\t" + Tags.TABLE_HEADER_CELL.format(row.value) + "\n");
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
			out.append(String.format("found (%d hits)\n\t", r.nHits));
			getSnippetStrategy().write(out, r);
		} else {
			out.append("<em>not</em> found");
		}
		out.append(Tags.TABLE_CELL.close() + "\n");
	}

	private static String tinySemiUnique() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bytes_8 = ByteBuffer.allocate(8);
		ByteBuffer bytes_4 = ByteBuffer.allocate(4);
		bytes_8.putLong(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
		bytes_4.putInt(bytes_8.getInt(0) ^ bytes_8.get(4));
		return DatatypeConverter.printBase64Binary(bytes_4.array()).replace("=", "");
	}
	
}
