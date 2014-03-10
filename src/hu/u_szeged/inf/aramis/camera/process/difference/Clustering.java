package hu.u_szeged.inf.aramis.camera.process.difference;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.util.List;
import java.util.Set;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.PointStatus;

import static hu.u_szeged.inf.aramis.model.Coordinate.coordinate;

public class Clustering {
    private final int eps;
    private final int minPts;

    private Clustering(int eps, int minPts) {
        this.eps = eps;
        this.minPts = minPts;
    }

    public static Clustering clustering(int eps, int minPts) {
        return new Clustering(eps, minPts);
    }

    public List<Cluster<Coordinate>> cluster(Table<Integer, Integer, Boolean> coordinates) {
        List<Cluster<Coordinate>> clusters = Lists.newArrayList();
        Table<Integer, Integer, PointStatus> visited = HashBasedTable.create();
        for (Table.Cell<Integer, Integer, Boolean> cell : coordinates.cellSet()) {
            if (visited.get(cell.getRowKey(), cell.getColumnKey()) != null) {
                continue;
            }
            List<Coordinate> neighborPts = getNeighbors(coordinate(cell.getRowKey(), cell.getColumnKey()), coordinates);
            if (neighborPts.size() >= minPts) {
                clusters.add(createCluster(cell, neighborPts, visited, coordinates));
            } else {
                visited.put(cell.getRowKey(), cell.getColumnKey(), PointStatus.NOISE);
            }
        }
        return clusters;
    }

    private Cluster<Coordinate> createCluster(Table.Cell<Integer, Integer, Boolean> cell, List<Coordinate> neighborPts, Table<Integer, Integer, PointStatus> visited, Table<Integer, Integer, Boolean> coordinates) {
        Cluster<Coordinate> cluster = new Cluster<Coordinate>();
        cluster.addPoint(coordinate(cell.getRowKey(), cell.getColumnKey()));
        visited.put(cell.getRowKey(), cell.getColumnKey(), PointStatus.PART_OF_CLUSTER);
        List<Coordinate> seeds = Lists.newArrayList(neighborPts);
        Set<Coordinate> seedSet = Sets.newLinkedHashSet(neighborPts);
        int index = 0;
        while (seeds.size() > index) {
            Coordinate point = seeds.get(index);
            PointStatus pointStatus = visited.get(point.x, point.y);
            if (pointStatus == null) {
                final List<Coordinate> currentNeighbors = getNeighbors(point, coordinates);
                if (currentNeighbors.size() >= minPts) {
                    merge(seeds, seedSet, currentNeighbors);
                }
            }

            if (pointStatus != PointStatus.PART_OF_CLUSTER) {
                visited.put(point.x, point.y, PointStatus.PART_OF_CLUSTER);
                cluster.addPoint(point);
            }
            index++;
        }
        return cluster;
    }

    private void merge(List<Coordinate> one, Set<Coordinate> seedSet, List<Coordinate> two) {
        for (Coordinate item : two) {
            if (!seedSet.contains(item)) {
                one.add(item);
                seedSet.add(item);
            }
        }
    }

    private List<Coordinate> getNeighbors(Coordinate point, Table<Integer, Integer, Boolean> coordinates) {
        List<Coordinate> neighbors = Lists.newArrayList();
        for (int x = -eps; x <= eps; x++) {
            for (int y = -eps; y <= eps; y++) {
                if (x == 0 && y == 0 || !coordinates.contains(point.x + x, point.y + y)) {
                    continue;
                }
                neighbors.add(coordinate(point.x + x, point.y + y));
            }
        }
        return neighbors;
    }
}
