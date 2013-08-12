package hu.u_szeged.inf.aramis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import hu.u_szeged.inf.aramis.camera.TakePictureCallback;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);
    @ViewById(R.id.camera_preview)
    protected FrameLayout preview;
    @ViewById(R.id.image_preview)
    protected ImageView imagePreview;
    @Bean
    protected TakePictureCallback takePictureCallback;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupCamera() {
        setupImagePreview();
        try {
            camera = getCameraInstance();
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
        camera.takePicture(null, null, takePictureCallback);
        setupImagePreview();
    }

    private Camera getCameraInstance() {
        Camera c = camera;
        if (c == null) {
            c = Camera.open();
            c.setDisplayOrientation(90);
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
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
