package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import hu.u_szeged.inf.aramis.camera.picture.Clustering;
import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class PictureEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureEvaluator.class);

    public static Bitmap evaluate(Picture first, Picture second) {
        Bitmap output = first.bitmap.copy(first.bitmap.getConfig(), true);
        List<Picture> pictures = Lists.newArrayList(first, second);
        for (int x = 0; x < first.bitmap.getWidth(); x++) {
            for (int y = 0; y < first.bitmap.getWidth(); y++) {
                output.setPixel(x, y, evaluatePixel(pictures, x, y));
            }
        }
        return output;
    }

    public static Bitmap evaluate(List<Picture> pictures, Set<Coordinate> coordinates) {
        Table<Integer, Integer, Boolean> table = HashBasedTable.create();
        Bitmap original = pictures.get(pictures.size() - 1).bitmap;
        Bitmap output = original.copy(original.getConfig(), true);
        LOGGER.info("Starting evaluate pictures!");
        LOGGER.debug("Coordinates number: {}", coordinates.size());
        for (Coordinate coordinate : coordinates) {
            Integer color = evaluatePixel(pictures, coordinate.x, coordinate.y);
            output.setPixel(coordinate.x, coordinate.y, color);
            table.put(coordinate.x, coordinate.y, false);
        }
        LOGGER.info("Starting clusterize picture!");
        clusterize(output, table);
        return output;
    }

    private static Integer evaluatePixel(List<Picture> pictures, int x, int y) {
        List<Integer> pixelsFromPictures = Lists.newArrayList();
        for (Picture picture : pictures) {
            int pixel = picture.bitmap.getPixel(x, y);
            pixelsFromPictures.add(pixel);
        }
        Collections.sort(pixelsFromPictures);
        return pixelsFromPictures.get(pictures.size() / 2);
    }

    private static void clusterize(Bitmap second, Table<Integer, Integer, Boolean> table) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Cluster<Coordinate>> clusters = Clustering.clustering(1, 2).cluster(table);
        stopwatch.stop();
        LOGGER.info("Cluster's number {}", clusters.size());
        Bitmap result = second.copy(second.getConfig(), true);
        int count = 0;
        for (Cluster<Coordinate> cluster : clusters) {
            for (Coordinate coordinate : cluster.getPoints()) {
                result.setPixel(coordinate.x, coordinate.y, Color.rgb(count * 10, count * 10, 0));
            }
            count++;
        }
        PictureSaver.save(Picture.picture(stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms_clusters_" + clusters.size(), result));
    }
}
