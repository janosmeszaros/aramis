package hu.u_szeged.inf.aramis.model;

import android.graphics.Bitmap;

import static hu.u_szeged.inf.aramis.utils.FilterUtils.filterWithGaussian;

public class BlurredPicture {
    public final Picture picture;

    private BlurredPicture(Picture picture) {
        this.picture = picture;
    }

    public static BlurredPicture blurredPicture(Bitmap bitmap, String name) {
        Bitmap filteredBitmap = filterWithGaussian(bitmap);
        return new BlurredPicture(Picture.picture(name, filteredBitmap));
    }


}
