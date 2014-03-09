package hu.u_szeged.inf.aramis;

import android.app.Activity;
import android.graphics.Bitmap;
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

import hu.u_szeged.inf.aramis.activities.listpictures.PictureListActivity_;
import hu.u_szeged.inf.aramis.camera.DirectoryHelper;
import hu.u_szeged.inf.aramis.camera.TakePictureCallback;
import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;

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
    }

    @AfterViews
    protected void setupCamera() {
        //setupImagePreview();
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

    private void setupImagePreview() {
        Bitmap bitmap = DirectoryHelper.getLastPictureFromAlbum(PictureSaver.ALBUM_NAME);
        imagePreview.setImageBitmap(bitmap);
    }

    private void setupPreview() {
        cameraPreview = CameraPreview_.build(getBaseContext());
        cameraPreview.setupCamera(camera);
        preview.addView(cameraPreview);
    }

    @Click(R.id.button_capture)
    public void takePicture() {
        LOGGER.info("Start taking pictures!");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PictureSaver.newAlbum();
        camera.setOneShotPreviewCallback(takePictureCallback);
    }

    @Click(R.id.image_preview)
    public void showPictureList() {
        PictureListActivity_.intent(this).start();
    }

    private Camera getCameraInstance() {
        Camera c = camera;
        if (c == null) {
            c = open();
            c.setDisplayOrientation(90);
            Camera.Parameters parameters = c.getParameters();
            parameters.setRotation(90);
            takePictureCallback.setSize(parameters.getPictureSize());
            c.setParameters(parameters);
        }
        return c;
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
        LOGGER.info("OnResume");
        super.onResume();
        if (camera == null) {
            LOGGER.info("Resetup camera");
            setupCamera();
        }
    }

    @Override
    protected void onPause() {
        LOGGER.info("Releasing camera");
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