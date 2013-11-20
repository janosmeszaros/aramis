package hu.u_szeged.inf.aramis.camera.picture;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static hu.u_szeged.inf.aramis.model.Coordinate.coordinate;
import static java.lang.Math.abs;

public class DiffCounter implements Callable<List<Coordinate>> {
    public static final int BORDER = 40;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiffCounter.class);
    private final CountDownLatch countDownLatch;
    private final Picture first;
    private final Picture second;

    public DiffCounter(CountDownLatch countDownLatch, Picture first, Picture second) {
        this.countDownLatch = countDownLatch;
        this.first = first;
        this.second = second;
    }

    @Override
    public List<Coordinate> call() {
        List<Coordinate> coordinates = getDiffPicture(first, second);
        countDownLatch.countDown();
        return coordinates;
    }

    private List<Coordinate> getDiffPicture(Picture first, Picture second) {
        PictureSaver.save(first);
        PictureSaver.save(second);
        Bitmap result = second.bitmap.copy(second.bitmap.getConfig(), true);
        LOGGER.info("Start creating diff");
        if (first.bitmap.getWidth() != second.bitmap.getWidth() || first.bitmap.getHeight() != second.bitmap.getHeight()) {
            throw new IllegalArgumentException("There are differences in the two picture dimensions");
        }

        int width = first.bitmap.getWidth();
        int height = first.bitmap.getHeight();
        List<Coordinate> coordinates = new ArrayList<Coordinate>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int totalDiff = countTotalDiff(first.bitmap.getPixel(x, y), second.bitmap.getPixel(x, y));
                if (totalDiff > BORDER) {
                    coordinates.add(coordinate(x, y));
                    result.setPixel(x, y, Color.BLUE);
                }
            }
        }
        LOGGER.info("Diff created!");
        PictureSaver.save(Picture.picture(second.name + "_diffs", result));
        return coordinates;
    }

    public static int countTotalDiff(int firstPixel, int secondPixel) {
        int redDelta = abs(red(firstPixel) - red(secondPixel));
        int greenDelta = abs(green(firstPixel) - green(secondPixel));
        int blueDelta = abs(blue(firstPixel) - blue(secondPixel));
        return redDelta + greenDelta + blueDelta;
    }
}
