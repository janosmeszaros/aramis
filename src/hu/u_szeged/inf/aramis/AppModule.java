package hu.u_szeged.inf.aramis;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import hu.u_szeged.inf.aramis.activities.listpictures.ProgressBarHandler;
import hu.u_szeged.inf.aramis.camera.ClusterUtils;
import hu.u_szeged.inf.aramis.camera.PictureCollector;
import hu.u_szeged.inf.aramis.camera.PictureEvaluator;
import hu.u_szeged.inf.aramis.camera.picture.Clustering;

public class AppModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(PictureCollector.class).in(Scopes.SINGLETON);
        binder.bind(PictureEvaluator.class).in(Scopes.SINGLETON);
        binder.bind(ClusterUtils.class).in(Scopes.SINGLETON);
        binder.bind(ProgressBarHandler.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    private Clustering clusteringProvider() {
        return Clustering.clustering(1, 2);
    }
}
