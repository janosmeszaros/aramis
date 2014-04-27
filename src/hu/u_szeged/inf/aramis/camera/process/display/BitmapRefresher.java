package hu.u_szeged.inf.aramis.camera.process.display;

import android.graphics.Bitmap;

import com.google.common.collect.Maps;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.util.Map;

import hu.u_szeged.inf.aramis.camera.process.BackgroundEvaluator;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class BitmapRefresher {
    private final BackgroundEvaluator evaluator;
    private final Picture backgroundPicture;

    public BitmapRefresher(BackgroundEvaluator evaluator, Picture backgroundPicture) {
        this.evaluator = evaluator;
        this.backgroundPicture = backgroundPicture;
    }

    public Map<Picture, Bitmap> refreshBitmaps(Map<Picture, Bitmap> pictures,
                                               Map<Picture, Cluster<Coordinate>> clusters) {
        Map<Picture, Bitmap> result = Maps.newLinkedHashMap();
        for (Map.Entry<Picture, Cluster<Coordinate>> entry : clusters.entrySet()) {
            Picture key = entry.getKey();
            Cluster<Coordinate> cluster = entry.getValue();
            Bitmap resultBitmap = evaluator.switchColors(pictures.get(key), backgroundPicture.bitmap, cluster.getPoints());
            result.put(key, resultBitmap);
        }
        return result;
    }
}
