package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.testutils.CustomShadowBitmap;

import static hu.u_szeged.inf.aramis.model.Coordinate.coordinate;
import static hu.u_szeged.inf.aramis.model.Picture.picture;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PictureEvaluatorTest {
    private List<Picture> pictures;
    private List<Coordinate> diffCoordinates;

    @Before
    @Config(shadows = {CustomShadowBitmap.class})
    public void setup() {
        pictures = new ArrayList<Picture>();
        diffCoordinates = new ArrayList<Coordinate>();

        Bitmap bitmap1 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_4444);
        Bitmap bitmap2 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_4444);
        Bitmap bitmap3 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_4444);
        bitmap1.setPixel(0, 0, Color.BLACK);
        bitmap2.setPixel(0, 0, Color.WHITE);
        bitmap3.setPixel(0, 0, Color.BLUE);
        pictures.add(picture(bitmap1));
        pictures.add(picture(bitmap2));
        pictures.add(picture(bitmap3));
        diffCoordinates.add(coordinate(1, 1));
    }

    @Test
    @Config(shadows = {CustomShadowBitmap.class})
    public void testEvaluate() throws IOException {
        Picture picture = PictureEvaluator.evaluate(pictures);

        assertThat(picture.bitmap.getPixel(0, 0), equalTo(Color.BLUE));
    }
}
