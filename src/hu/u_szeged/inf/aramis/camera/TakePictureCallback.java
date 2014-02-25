package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
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
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.MainApplication;
import hu.u_szeged.inf.aramis.activities.DifferencePicturesActivity_;
import hu.u_szeged.inf.aramis.activities.ResultActivity_;
import hu.u_szeged.inf.aramis.activities.listpictures.ProgressBarHandler;
import hu.u_szeged.inf.aramis.camera.picture.CannyEdgeDetector;
import hu.u_szeged.inf.aramis.camera.picture.Clustering;
import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.model.Rectangle;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static hu.u_szeged.inf.aramis.camera.picture.PictureSaver.getFilePathForPicture;
import static hu.u_szeged.inf.aramis.model.Picture.picture;
import static java.math.RoundingMode.HALF_UP;

@EBean
public class TakePictureCallback implements Camera.PreviewCallback {
    public static final int PICTURE_NUMBER = 5;
    public static final BigDecimal SIMILARITY = new BigDecimal(0.8);
    private static final Logger LOGGER = LoggerFactory.getLogger(TakePictureCallback.class);
    public static final int BORDER = 10;
    @App
    protected MainApplication application;
    @Inject
    protected Context context;
    @Inject
    protected PictureCollector collector;
    @Inject
    protected PictureEvaluator evaluator;
    @Inject
    protected CannyEdgeDetector detector;
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
        String name = String.valueOf(collector.getSize());
        decodePicture(bytes, name);
        if (collector.getSize() < PICTURE_NUMBER) {
            sleep();
            camera.setOneShotPreviewCallback(this);
        } else {
            evaluate();
        }
    }

    @Background
    protected void evaluate() {
        try {
            //progressBarHandler.start();
            Set<Coordinate> diffCoordinates = collector.getDiffCoordinates();
            List<Picture> pictures = collector.getPictures();
            Bitmap result = evaluator.evaluate(pictures, diffCoordinates);
            Picture backgroundPicture = picture(PictureSaver.DATE_TIME_FORMATTER.print(new DateTime()) + "_background", result);
            savePicture(backgroundPicture);
            collector.clear();
            multipleCounterScheduler.schedule(backgroundPicture, pictures, diffCoordinates);
            Map<Picture, Set<Coordinate>> resultBitmaps = multipleCounterScheduler.getDiffCoordinates();
            Map<Picture, List<Cluster<Coordinate>>> clustersForPictures = getClustersForPictures(resultBitmaps);
            createEdges(clustersForPictures);
            //progressBarHandler.stop();
            startPagerActivity(transformResult(resultBitmaps));
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted exception", ExceptionUtils.getRootCause(e));
        } catch (ExecutionException e) {
            LOGGER.error("Execution exception", ExceptionUtils.getRootCause(e));
        }

    }

    private void createEdges(Map<Picture, List<Cluster<Coordinate>>> clustersForPictures) {
        for (Map.Entry<Picture, List<Cluster<Coordinate>>> entry : clustersForPictures.entrySet()) {
            List<Cluster<Coordinate>> clusterList = entry.getValue();
            Iterator<Cluster<Coordinate>> iterator = clusterList.iterator();
            while (iterator.hasNext()) {
                Cluster<Coordinate> actual = iterator.next();
                Optional<Rectangle> edges = findEdges(actual);
                if (edges.isPresent()) {
                    String edgesName = String.format("edges_%d_%d_%d_%d", edges.get().minX, edges.get().minY, edges.get().maxX, edges.get().maxY);
                    LOGGER.info(edgesName);
                    Bitmap bitmap = cutPart(entry.getKey().bitmap, edges.get());
                    savePicture(picture(String.format("bitmap_%d_%d_%d_%d", edges.get().minX, edges.get().minY, edges.get().maxX - edges.get().minX, edges.get().maxY - edges.get().minY), bitmap));
                    savePicture(picture(edgesName, detector.process(bitmap)));
                } else {
                    iterator.remove();
                }
            }
        }

    }

    private Bitmap cutPart(Bitmap bitmap, Rectangle edges) {
        int minX = edges.minX - BORDER;
        int minY = edges.minY - BORDER;
        int maxX = edges.maxX + BORDER;
        int maxY = edges.maxY + BORDER;
        if (minX <= 0) {
            minX = 1;
        }
        if (minY <= 0) {
            minY = 1;
        }
        if (maxX > bitmap.getWidth()) {
            maxX = bitmap.getWidth();
        }
        if (maxY > bitmap.getHeight()) {
            maxY = bitmap.getHeight();
        }
        LOGGER.info("Crop image with values: {} {} {} {}", new Object[]{minX, minY, maxX, maxY});
        return Bitmap.createBitmap(bitmap, minX, minY, maxX - minX, maxY - minY);
    }

    private Optional<Rectangle> findEdges(Cluster<Coordinate> cluster) {
        int minX = 1000;
        int minY = 1000;
        int maxX = 0;
        int maxY = 0;

        for (Coordinate coordinate : cluster.getPoints()) {
            if (coordinate.x < minX) {
                minX = coordinate.x;
            }
            if (coordinate.x > maxX) {
                maxX = coordinate.x;
            }
            if (coordinate.y < minY) {
                minY = coordinate.y;
            }
            if (coordinate.y > maxY) {
                maxY = coordinate.y;
            }
        }
        Rectangle rectangle = Rectangle.rectangle(minX, minY, maxX, maxY);
        BigDecimal boundingArea = rectangle.getArea();
        BigDecimal area = new BigDecimal(cluster.getPoints().size());
        LOGGER.info("Divide area {} with {})", boundingArea, area);
        BigDecimal divide = area.divide(boundingArea, 4, HALF_UP);
        LOGGER.info("Similarity of {}  = {}", rectangle, divide);
        if (area.compareTo(new BigDecimal(100)) < 1 || divide.compareTo(SIMILARITY) > -1) {
            return Optional.absent();
        } else {
            return Optional.of(rectangle);
        }
    }

    private Map<String, Set<Coordinate>> transformResult(Map<Picture, Set<Coordinate>> result) {
        Map<String, Set<Coordinate>> transformedMap = Maps.newLinkedHashMap();
        for (Map.Entry<Picture, Set<Coordinate>> entry : result.entrySet()) {
            try {
                transformedMap.put(getFilePathForPicture(entry.getKey()), entry.getValue());
            } catch (IOException e) {
                LOGGER.error("Cannot find picture on the device!", e);
            }
        }
        return transformedMap;
    }

    private Map<Picture, List<Cluster<Coordinate>>> getClustersForPictures(Map<Picture, Set<Coordinate>> resultBitmaps) {
        Map<Picture, List<Cluster<Coordinate>>> clusterisedPictures = Maps.newLinkedHashMap();
        for (Map.Entry<Picture, Set<Coordinate>> entry : resultBitmaps.entrySet()) {
            List<Cluster<Coordinate>> clusterList = clustering.cluster(transformSet(entry.getValue()));
            clusterisedPictures.put(entry.getKey(), clusterList);
        }
        return clusterisedPictures;
    }

    private Table<Integer, Integer, Boolean> transformSet(Set<Coordinate> diffCoordinates) {
        Table<Integer, Integer, Boolean> table = HashBasedTable.create(diffCoordinates.size(), diffCoordinates.size());
        for (Coordinate coordinate : diffCoordinates) {
            table.put(coordinate.x, coordinate.y, false);
        }
        return table;
    }

    @UiThread
    protected void startResultActivity(Picture picture, List<Cluster<Coordinate>> clusters, Picture clusteredPicture) {
        try {
            ResultActivity_.intent(context).resultBitmapPath(getFilePathForPicture(picture)).clusterBitmapPath(getFilePathForPicture(clusteredPicture)).clusters(clusters).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    protected void startPagerActivity(Map<String, Set<Coordinate>> resultBitmaps) {
        DifferencePicturesActivity_.intent(context).resultBitmapPaths(resultBitmaps).start();
    }

    @Background
    protected void decodePicture(byte[] bytes, String name) {
        LOGGER.info("Start decoding picture");
        decodeYUV420SP(bytes, size.width, size.height);
        LOGGER.info("Picture decoded");
        Picture picture = picture(name, createBitmap(pixels, size.width, size.height, ARGB_8888));
        collector.addPicture(picture);
        savePicture(picture);
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
