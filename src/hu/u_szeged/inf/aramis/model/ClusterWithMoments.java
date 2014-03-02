package hu.u_szeged.inf.aramis.model;

import org.apache.commons.math3.ml.clustering.Cluster;

public class ClusterWithMoments {
    public final Cluster<Coordinate> cluster;
    public final MomentsVector momentsVector;

    private ClusterWithMoments(Cluster<Coordinate> cluster, MomentsVector momentsVector) {
        this.cluster = cluster;
        this.momentsVector = momentsVector;
    }

    public static ClusterWithMoments pictureWithMoments(Cluster<Coordinate> cluster,
                                                        MomentsVector momentsVector) {
        return new ClusterWithMoments(cluster, momentsVector);
    }

}
