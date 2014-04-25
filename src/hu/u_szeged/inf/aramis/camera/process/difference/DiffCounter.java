package hu.u_szeged.inf.aramis.camera.process.difference;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import hu.u_szeged.inf.aramis.camera.utils.PictureSaver;
import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.utils.FilterUtils;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static java.lang.Math.abs;

public class DiffCounter implements Callable<Table<Integer, Integer, Boolean>> {
    public static final int BORDER = 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiffCounter.class);
    private final Optional<BlurredPicture> first;
    private final Optional<Picture> background;
    private final CountDownLatch countDownLatch;
    private final BlurredPicture second;
    private final Table<Integer, Integer, Boolean> allDifferenceCoordinates;

    public DiffCounter(CountDownLatch countDownLatch, BlurredPicture first, BlurredPicture second) {
        if (first.picture.bitmap.getWidth() != second.picture.bitmap.getWidth() || first.picture.bitmap.getHeight() != second.picture.bitmap.getHeight()) {
            throw new IllegalArgumentException("There are differences in the two picture dimensions");
        }
        this.countDownLatch = countDownLatch;
        this.first = Optional.of(first);
        this.background = Optional.absent();
        this.second = second;
        this.allDifferenceCoordinates = HashBasedTable.create();
    }

    public DiffCounter(CountDownLatch countDownLatch, Picture first, BlurredPicture second, Table<Integer, Integer, Boolean> allDifferenceCoordinates) {
        if (first.bitmap.getWidth() != second.picture.bitmap.getWidth() || first.bitmap.getHeight() != second.picture.bitmap.getHeight()) {
            throw new IllegalArgumentException("There are differences in the two picture dimensions");
        }
        this.countDownLatch = countDownLatch;
        this.background = Optional.of(first);
        this.first = Optional.absent();
        this.second = second;
        this.allDifferenceCoordinates = HashBasedTable.create(allDifferenceCoordinates);
    }

    @Override
    public Table<Integer, Integer, Boolean> call() {
        Table<Integer, Integer, Boolean> coordinates;
        if (allDifferenceCoordinates.isEmpty()) {
            coordinates = getDiffCoordinatesFromAllPixels();
        } else {
            coordinates = getDiffCoordinatesFromDiffs();
        }
        countDownLatch.countDown();
        return coordinates;
    }

    private Table<Integer, Integer, Boolean> getDiffCoordinatesFromAllPixels() {
        LOGGER.info("Start creating diff");
        Bitmap firstBitmap = first.get().picture.bitmap;
        Bitmap secondBitmap = second.picture.bitmap;
        Bitmap diffBitmap = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(), Bitmap.Config.RGB_565);
        for (int x = 0; x < firstBitmap.getWidth(); x++) {
            for (int y = 0; y < firstBitmap.getHeight(); y++) {
                if (countTotalDiff(firstBitmap.getPixel(x, y), secondBitmap.getPixel(x, y)) > BORDER) {
                    diffBitmap.setPixel(x, y, Color.WHITE);
                }
            }
        }
        Bitmap closed = FilterUtils.morphologicalClosure(diffBitmap);
        savePicture(Picture.picture(Joiner.on("_").join(first.get().picture.name, second.picture.name, "diff"), closed));
        return getResult(closed);
    }

    private Table<Integer, Integer, Boolean> getDiffCoordinatesFromDiffs() {
        LOGGER.info("Start creating diff with given coordinates no: {}", allDifferenceCoordinates.size());
        Bitmap firstBitmap = background.get().bitmap;
        Bitmap secondBitmap = second.picture.bitmap;
        Bitmap diffBitmap = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(), Bitmap.Config.RGB_565);
        for (Table.Cell<Integer, Integer, Boolean> coordinate : allDifferenceCoordinates.cellSet()) {
            Integer x = coordinate.getRowKey();
            Integer y = coordinate.getColumnKey();
            int diff = countTotalDiff(firstBitmap.getPixel(x, y), secondBitmap.getPixel(x, y));
            if (diff > BORDER) {
                diffBitmap.setPixel(x, y, Color.WHITE);
            }
        }
        savePicture(Picture.picture(Joiner.on("_").join(background.get().name, second.picture.name, "diff"), diffBitmap));
        Bitmap closed = FilterUtils.morphologicalClosure(diffBitmap);
        return getResult(closed);
    }

    private Table<Integer, Integer, Boolean> getResult(Bitmap bitmap) {
        ImmutableTable.Builder<Integer, Integer, Boolean> result = ImmutableTable.builder();
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (bitmap.getPixel(x, y) == Color.WHITE) {
                    result.put(x, y, false);
                }
            }
        }
        return result.build();
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
