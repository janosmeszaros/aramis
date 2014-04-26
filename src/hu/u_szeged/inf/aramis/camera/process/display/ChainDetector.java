package hu.u_szeged.inf.aramis.camera.process.display;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import hu.u_szeged.inf.aramis.model.ClusterPair;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.MotionSeries;
import hu.u_szeged.inf.aramis.model.Picture;

public class ChainDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainDetector.class);

    public Map<Picture, Bitmap> markChains(Set<Picture> pictures, List<MotionSeries> motionSeriesList) {
        Map<Picture, Bitmap> result = Maps.newHashMap(Maps.asMap(pictures, new Function<Picture, Bitmap>() {
            @Override
            public Bitmap apply(Picture input) {
                return input.bitmap.copy(input.bitmap.getConfig(), true);
            }
        }));
        return markChains(result, motionSeriesList);
    }

    public Map<Picture, Bitmap> markChains(Map<Picture, Bitmap> pictures, List<MotionSeries> motionSeriesList) {
        for (MotionSeries motionSeries : motionSeriesList) {
            for (Map.Entry<Picture, Cluster<Coordinate>> entry : motionSeries.getMap().entrySet()) {
                Picture key = entry.getKey();
                pictures.put(key, setPixels(motionSeries.getColor(),
                        pictures.get(key), entry.getValue().getPoints()));
            }
        }
        return pictures;
    }

    private Bitmap setPixels(int color, Bitmap bitmap, List<Coordinate> coordinates) {
        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        for (Coordinate coordinate : coordinates) {
            result.setPixel(coordinate.x, coordinate.y, color);
        }
        return result;
    }

    public List<MotionSeries> spotChains(Map<Picture, List<ClusterPair>> map) {
        List<MotionSeries> motionSeriesList = Lists.newArrayList();
        for (Map.Entry<Picture, List<ClusterPair>> entry : map.entrySet()) {
            LOGGER.info("Processing pairs for picture #{}", entry.getKey().name);
            List<MotionSeries> motionSeriesListForActualPicture = Lists.newArrayList();
            for (ClusterPair clusterPair : entry.getValue()) {
                boolean isPut = false;
                for (MotionSeries motionSeries : motionSeriesList) {
                    if (motionSeries.putValue(entry.getKey(), clusterPair)) {
                        isPut = true;
                        break;
                    }
                }
                if (!isPut) {
                    int color = randomColor();
                    motionSeriesListForActualPicture.add(new MotionSeries(color, clusterPair, entry.getKey()));
                }
            }
            motionSeriesList.addAll(motionSeriesListForActualPicture);
        }
        return motionSeriesList;
    }

    private int randomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
}
