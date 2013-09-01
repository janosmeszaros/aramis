package hu.u_szeged.inf.aramis.camera.picture;

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
    private CountDownLatch done;
    private final Picture first;
    private final Picture second;

    public DiffCounter(CountDownLatch done, Picture first, Picture second) {
        this.done = done;
        this.first = first;
        this.second = second;
    }

    @Override
    public List<Coordinate> call() throws Exception {
        List<Coordinate> coordinates = getDiffPicture(first, second);
        done.countDown();
        return coordinates;
    }

    private List<Coordinate> getDiffPicture(Picture first, Picture second) {
        LOGGER.info("Start creating diff");
        int width = first.bitmap.getWidth();
        int height = first.bitmap.getHeight();
        List<Coordinate> coordinates = new ArrayList<Coordinate>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int totalDiff = countTotalDiff(first.bitmap.getPixel(x, y), second.bitmap.getPixel(x, y));
                if (totalDiff > BORDER) {
                    coordinates.add(coordinate(x, y));
                }
            }
        }
        LOGGER.info("Diff created!");
        return coordinates;
    }

    public static int countTotalDiff(int firstPixel, int secondPixel) {
        int redDelta = abs(red(firstPixel) - red(secondPixel));
        int greenDelta = abs(green(firstPixel) - green(secondPixel));
        int blueDelta = abs(blue(firstPixel) - blue(secondPixel));
        return redDelta + greenDelta + blueDelta;
    }
}
