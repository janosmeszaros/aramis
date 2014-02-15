package hu.u_szeged.inf.aramis.camera.picture;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.testutils.CustomShadowBitmap;

import static hu.u_szeged.inf.aramis.model.Coordinate.coordinate;
import static hu.u_szeged.inf.aramis.model.Picture.picture;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class DiffCounterTest {

    private DiffCounter underTest;
    private CountDownLatch countDownLatch;


    @Before
    @Config(shadows = {CustomShadowBitmap.class})
    public void setup() {
        countDownLatch = mock(CountDownLatch.class);
    }

    @Test
    @Config(shadows = {CustomShadowBitmap.class})
    public void testCallWhenOneDiffIsPresent() {
        Picture picture1 = createSamplePicture(20, 30, 40);
        picture1.bitmap.setPixel(1, 1, Color.rgb(20 + DiffCounter.BORDER + 1, 30, 40));
        Picture picture2 = createSamplePicture(20, 30, 40);
        underTest = new DiffCounter(countDownLatch, picture1, picture2, ImmutableSet.<Coordinate>of());

        Set<Coordinate> diffs = underTest.call();

        assertThat(diffs.size(), equalTo(1));
        assertThat(diffs, hasItem(coordinate(1, 1)));
    }

    @Test
    @Config(shadows = {CustomShadowBitmap.class})
    public void testCallWhenNoDiffIsPresent() {
        Picture picture1 = createSamplePicture(20, 30, 40);
        Picture picture2 = createSamplePicture(20, 30, 40);
        underTest = new DiffCounter(countDownLatch, picture1, picture2, ImmutableSet.<Coordinate>of());

        Set<Coordinate> diffs = underTest.call();

        assertThat(diffs.size(), equalTo(0));
    }

    @Test
    @Config(shadows = {CustomShadowBitmap.class})
    public void testCallWhenTwoDiffsArePresent() {
        Picture picture1 = createSamplePicture(20, 30, 40);
        picture1.bitmap.setPixel(1, 1, Color.rgb(20, 30, 40 + DiffCounter.BORDER + 1));
        Picture picture2 = createSamplePicture(20, 30, 40);
        picture2.bitmap.setPixel(0, 1, Color.rgb(20, 30 + DiffCounter.BORDER + 1, 40));
        underTest = new DiffCounter(countDownLatch, picture1, picture2, ImmutableSet.<Coordinate>of());

        Set<Coordinate> diffs = underTest.call();

        assertThat(diffs.size(), equalTo(2));
        assertThat(diffs, hasItem(coordinate(1, 1)));
        assertThat(diffs, hasItem(coordinate(0, 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    @Config(shadows = {CustomShadowBitmap.class})
    public void testCallWhenThereIsDifferenceInPictureWeight() {
        Picture picture1 = createSamplePicture(20, 30, 40);
        Picture picture2 = picture("name", Bitmap.createBitmap(2, 3, Bitmap.Config.ARGB_4444));
        underTest = new DiffCounter(countDownLatch, picture1, picture2, ImmutableSet.<Coordinate>of());

        underTest.call();
    }


    @Test(expected = IllegalArgumentException.class)
    @Config(shadows = {CustomShadowBitmap.class})
    public void testCallWhenThereIsDifferenceInPictureHeight() {
        Picture picture1 = createSamplePicture(20, 30, 40);
        Picture picture2 = picture("name", Bitmap.createBitmap(3, 2, Bitmap.Config.ARGB_4444));
        underTest = new DiffCounter(countDownLatch, picture1, picture2, ImmutableSet.<Coordinate>of());

        underTest.call();
    }

    private Picture createSamplePicture(int red, int green, int blue) {
        Bitmap bitmap1 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_4444);
        fillBitmap(bitmap1, Color.rgb(red, green, blue));
        return picture("name", bitmap1);
    }

    private void fillBitmap(Bitmap bitmap1, int color) {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                bitmap1.setPixel(x, y, color);
            }
        }
    }
}
