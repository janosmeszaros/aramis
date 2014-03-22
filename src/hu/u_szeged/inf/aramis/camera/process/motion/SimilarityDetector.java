package hu.u_szeged.inf.aramis.camera.process.motion;

import com.google.common.base.Optional;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Rectangle;
import hu.u_szeged.inf.aramis.model.SimilarityVector;

import static hu.u_szeged.inf.aramis.model.SimilarityVector.similarityVector;
import static hu.u_szeged.inf.aramis.utils.ClusterUtils.findBoundingBox;
import static java.lang.Math.abs;

public class SimilarityDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarityDetector.class);
    public final Double distanceBorder;
    public final Double momentBorder;
    public final Double areaDifferenceBorder;

    public SimilarityDetector(Double distanceBorder, Double momentBorder, Double areaBorder) {
        this.distanceBorder = distanceBorder;
        this.momentBorder = momentBorder;
        this.areaDifferenceBorder = areaBorder;
    }

    public Optional<SimilarityVector> countSimilarity(Cluster<Coordinate> first,
                                                      Cluster<Coordinate> second,
                                                      Double momentsDistance) {
        Rectangle boundingBox1 = findBoundingBox(first.getPoints());
        Rectangle boundingBox2 = findBoundingBox(second.getPoints());
        Double distance = countEuclideanDistance(boundingBox1, boundingBox2);
        Double areaDifference = countAreaDifference(first.getPoints().size(), second.getPoints().size());
        boolean shouldFiltered = momentsDistance > momentBorder
                || distance > distanceBorder
                || areaDifference > areaDifferenceBorder;
        Optional<SimilarityVector> result = Optional.absent();
        if (!shouldFiltered) {
            result = Optional.of(similarityVector(momentsDistance, distance, areaDifference));
        }
        return result;
    }

    private Double countAreaDifference(int size, int size1) {
        return Double.valueOf(abs(size - size1));
    }

    private Double countEuclideanDistance(Rectangle boundingBox1, Rectangle boundingBox2) {
        return Math.sqrt(abs(boundingBox1.maxX - boundingBox2.maxX) + abs(boundingBox1.maxY - boundingBox2.maxY));
    }
}
