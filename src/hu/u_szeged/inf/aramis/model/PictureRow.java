package hu.u_szeged.inf.aramis.model;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import java.io.File;

public class PictureRow {
    public final Bitmap bitmap;
    public final DateTime lastModified;
    public final ImmutableList<File> files;

    private PictureRow(DateTime lastModified, ImmutableList<File> files) {
        Validate.notNull(lastModified, "lastModified must not be null");
        Validate.notEmpty(files, "files must not be null");
        this.bitmap = BitmapFactory.decodeFile(files.get(0).getAbsolutePath());
        this.lastModified = lastModified;
        this.files = files;
    }

    public static PictureRow pictureRow(DateTime lastModified, ImmutableList<File> file) {
        return new PictureRow(lastModified, file);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
