package hu.u_szeged.inf.aramis.camera.process;

import android.graphics.Bitmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.camera.TakePictureCallback.PICTURE_NUMBER;

public class BackgroundEvaluator {
    private final Logger LOGGER = LoggerFactory.getLogger(BackgroundEvaluator.class);
    private int[] pixels = new int[PICTURE_NUMBER];

    public Bitmap evaluate(List<Picture> pictures) {
        Bitmap original = pictures.get(pictures.size() - 1).bitmap;
        Bitmap output = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        LOGGER.info("Start creating background picture!");
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

    private int evaluatePixel(List<Picture> pictures, int x, int y) {
        for (int i = 0; i < pictures.size(); i++) {
            pixels[i] = pictures.get(i).bitmap.getPixel(x, y);
        }
        Arrays.sort(pixels);
        return pixels[PICTURE_NUMBER / 2];
    }
}
