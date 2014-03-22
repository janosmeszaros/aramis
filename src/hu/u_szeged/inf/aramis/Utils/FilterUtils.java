package hu.u_szeged.inf.aramis.utils;

import android.graphics.Bitmap;

import com.jabistudio.androidjhlabs.filter.GaussianFilter;
import com.jabistudio.androidjhlabs.filter.MaximumFilter;
import com.jabistudio.androidjhlabs.filter.MedianFilter;
import com.jabistudio.androidjhlabs.filter.MinimumFilter;
import com.jabistudio.androidjhlabs.filter.SharpenFilter;

public class FilterUtils {
    public static final GaussianFilter GAUSSIAN_FILTER = new GaussianFilter(4.0f);
    public static final MedianFilter MEDIAN_FILTER = new MedianFilter();
    public static final MaximumFilter MAX_FILTER = new CustomMaximumFilter(6, 6);
    public static final MinimumFilter MIN_FILTER = new CustomMinimumFilter(6, 6);
    public static final SharpenFilter SHARPEN_FILTER = new SharpenFilter();

    public static Bitmap filterWithGaussian(Bitmap bitmap) {
        int[] pixels = getPixels(bitmap);
        pixels = GAUSSIAN_FILTER.filter(pixels, bitmap.getWidth(), bitmap.getHeight());
        return Bitmap.createBitmap(pixels, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    public static Bitmap filterWithMedian(Bitmap bitmap) {
        int[] pixels = getPixels(bitmap);
        pixels = MEDIAN_FILTER.filter(pixels, bitmap.getWidth(), bitmap.getHeight());
        return Bitmap.createBitmap(pixels, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    public static Bitmap morphologicalClosure(Bitmap bitmap) {
        int[] pixels = getPixels(bitmap);
        pixels = MAX_FILTER.filter(pixels, bitmap.getWidth(), bitmap.getHeight());
        pixels = MIN_FILTER.filter(pixels, bitmap.getWidth(), bitmap.getHeight());
        return Bitmap.createBitmap(pixels, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    public static Bitmap sharp(Bitmap bitmap) {
        int[] pixels = getPixels(bitmap);
        pixels = SHARPEN_FILTER.filter(pixels, bitmap.getWidth(), bitmap.getHeight());
        return Bitmap.createBitmap(pixels, bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    private static synchronized int[] getPixels(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        bitmap.recycle();
        return pixels;
    }
}
