package hu.u_szeged.inf.aramis.model;

import com.google.common.collect.Maps;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MotionSeries {
    private static final Logger LOGGER = LoggerFactory.getLogger(MotionSeries.class);

    private final int color;
    private Map<Picture, Cluster<Coordinate>> map;
    private ClusterPair lastItem;

    public MotionSeries(int color, ClusterPair lastItem, Picture picture) {
        map = Maps.newLinkedHashMap();
        this.color = color;
        this.lastItem = lastItem;
        this.map.put(picture, lastItem.first);
    }

    public boolean putValue(Picture picture, ClusterPair nextItem) {
        if (compareItems(nextItem)) {
            map.put(picture, nextItem.first);
            lastItem = nextItem;
            return true;
        } else {
            return false;
        }
    }

    private boolean compareItems(ClusterPair next) {
        if (lastItem.second.isPresent()) {
            return compareClusters(next.first, lastItem.second.get());
        } else {
            return compareClusters(next.first, lastItem.first);
        }
    }

    private boolean compareClusters(Cluster<Coordinate> next, Cluster<Coordinate> previous) {
        return next.getPoints().equals(previous.getPoints());
    }

    public int getColor() {
        return color;
    }

    public Map<Picture, Cluster<Coordinate>> getMap() {
        return map;
    }
}
