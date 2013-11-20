package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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
public class PictureEvaluatorUnitTest {
    private List<Picture> pictures;
    private List<Coordinate> diffCoordinates;

    @Before
    @Config(shadows = {CustomShadowBitmap.class})
    public void setup() {
        pictures = new ArrayList<Picture>();
        diffCoordinates = new ArrayList<Coordinate>();

        createSamplePicture(Color.BLUE);
        createSamplePicture(Color.BLACK);
        createSamplePicture(Color.WHITE);
    }

    private void createSamplePicture(int color) {
        Bitmap bitmap1 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_4444);
        fillBitmap(bitmap1, color);
        pictures.add(picture("name", bitmap1));
    }

    private void fillBitmap(Bitmap bitmap1, int color) {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                bitmap1.setPixel(x, y, color);
            }
        }
    }

    @Test
    @Config(shadows = {CustomShadowBitmap.class})
    public void testEvaluateWhenOnlyOnePixelIsDifferent() {
        diffCoordinates.add(coordinate(1, 1));

        Bitmap bitmap = PictureEvaluator.evaluate(pictures, diffCoordinates);

        assertThat(bitmap.getPixel(0, 0), equalTo(Color.WHITE));
        assertThat(bitmap.getPixel(1, 1), equalTo(Color.BLUE));
    }

    @Test
    @Config(shadows = {CustomShadowBitmap.class})
    public void testEvaluateWhenNoneOfThePixelsAreDifferent() {
        Bitmap bitmap = PictureEvaluator.evaluate(pictures, diffCoordinates);

        assertThat(bitmap, equalTo(pictures.get(pictures.size() - 1).bitmap));
    }

    @Test
    @Config(shadows = {CustomShadowBitmap.class})
    public void testEvaluateWhenAllOfThePixelsAreDifferent() {
        diffCoordinates.add(coordinate(1, 1));
        diffCoordinates.add(coordinate(0, 1));
        diffCoordinates.add(coordinate(1, 0));
        diffCoordinates.add(coordinate(0, 0));

        Bitmap bitmap = PictureEvaluator.evaluate(pictures, diffCoordinates);

        assertThat(bitmap.getPixel(1, 1), equalTo(Color.BLUE));
        assertThat(bitmap.getPixel(0, 1), equalTo(Color.BLUE));
        assertThat(bitmap.getPixel(1, 0), equalTo(Color.BLUE));
        assertThat(bitmap.getPixel(0, 0), equalTo(Color.BLUE));
    }

    @Test
    @Config(shadows = {CustomShadowBitmap.class})
    public void testEvaluateWhenThereAreOddNumberOfPictures() {
        createSamplePicture(Color.RED);
        diffCoordinates.add(coordinate(1, 1));

        Bitmap bitmap = PictureEvaluator.evaluate(pictures, diffCoordinates);

        assertThat(bitmap.getPixel(1, 1), equalTo(Color.RED));
    }
}
