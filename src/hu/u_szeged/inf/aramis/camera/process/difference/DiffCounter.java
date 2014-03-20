package hu.u_szeged.inf.aramis.camera.process.difference;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import hu.u_szeged.inf.aramis.camera.utils.PictureSaver;
import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.utils.FilterUtils;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static java.lang.Math.abs;

public class DiffCounter implements Callable<Set<Coordinate>> {
    public static final int BORDER = 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiffCounter.class);
    private final CountDownLatch countDownLatch;
    private final BlurredPicture first;
    private final BlurredPicture second;
    private final Set<Coordinate> allDifferenceCoordinates;

    public DiffCounter(CountDownLatch countDownLatch, BlurredPicture first, BlurredPicture second, Set<Coordinate> allDifferenceCoordinates) {
        if (first.picture.bitmap.getWidth() != second.picture.bitmap.getWidth() || first.picture.bitmap.getHeight() != second.picture.bitmap.getHeight()) {
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
            coordinates = getDiffCoordinatesFromAllPixels();
        } else {
            coordinates = getDiffCoordinatesFromDiffs();
        }
        countDownLatch.countDown();
        return coordinates;
    }

    private Set<Coordinate> getDiffCoordinatesFromAllPixels() {
        LOGGER.info("Start creating diff");
        Bitmap firstBitmap = first.picture.bitmap;
        Bitmap secondBitmap = second.picture.bitmap;
        Bitmap diffBitmap = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(), Bitmap.Config.RGB_565);
        for (int x = 0; x < firstBitmap.getWidth(); x++) {
            for (int y = 0; y < firstBitmap.getHeight(); y++) {
                if (countTotalDiff(firstBitmap.getPixel(x, y), secondBitmap.getPixel(x, y)) > BORDER) {
                    diffBitmap.setPixel(x, y, Color.WHITE);
                }
            }
        }
        Bitmap filteredBitmap = FilterUtils.filterWithMedian(diffBitmap);
        Bitmap closed = FilterUtils.morphologicalClosure(filteredBitmap);
        savePicture(Picture.picture(Joiner.on("_").join(first.picture.name, second.picture.name, "diff"), closed));
        return getResult(closed);
    }

    private Set<Coordinate> getDiffCoordinatesFromDiffs() {
        LOGGER.info("Start creating diff with given coordinates");
        Bitmap firstBitmap = first.picture.bitmap;
        Bitmap secondBitmap = second.picture.bitmap;
        Bitmap diffBitmap = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(), Bitmap.Config.RGB_565);
        for (Coordinate coordinate : allDifferenceCoordinates) {
            if (countTotalDiff(firstBitmap.getPixel(coordinate.x, coordinate.y), secondBitmap.getPixel(coordinate.x, coordinate.y)) > BORDER) {
                diffBitmap.setPixel(coordinate.x, coordinate.y, Color.WHITE);
            }
        }
        Bitmap filteredBitmap = FilterUtils.filterWithMedian(diffBitmap);
        Bitmap closed = FilterUtils.morphologicalClosure(filteredBitmap);
        savePicture(Picture.picture(Joiner.on("_").join(first.picture.name, second.picture.name, "diff"), closed));
        return getResult(closed);
    }

    private Set<Coordinate> getResult(Bitmap bitmap) {
        Set<Coordinate> result = Sets.newLinkedHashSet();
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (bitmap.getPixel(x, y) == Color.WHITE) {
                    result.add(Coordinate.coordinate(x, y));
                }
            }
        }
        return result;
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
