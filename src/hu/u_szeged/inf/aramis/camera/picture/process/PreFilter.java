package hu.u_szeged.inf.aramis.camera.picture.process;

import com.google.common.collect.ImmutableList;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.math.BigDecimal;
import java.util.List;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Rectangle;

import static hu.u_szeged.inf.aramis.Utils.ClusterUtils.findBoundingBox;
import static java.math.RoundingMode.HALF_UP;

public class PreFilter {
    private final BigDecimal areaBorder;
    private final BigDecimal similarityBorder;

    public PreFilter(BigDecimal areaBorder, BigDecimal similarityBorder) {
        this.areaBorder = areaBorder;
        this.similarityBorder = similarityBorder;
    }

    public List<Cluster<Coordinate>> filter(List<Cluster<Coordinate>> clusters) {
        ImmutableList.Builder<Cluster<Coordinate>> builder = ImmutableList.builder();
        for (Cluster<Coordinate> cluster : clusters) {
            Rectangle boundingBox = findBoundingBox(cluster.getPoints());
            BigDecimal boundingArea = boundingBox.getArea();
            BigDecimal area = new BigDecimal(cluster.getPoints().size());
            BigDecimal rectaglines = area.divide(boundingArea, 4, HALF_UP);
            if (area.compareTo(areaBorder) > -1 || rectaglines.compareTo(similarityBorder) < 1) {
                builder.add(cluster);
            }
        }
        return builder.build();
    }
}
