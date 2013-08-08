package hu.u_szeged.inf.aramis;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.widget.FrameLayout;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.u_szeged.inf.aramis.camera.TakePictureCallback;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);
    @ViewById(R.id.camera_preview)
    protected FrameLayout preview;
    @Bean
    protected TakePictureCallback takePictureCallback;

    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupCamera() {
        camera = getCameraInstance();
        CameraPreview cameraPreview = CameraPreview_.build(getBaseContext());
        cameraPreview.setupCamera(camera);
        preview.addView(cameraPreview);
    }

    @Click(R.id.button_capture)
    public void takePicture() {
        camera.takePicture(null, null, takePictureCallback);
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
            c.setDisplayOrientation(90);
        } catch (Exception e) {
            LOGGER.error("Cant find camera", ExceptionUtils.getRootCause(e));
        }
        return c;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
