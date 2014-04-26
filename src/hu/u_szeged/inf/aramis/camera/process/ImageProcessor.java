package hu.u_szeged.inf.aramis.camera.process;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EBean;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.MainApplication;
import hu.u_szeged.inf.aramis.camera.process.difference.Clustering;
import hu.u_szeged.inf.aramis.camera.process.difference.MultipleCounterScheduler;
import hu.u_szeged.inf.aramis.camera.process.display.ChainDetector;
import hu.u_szeged.inf.aramis.camera.process.motion.ClusterComparator;
import hu.u_szeged.inf.aramis.camera.utils.PictureSaver;
import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.ClusterPair;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.model.ProcessResult;

import static hu.u_szeged.inf.aramis.model.BlurredPicture.blurredPicture;
import static hu.u_szeged.inf.aramis.model.Picture.picture;
import static hu.u_szeged.inf.aramis.model.ProcessResult.processResult;

@EBean
public class ImageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessor.class);
    @App
    protected MainApplication application;
    @Inject
    private PictureEvaluator evaluator;
    @Inject
    private MultipleCounterScheduler multipleCounterScheduler;
    @Inject
    private ClusterComparator clusterComparator;
    @Inject
    private Clustering clustering;
    @Inject
    private Context context;
    @Inject
    private ChainDetector chainDetector;

    @AfterInject
    void injectRoboGuiceDependencies() {
        application.getInjector().injectMembers(this);
    }

    public ProcessResult processImages(Table<Integer, Integer, Boolean> diffCoordinates, List<BlurredPicture> blurredPictures) throws InterruptedException, ExecutionException, IOException {
        List<Picture> originalPictures = loadOriginalPictures(blurredPictures);
        Bitmap result = evaluator.evaluate(originalPictures);
        Picture backgroundPicture = picture(PictureSaver.DATE_TIME_FORMATTER.print(new DateTime()) + "_background", result);
        PictureSaver.save(backgroundPicture);
        BlurredPicture blurredBackground = blurredPicture(result.copy(result.getConfig(), false), backgroundPicture.name);
        multipleCounterScheduler.schedule(blurredBackground, blurredPictures, diffCoordinates);
        Map<Picture, Table<Integer, Integer, Boolean>> resultBitmaps = multipleCounterScheduler.getDiffCoordinates2();
        Map<Picture, List<Cluster<Coordinate>>> clustersForPictures = getClustersForPictures(resultBitmaps);
        Map<Picture, List<ClusterPair>> pictureListMap = clusterComparator.countSimilarity(clustersForPictures);
        return processResult(loadOriginalInputs(pictureListMap), backgroundPicture);
    }


    private List<Picture> loadOriginalPictures(List<BlurredPicture> blurredPictures) {
        List<Picture> result = Lists.newArrayList();
        for (BlurredPicture picture : blurredPictures) {
            try {
                LOGGER.info("Finding {}", picture.picture.name);
                String filePathForPicture = PictureSaver.getFilePathForPicture(picture.picture.name);
                Bitmap bitmap = BitmapFactory.decodeFile(filePathForPicture);
                result.add(picture(picture.picture.name, bitmap));
            } catch (IOException e) {
                LOGGER.error("Cannot find picture {}", picture.picture.name);
            }
        }
        return result;
    }

    private Map<Picture, List<ClusterPair>> loadOriginalInputs(Map<Picture, List<ClusterPair>> map) {
        Map<Picture, List<ClusterPair>> result = Maps.newHashMap();
        for (Map.Entry<Picture, List<ClusterPair>> entry : map.entrySet()) {
            try {
                String filePathForPicture = PictureSaver.getFilePathForPicture(entry.getKey().name);
                Bitmap bitmap = BitmapFactory.decodeFile(filePathForPicture);
                entry.getKey().bitmap.recycle();
                result.put(picture(entry.getKey().name, bitmap), entry.getValue());
            } catch (IOException e) {
                LOGGER.error("Cannot find picture {}", entry.getKey().name);
            }
        }
        return result;
    }

    private Map<Picture, List<Cluster<Coordinate>>> getClustersForPictures
            (Map<Picture, Table<Integer, Integer, Boolean>> resultBitmaps) {
        Map<Picture, List<Cluster<Coordinate>>> clusterisedPictures = Maps.newLinkedHashMap();
        for (Map.Entry<Picture, Table<Integer, Integer, Boolean>> entry : resultBitmaps.entrySet()) {
            List<Cluster<Coordinate>> clusterList = clustering.cluster(entry.getValue());
            clusterisedPictures.put(entry.getKey(), clusterList);
        }
        return clusterisedPictures;
    }
}
