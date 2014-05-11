package hu.u_szeged.inf.aramis.camera.process.difference;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableTable;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;
import com.jabistudio.androidjhlabs.filter.util.PixelUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import hu.u_szeged.inf.aramis.camera.utils.PictureSaver;
import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.DifferenceResult;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.utils.FilterUtils;

public class DiffCounter implements Callable<DifferenceResult> {
    public static final int BORDER = 15;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiffCounter.class);
    protected final int[] pixelsFromFirstBitmap;
    protected final int[] pixelsFromSecondBitmap;
    protected final String firstName;
    protected final String secondName;
    protected final int width;
    protected final int height;

    protected DiffCounter(int[] pixelsFromFirstBitmap, int[] pixelsFromSecondBitmap, String firstName, String secondName, int width, int height) {
        this.pixelsFromFirstBitmap = pixelsFromFirstBitmap;
        this.pixelsFromSecondBitmap = pixelsFromSecondBitmap;
        this.firstName = firstName;
        this.secondName = secondName;
        this.width = width;
        this.height = height;
    }

    public static DiffCounter diffCounter(BlurredPicture first, BlurredPicture second) {
        int width = first.picture.bitmap.getWidth();
        int height = first.picture.bitmap.getHeight();
        if (width != second.picture.bitmap.getWidth() || height != second.picture.bitmap.getHeight()) {
            throw new IllegalArgumentException("There are differences in the two picture dimensions");
        }
        return new DiffCounter(AndroidUtils.bitmapToIntArray(first.picture.bitmap),
                AndroidUtils.bitmapToIntArray(second.picture.bitmap),
                first.picture.name,
                second.picture.name,
                width,
                height);
    }

    @Override
    public DifferenceResult call() {
        return getDiffCoordinates();
    }

    protected DifferenceResult getDiffCoordinates() {
        LOGGER.info("Start creating diff");
        Bitmap diffBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int yOffset;
        int fullOffset;
        for (int y = 0; y < height; y++) {
            yOffset = y * width;
            for (int x = 0; x < width; x++) {
                fullOffset = yOffset + x;
                if (!PixelUtils.nearColors(pixelsFromFirstBitmap[fullOffset], pixelsFromSecondBitmap[fullOffset], BORDER)) {
                    diffBitmap.setPixel(x, y, Color.WHITE);
                }
            }
        }
        Bitmap closed = FilterUtils.morphologicalClosure(diffBitmap);
        savePicture(Picture.picture(Joiner.on("_").join(firstName, secondName, "diff"), closed));
        return DifferenceResult.differenceResult(getResult(closed));
    }

    protected ImmutableTable<Integer, Integer, Boolean> getResult(Bitmap bitmap) {
        ImmutableTable.Builder<Integer, Integer, Boolean> result = ImmutableTable.builder();
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (bitmap.getPixel(x, y) == Color.WHITE) {
                    result.put(x, y, false);
                }
            }
        }
        bitmap.recycle();
        return result.build();
    }

    protected void savePicture(Picture picture) {
        PictureSaver.save(picture);
    }
}
