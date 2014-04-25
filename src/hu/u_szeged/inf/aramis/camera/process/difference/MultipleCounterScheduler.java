package hu.u_szeged.inf.aramis.camera.process.difference;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.Picture;

public class MultipleCounterScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleCounterScheduler.class);
    private final CounterScheduler counterScheduler;
    private final Map<BlurredPicture, FutureTask<Table<Integer, Integer, Boolean>>> tasks = Maps.newHashMap();

    private MultipleCounterScheduler(CounterScheduler counterScheduler) {
        this.counterScheduler = counterScheduler;
    }

    public static MultipleCounterScheduler multipleCounterScheduler(CounterScheduler counterScheduler) {
        return new MultipleCounterScheduler(counterScheduler);
    }

    public void schedule(Picture background, List<BlurredPicture> pictures, Table<Integer, Integer, Boolean> differenceCoordinates) {
        for (BlurredPicture picture : pictures) {
            LOGGER.info("Schedule task for background and {}", picture.picture.name);
            FutureTask<Table<Integer, Integer, Boolean>> task = counterScheduler.schedule(background, picture, differenceCoordinates);
            tasks.put(picture, task);
        }
    }

    public Map<Picture, Table<Integer, Integer, Boolean>> getDiffCoordinates() throws InterruptedException, ExecutionException {
        LOGGER.info("Waiting for countdown!");
        Map<Picture, Table<Integer, Integer, Boolean>> result = Maps.newLinkedHashMap();
        counterScheduler.countDown.await();
        for (Map.Entry<BlurredPicture, FutureTask<Table<Integer, Integer, Boolean>>> entry : tasks.entrySet()) {
            Picture picture = entry.getKey().picture;
            FutureTask<Table<Integer, Integer, Boolean>> task = entry.getValue();
            result.put(picture, task.get());
        }
        tasks.clear();
        counterScheduler.clear();
        return result;
    }
}
