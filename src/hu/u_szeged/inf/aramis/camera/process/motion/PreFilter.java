package hu.u_szeged.inf.aramis.camera.process.motion;

import com.google.common.collect.ImmutableList;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Rectangle;

import static hu.u_szeged.inf.aramis.utils.ClusterUtils.findBoundingBox;
import static java.math.RoundingMode.HALF_UP;

public class PreFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreFilter.class);
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
            } else {
                LOGGER.info("Cluster filtered out {}", cluster);
            }
        }
        return builder.build();
    }
}
