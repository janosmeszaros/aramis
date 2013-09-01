package hu.u_szeged.inf.aramis.camera.picture;

import android.graphics.Bitmap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import hu.u_szeged.inf.aramis.camera.DirectoryHelper;
import hu.u_szeged.inf.aramis.model.Picture;

public class PictureSaver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureSaver.class);
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMMdd-HHmmssSS");

    public static void save(Picture picture) {
        try {
            File file = FileUtils.getFile(DirectoryHelper.getAlbumStorageDir("aramis"), StringUtils.join(formatter.print(new DateTime()), ".jpeg"));
            LOGGER.info("Trying to save picture to {}", file.getAbsolutePath());
            if (file.createNewFile()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                picture.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                FileUtils.writeByteArrayToFile(file, out.toByteArray());
                out.close();
                LOGGER.info("Picture saved to " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error("Error writing picture file!", ExceptionUtils.getRootCause(e));
        }
    }
}
