/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.File;
import java.util.Date;

/**
 *
 * @author kasper
 */
public class FileNames {
	public File createHtmlResultsFile() {
		return createResultsFile("", "html");
	}

	public File createJsonResultsFile(Concept concept) {
		return createResultsFile(concept.getName().getLocalPart(), "json");
	}

	private File createResultsFile(String infixConcept, String extension) {
		String seperatedInfixConcept;
		if (!infixConcept.isEmpty() && !infixConcept.startsWith("-")) {
			seperatedInfixConcept = "-" + infixConcept;
		} else {
			seperatedInfixConcept = infixConcept;
		}
		Date now = new Date();
		String fileName = String.format("results%1-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS.%3",
				seperatedInfixConcept, now, extension);
		File f = new File(fileName);

		return f;
	}

	
}
