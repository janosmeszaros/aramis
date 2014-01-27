package hu.u_szeged.inf.aramis.camera;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import hu.u_szeged.inf.aramis.camera.picture.DiffCounter;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class CounterScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterScheduler.class);
    public final CountDownLatch countDown;
    public final ExecutorService executorService;
    public final List<FutureTask> tasks = Lists.newArrayList();

    private CounterScheduler(CountDownLatch countDown, ExecutorService executorService) {
        this.countDown = countDown;
        this.executorService = executorService;
    }

    public static CounterScheduler counterScheduler(CountDownLatch countDown, ExecutorService executorService) {
        return new CounterScheduler(countDown, executorService);
    }

    public void schedule(Picture one, Picture two) {
        FutureTask<Set<Coordinate>> task = new FutureTask<Set<Coordinate>>(new DiffCounter(countDown, one, two));
        tasks.add(task);
        executorService.execute(task);
    }

    public Set<Coordinate> getDiffCoordinates() throws InterruptedException, ExecutionException {
        LOGGER.info("Waiting for countdown!");
        countDown.await();
        LOGGER.info("Countdown finished!");
        Set<Coordinate> diffCoordinates = Sets.newHashSet();
        for (FutureTask<Set<Coordinate>> task : tasks) {
            Set<Coordinate> coordinates = task.get();
            LOGGER.debug("Adding {} coordinates for task no: {}", coordinates.size(), tasks.indexOf(task));
            diffCoordinates.addAll(coordinates);
        }
        return diffCoordinates;
    }

    public void clear() {
        tasks.clear();
    }
}
