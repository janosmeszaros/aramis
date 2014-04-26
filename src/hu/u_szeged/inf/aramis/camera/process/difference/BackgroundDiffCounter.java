package hu.u_szeged.inf.aramis.camera.process.difference;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.base.Joiner;
import com.google.common.collect.Table;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;
import com.jabistudio.androidjhlabs.filter.util.PixelUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.DifferenceResult;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.utils.FilterUtils;

public class BackgroundDiffCounter extends DiffCounter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundDiffCounter.class);
    private final Picture picture;
    private final Table<Integer, Integer, Boolean> allDifferenceCoordinates;

    private BackgroundDiffCounter(int[] pixelsFromFirstBitmap,
                                  int[] pixelsFromSecondBitmap,
                                  String firstName,
                                  String secondName,
                                  int width,
                                  int height,
                                  Picture picture,
                                  Table<Integer, Integer, Boolean> allDifferenceCoordinates) {
        super(pixelsFromFirstBitmap, pixelsFromSecondBitmap, firstName, secondName, width, height);
        this.picture = picture;
        this.allDifferenceCoordinates = allDifferenceCoordinates;
    }

    public static BackgroundDiffCounter backgroundDiffCounter(BlurredPicture background,
                                                              BlurredPicture second,
                                                              Table<Integer, Integer, Boolean> allDifferenceCoordinates) {
        int width = background.picture.bitmap.getWidth();
        int height = background.picture.bitmap.getHeight();
        if (width != second.picture.bitmap.getWidth() || height != second.picture.bitmap.getHeight()) {
            throw new IllegalArgumentException("There are differences in the two picture dimensions");
        }
        return new BackgroundDiffCounter(AndroidUtils.bitmapToIntArray(background.picture.bitmap),
                AndroidUtils.bitmapToIntArray(second.picture.bitmap),
                background.picture.name,
                second.picture.name,
                width,
                height,
                second.picture,
                allDifferenceCoordinates);
    }

    protected DifferenceResult getDiffCoordinates() {
        LOGGER.info("Start creating diff with given coordinates no: {}", allDifferenceCoordinates.size());
        Bitmap diffBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int yOffset;
        int fullOffset;
        for (Integer y : allDifferenceCoordinates.columnKeySet()) {
            yOffset = y * width;
            for (Map.Entry<Integer, Boolean> row : allDifferenceCoordinates.column(y).entrySet()) {
                int x = row.getKey();
                fullOffset = x + yOffset;
                if (!PixelUtils.nearColors(pixelsFromFirstBitmap[fullOffset], pixelsFromSecondBitmap[fullOffset], BORDER)) {
                    diffBitmap.setPixel(x, y, Color.WHITE);
                }
            }
        }
        Bitmap closed = FilterUtils.morphologicalClosure(diffBitmap);
        savePicture(Picture.picture(Joiner.on("_").join(firstName, secondName, "diff"), closed));
        return DifferenceResult.differenceResult(getResult(closed), picture);
    }
}
