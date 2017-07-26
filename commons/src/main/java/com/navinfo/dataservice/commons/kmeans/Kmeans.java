/*
 * This file is part of ekmeans.
 *
 * ekmeans is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * ekmeans is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Foobar. If not, see <http://www.gnu.org/licenses/>.
 * 
 * ekmeans  Copyright (C) 2012  Pierre-David Belanger <pierredavidbelanger@gmail.com>
 * 
 * Contributor(s): Pierre-David Belanger <pierredavidbelanger@gmail.com>
 */
package com.navinfo.dataservice.commons.kmeans;

import java.util.Arrays;

public class Kmeans {

    public interface Listener {

        void iteration(int iteration, int move);

    }

    public interface PointDistanceFunction {

        double distance(KPoint p1, KPoint p2);

    }

    public static final PointDistanceFunction EUCLIDEAN_DISTANCE_FUNCTION = new PointDistanceFunction() {

        public double distance(KPoint p1, KPoint p2) {
            double s = 0;
            s += Math.pow(Math.abs(p1.getX() - p2.getX()), 2);
            s += Math.pow(Math.abs(p1.getY() - p2.getY()), 2);
            return Math.sqrt(s);
        }
    };

    public static class DistanceFunction{

        private final PointDistanceFunction doubleDistanceFunction;

        public DistanceFunction(PointDistanceFunction doubleDistanceFunction) {
            this.doubleDistanceFunction = doubleDistanceFunction;
        }

        public void distance(boolean[] changed, double[][] distances, KPoint[] centroids, KPoint[] points) {
            for (int c = 0; c < centroids.length; c++) {
                if (!changed[c]) continue;
                KPoint centroid = centroids[c];
                for (int p = 0; p < points.length; p++) {
                    KPoint point = points[p];
                    distances[c][p] = doubleDistanceFunction.distance(centroid, point);
                }
            }
        }
    }

    public static class CenterFunction {

        public void center(boolean[] changed, int[] assignments, KPoint[] centroids, KPoint[] points) {
            for (int c = 0; c < centroids.length; c++) {
                if (!changed[c]) continue;
                KPoint centroid = centroids[c];
                int n = 0;
                for (int p = 0; p < points.length; p++) {
                    if (assignments[p] != c) continue;
                    KPoint point = points[p];
                    if (n++ == 0)
                    {
                        centroid.setX(0);
                        centroid.setY(0);
                    }
                    centroid.setX(centroid.getX()+point.getX());
                    centroid.setY(centroid.getY()+point.getY());
                }
                if (n > 0) {
                    centroid.setX(centroid.getX()/n);
                    centroid.setY(centroid.getY()/n);
                }
            }
        }
    }

    protected final KPoint[] centroids;
    protected final KPoint[] points;
    protected final boolean equal;
    protected final DistanceFunction distanceFunction;
    protected final CenterFunction centerFunction;
    protected final Listener listener;

    protected final int idealSum;
    protected final double[][] distances;
    protected final int[] assignments;
    protected final boolean[] changed;
    protected final int[] sums;
    protected final boolean[] done;

    private static final int MIN = 0;
    private static final int MAX = 1;
    private static final int LEN = 2;

    private static final int X = 0;
    private static final int Y = 1;

