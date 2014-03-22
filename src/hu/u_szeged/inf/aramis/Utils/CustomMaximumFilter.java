package hu.u_szeged.inf.aramis.utils;

import android.graphics.Rect;

import com.jabistudio.androidjhlabs.filter.MaximumFilter;
import com.jabistudio.androidjhlabs.filter.util.PixelUtils;

public class CustomMaximumFilter extends MaximumFilter {
    private final int xSize;
    private final int ySize;

    public CustomMaximumFilter(int xSize, int ySize) {
        this.xSize = xSize / 2;
        this.ySize = ySize / 2;
    }

    @Override
    protected int[] filterPixels(int width, int height, int[] inPixels, Rect transformedSpace) {
        int index = 0;
        int[] outPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = 0xff000000;
                for (int dy = -ySize; dy <= ySize; dy++) {
                    int iy = y + dy;
                    int ioffset;
                    if (0 <= iy && iy < height) {
                        ioffset = iy * width;
                        for (int dx = -xSize; dx <= xSize; dx++) {
                            int ix = x + dx;
                            if (0 <= ix && ix < width) {
                                pixel = PixelUtils.combinePixels(pixel, inPixels[ioffset + ix], PixelUtils.MAX);
                            }
                        }
                    }
                }
                outPixels[index++] = pixel;
            }
        }
        return outPixels;
    }
}
