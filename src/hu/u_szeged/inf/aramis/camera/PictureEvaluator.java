package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class PictureEvaluator {
    private final Logger LOGGER = LoggerFactory.getLogger(PictureEvaluator.class);

    public Bitmap evaluate(Picture first, Picture second) {
        Bitmap output = first.bitmap.copy(first.bitmap.getConfig(), true);
        List<Picture> pictures = Lists.newArrayList(first, second);
        for (int x = 0; x < first.bitmap.getWidth(); x++) {
            for (int y = 0; y < first.bitmap.getWidth(); y++) {
                output.setPixel(x, y, evaluatePixel(pictures, x, y));
            }
        }
        return output;
    }

    public Bitmap evaluate(List<Picture> pictures, Set<Coordinate> coordinates) {
        Bitmap original = pictures.get(pictures.size() - 1).bitmap;
        Bitmap output = original.copy(original.getConfig(), true);
        LOGGER.info("Starting evaluate pictures!" + Thread.currentThread().getName());
        LOGGER.debug("Coordinates number: {}", coordinates.size());
        for (Coordinate coordinate : coordinates) {
            Integer color = evaluatePixel(pictures, coordinate.x, coordinate.y);
            output.setPixel(coordinate.x, coordinate.y, color);
        }
        return output;
    }

    public Bitmap switchColors(Picture cluster, Picture result, List<Coordinate> coordinates) {
        Bitmap output = cluster.bitmap.copy(cluster.bitmap.getConfig(), true);
        LOGGER.debug("Coordinates number: {}", coordinates.size());
        for (Coordinate coordinate : coordinates) {
            output.setPixel(coordinate.x, coordinate.y, result.bitmap.getPixel(coordinate.x, coordinate.y));
        }
        return output;
    }

    private Integer evaluatePixel(List<Picture> pictures, int x, int y) {
        List<Integer> pixelsFromPictures = Lists.newArrayList();
        for (Picture picture : pictures) {
            int pixel = picture.bitmap.getPixel(x, y);
            pixelsFromPictures.add(pixel);
        }
        Collections.sort(pixelsFromPictures);
        return pixelsFromPictures.get(pictures.size() / 2);
    }
}
