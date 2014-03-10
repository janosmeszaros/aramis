package hu.u_szeged.inf.aramis.camera.process.display;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.MotionSeries;
import hu.u_szeged.inf.aramis.model.Picture;

public class ChainResolver {
    private final List<MotionSeries> motionSeriesList;

    public ChainResolver(List<MotionSeries> motionSeriesList) {
        this.motionSeriesList = motionSeriesList;
    }

    public Map<Picture, Cluster<Coordinate>> findChainFor(Picture picture, Cluster<Coordinate> cluster) {
        for (MotionSeries motionSeries : motionSeriesList) {
            Cluster<Coordinate> actualCluster = motionSeries.getMap().get(picture);
            if (actualCluster != null && actualCluster.getPoints().equals(cluster.getPoints())) {
                return motionSeries.getMap();
            }
        }
        throw new IllegalArgumentException("Cant find cluster");
    }
}
