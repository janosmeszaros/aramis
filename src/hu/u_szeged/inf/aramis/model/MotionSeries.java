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
    private Pair lastItem;

    public MotionSeries(int color, Pair lastItem, Picture picture) {
        map = Maps.newLinkedHashMap();
        this.color = color;
        this.lastItem = lastItem;
        this.map.put(picture, lastItem.first);
    }

    public boolean putValue(Picture picture, Pair nextItem) {
        if (lastItem.second.isPresent() && nextItem.first.equals(lastItem.second.get())) {
            LOGGER.info("Comparing ");
            map.put(picture, nextItem.first);
            lastItem = nextItem;
            return true;
        } else {
            return false;
        }
    }

    public int getColor() {
        return color;
    }

    public Map<Picture, Cluster<Coordinate>> getMap() {
        return map;
    }
}