    public Kmeans(KPoint[] points, int k, boolean equal, Listener listener) {
        this(points, k, equal, new DistanceFunction(EUCLIDEAN_DISTANCE_FUNCTION), new CenterFunction(), listener);
    }
    public Kmeans(KPoint[] points, int k, boolean equal) {
        this(points, k, equal, null);
    }
    public Kmeans(KPoint[] points,int k, boolean equal, DistanceFunction distanceFunction, CenterFunction centerFunction, Listener listener) {
        this.points = points;
        this.distanceFunction = distanceFunction;
        this.centerFunction = centerFunction;
        this.centroids = new KPoint[k];
        double[][] minmaxlens = new double[][]{
                {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
                {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY},
                {0d, 0d}
        };
        int sum=0;
        for(KPoint point : points){
            if (point.getX() < minmaxlens[MIN][X]) {
                minmaxlens[MIN][X] = point.getX();
            }
            if (point.getY() < minmaxlens[MIN][Y]) {
                minmaxlens[MIN][Y] = point.getY();
            }
            if (point.getX() > minmaxlens[MAX][X]) {
                minmaxlens[MAX][X] = point.getX();
            }
            if (point.getY() > minmaxlens[MAX][Y]) {
                minmaxlens[MAX][Y] = point.getY();
            }

            sum += point.getCount();
        }
        minmaxlens[LEN][X] = minmaxlens[MAX][X] - minmaxlens[MIN][X];
        minmaxlens[LEN][Y] = minmaxlens[MAX][Y] - minmaxlens[MIN][Y];
        for (int i = 0; i < k; i++) {
            centroids[i] = new KPoint();
            centroids[i].setX(minmaxlens[MIN][X] + (minmaxlens[LEN][X] / 2d));
            centroids[i].setY(minmaxlens[MIN][Y] + (minmaxlens[LEN][Y] / 2d));
        }
        idealSum = sum / k;

        distances = new double[centroids.length][points.length];
        assignments = new int[points.length];
        Arrays.fill(assignments, -1);
        changed = new boolean[centroids.length];
        Arrays.fill(changed, true);
        sums = new int[centroids.length];
        done = new boolean[centroids.length];
        this.equal = equal;
        this.listener = listener;
    }

    public int[] run() {
        return run(128);
    }

    public int[] run(int iteration) {
        calculateDistances();
        int move = makeAssignments();
        int i = 0;
        while (move > 0 && i++ < iteration) {
            if (points.length >= centroids.length) {
                move = fillEmptyCentroids();
            }
            moveCentroids();
            calculateDistances();
            move += makeAssignments();
            if (listener != null) {
                listener.iteration(i, move);
            }
        }
        return assignments;
    }

    protected void calculateDistances() {
        distanceFunction.distance(changed, distances, centroids, points);
        Arrays.fill(changed, false);
    }

    protected int makeAssignments() {
        int move = 0;
        Arrays.fill(sums, 0);
        for (int p = 0; p < points.length; p++) {
            int nc = nearestCentroid(p);
            if (nc == -1) {
                continue;
            }
            if (assignments[p] != nc) {
                if (assignments[p] != -1) {
                    changed[assignments[p]] = true;
                }
                changed[nc] = true;
                assignments[p] = nc;
                move++;
            }
            KPoint point = points[p];
            sums[nc]+=point.getCount();
            if (equal && sums[nc] > idealSum) {
                move += remakeAssignments(nc);
            }
        }
        return move;
    }

    protected int remakeAssignments(int cc) {
        int move = 0;
        double md = Double.POSITIVE_INFINITY;
        int nc = -1;
        int np = -1;
        for (int p = 0; p < points.length; p++) {
            if (assignments[p] != cc) {
                continue;
            }
            for (int c = 0; c < centroids.length; c++) {
                if (c == cc || done[c]) {
                    continue;
                }
                double d = distances[c][p];
                if (d < md) {
                    md = d;
                    nc = c;
                    np = p;
                }
            }
        }
        if (nc != -1 && np != -1) {
            if (assignments[np] != nc) {
                if (assignments[np] != -1) {
                    changed[assignments[np]] = true;
                }
                changed[nc] = true;
                assignments[np] = nc;
                move++;
            }
            KPoint point = points[np];
            sums[cc]-=point.getCount();
            sums[nc]+=point.getCount();
            if (sums[nc] > idealSum) {
                done[cc] = true;
                move += remakeAssignments(nc);
                done[cc] = false;
            }
        }
        return move;
    }

    protected int nearestCentroid(int p) {
        double md = Double.POSITIVE_INFINITY;
        int nc = -1;
        for (int c = 0; c < centroids.length; c++) {
            double d = distances[c][p];
            if (d < md) {
                md = d;
                nc = c;
            }
        }
        return nc;
    }

    protected int nearestPoint(int inc, int fromc) {
        double md = Double.POSITIVE_INFINITY;
        int np = -1;
        for (int p = 0; p < points.length; p++) {
            if (assignments[p] != inc) {
                continue;
            }
            double d = distances[fromc][p];
            if (d < md) {
                md = d;
                np = p;
            }
        }
        return np;
    }

    protected int largestCentroid(int except) {
        int lc = -1;
        int mc = 0;
        for (int c = 0; c < centroids.length; c++) {
            if (c == except) {
                continue;
            }
            if (sums[c] > mc) {
                lc = c;
            }
        }
        return lc;
    }

    protected int fillEmptyCentroids() {
        int move = 0;
        for (int c = 0; c < centroids.length; c++) {
            if (sums[c] == 0) {
                int lc = largestCentroid(c);
                int np = nearestPoint(lc, c);
                KPoint point = points[np];
                assignments[np] = c;
                sums[c]+=point.getCount();
                sums[lc]-=point.getCount();
                changed[c] = true;
                changed[lc] = true;
                move++;
            }
        }
        return move;
    }

    protected void moveCentroids() {
        centerFunction.center(changed, assignments, centroids, points);
    }

    public KPoint[] getCentroids(){
        return centroids;
    }

    public int[] getAssignments() {
        return assignments;
    }

    public int[] getCounts() {
        return sums;
    }
}
