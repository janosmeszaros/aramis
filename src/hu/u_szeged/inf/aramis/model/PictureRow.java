package hu.u_szeged.inf.aramis.model;


import android.graphics.Bitmap;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;

public class PictureRow {
    public final Bitmap bitmap;
    public final DateTime lastModified;

    private PictureRow(Bitmap bitmap, DateTime lastModified) {
        Validate.notNull(bitmap, "bitmap must not be null");
        Validate.notNull(lastModified, "lastModified must not be null");
        this.bitmap = bitmap;
        this.lastModified = lastModified;
    }

    public static PictureRow pictureRow(Bitmap bitmap, DateTime lastModified) {
        return new PictureRow(bitmap, lastModified);
    }
}
