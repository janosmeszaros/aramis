package hu.u_szeged.inf.aramis.testutils;

import android.graphics.Bitmap;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowBitmap;

import static org.robolectric.Robolectric.shadowOf;

@Implements(Bitmap.class)
public class CustomShadowBitmap extends ShadowBitmap {
    private int[][] pixels;


    @Implementation
    public void setPixel(int x, int y, int color) {
        if (pixels == null) {
            pixels = new int[getWidth()][getHeight()];
        }
        pixels[x][y] = color;
    }

    @Implementation
    public int getPixel(int x, int y) {
        if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) {
            throw new IllegalArgumentException();
        }
        return pixels[x][y];
    }

    @Implementation
    public Bitmap copy(Bitmap.Config config, boolean isMutable) {
        Bitmap newBitmap = Robolectric.newInstanceOf(Bitmap.class);
        CustomShadowBitmap shadowBitmap = (CustomShadowBitmap) shadowOf(newBitmap);
        shadowBitmap.setPixels(pixels);
        shadowBitmap.setHeight(getHeight());
        shadowBitmap.setWidth(getWidth());
        return newBitmap;
    }

    public void setPixels(int[][] pixels) {
        this.pixels = pixels;
    }
}
