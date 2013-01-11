/* variables
	poi_count_in_corpus
	corpus_total
return
	relative_frequency
*/
import java.lang.Math;

Double rf = new Double(-Math.log((double)(Integer.parseInt( poi_count_in_corpus ) ) / ((double) (Integer.parseInt( corpus_total )))));

relative_frequency = rf.toString();