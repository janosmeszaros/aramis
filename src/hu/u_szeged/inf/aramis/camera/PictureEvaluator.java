package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hu.u_szeged.inf.aramis.camera.picture.DiffCounter;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class PictureEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureEvaluator.class);

    public static Picture evaluate(List<Picture> pictures, List<Coordinate> diffCoordinates) throws IOException {
        Bitmap original = pictures.get(pictures.size() - 1).bitmap;
        Bitmap output = original.copy(original.getConfig(), true);
        LOGGER.debug("Starting evaluate pictures!");
        for (Coordinate coordinate : diffCoordinates) {
            Map<Integer, Integer> occurrences = new HashMap<Integer, Integer>();
            for (Picture picture : pictures) {
                int actualValue = picture.bitmap.getPixel(coordinate.x, coordinate.y);
                Integer key = getMatchingKey(occurrences.keySet(), actualValue);
                Integer value = occurrences.get(key);
                if (value == null) {
                    value = 1;
                }
                occurrences.put(key, value);
            }

            Integer maxValueInMap = (Collections.max(occurrences.values()));
            for (Map.Entry<Integer, Integer> entry : occurrences.entrySet()) {
                if (entry.getValue().equals(maxValueInMap)) {
                    output.setPixel(coordinate.x, coordinate.y, entry.getKey().intValue());
                    break;
                }
            }
        }
        return Picture.picture(output);
    }

    private static Integer getMatchingKey(Set<Integer> keys, int actualValue) {
        Integer result = actualValue;
        for (Integer key : keys) {
            if (DiffCounter.countTotalDiff(key, actualValue) < DiffCounter.BORDER) {
                result = key;
                break;
            }
        }
        return result;
    }
}
