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

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.MotionSeries;
import hu.u_szeged.inf.aramis.model.Pair;
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
        for (MotionSeries motionSeries : motionSeriesList) {
            for (Map.Entry<Picture, Cluster<Coordinate>> entry : motionSeries.getMap().entrySet()) {
                Picture key = entry.getKey();
                LOGGER.info("Coloring coordinates to {} for picture {}", motionSeries.getColor(), key.name);
                result.put(key, setPixels(motionSeries.getColor(),
                        result.get(key), entry.getValue().getPoints()));
            }
        }
        return result;
    }

    private Bitmap setPixels(int color, Bitmap bitmap, List<Coordinate> coordinates) {
        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        for (Coordinate coordinate : coordinates) {
            result.setPixel(coordinate.x, coordinate.y, color);
        }
        return result;
    }

    public List<MotionSeries> spotChains(Map<Picture, List<Pair>> map) {
        List<MotionSeries> motionSeriesList = Lists.newArrayList();
        for (Map.Entry<Picture, List<Pair>> entry : map.entrySet()) {
            LOGGER.info("Processing pairs for picture #{}", entry.getKey().name);
            List<MotionSeries> motionSeriesListForActualPicture = Lists.newArrayList();
            for (Pair pair : entry.getValue()) {
                boolean isPutted = false;
                for (MotionSeries motionSeries : motionSeriesList) {
                    if (motionSeries.putValue(entry.getKey(), pair)) {
                        isPutted = true;
                        LOGGER.info("Putting to series for picture {} to {}", entry.getKey().name, motionSeries.getColor());
                        break;
                    }
                }
                if (!isPutted) {
                    motionSeriesListForActualPicture.add(new MotionSeries(randomColor(), pair, entry.getKey()));
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
