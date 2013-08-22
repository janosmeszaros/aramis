package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.hardware.Camera;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import static hu.u_szeged.inf.aramis.model.Picture.picture;

@EBean
public class TakePictureCallback implements Camera.PictureCallback {
    @RootContext
    protected Context context;
    @Bean
    protected PictureCollector collector;

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        collector.addPicture(picture(bytes));
        camera.startPreview();
    }
}
