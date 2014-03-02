package hu.u_szeged.inf.aramis.camera.picture.process;

import org.apache.commons.math3.ml.clustering.Cluster;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Rectangle;

import static hu.u_szeged.inf.aramis.Utils.ClusterUtils.findBoundingBox;
import static java.lang.Math.abs;

public class SimilarityDetector {
    private final Double distanceBorder;
    private final Double momentBorder;

    public SimilarityDetector(Double distanceBorder, Double momentBorder) {
        this.distanceBorder = distanceBorder;
        this.momentBorder = momentBorder;
    }

    public boolean isSimilar(Cluster<Coordinate> first,
                             Cluster<Coordinate> second,
                             Double momentsDistance) {
        return momentsDistance > momentBorder &&
                countEuclideanDistance(first, second) < distanceBorder;
    }

    private Double countEuclideanDistance(Cluster<Coordinate> firstCluster, Cluster<Coordinate> secondCluster) {
        Rectangle boundingBox1 = findBoundingBox(firstCluster.getPoints());
        Rectangle boundingBox2 = findBoundingBox(secondCluster.getPoints());
        return Math.sqrt(abs(boundingBox1.maxX - boundingBox2.maxX) + abs(boundingBox1.maxY - boundingBox2.maxY));
    }
}
