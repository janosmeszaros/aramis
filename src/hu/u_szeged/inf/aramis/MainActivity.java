package hu.u_szeged.inf.aramis;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.inject.Inject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.RoboGuice;
import com.googlecode.androidannotations.annotations.SeekBarProgressChange;
import com.googlecode.androidannotations.annotations.ViewById;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hu.u_szeged.inf.aramis.activities.fileselector.FileChooser_;
import hu.u_szeged.inf.aramis.activities.listpictures.PictureListActivity_;
import hu.u_szeged.inf.aramis.camera.TakePictureCallback;

import static android.hardware.Camera.open;

@EActivity(R.layout.activity_main)
@RoboGuice
public class MainActivity extends Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);
    @ViewById(R.id.camera_preview)
    protected FrameLayout preview;
    @ViewById(R.id.image_preview)
    protected ImageView imagePreview;
    @ViewById(R.id.seekBar)
    protected SeekBar brightness;
    @ViewById(R.id.button_capture)
    protected Button captureButton;
    @Inject
    protected TakePictureCallback takePictureCallback;
    private Camera camera;
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @AfterViews
    protected void setupCamera() {
        try {
            camera = getCameraInstance();
            brightness.setMax(camera.getParameters().getMaxExposureCompensation() - camera.getParameters().getMinExposureCompensation());
            brightness.setProgress(0);
            setupPreview();
        } catch (Exception e) {
            Toast error = Toast.makeText(getBaseContext(), "Error getting camera instance!", Toast.LENGTH_LONG);
            error.show();
        }
    }

    private void setupPreview() {
        cameraPreview = CameraPreview_.build(getBaseContext());
        cameraPreview.setupCamera(camera);
        preview.addView(cameraPreview);
    }

    @Click(R.id.button_capture)
    public void takePicture() {
        LOGGER.info("Start taking pictures!");
        camera.setOneShotPreviewCallback(takePictureCallback);
    }

    @Click(R.id.image_preview)
    public void showPictureList() {
        PictureListActivity_.intent(this).start();
    }

    @Click(R.id.button_select)
    public void startSelector() {
        FileChooser_.intent(this).start();
    }

    private Camera getCameraInstance() {
        Camera c = camera;
        if (c == null) {
            c = open();
            c.setDisplayOrientation(90);
            Camera.Parameters parameters = c.getParameters();
            parameters.setRotation(90);
            Camera.Size size = determinePictureSize(parameters);
            LOGGER.info("Setting camera size to {} {}", size.width, size.height);
            parameters.setPictureSize(size.width, size.height);
            takePictureCallback.setSize(parameters.getPictureSize());
            c.setParameters(parameters);
        }
        return c;
    }

    private Camera.Size determinePictureSize(Camera.Parameters parameters) {
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        Collections.sort(supportedPictureSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size, Camera.Size size2) {
                return Integer.valueOf(size.width * size.height).compareTo(size2.height * size2.width);
            }
        });
        return supportedPictureSizes.get(0);
    }

    @SeekBarProgressChange(R.id.seekBar)
    protected void setupBrightness(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setExposureCompensation(progress - parameters.getMaxExposureCompensation());
            camera.setParameters(parameters);
            LOGGER.info("Exposure set to " + (progress - parameters.getMaxExposureCompensation()));
        } else {
            LOGGER.debug("Seekbar's value changed to {} pragmatically!", progress);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            setupCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            cameraPreview.getHolder().removeCallback(cameraPreview);
            camera.release();
            camera = null;
        }
    }
}