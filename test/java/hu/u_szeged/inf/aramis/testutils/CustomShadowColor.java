package hu.u_szeged.inf.aramis.testutils;

import android.graphics.Color;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowColor;

@Implements(Color.class)
public class CustomShadowColor extends ShadowColor {

    @Implementation
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    @Implementation
    public static int blue(int color) {
        return color & 0xFF;
    }

    @Implementation
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    @Implementation
    public static int alpha(int color) {
        return color >>> 24;
    }
}
