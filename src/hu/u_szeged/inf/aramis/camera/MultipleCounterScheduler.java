package hu.u_szeged.inf.aramis.camera;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

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

    public void schedule(Picture background, List<Picture> pictures) {
        for (Picture picture : pictures) {
            LOGGER.info("Schedule task for background and {}", picture.name);
            counterScheduler.schedule(background, picture);
            tasks.put(picture, counterScheduler.tasks.get(counterScheduler.tasks.size() - 1));
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
        ImmutableList<Map.Entry<Picture, Set<Coordinate>>> sortedResult = Ordering.natural().
                onResultOf(new Function<Map.Entry<Picture, Set<Coordinate>>, String>() {
                    @Override
                    public String apply(Map.Entry<Picture, Set<Coordinate>> input) {
                        return input.getKey().name;
                    }
                }).immutableSortedCopy(result.entrySet());

        Map<String, Set<Coordinate>> sortedMap = Maps.newLinkedHashMap();
        for (Map.Entry<Picture, Set<Coordinate>> entry : sortedResult) {
            try {
                sortedMap.put(getFilePathForPicture(entry.getKey()), entry.getValue());
            } catch (IOException e) {
                LOGGER.error("Cannot find picture on the device!", e);
            }
        }
        return sortedMap;
    }
}
