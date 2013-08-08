package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.hardware.Camera;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@EBean
public class TakePictureCallback implements Camera.PictureCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(TakePictureCallback.class);
    @RootContext
    Context context;

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        try {
            File file = FileUtils.getFile(context.getFilesDir(), "temp.jpg");
            FileUtils.writeByteArrayToFile(file, bytes);
            LOGGER.info("Picture saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error writing picture file!", ExceptionUtils.getRootCause(e));
        }
    }
}
