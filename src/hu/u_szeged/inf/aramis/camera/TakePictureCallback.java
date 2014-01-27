package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.UiThread;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.MainApplication;
import hu.u_szeged.inf.aramis.activities.ResultActivity_;
import hu.u_szeged.inf.aramis.activities.listpictures.ProgressBarHandler;
import hu.u_szeged.inf.aramis.camera.picture.Clustering;
import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static hu.u_szeged.inf.aramis.camera.picture.PictureSaver.getFilePathForPicture;
import static hu.u_szeged.inf.aramis.model.Picture.picture;

@EBean
public class TakePictureCallback implements Camera.PreviewCallback {
    public static final int PICTURE_NUMBER = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(TakePictureCallback.class);
    @App
    protected MainApplication application;
    @Inject
    protected Context context;
    @Inject
    protected PictureCollector collector;
    @Inject
    protected PictureEvaluator evaluator;
    @Inject
    protected ProgressBarHandler progressBarHandler;
    @Inject
    protected ClusterUtils clusterCounter;
    @Inject
    protected Clustering clustering;
    @Inject
    protected MultipleCounterScheduler multipleCounterScheduler;
    private int[] pixels;
    private Camera.Size size;

    @AfterInject
    void injectRoboGuiceDependencies() {
        application.getInjector().injectMembers(this);
    }

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
            //progressBarHandler.start();
            Set<Coordinate> diffCoordinates = collector.getDiffCoordinates();
            List<Picture> pictures = collector.getPictures();
            Bitmap result = evaluator.evaluate(pictures, diffCoordinates);
            Picture backgroundPicture = picture(name + "_background", result);
            savePicture(backgroundPicture);
            collector.clear();
            multipleCounterScheduler.schedule(backgroundPicture, pictures);
            multipleCounterScheduler.getDiffCoordinates();
            List<Cluster<Coordinate>> clusterList = clustering.cluster(transformSet(diffCoordinates));
            Picture clusteredPicture = clusterCounter.createBitmapFromClusters(backgroundPicture.bitmap, clusterList);
            //progressBarHandler.stop();
            startResultActivity(backgroundPicture, clusterList, clusteredPicture);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted exception", ExceptionUtils.getRootCause(e));
        } catch (ExecutionException e) {
            LOGGER.error("Execution exception", ExceptionUtils.getRootCause(e));
        }

    }

    @UiThread
    protected void startResultActivity(Picture picture, List<Cluster<Coordinate>> clusters, Picture clusteredPicture) {
        try {
            ResultActivity_.intent(context).resultBitmapPath(getFilePathForPicture(picture)).clusterBitmapPath(getFilePathForPicture(clusteredPicture)).clusters(clusters).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background
    protected void decodePicture(byte[] bytes, String name) {
        LOGGER.info("Start decoding picture");
        decodeYUV420SP(bytes, size.width, size.height);
        LOGGER.info("Picture decoded");
        Picture picture = picture(name, createBitmap(pixels, size.width, size.height, ARGB_8888));
        collector.addPicture(picture);
        //savePicture(picture);
    }

    @Background
    protected void savePicture(Picture picture) {
        PictureSaver.save(picture);
    }

    private Table<Integer, Integer, Boolean> transformSet(Set<Coordinate> diffCoordinates) {
        Table<Integer, Integer, Boolean> table = HashBasedTable.create(diffCoordinates.size(), diffCoordinates.size());
        for (Coordinate coordinate : diffCoordinates) {
            table.put(coordinate.x, coordinate.y, false);
        }
        return table;
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
