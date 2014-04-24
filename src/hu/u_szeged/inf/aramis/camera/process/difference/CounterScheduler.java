package hu.u_szeged.inf.aramis.camera.process.difference;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import hu.u_szeged.inf.aramis.model.BlurredPicture;

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

    public void schedule(BlurredPicture one, BlurredPicture two) {
        FutureTask<Table<Integer, Integer, Boolean>> task = new FutureTask<Table<Integer, Integer, Boolean>>(new DiffCounter(countDown, one, two, HashBasedTable.<Integer, Integer, Boolean>create()));
        startTask(task);
    }

    public FutureTask<Table<Integer, Integer, Boolean>> schedule(BlurredPicture one, BlurredPicture two, Table<Integer, Integer, Boolean> differenceCoordinates) {
        FutureTask<Table<Integer, Integer, Boolean>> task = new FutureTask<Table<Integer, Integer, Boolean>>(new DiffCounter(countDown, one, two, differenceCoordinates));
        startTask(task);
        return task;
    }

    private void startTask(FutureTask<Table<Integer, Integer, Boolean>> task) {
        tasks.add(task);
        executorService.execute(task);
    }

    public Table<Integer, Integer, Boolean> getDiffCoordinates() throws InterruptedException, ExecutionException {
        LOGGER.info("Waiting for countdown!");
        countDown.await();
        LOGGER.info("Countdown finished!");
        Table<Integer, Integer, Boolean> diffCoordinates = HashBasedTable.create();
        for (FutureTask<Table<Integer, Integer, Boolean>> task : tasks) {
            Table<Integer, Integer, Boolean> coordinates = task.get();
            LOGGER.debug("Adding {} coordinates for task no: {}", coordinates.size(), tasks.indexOf(task));
            diffCoordinates.putAll(coordinates);
        }
        return diffCoordinates;
    }

    public void clear() {
        tasks.clear();
    }
}
