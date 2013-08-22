package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hu.u_szeged.inf.aramis.model.Picture;

@EBean
public class PictureCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureCollector.class);
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd-hhmm");

    @RootContext
    protected Context context;
    private List<Picture> pictures = new ArrayList<Picture>();

    public void addPicture(Picture picture) {
        LOGGER.info(StringUtils.join("Picture number ", pictures.size()), " saved!");
        pictures.add(picture);
        save();
    }

    private void save() {
        try {
            File file = FileUtils.getFile(DirectoryHelper.getAlbumStorageDir("aramis"), new DateTime(formatter).toString());
            LOGGER.info("Trying to save picture to {}", file.getAbsolutePath());
            if (file.createNewFile()) {
                FileUtils.writeByteArrayToFile(file, pictures.get(0).bytes);
                LOGGER.info("Picture saved to " + file.getAbsolutePath());
            } else {
                Toast.makeText(context, "Couldn't create image file!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            LOGGER.error("Error writing picture file!", ExceptionUtils.getRootCause(e));
        }
    }
}
