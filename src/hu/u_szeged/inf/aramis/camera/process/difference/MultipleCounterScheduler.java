package hu.u_szeged.inf.aramis.camera.process.difference;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.camera.TakePictureCallback;
import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.DifferenceResult;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.camera.process.difference.BackgroundDiffCounter.backgroundDiffCounter;

public class MultipleCounterScheduler extends CounterScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleCounterScheduler.class);

    private MultipleCounterScheduler(CompletionService<DifferenceResult> completionService) {
        super(completionService);
    }

    public static MultipleCounterScheduler multipleCounterScheduler(CompletionService<DifferenceResult> completionService) {
        return new MultipleCounterScheduler(completionService);
    }

    public void schedule(BlurredPicture background, List<BlurredPicture> pictures, Table<Integer, Integer, Boolean> differenceCoordinates) {
        for (BlurredPicture picture : pictures) {
            LOGGER.info("Schedule task for background and {}", picture.picture.name);
            DiffCounter diffCounter = backgroundDiffCounter(background, picture, differenceCoordinates);
            completionService.submit(diffCounter);
        }
    }

    public Map<Picture, Table<Integer, Integer, Boolean>> getDifferenceCoordinates() throws InterruptedException, ExecutionException {
        LOGGER.info("Waiting for countdown!");
        Map<Picture, Table<Integer, Integer, Boolean>> result = Maps.newLinkedHashMap();
        for (int i = 0; i < TakePictureCallback.PICTURE_NUMBER; i++) {
            DifferenceResult diffResult = completionService.take().get();
            result.put(diffResult.picture.get(), diffResult.table);
        }
        return result;
    }
}
