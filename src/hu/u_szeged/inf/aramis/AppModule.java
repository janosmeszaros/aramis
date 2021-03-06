package hu.u_szeged.inf.aramis;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.u_szeged.inf.aramis.camera.process.BackgroundEvaluator;
import hu.u_szeged.inf.aramis.camera.process.ImageProcessor;
import hu.u_szeged.inf.aramis.camera.process.PictureCollector;
import hu.u_szeged.inf.aramis.camera.process.difference.Clustering;
import hu.u_szeged.inf.aramis.camera.process.difference.CounterScheduler;
import hu.u_szeged.inf.aramis.camera.process.difference.MultipleCounterScheduler;
import hu.u_szeged.inf.aramis.camera.process.display.ChainDetector;
import hu.u_szeged.inf.aramis.camera.process.motion.ClusterComparator;
import hu.u_szeged.inf.aramis.camera.process.motion.MomentsCounter;
import hu.u_szeged.inf.aramis.camera.process.motion.MomentsDistanceCounter;
import hu.u_szeged.inf.aramis.camera.process.motion.PairMatcher;
import hu.u_szeged.inf.aramis.camera.process.motion.PreFilter;
import hu.u_szeged.inf.aramis.camera.process.motion.SimilarityDetector;
import hu.u_szeged.inf.aramis.model.DifferenceResult;
import hu.u_szeged.inf.aramis.utils.ClusterUtils;

import static com.google.inject.Scopes.SINGLETON;
import static hu.u_szeged.inf.aramis.camera.process.PictureCollector.pictureCollector;
import static hu.u_szeged.inf.aramis.camera.process.difference.Clustering.clustering;
import static hu.u_szeged.inf.aramis.camera.process.difference.CounterScheduler.counterScheduler;
import static hu.u_szeged.inf.aramis.camera.process.difference.MultipleCounterScheduler.multipleCounterScheduler;

public class AppModule implements Module {
    public static final double MOMENT_BORDER = 5.0;
    public static final double DISTANCE_BORDER = 250.0;
    public static final double AREA_DIFFERENCE_BORDER = 3000.0;
    public static final BigDecimal PRE_FILTER_SIMILARITY_BORDER = new BigDecimal(0.90);
    public static final BigDecimal PRE_FILTER_AREA_BORDER = new BigDecimal(3000);

    @Override
    public void configure(Binder binder) {
        binder.bind(BackgroundEvaluator.class).in(SINGLETON);
        binder.bind(ClusterUtils.class).in(SINGLETON);
        binder.bind(ChainDetector.class).in(SINGLETON);
        binder.bind(ImageProcessor.class).in(SINGLETON);
    }

    @Provides
    @Singleton
    private Clustering clusteringProvider() {
        return clustering(1, 2);
    }

    @Provides
    @Singleton
    private ExecutorService executorServiceProvider() {
        return Executors.newCachedThreadPool();
    }

    @Provides
    private CompletionService completionServiceProvider(ExecutorService executorService) {
        return new ExecutorCompletionService<DifferenceResult>(executorService);
    }

    @Provides
    @Singleton
    private PictureCollector pictureCollectorProvider(CounterScheduler counterScheduler) {
        return pictureCollector(counterScheduler);
    }

    @Provides
    @Singleton
    private CounterScheduler counterSchedulerProvider(CompletionService completionService) {
        return counterScheduler(completionService);
    }

    @Provides
    @Singleton
    private MultipleCounterScheduler multipleCounterSchedulerProvider(CompletionService completionService) {
        return multipleCounterScheduler(completionService);
    }

    @Provides
    @Singleton
    private ClusterComparator clusterComparator(MomentsCounter momentsCounter,
                                                PairMatcher pairMatcher,
                                                PreFilter preFilter) {
        return new ClusterComparator(momentsCounter, pairMatcher, preFilter);
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
    private PairMatcher pairMatcher(SimilarityDetector detector,
                                    MomentsDistanceCounter momentsDistanceCounter) {
        return new PairMatcher(detector, momentsDistanceCounter);
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
