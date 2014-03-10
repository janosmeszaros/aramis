package hu.u_szeged.inf.aramis.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.camera.utils.PictureSaver.getFilePathForPicture;
import static hu.u_szeged.inf.aramis.model.Picture.picture;

public final class MapUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapUtils.class);

    private MapUtils() {
    }

    public static <E> Map<Picture, E> sortMapWithPicture(Map<Picture, E> result) {
        ImmutableList<Picture> sortedResult = Ordering.natural().
                onResultOf(new Function<Picture, String>() {
                    @Override
                    public String apply(Picture input) {
                        return input.name;
                    }
                }).immutableSortedCopy(result.keySet());

        Map<Picture, E> sortedMap = Maps.newLinkedHashMap();
        for (Picture entry : sortedResult) {
            E pictureEdgesList = result.get(entry);
            sortedMap.put(entry, pictureEdgesList);
        }
        return sortedMap;
    }

    public static <E> Map<String, E> transformPictureMapToString(Map<Picture, E> result) {
        Map<String, E> transformedMap = Maps.newLinkedHashMap();
        for (Map.Entry<Picture, E> entry : result.entrySet()) {
            try {
                transformedMap.put(getFilePathForPicture(entry.getKey()), entry.getValue());
            } catch (IOException e) {
                LOGGER.error("Cannot find picture on the device!", e);
            }
        }
        return transformedMap;
    }

    public static <E> Map<Picture, E> transformStringMapToPicture(Map<String, E> resultBitmapPaths) {
        Map<Picture, E> transformed = Maps.newTreeMap();
        for (Map.Entry<String, E> entry : resultBitmapPaths.entrySet()) {
            LOGGER.info("Get picture from {}", entry.getKey());
            Picture picture = picture(entry.getKey(), createPicture(entry.getKey()));
            transformed.put(picture, entry.getValue());
        }
        return transformed;
    }

    private static Bitmap createPicture(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(path, options);
    }

}