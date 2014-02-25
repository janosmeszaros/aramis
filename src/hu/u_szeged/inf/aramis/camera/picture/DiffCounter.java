package hu.u_szeged.inf.aramis.camera.picture;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static hu.u_szeged.inf.aramis.model.Coordinate.coordinate;
import static java.lang.Math.abs;

public class DiffCounter implements Callable<Set<Coordinate>> {
    public static final int BORDER = 50;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiffCounter.class);
    private final CountDownLatch countDownLatch;
    private final Picture first;
    private final Picture second;
    private final Set<Coordinate> allDifferenceCoordinates;

    public DiffCounter(CountDownLatch countDownLatch, Picture first, Picture second, Set<Coordinate> allDifferenceCoordinates) {
        if (first.bitmap.getWidth() != second.bitmap.getWidth() || first.bitmap.getHeight() != second.bitmap.getHeight()) {
            throw new IllegalArgumentException("There are differences in the two picture dimensions");
        }
        this.countDownLatch = countDownLatch;
        this.first = first;
        this.second = second;
        this.allDifferenceCoordinates = allDifferenceCoordinates;
    }

    @Override
    public Set<Coordinate> call() {
        Set<Coordinate> coordinates;
        if (allDifferenceCoordinates.isEmpty()) {
            coordinates = getDiffCoordinatesFromAllThePixels();
        } else {
            coordinates = getDiffCoordinatesFromDiffs();
        }
        countDownLatch.countDown();
        return coordinates;
    }

    private Set<Coordinate> getDiffCoordinatesFromAllThePixels() {
        LOGGER.info("Start creating diff");
        Bitmap diffBitmap = second.bitmap.copy(second.bitmap.getConfig(), true);
        Set<Coordinate> coordinates = Sets.newLinkedHashSet();
        for (int x = 0; x < first.bitmap.getWidth(); x++) {
            for (int y = 0; y < first.bitmap.getHeight(); y++) {
                if (countTotalDiff(first.bitmap.getPixel(x, y), second.bitmap.getPixel(x, y)) > BORDER) {
                    coordinates.add(coordinate(x, y));
                    diffBitmap.setPixel(x, y, Color.BLUE);
                }
            }
        }
        LOGGER.info("Number of differences: {}", coordinates.size());
        savePicture(Picture.picture(Joiner.on("_").join(first.name, second.name, "diff"), diffBitmap));
        return coordinates;
    }

    private Set<Coordinate> getDiffCoordinatesFromDiffs() {
        LOGGER.info("Start creating diff with given coordinates");
        Bitmap diffBitmap = second.bitmap.copy(second.bitmap.getConfig(), true);
        Set<Coordinate> coordinates = Sets.newLinkedHashSet();
        for (Coordinate coordinate : allDifferenceCoordinates) {
            if (countTotalDiff(first.bitmap.getPixel(coordinate.x, coordinate.y), second.bitmap.getPixel(coordinate.x, coordinate.y)) > BORDER) {
                coordinates.add(coordinate(coordinate.x, coordinate.y));
                diffBitmap.setPixel(coordinate.x, coordinate.y, Color.BLUE);
            }
        }
        LOGGER.info("Number of differences: {}", coordinates.size());
        savePicture(Picture.picture(Joiner.on("_").join(first.name, second.name, "diff"), diffBitmap));
        return coordinates;
    }

    protected void savePicture(Picture picture) {
        PictureSaver.save(picture);
    }

    private synchronized int countTotalDiff(int firstPixel, int secondPixel) {
        int luminanceFirst = luminance(red(firstPixel), green(firstPixel), blue(firstPixel));
        int luminanceSecond = luminance(red(secondPixel), green(secondPixel), blue(secondPixel));
        return abs(luminanceFirst - luminanceSecond);
    }

    private int luminance(int r, int g, int b) {
        return Math.round(0.299f * r + 0.587f * g + 0.114f * b);
    }
}
