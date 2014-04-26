package hu.u_szeged.inf.aramis.model;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableTable;

public class DifferenceResult {
    public final ImmutableTable<Integer, Integer, Boolean> table;
    public final Optional<Picture> picture;

    private DifferenceResult(ImmutableTable<Integer, Integer, Boolean> table, Optional<Picture> picture) {
        this.table = table;
        this.picture = picture;
    }

    public static DifferenceResult differenceResult(ImmutableTable<Integer, Integer, Boolean> table, Picture picture) {
        return new DifferenceResult(table, Optional.of(picture));
    }

    public static DifferenceResult differenceResult(ImmutableTable<Integer, Integer, Boolean> table) {
        return new DifferenceResult(table, Optional.<Picture>absent());
    }
}
