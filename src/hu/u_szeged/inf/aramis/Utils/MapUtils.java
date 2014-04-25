package hu.u_szeged.inf.aramis.utils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import hu.u_szeged.inf.aramis.model.Picture;

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
}