package hu.u_szeged.inf.aramis.camera.picture.process;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.u_szeged.inf.aramis.model.ClusterWithMoments;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.MomentsVector;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.model.Rectangle;

import static hu.u_szeged.inf.aramis.Utils.ClusterUtils.findBoundingBox;
import static hu.u_szeged.inf.aramis.model.ClusterWithMoments.pictureWithMoments;

public final class MomentsCounter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MomentsCounter.class);

    public ClusterWithMoments countMoments(Picture picture, Cluster<Coordinate> cluster) {
        Rectangle boundingBox = findBoundingBox(cluster.getPoints());
        double[][] array = createArray(boundingBox);
        for (Coordinate coordinate : cluster.getPoints()) {
            array[boundingBox.maxX - coordinate.x][boundingBox.maxY - coordinate.y] =
                    picture.bitmap.getPixel(coordinate.x, coordinate.y);
        }
        MomentsVector huMoments = Moments.getHuMoments(array);
        LOGGER.info("Central moment for {} = {}", picture.name, huMoments);
        return pictureWithMoments(cluster, huMoments);
    }

    private double[][] createArray(Rectangle rectangle) {
        LOGGER.info("Create array from {}", rectangle);
        LOGGER.info("Create array with sizes {} {}", rectangle.maxX - rectangle.minX + 1, rectangle.maxY - rectangle.minY + 1);
        return new double[rectangle.maxX - rectangle.minX + 1][rectangle.maxY - rectangle.minY + 1];
    }

}
