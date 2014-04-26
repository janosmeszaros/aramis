package hu.u_szeged.inf.aramis.camera.process;

import android.graphics.Bitmap;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import hu.u_szeged.inf.aramis.camera.TakePictureCallback;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class PictureEvaluator {
    private final Logger LOGGER = LoggerFactory.getLogger(PictureEvaluator.class);
    private double[] pixels = new double[TakePictureCallback.PICTURE_NUMBER];
    private final Median median = new Median();

    public Bitmap evaluate(List<Picture> pictures) {
        Bitmap original = pictures.get(pictures.size() - 1).bitmap;
        Bitmap output = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        LOGGER.info("Start evaluating pictures!");
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                double color = evaluatePixel(pictures, x, y);
                output.setPixel(x, y, (int) color);
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

    private double evaluatePixel(List<Picture> pictures, int x, int y) {
        for (int i = 0; i < pictures.size(); i++) {
            int pixel = pictures.get(i).bitmap.getPixel(x, y);
            pixels[i] = pixel;
        }
        return median.evaluate(pixels);
    }
}
