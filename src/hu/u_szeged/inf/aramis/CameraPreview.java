package hu.u_szeged.inf.aramis;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.googlecode.androidannotations.annotations.EView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@EView
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraPreview.class);
    private SurfaceHolder mHolder;
    private Camera camera;

    public CameraPreview(Context context) {
        super(context);
    }

    public void setupCamera(Camera camera) {
        this.camera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            LOGGER.error("Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            camera.stopPreview();
            camera.setPreviewDisplay(mHolder);
            camera.startPreview();
        } catch (Exception e) {
            LOGGER.error("Error starting camera preview: " + e.getMessage());
        }
    }
}