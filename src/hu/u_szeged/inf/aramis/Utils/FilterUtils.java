package hu.u_szeged.inf.aramis.utils;

import android.graphics.Bitmap;

import com.jabistudio.androidjhlabs.filter.GaussianFilter;
import com.jabistudio.androidjhlabs.filter.MaximumFilter;
import com.jabistudio.androidjhlabs.filter.MedianFilter;
import com.jabistudio.androidjhlabs.filter.MinimumFilter;

public class FilterUtils {
    public static final GaussianFilter GAUSSIAN_FILTER = new GaussianFilter(4.0f);
    public static final MedianFilter MEDIAN_FILTER = new MedianFilter();
    public static final MaximumFilter MAX_FILTER = new MaximumFilter();
    public static final MinimumFilter MIN_FILTER = new MinimumFilter();
    private static int[] pixels;
    private static int[] filtered;


    public static synchronized Bitmap filterWithGaussian(Bitmap bitmap) {
        getPixels(bitmap);
        filtered = GAUSSIAN_FILTER.filter(pixels, bitmap.getWidth(), bitmap.getHeight());
        return Bitmap.createBitmap(filtered, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    public static synchronized Bitmap filterWithMedian(Bitmap bitmap) {
        getPixels(bitmap);
        filtered = MEDIAN_FILTER.filter(pixels, bitmap.getWidth(), bitmap.getHeight());
        return Bitmap.createBitmap(filtered, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    public static synchronized Bitmap morphologicalClosure(Bitmap bitmap) {
        getPixels(bitmap);
        filtered = MAX_FILTER.filter(pixels, bitmap.getWidth(), bitmap.getHeight());
        int[] erozed = MIN_FILTER.filter(filtered, bitmap.getWidth(), bitmap.getHeight());
        return Bitmap.createBitmap(erozed, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    private static void getPixels(Bitmap bitmap) {
        pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }
}
