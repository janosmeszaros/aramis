package hu.u_szeged.inf.aramis.camera.process.difference;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class MultipleCounterScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleCounterScheduler.class);
    private final CounterScheduler counterScheduler;
    private final Map<BlurredPicture, FutureTask<Set<Coordinate>>> tasks = Maps.newHashMap();

    private MultipleCounterScheduler(CounterScheduler counterScheduler) {
        this.counterScheduler = counterScheduler;
    }

    public static MultipleCounterScheduler multipleCounterScheduler(CounterScheduler counterScheduler) {
        return new MultipleCounterScheduler(counterScheduler);
    }

    public void schedule(BlurredPicture background, List<BlurredPicture> pictures, Set<Coordinate> differenceCoordinates) {
        for (BlurredPicture picture : pictures) {
            LOGGER.info("Schedule task for background and {}", picture.picture.name);
            FutureTask<Set<Coordinate>> task = counterScheduler.schedule(background, picture, differenceCoordinates);
            tasks.put(picture, task);
        }
    }

    public Map<Picture, Set<Coordinate>> getDiffCoordinates() throws InterruptedException, ExecutionException {
        LOGGER.info("Waiting for countdown!");
        Map<Picture, Set<Coordinate>> result = Maps.newLinkedHashMap();
        counterScheduler.countDown.await();
        for (Map.Entry<BlurredPicture, FutureTask<Set<Coordinate>>> entry : tasks.entrySet()) {
            Picture picture = entry.getKey().picture;
            FutureTask<Set<Coordinate>> task = entry.getValue();
            result.put(picture, task.get());
        }
        tasks.clear();
        counterScheduler.clear();
        return result;
    }
}
