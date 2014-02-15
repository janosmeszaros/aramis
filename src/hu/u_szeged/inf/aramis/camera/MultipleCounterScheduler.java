package hu.u_szeged.inf.aramis.camera;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.camera.picture.PictureSaver.getFilePathForPicture;

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

    public Map<String, Set<Coordinate>> getDiffCoordinates() throws InterruptedException, ExecutionException {
        LOGGER.info("Waiting for countdown!");
        Map<Picture, Set<Coordinate>> result = Maps.newLinkedHashMap();
        counterScheduler.countDown.await();
        for (Map.Entry<Picture, FutureTask<Set<Coordinate>>> entry : tasks.entrySet()) {
            Picture picture = entry.getKey();
            FutureTask<Set<Coordinate>> task = entry.getValue();
            result.put(picture, task.get());
        }
        return sortResult(result);
    }

    private Map<String, Set<Coordinate>> sortResult(Map<Picture, Set<Coordinate>> result) {
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
}
