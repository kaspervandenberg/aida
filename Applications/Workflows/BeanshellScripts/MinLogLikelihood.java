/* variables
	poi_frequency
	dp_frequency
	poi_dp_frequency
	corpus_frequency
return
	minloglikelihood
*/
import java.lang.Math;

Double mll = new Double(-((double) Math.log(((double) Integer.parseInt( poi_dp_frequency ) ) / ((double) Integer.parseInt(poi_frequency))) - Math.log(((double) Integer.parseInt( dp_frequency )) / ((double) Integer.parseInt( corpus_frequency )))));

minloglikelihood = mll.toString();