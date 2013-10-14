package hu.u_szeged.inf.aramis.model;


import android.graphics.Bitmap;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import java.io.File;

public class PictureRow {
    public final Bitmap bitmap;
    public final DateTime lastModified;
    public final File file;

    private PictureRow(Bitmap bitmap, DateTime lastModified, File file) {
        Validate.notNull(bitmap, "bitmap must not be null");
        Validate.notNull(lastModified, "lastModified must not be null");
        this.bitmap = bitmap;
        this.lastModified = lastModified;
        this.file = file;
    }

    public static PictureRow pictureRow(Bitmap bitmap, DateTime lastModified, File file) {
        return new PictureRow(bitmap, lastModified, file);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
