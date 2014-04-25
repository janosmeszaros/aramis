package hu.u_szeged.inf.aramis.camera.process;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EBean;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.joda.time.DateTime;

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
import hu.u_szeged.inf.aramis.utils.FilterUtils;

import static hu.u_szeged.inf.aramis.model.Picture.picture;
import static hu.u_szeged.inf.aramis.model.ProcessResult.processResult;

@EBean
public class ImageProcessor {
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

    public ProcessResult processImages(Table<Integer, Integer, Boolean> diffCoordinates, List<BlurredPicture> pictures, List<Picture> pictureList) throws InterruptedException, ExecutionException, IOException {
        Bitmap result = evaluator.evaluate(pictures, diffCoordinates);
        Picture backgroundPicture = picture(PictureSaver.DATE_TIME_FORMATTER.print(new DateTime()) + "_background", FilterUtils.sharp(result));
        PictureSaver.save(backgroundPicture);
        multipleCounterScheduler.schedule(backgroundPicture, pictures, diffCoordinates);
        Map<Picture, Table<Integer, Integer, Boolean>> resultBitmaps = multipleCounterScheduler.getDiffCoordinates();
        Map<Picture, List<Cluster<Coordinate>>> clustersForPictures = getClustersForPictures(resultBitmaps);
        Map<Picture, List<ClusterPair>> pictureListMap = clusterComparator.countSimilarity(clustersForPictures);
        return processResult(pictureListMap, backgroundPicture);
    }

    private Map<Picture, List<Cluster<Coordinate>>> getClustersForPictures(Map<Picture, Table<Integer, Integer, Boolean>> resultBitmaps) {
        Map<Picture, List<Cluster<Coordinate>>> clusterisedPictures = Maps.newLinkedHashMap();
        for (Map.Entry<Picture, Table<Integer, Integer, Boolean>> entry : resultBitmaps.entrySet()) {
            List<Cluster<Coordinate>> clusterList = clustering.cluster(entry.getValue());
            clusterisedPictures.put(entry.getKey(), clusterList);
        }
        return clusterisedPictures;
    }
}
