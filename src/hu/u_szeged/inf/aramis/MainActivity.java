package hu.u_szeged.inf.aramis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.SeekBarProgressChange;
import com.googlecode.androidannotations.annotations.ViewById;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Timer;

import hu.u_szeged.inf.aramis.camera.TakePictureCallback;

import static android.hardware.Camera.open;

@EActivity(R.layout.activity_main)
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
    @Bean
    protected TakePictureCallback takePictureCallback;
    private Camera camera;

    private Timer pictureTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupCamera() {
        setupImagePreview();
        try {
            camera = getCameraInstance();
            brightness.setMax(camera.getParameters().getMaxExposureCompensation() - camera.getParameters().getMinExposureCompensation());
            brightness.setProgress(brightness.getMax());
            LOGGER.info("Progress set to " + brightness.getMax());
            setupPreview();
        } catch (Exception e) {
            Toast error = Toast.makeText(getBaseContext(), "Error getting camera instance!", Toast.LENGTH_LONG);
            error.show();
        }
    }

    private void setupImagePreview() {
        File file = FileUtils.getFile(getBaseContext().getFilesDir(), "temp.jpg");
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        imagePreview.setImageBitmap(bitmap);
    }

    private void setupPreview() {
        CameraPreview cameraPreview = CameraPreview_.build(getBaseContext());
        cameraPreview.setupCamera(camera);
        preview.addView(cameraPreview);
    }

    @Click(R.id.button_capture)
    public void takePicture() {
        LOGGER.info("Start taking picture!");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        new Thread() {
            @Override
            public void run() {
                camera.takePicture(null, null, takePictureCallback);
            }
        }.start();
    }

    private Camera getCameraInstance() {
        Camera c = camera;
        if (c == null) {
            c = open();
            c.setDisplayOrientation(90);
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
            camera.release();
            camera = null;
        }
    }
}