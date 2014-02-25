package hu.u_szeged.inf.aramis.camera;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class MultipleCounterScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleCounterScheduler.class);
    private final CounterScheduler counterScheduler;
    private final Map<Picture, FutureTask<Set<Coordinate>>> tasks = Maps.newHashMap();

    private MultipleCounterScheduler(CounterScheduler counterScheduler) {
        this.counterScheduler = counterScheduler;
    }

    public static MultipleCounterScheduler multipleCounterScheduler(CounterScheduler counterScheduler) {
        return new MultipleCounterScheduler(counterScheduler);
    }

    public void schedule(Picture background, List<Picture> pictures, Set<Coordinate> differenceCoordinates) {
        for (Picture picture : pictures) {
            LOGGER.info("Schedule task for background and {}", picture.name);
            FutureTask<Set<Coordinate>> task = counterScheduler.schedule(background, picture, differenceCoordinates);
            tasks.put(picture, task);
        }
    }

    public Map<Picture, Set<Coordinate>> getDiffCoordinates() throws InterruptedException, ExecutionException {
        LOGGER.info("Waiting for countdown!");
        Map<Picture, Set<Coordinate>> result = Maps.newLinkedHashMap();
        counterScheduler.countDown.await();
        for (Map.Entry<Picture, FutureTask<Set<Coordinate>>> entry : tasks.entrySet()) {
            Picture picture = entry.getKey();
            FutureTask<Set<Coordinate>> task = entry.getValue();
            result.put(picture, task.get());
        }
        return result;
    }
}
