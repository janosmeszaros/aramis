package hu.u_szeged.inf.aramis.camera;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class MultipleCounterScheduler {
    private final CounterScheduler counterScheduler;
    private final Map<Picture, FutureTask<Set<Coordinate>>> tasks = Maps.newHashMap();

    private MultipleCounterScheduler(CounterScheduler counterScheduler) {
        this.counterScheduler = counterScheduler;
    }

    public static MultipleCounterScheduler multipleCounterScheduler(CounterScheduler counterScheduler) {
        return new MultipleCounterScheduler(counterScheduler);
    }

    public void schedule(Picture background, List<Picture> pictures) {
        for (Picture picture : pictures) {
            counterScheduler.schedule(background, picture);
            tasks.put(picture, counterScheduler.tasks.get(counterScheduler.tasks.size() - 1));
        }
    }

    public Map<Picture, Set<Coordinate>> getDiffCoordinates() throws InterruptedException, ExecutionException {
        Map<Picture, Set<Coordinate>> result = Maps.newHashMap();
        counterScheduler.countDown.await();
        for (Map.Entry<Picture, FutureTask<Set<Coordinate>>> entry : tasks.entrySet()) {
            Picture picture = entry.getKey();
            FutureTask<Set<Coordinate>> task = entry.getValue();
            result.put(picture, task.get());
        }
        return result;
    }
}
