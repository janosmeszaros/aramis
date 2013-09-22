package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.u_szeged.inf.aramis.model.Picture;

public class PictureEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureEvaluator.class);

    public static Picture evaluate(List<Picture> pictures) {
        Bitmap original = pictures.get(pictures.size() - 1).bitmap;
        Bitmap output = original.copy(original.getConfig(), true);
        LOGGER.debug("Starting evaluate pictures!");
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                List<Integer> pixelsFromPictures = new ArrayList<Integer>();
                for (Picture picture : pictures) {
                    int pixel = picture.bitmap.getPixel(x, y);
                    pixelsFromPictures.add(pixel);
                }
                Collections.sort(pixelsFromPictures);
                output.setPixel(x, y, pixelsFromPictures.get(pictures.size() / 2));
            }
        }
        return Picture.picture(output);
    }

}
