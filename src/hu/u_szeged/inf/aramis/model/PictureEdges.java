package hu.u_szeged.inf.aramis.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class PictureEdges {
    public final ImmutableList<Coordinate> edgeCoordinates;

    private PictureEdges(ImmutableList<Coordinate> edgeCoordinates) {
        this.edgeCoordinates = edgeCoordinates;
    }

    public static PictureEdges pictureEdges(List<Coordinate> edgeCoordinates) {
        return new PictureEdges(ImmutableList.copyOf(edgeCoordinates));
    }
}

