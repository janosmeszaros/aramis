package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.hardware.Camera;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;

import static hu.u_szeged.inf.aramis.camera.PictureEvaluator.evaluate;
import static hu.u_szeged.inf.aramis.model.Picture.picture;

@EBean
public class TakePictureCallback implements Camera.PictureCallback {
    public static final int PICTURE_NUMBER = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(TakePictureCallback.class);
    @RootContext
    protected Context context;
    @Bean
    protected PictureCollector collector;

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        collector.addPicture(picture(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
        camera.startPreview();
        if (collector.getSize() < PICTURE_NUMBER) {
            sleep();
            camera.takePicture(null, null, this);
        } else {
            try {
                PictureSaver.save(evaluate(collector.getPictures(), collector.getDiffCoordinates()));
                collector.clear();
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted exception", ExceptionUtils.getRootCause(e));
            } catch (ExecutionException e) {
                LOGGER.error("Execution exception", ExceptionUtils.getRootCause(e));
            }
        }
    }

    private void sleep() {
        try {
            LOGGER.info("Sleeping for 1000 ms!");
            Thread.sleep(1000);
            LOGGER.info("He is awake!!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
