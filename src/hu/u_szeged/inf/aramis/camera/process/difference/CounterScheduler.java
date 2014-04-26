package hu.u_szeged.inf.aramis.camera.process.difference;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.camera.TakePictureCallback;
import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.DifferenceResult;

public class CounterScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterScheduler.class);
    protected final CompletionService<DifferenceResult> completionService;

    protected CounterScheduler(CompletionService<DifferenceResult> completionService) {
        this.completionService = completionService;
    }

    public static CounterScheduler counterScheduler(CompletionService<DifferenceResult> completionService) {
        return new CounterScheduler(completionService);
    }

    public void schedule(BlurredPicture one, BlurredPicture two) {
        completionService.submit(DiffCounter.diffCounter(one, two));
    }

    public Table<Integer, Integer, Boolean> getDiffCoordinates() throws InterruptedException, ExecutionException {
        Table<Integer, Integer, Boolean> diffCoordinates = HashBasedTable.create();
        for (int i = 0; i < TakePictureCallback.PICTURE_NUMBER - 1; i++) {
            Table<Integer, Integer, Boolean> coordinates = completionService.take().get().table;
            LOGGER.debug("Adding {} coordinates for task no: {}", coordinates.size(), i);
            diffCoordinates.putAll(coordinates);
        }
        return ImmutableTable.<Integer, Integer, Boolean>builder().putAll(diffCoordinates).build();
    }
}
