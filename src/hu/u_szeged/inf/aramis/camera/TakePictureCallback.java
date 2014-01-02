package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;
import android.hardware.Camera;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;
import com.googlecode.androidannotations.annotations.UiThread;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.MainActivity;
import hu.u_szeged.inf.aramis.activities.ResultActivity_;
import hu.u_szeged.inf.aramis.activities.listpictures.ProgressBarHandler;
import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.model.Picture.picture;

@EBean
public class TakePictureCallback implements Camera.PreviewCallback {
    public static final int PICTURE_NUMBER = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(TakePictureCallback.class);
    @RootContext
    protected MainActivity context;
    @Bean
    protected PictureCollector collector;
    @Bean
    protected PictureEvaluator evaluator;
    @Bean
    protected ProgressBarHandler progressBarHandler;
    private int[] pixels;
    private Camera.Size size;

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        String name = PictureSaver.DATE_TIME_FORMATTER.print(new DateTime());
        decodePicture(bytes, name);
        if (collector.getSize() < PICTURE_NUMBER) {
            sleep();
            camera.setOneShotPreviewCallback(this);
        } else {
            evaluate(name);
        }
    }

    @Background
    protected void evaluate(String name) {
        try {
            progressBarHandler.start();
            Set<Coordinate> diffCoordinates = collector.getDiffCoordinates();
            Bitmap result = evaluator.evaluate(collector.getPictures(), diffCoordinates);
            Picture resultPicture = picture(name + "_result", result);
            savePicture(resultPicture);
            collector.clear();
            progressBarHandler.stop();
            startResultActivity(resultPicture);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted exception", ExceptionUtils.getRootCause(e));
        } catch (ExecutionException e) {
            LOGGER.error("Execution exception", ExceptionUtils.getRootCause(e));
        }

    }

    @UiThread
    protected void startResultActivity(Picture picture) {
        try {
            ResultActivity_.intent(context).bitmapPath(PictureSaver.getDirectoryToSave(picture)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background
    protected void decodePicture(byte[] bytes, String name) {
        LOGGER.info("Start decoding picture");
        decodeYUV420SP(bytes, size.width, size.height);
        LOGGER.info("Picture decoded");
        Picture picture = picture(name, Bitmap.createBitmap(pixels, size.width, size.height, Bitmap.Config.ARGB_8888));
        collector.addPicture(picture);
        //savePicture(picture);
    }

    @Background
    protected void savePicture(Picture picture) {
        PictureSaver.save(picture);
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

    private void decodeYUV420SP(byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143)
                    b = 262143;

                pixels[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public void setSize(Camera.Size size) {
        this.size = size;
        pixels = new int[size.height * size.width];
    }
}
