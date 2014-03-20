package hu.u_szeged.inf.aramis.model;

import android.graphics.Bitmap;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Picture implements Comparable {
    public final Bitmap bitmap;
    public final String name;


    private Picture(Bitmap bitmap, String name) {
        this.bitmap = bitmap;
        this.name = name;
    }

    public static Picture picture(String name, Bitmap bitmap) {
        Validate.notEmpty(name, "name must not be empty!");
        Validate.notNull(bitmap, "bitmap must not be null!");
        return new Picture(bitmap, name);
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

    @Override
    public int compareTo(Object o) {
        return this.name.compareTo(((Picture) o).name);
    }
}
