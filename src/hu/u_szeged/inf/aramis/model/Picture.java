package hu.u_szeged.inf.aramis.model;

import android.graphics.Bitmap;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Picture {
    public final Bitmap bitmap;

    private Picture(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public static Picture picture(Bitmap bitmap) {
        Validate.notNull(bitmap, "bitmap must not be null!");
        return new Picture(bitmap);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
