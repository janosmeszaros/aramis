package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.hardware.Camera;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static hu.u_szeged.inf.aramis.model.Picture.picture;

@EBean
public class TakePictureCallback implements Camera.PictureCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(TakePictureCallback.class);
    @RootContext
    protected Context context;
    @Bean
    protected PictureCollector collector;

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        LOGGER.info("Picture size {} {}", camera.getParameters().getPictureSize().height, camera.getParameters().getPictureSize().width);
        collector.addPicture(picture(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
        camera.startPreview();
        if (collector.getSize() < 5) {
            sleep();
            camera.takePicture(null, null, this);
        } else {
            try {
                collector.save(PictureEvaluator.evaluate(collector.getPictures()));
            } catch (IOException e) {
                LOGGER.error("Error evaluate pictures {}", e.getMessage());
            }
            collector.clear();
        }
    }

    private void sleep() {
        try {
            LOGGER.info("Sleeping for 1000 ms!");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
