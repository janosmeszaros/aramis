package hu.u_szeged.inf.aramis;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.u_szeged.inf.aramis.Utils.ClusterUtils;
import hu.u_szeged.inf.aramis.activities.listpictures.ProgressBarHandler;
import hu.u_szeged.inf.aramis.camera.CounterScheduler;
import hu.u_szeged.inf.aramis.camera.MultipleCounterScheduler;
import hu.u_szeged.inf.aramis.camera.PictureCollector;
import hu.u_szeged.inf.aramis.camera.PictureEvaluator;
import hu.u_szeged.inf.aramis.camera.TakePictureCallback;
import hu.u_szeged.inf.aramis.camera.picture.CannyEdgeDetector;
import hu.u_szeged.inf.aramis.camera.picture.Clustering;
import hu.u_szeged.inf.aramis.camera.picture.process.ClusterComparator;
import hu.u_szeged.inf.aramis.camera.picture.process.MomentsCounter;
import hu.u_szeged.inf.aramis.camera.picture.process.MomentsDistanceCounter;
import hu.u_szeged.inf.aramis.camera.picture.process.PairMatcher;
import hu.u_szeged.inf.aramis.camera.picture.process.PreFilter;
import hu.u_szeged.inf.aramis.camera.picture.process.SimilarityDetector;
import hu.u_szeged.inf.aramis.camera.picture.process.post.ChainDetector;

import static hu.u_szeged.inf.aramis.camera.CounterScheduler.counterScheduler;
import static hu.u_szeged.inf.aramis.camera.MultipleCounterScheduler.multipleCounterScheduler;
import static hu.u_szeged.inf.aramis.camera.PictureCollector.pictureCollector;
import static hu.u_szeged.inf.aramis.camera.picture.Clustering.clustering;

public class AppModule implements Module {
    public static final double MOMENT_BORDER = 4.0;
    public static final double DISTANCE_BORDER = 250.0;
    public static final double AREA_DIFFERENCE_BORDER = 2000.0;
    public static final BigDecimal PRE_FILTER_SIMILARITY_BORDER = new BigDecimal(0.85);
    public static final BigDecimal PRE_FILTER_AREA_BORDER = new BigDecimal(100);

    @Override
    public void configure(Binder binder) {
        binder.bind(PictureEvaluator.class).in(Scopes.SINGLETON);
        binder.bind(ClusterUtils.class).in(Scopes.SINGLETON);
        binder.bind(ProgressBarHandler.class).in(Scopes.SINGLETON);
        binder.bind(CannyEdgeDetector.class).in(Scopes.SINGLETON);
        binder.bind(ChainDetector.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    private Clustering clusteringProvider() {
        return clustering(1, 2);
    }

    @Provides
    @Singleton
    private CountDownLatch countDownProvider() {
        return new CountDownLatch(TakePictureCallback.PICTURE_NUMBER - 1);
    }

    @Provides
    @Singleton
    private ExecutorService executorServiceProvider() {
        return Executors.newCachedThreadPool();
    }

    @Provides
    @Singleton
    private PictureCollector pictureCollectorProvider(CounterScheduler counterScheduler) {
        return pictureCollector(counterScheduler);
    }

    @Provides
    @Singleton
    private CounterScheduler counterSchedulerProvider(CountDownLatch countDownLatch, ExecutorService executorService) {
        return counterScheduler(countDownLatch, executorService);
    }

    @Provides
    @Singleton
    private MultipleCounterScheduler multipleCounterSchedulerProvider(CounterScheduler counterScheduler) {
        return multipleCounterScheduler(counterScheduler);
    }

    @Provides
    @Singleton
    private ClusterComparator clusterComparator(MomentsCounter momentsCounter,
                                                MomentsDistanceCounter distanceCounter,
                                                PairMatcher pairMatcher,
                                                PreFilter preFilter) {
        return new ClusterComparator(momentsCounter, distanceCounter, pairMatcher, preFilter);
    }

    @Provides
    @Singleton
    private MomentsCounter momentsCounter() {
        return new MomentsCounter();
    }

    @Provides
    @Singleton
    private MomentsDistanceCounter momentsDistanceCounter() {
        return new MomentsDistanceCounter();
    }

    @Provides
    @Singleton
    private PairMatcher pairMatcher(SimilarityDetector detector) {
        return new PairMatcher(detector);
    }

    @Provides
    @Singleton
    private SimilarityDetector similarityDetector() {
        return new SimilarityDetector(DISTANCE_BORDER, MOMENT_BORDER, AREA_DIFFERENCE_BORDER);
    }

    @Provides
    @Singleton
    private PreFilter preFilter() {
        return new PreFilter(PRE_FILTER_AREA_BORDER, PRE_FILTER_SIMILARITY_BORDER);
    }
}
