package hu.u_szeged.inf.aramis.camera.picture.process;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Rectangle;

import static hu.u_szeged.inf.aramis.Utils.ClusterUtils.findBoundingBox;
import static java.lang.Math.abs;

public class SimilarityDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarityDetector.class);
    private final Double distanceBorder;
    private final Double momentBorder;
    private final Double areaDifferenceBorder;

    public SimilarityDetector(Double distanceBorder, Double momentBorder, Double areaBorder) {
        this.distanceBorder = distanceBorder;
        this.momentBorder = momentBorder;
        this.areaDifferenceBorder = areaBorder;
    }

    public boolean isSimilar(Cluster<Coordinate> first,
                             Cluster<Coordinate> second,
                             Double momentsDistance) {
        Rectangle boundingBox1 = findBoundingBox(first.getPoints());
        Rectangle boundingBox2 = findBoundingBox(second.getPoints());
        return momentsDistance > momentBorder
                && countEuclideanDistance(boundingBox1, boundingBox2) < distanceBorder
                && countAreaDifference(boundingBox1, boundingBox2) < areaDifferenceBorder;
    }

    private Double countAreaDifference(Rectangle boundingBox1, Rectangle boundingBox2) {
        double value = abs(boundingBox1.getArea().subtract(boundingBox2.getArea()).doubleValue());
        LOGGER.info("Area diff {}", value);
        return value;
    }

    private Double countEuclideanDistance(Rectangle boundingBox1, Rectangle boundingBox2) {
        return Math.sqrt(abs(boundingBox1.maxX - boundingBox2.maxX) + abs(boundingBox1.maxY - boundingBox2.maxY));
    }
}
