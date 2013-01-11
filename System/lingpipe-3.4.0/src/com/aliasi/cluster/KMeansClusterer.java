/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */


package com.aliasi.cluster;

import com.aliasi.util.Arrays;
import com.aliasi.util.Distance;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.SmallSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A <code>KMeansClusterer</code> provides an implementation of
 * standard k-means clustering based on vectors constructed by feature
 * extractors.  An instance fixes a specific value of <code>k</code>,
 * the number of clusters returned.
 *
 * <h3>K-Means Clustering Algorithm</h3>
 *
 * <p>If fewer than the maximum number of elements, <code>k</code>, is
 * clustered, a clustering with that number of singleton clusters is
 * returned.
 *
 * <p>The elements being clustered are first converted into feature
 * vectors using a feature extractor.  These feature vectors are then
 * evenly distributed among the clusters using random assignment.
 *
 * <p>Each cluster is represented by the centroid of the feature
 * vectors assigned to it.  Centroids are just dimension-wise averages:
 *
 * <pre>
 *     centroid(v[0],...,v[n-1]) = (v[0] + ... + v[n-1]) / n</pre>
 *
 * and are thus vectors in the same space as the feature vectors
 * being clustered.
 *
 * <p>Feature vectors are always compared to cluster centroids
 * using Euclidean distance:
 *
 * <pre>
 *     distance(x,y) = sqrt( (x - y) * (x - y) )
 *                   = sqrt(<big><big>&Sigma;</big></big><sub><sub>i</sub></sub> (x[i] - y[i])<sup>2</sup>)</pre>
 *
 * Thus any normalization should be done as part of the feature
 * extraction phase.  A common normalization is to divide each feature
 * vector by its length to produce normal vectors, which weights each
 * dimension equally and results in distances monotonically related to
 * cosines.
 *
 * <p>The algorithm then iteratively improves cluster assignments.
 * Each iteration consists of walking through each feature vector and
 * assigning it to the cluster with the closest centroid.  Ties are
 * determined non-deterministically.  The centroids are then
 * recomputed at the end of each iteration.  When no elements change
 * clusters during an iteration, the result is returned.  The
 * algorithm will also return if the maximum number of iterations is
 * reached without arriving at a fixed point.
 *
 * <p>These iterations have some fancy names in the literature.  The
 * whole approach is known as &quot;Lloyd's Algorithm&quot;,
 * &quot;Voronoi iteration&quot;, or simply &quot;relaxation&quot;.
 * The final centroids forms a so-called &quot;Voronoi Tesselation&quot;
 * of the feature vector space of the objects being clustered.  This
 * tesselation is similar to k-nearest neighbors classification (see
 * {@link com.aliasi.classify.KnnClassifier}).
 *
 *
 * <h3>K-means as Minimization</h3>
 *
 * <p>K-means clustering may be viewed as a gradient descent
 * solution to the minimization of distances to cluster centroids,
 * which we define here as error:
 *
 * <pre>
 *     error(cs) = <big><big><big>&Sigma;</big></big></big><sub>c in cs</sub> <big><big><big>&Sigma;</big></big></big><sub>x in c</sub> distance(x,centroid(C))</pre>
 *
 * where <code>cs</code> is the set of clusters, <code>c</code> is a
 * cluster in <code>c</code>, <code>centroid(c)</code>
 * is the vector representing the centroid of the cluster <code>c</code>, and
 * <code>distance(x,centroid(x))</code> is the distance between an
 * element <code>x</code> of cluster <code>c</code> and the cluster's
 * centroid.
 *
 *
 * <h3>Convergence Guarantees</h3>
 *
 * <p>K-means clustering is guaranteed to converge to a solution
 * because each increment reduces error as defined in the previous
 * section.  There are only finitely many clusterings, so eventually
 * k-means will converge.  The only problem is that the size of the
 * finite set of clusterings is exponential in the number of input
 * elements.
 *
 * <h3>Relation to Gaussian Mixtures and Expectation/Maximization</h3>
 *
 * <p>The k-means clustering algorithm is implicitly based on a
 * multi-dimensional Gaussian with independent dimensions with their
 * own means and variances.  Estimates are carried out by maximum
 * likelihood; that is, no prior, or equivalently the fully
 * uninformative prior, is used.  Where k-means differs from standard
 * expectation/maximization (EM) is that k-means reweights the
 * expectations so that the closest centroid gets an expectation of 1.0
 * and all other centroids get an expectation of 0.0.  This approach
 * has been called &quot;winner-take-all&quot; EM in the EM literature.
 *
 *
 * <h3>Local Maxima and Multiple Runs</h3>
 *
 * <p>Like the EM algorithm, k-means clustering is highly sensitive
 * to the initial assignment of elements.  In practice, it is often
 * helpful to apply k-means clustering repeatedly to the same input
 * data, returning the clustering which optimizes some evaluation
 * metric.  This metric is typically within-cluster scatter, because
 * the number of dimensions is fixed.  Within-cluster scatter may
 * be computed with the static method {@link ClusterScore#withinClusterScatter(Set,Distance)}.
 *
 *
 * <h3>Degenerate Solutions</h3>
 *
 * <p>In some cases, the iterative approach taken by k-means leads to
 * a solution in which not every cluster is populated.  This happens
 * during a step in which no feature vector is closest to a given
 * centroid.  In many of these cases, rerunning the clusterer will
 * find a solution with k clusters.
 *
 *
 * <h3>Picking a good <code>K</code></h3>
 *
 * <p>The number of clusters, k, may also be varied.  In this case,
 * new k-means clustering instances must be created, as each uses
 * a fixed number of clusters.
 *
 * <p>By varying <code>k</code>, the maximum number of clusters,
 * the within-cluster scatter may be compared across different choices
 * of <code>k</code>.  Typically, a value of <code>k</code> is chosen
 * at a knee of the within-cluster scatter scores.  There are automatic
 * ways of performing this selection, though they are heuristically
 * rather than theoretically motivated.
 *
 * <p>In practice, it is technically possible, though unlikely, for
 * clusters to wind up with no elements in them.  This implementation
 * will simply return fewer clusters than the maximum specified in
 * this case.
 *
 *
 * <h3>Multiple Runs and Bootstrap Relation Estimates</h3>
 *
 * <p>Multiple runs may be used to provide bootstrap estimates
 * of the relatedness of any two elements.   Bootstrap estimates
 * work by subsampling the elements to cluster with replacement
 * and then running k-means on them.  This is repeated multiple
 * times, and the percentage of runs in which two elements fall
 * in the same cluster forms the bootstrap estimate of the likelihood
 * that they are in the same cluster.
 *
 *
 * <h3>Efficiency</h3>
 *
 * <p>The main advantage K-means has over hierarchical clustering is
 * that it only requires linear time in the number of elements being
 * clustered.  There's a constant factor for the maximum number of
 * iterations allowed and another constant factor for the length of
 * the feature vectors.
 *
 * <h3>Implementation Notes</h3>
 *
 * <p>The current implementation is an
 * inefficient brute-force approach that computes the distance between
 * every element and every centroid on every iteration.  These
 * distances are computed between maps, which itself is not very
 * efficient.  In the future, we may substitute a more efficient
 * version of k-means implemented to this same interface.
 *
 * <p>More efficient implementations could use kd-trees to index the
 * points, could cache scores of input feature vectors against
 * clusters, and would only compute differential updates when elements
 * change cluster.  Threading could also be used to compute the
 * distances.
 *
 * <h3>References</h3>
 *
 * <ul>
 * <li>
 * J. B. MacQueen. 1967.  Some Methods for classification and Analysis of Multivariate Observations.
 * <i> Proceedings of 5-th Berkeley Symposium on Mathematical Statistics and Probability</i>.
 * University of California Press.
 *
 * <li>
 * Andrew Moore's <a href="http://www.autonlab.org/tutorials/kmeans11.pdf">K-Means Tutorial</a> including most of the mathematics
 * </li>
 *
 * <li>
 * Matteo Matteucci's <a href="http://www.elet.polimi.it/upload/matteucc/Clustering/tutorial_html/kmeans.html">K-Means Tutorial</a> including a
very nice <a href="http://www.elet.polimi.it/upload/matteucc/Clustering/tutorial_html/AppletKM.html">interactive servlet demo</a>
 * </li>
 *
 * <li>
 * Hastie, T., R. Tibshirani and J.H. Friedman. 2001. <i>The
 * Elements of Statistical Learning</i>. Springer-Verlag.
 * </li>
 * <li>
 * Wikipedia: <a href="http://en.wikipedia.org/wiki/K-means_algorithm">K-means Algorithm</a>
 * </li>
 * </ul>
 *
 * @author Bob Carpenter
 * @version 3.1.1
 * @since   LingPipe2.0
 */
public class KMeansClusterer<E> implements Clusterer<E> {

    final FeatureExtractor<E> mFeatureExtractor;
    final int mNumClusters;
    final int mMaxIterations;


    /**
     * Construct a k-means clusterer with the specified feature
     * extractor, number of clusters and limit on number of iterations
     * to run optimization.  Initialization of each cluster is with
     * random shuffling of all elements into a cluster.
     *
     * <p>If the number of iterations is set to zero, the result
     * will be random balanced clusterings of the specified size.
     *
     * @param featureExtractor Feature extractor for this clusterer.
     * @param numClusters Number of clusters to return.
     * @param maxIterations Maximum number of iterations during
     * optimization.
     * @throws IllegalArgumentException If the number of clusters is
     * less than 1, or if the maximum number of iterations is less
     * than 0.
     */
    public KMeansClusterer(FeatureExtractor<E> featureExtractor,
                           int numClusters,
                           int maxIterations) {
        if (numClusters < 1) {
            String msg = "Number of clusters must be positive."
                + " Found numClusters=" + numClusters;
            throw new IllegalArgumentException(msg);
        }
        if (maxIterations < 0) {
            String msg = "Number of iterations must be non-negative."
                + " Found maxIterations=" + maxIterations;
            throw new IllegalArgumentException(msg);
        }

        mFeatureExtractor = featureExtractor;
        mNumClusters = numClusters;
        mMaxIterations = maxIterations;
    }


    /**
     * Returns the feature extractor for this clusterer.
     *
     * @return The feature extractor for this clusterer.
     */
    public FeatureExtractor<E> featureExtractor() {
        return mFeatureExtractor;
    }

    /**
     * Returns the number of clusters this clusterer will return.
     * This is the &quot;<code>k</code>&quot; in &quot;k-means&quot;.
     *
     * @return The number of clusters this clusterer will return.
     */
    public int numClusters() {
        return mNumClusters;
    }

    /**
     * Recluster the specified clustering using up to the specified
     * number of k-means iterations.  This method allows users to
     * specify their own initial clusterings, which are then reallocated
     * using the standard k-means algorithm.
     *
     * @param clustering Clustering to recluster.
     * @param maxIterations Maximum number of reclustering iterations.
     * @return New clustering of input elements.
     */
    public Set<Set<E>> recluster(Set<Set<E>> clustering,
                                 int maxIterations) {

        int numElements = numElements(clustering);
        Object[] elements = new Object[numElements];
        Map<String,? extends Number>[] featureMaps
            = (Map<String,? extends Number>[]) new Map[numElements];

        ObjectToDoubleMap<String>[] centroids
            = (ObjectToDoubleMap<String>[])
            new ObjectToDoubleMap[clustering.size()];
        for (int i = 0; i < centroids.length; ++i)
            centroids[i] = new ObjectToDoubleMap<String>();

        List<Integer>[] clusterElements
            = (List<Integer>[]) new List[clustering.size()];
        for (int i = 0; i < clusterElements.length; ++i)
            clusterElements[i] = new ArrayList<Integer>();

        int eltIndex = 0;
        int clusterIndex = 0;
        for (Set<E> cluster : clustering) {
            for (E e : cluster) {
                elements[eltIndex] = e;
                featureMaps[eltIndex] = mFeatureExtractor.features(e);
                add(centroids[clusterIndex],featureMaps[eltIndex]);
                clusterElements[clusterIndex].add(new Integer(eltIndex));
                ++eltIndex;
            }
            ++clusterIndex;
        }

        // loop performs scaling of centroids based on # of elts
        return clusterIterations(centroids,
                                 clusterElements,
                                 elements,
                                 featureMaps,
                                 maxIterations);

    }

    int numElements(Set<Set<E>> clustering) {
        int count = 0;
        for (Set<E> cluster : clustering)
            count += cluster.size();
        return count;
    }

    /**
     * Return a k-means clustering of the specified set of elements.
     * Note that this method is randomized and may return different
     * results over different runs.  See the class documentation for
     * more details.
     *
     * @param elementSet Set of elements to cluster.
     * @return Clustering of the specified elements.
     */
    public Set<Set<E>> cluster(Set<? extends E> elementSet) {
        // handle small input
        if (elementSet.size() <= mNumClusters) {
            Set<Set<E>> clustering
                = new HashSet<Set<E>>((3 * elementSet.size()) / 2);
            for (E elt : elementSet) {
                Set<E> cluster = SmallSet.<E>create(elt);
                clustering.add(cluster);
            }
            return clustering;
        }

        // randomly ordered elements
        Object[] elements = new Object[elementSet.size()];
        elementSet.toArray(elements);
        Arrays.<Object>permute(elements);

        // parallel array of extracted features
        Map<String,? extends Number>[] featureMaps
            = (Map<String,? extends Number>[]) new Map[elements.length];
        for (int i = 0; i < featureMaps.length; ++i)
            featureMaps[i] = mFeatureExtractor.features((E)elements[i]);

        // initial centroids (uses randomness of elements)
        ObjectToDoubleMap<String>[] centroids = createCentroids();
        List<Integer>[] clusterElements = createClusterElements();
        for (int i = 0; i < elements.length; ++i) {
            int clusterId = i % mNumClusters;
            add(centroids[clusterId],featureMaps[i]);
            clusterElements[clusterId].add(new Integer(i));
        }

        return clusterIterations(centroids,clusterElements,
                                 elements,featureMaps,
                                 mMaxIterations);
    }

    Set<Set<E>> clusterIterations(ObjectToDoubleMap<String>[] centroids,
                                  List<Integer>[] clusterElements,
                                  Object[] elements,
                                  Map<String,? extends Number>[] featureMaps,
                                  int maxIterations) {
        // iterate until fixed
        for (int iteration = 0; iteration < maxIterations; ++iteration) {
            scale(centroids,clusterElements); // always unscaled coming in
            // printCentroids(centroids);
            ObjectToDoubleMap<String>[] nextCentroids = createCentroids();
            List<Integer>[] nextClusterElements = createClusterElements();
            boolean fixed = true;
            for (int i = 0; i < mNumClusters; ++i) {
                List<Integer> cluster = clusterElements[i];
                for (int k = 0; k < cluster.size(); ++k) {
                    Integer eltIndexInt = cluster.get(k);
                    int eltIndex = eltIndexInt.intValue();
                    double closestDistance = Double.POSITIVE_INFINITY;
                    int closestIndex = -1;
                    for (int j = 0; j < mNumClusters; ++j) {
                        double distance
                            = euclideanDistance(centroids[j],
                                                featureMaps[eltIndex]);
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestIndex = j;
                        }
                    }
                    // printFeaturesClosest(featureMaps[eltIndex],closestIndex);
                    if (closestIndex == -1)
                        closestIndex = 0; // or assign at random
                    add(nextCentroids[closestIndex],
                        featureMaps[eltIndex]);
                    nextClusterElements[closestIndex].add(eltIndexInt);
                    if (closestIndex != i)
                        fixed = false;
                }
            }
            if (fixed) break;
            centroids = nextCentroids;
            clusterElements = nextClusterElements;
        }

        Set<Set<E>> clustering = new HashSet<Set<E>>((3 * mNumClusters) / 2);
        for (int i = 0; i < mNumClusters; ++i) {
            HashSet<E> cluster = new HashSet<E>();
            for (Integer k : clusterElements[i])
                cluster.add((E)elements[k.intValue()]);
            if (cluster.size() > 0)
                clustering.add(cluster);
        }
        return clustering;
    }

    void scale(ObjectToDoubleMap<String>[] centroids,
               List<Integer>[] nextClusterElements) {
        for (int i = 0; i < centroids.length; ++i) {
            double numElts = nextClusterElements[i].size();
            ObjectToDoubleMap<String> centroid = centroids[i];
            for (String s : centroid.keySet())
                centroid.set(s, centroid.getValue(s) / numElts);
        }
    }

    void printFeaturesClosest(Map<String,? extends Number> featureMap,
                              int closestIndex) {
        System.out.println("  features="
                           + featureMap.toString().trim().replaceAll("\n",", "));
        System.out.println("       closest centroid=" + closestIndex);
    }


    void printCentroids(ObjectToDoubleMap<String>[] centroids) {
        System.out.println("\nCentroids");
        for (int i = 0; i < centroids.length; ++i)
            System.out.println("  " + i + "  " + centroids[i]);
    }

    void scale(ObjectToDoubleMap<String> centroid,
               double scalar) {
    }

    List<Integer>[] createClusterElements() {
        List<Integer>[] clusterElements
            = (List<Integer>[]) new List[mNumClusters];
        for (int i = 0; i < clusterElements.length; ++i)
            clusterElements[i] = new ArrayList<Integer>();
        return clusterElements;
    }

    ObjectToDoubleMap<String>[] createCentroids() {
        ObjectToDoubleMap<String>[] centroids
            = (ObjectToDoubleMap<String>[])
            new ObjectToDoubleMap[mNumClusters];
        for (int i = 0; i < centroids.length; ++i)
            centroids[i] = new ObjectToDoubleMap<String>();
        return centroids;
    }

    static void add(ObjectToDoubleMap<String> centroid,
                    Map<String,? extends Number> featureMap) {
        for (Map.Entry<String,? extends Number> entry : featureMap.entrySet())
            centroid.increment(entry.getKey(),
                               entry.getValue().doubleValue());
    }

    static double euclideanDistance(ObjectToDoubleMap<String> centroid,
                                    Map<String,? extends Number> featureMap) {
        double sqDist = 0.0;
        for (Map.Entry<String,? extends Number> featureEntry
                 : featureMap.entrySet()) {
            double diff
                = featureEntry.getValue().doubleValue()
                - centroid.getValue(featureEntry.getKey());
            sqDist += diff * diff;
        }
        for (Map.Entry<String,Double> centroidEntry : centroid.entrySet()) {
            if (featureMap.containsKey(centroidEntry.getKey())) continue;
            double diff = centroidEntry.getValue().doubleValue();
            sqDist += diff * diff;
        }
        return sqDist;  // montonic related to dist = Math.sqrt(sqDist)
    }



}
