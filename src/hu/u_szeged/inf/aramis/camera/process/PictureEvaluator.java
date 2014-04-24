package hu.u_szeged.inf.aramis.camera.process;

import android.graphics.Bitmap;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.Coordinate;

public class PictureEvaluator {
    private final Logger LOGGER = LoggerFactory.getLogger(PictureEvaluator.class);

    public Bitmap evaluate(List<BlurredPicture> pictures, Table<Integer, Integer, Boolean> coordinates) {
        Bitmap original = pictures.get(pictures.size() - 1).picture.bitmap;
        Bitmap output = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        LOGGER.info("Start evaluating pictures!");
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                Integer color = evaluatePixel(pictures, x, y);
                output.setPixel(x, y, color);
            }
        }
        return output;
    }

    public Bitmap switchColors(Bitmap original, Bitmap background, List<Coordinate> coordinates) {
        Bitmap output = original.copy(original.getConfig(), true);
        LOGGER.debug("Coordinates number: {}", coordinates.size());
        for (Coordinate coordinate : coordinates) {
            output.setPixel(coordinate.x, coordinate.y, background.getPixel(coordinate.x, coordinate.y));
        }
        return output;
    }

    private Integer evaluatePixel(List<BlurredPicture> pictures, int x, int y) {
        List<Integer> pixelsFromPictures = Lists.newArrayList();
        for (BlurredPicture picture : pictures) {
            int pixel = picture.picture.bitmap.getPixel(x, y);
            pixelsFromPictures.add(pixel);
        }
        Collections.sort(pixelsFromPictures);
        return pixelsFromPictures.get(pictures.size() / 2);
    }
}
