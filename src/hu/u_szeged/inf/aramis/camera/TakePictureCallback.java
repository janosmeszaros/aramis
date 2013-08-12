package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.hardware.Camera;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@EBean
public class TakePictureCallback implements Camera.PictureCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(TakePictureCallback.class);
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy.MM.dd-hh:mm");
    @RootContext
    protected Context context;


    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        try {
            File file = FileUtils.getFile(DirectoryHelper.getAlbumStorageDir("aramis"), "temp.jpg");
            LOGGER.info("Trying to save picture to {}", file.getAbsolutePath());
            if (file.createNewFile()) {
                FileUtils.writeByteArrayToFile(file, bytes);
                LOGGER.info("Picture saved to " + file.getAbsolutePath());
            } else {
                Toast.makeText(context, "Couldn't create image file!", Toast.LENGTH_LONG).show();
            }
            camera.startPreview();
        } catch (IOException e) {
            LOGGER.error("Error writing picture file!", ExceptionUtils.getRootCause(e));
        }
    }
}
