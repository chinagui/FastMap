package com.navinfo.dataservice.commons.kmeans;

/**
 * Created by wangshishuai3966 on 2017/7/18.
 */
public class KPoint {
    private double x;
    private double y;
    private int count;
    private int gridId;

    public KPoint(){

    }

    public KPoint(int gridId, int count){
        this.gridId = gridId;
        String gridStr = String.valueOf(gridId);
        x = Integer.valueOf(gridStr.substring(0,2)+gridStr.substring(4,5)+gridStr.substring(6,7));
        y = Integer.valueOf(gridStr.substring(2,4)+gridStr.substring(5,6)+gridStr.substring(7,8));
        this.count = count;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getGridId() {
        return gridId;
    }

    public void setGridId(int gridId) {
        this.gridId = gridId;
    }

    public static void main(String[] args) {
        KPoint p = new KPoint(59567201,100);
        System.out.println(p.getX());
        System.out.println(p.getY());
    }
}
