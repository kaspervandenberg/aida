// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.contentNegotiation;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Implement HTTP content negotiation.  Select the best handler that can
 * produce content in the type preferred by the client.
 * 
 * @param <TOutputFormatter>	class that will format the output into a format 
 * 		accepted by the client.
 * 
 * @author	Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net> 
 */
public class ContentTypeNegotiator<TOutputFormatter> {
	private static final String QUALITY_PARAM = "q";
	private static final String QUALITY_PARAM_SEPARATOR = 
			Separators.PARAMETER.getRegExp() 
			+ QUALITY_PARAM 
			+ Separators.PARAM_VALUE.getRegExp();

	
	private final Map<MediaType, TOutputFormatter> formatterRepository =
			new HashMap<>();

	public void put(MediaType mediatype, TOutputFormatter formatter) {
		formatterRepository.put(mediatype, formatter);
	}

	public TOutputFormatter getPreferredFormatter(String acceptHeader) {
		return getPreferredFormatter(parseAcceptHeader(acceptHeader));
	}
	
	private TOutputFormatter getPreferredFormatter(
			Map<MediaType, Double> preferredTypes) {
		MediaType mostpreferred = null;
		double bestPreference = 0.0;

		for (MediaType supportedMediatype : formatterRepository.keySet()) {
			if(MediaType.mapContainsMatching(preferredTypes, supportedMediatype)) {
				double preference = MediaType.mapGetMatching(preferredTypes, supportedMediatype);
				if(preference > bestPreference) {
					mostpreferred = supportedMediatype;
					bestPreference = preference;
				}
			}
		}
		if(mostpreferred == null) {
			throw new NoSuchElementException("No accepted formatter found.");
		}
		TOutputFormatter result = formatterRepository.get(mostpreferred);
		if(result == null) {
			throw new IllegalStateException(String.format(
					"Repository does not contain an element for %s",
					mostpreferred.toString()));
		}
		return result;
		
	}

	private static Map<MediaType, Double> parseAcceptHeader(String header) {
		String types[] = Separators.LIST_ELEMENT.split(header);
		Map<MediaType, Double> result = new HashMap<>(types.length);
		
		for (String t : types) {
			String mediaRange_acceptParams[] = t.split(QUALITY_PARAM_SEPARATOR, 2);
			Double quality = 1.0;
			if(mediaRange_acceptParams.length == 2) {
				String s_quality = Separators.PARAMETER.split(mediaRange_acceptParams[1])[0];
				quality = Double.valueOf(s_quality);
			}
			result.put(MediaType.parse(mediaRange_acceptParams[0]), quality);
		}
		return result;
	}
}
