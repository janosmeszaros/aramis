package hu.u_szeged.inf.aramis;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.u_szeged.inf.aramis.activities.listpictures.ProgressBarHandler;
import hu.u_szeged.inf.aramis.camera.ClusterUtils;
import hu.u_szeged.inf.aramis.camera.CounterScheduler;
import hu.u_szeged.inf.aramis.camera.MultipleCounterScheduler;
import hu.u_szeged.inf.aramis.camera.PictureCollector;
import hu.u_szeged.inf.aramis.camera.PictureEvaluator;
import hu.u_szeged.inf.aramis.camera.TakePictureCallback;
import hu.u_szeged.inf.aramis.camera.picture.Clustering;

import static hu.u_szeged.inf.aramis.camera.CounterScheduler.counterScheduler;
import static hu.u_szeged.inf.aramis.camera.MultipleCounterScheduler.multipleCounterScheduler;
import static hu.u_szeged.inf.aramis.camera.PictureCollector.pictureCollector;
import static hu.u_szeged.inf.aramis.camera.picture.Clustering.clustering;

public class AppModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(PictureEvaluator.class).in(Scopes.SINGLETON);
        binder.bind(ClusterUtils.class).in(Scopes.SINGLETON);
        binder.bind(ProgressBarHandler.class).in(Scopes.SINGLETON);
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
}
