package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.model.Picture;

public class PictureEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureEvaluator.class);

    public static Picture evaluate(List<Picture> pictures) throws IOException {
        Bitmap output = Bitmap.createBitmap(pictures.get(0).bitmap.getWidth(), pictures.get(0).bitmap.getHeight(), pictures.get(0).bitmap.getConfig());
        LOGGER.debug("Starting evaluate pictures!");
        for (int width = 0; width < output.getWidth(); width++) {
            for (int height = 0; height < output.getHeight(); height++) {
                Map<Integer, Integer> occurrences = new HashMap<Integer, Integer>();
                for (Picture picture : pictures) {
                    Integer value = occurrences.get(picture.bitmap.getPixel(width, height));
                    if (value == null) {
                        value = 1;
                    }
                    occurrences.put(picture.bitmap.getPixel(width, height), value);
                }
                int maxValueInMap = (Collections.max(occurrences.values()));
                for (Map.Entry<Integer, Integer> entry : occurrences.entrySet()) {
                    if (entry.getValue() == maxValueInMap) {
                        output.setPixel(width, height, entry.getKey());
                        break;
                    }
                }
            }
        }
        return Picture.picture(output);
    }
}
