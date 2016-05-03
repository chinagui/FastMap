package com.navinfo.navicommons.geo.computation;

import java.util.ArrayList;
import java.util.List;

/** 
* @ClassName: DoublePolyline 
* @author Xiao Xiaowen 
* @date 2016年4月29日 上午11:24:17 
* @Description: TODO
*/
public class DoublePolyline {
	private DoublePoint[] points;
	public DoublePolyline(DoublePoint[] points){
		this.points=points;
	}
	
	public DoublePoint getsPoint() {
		return points[0];
	}
	public void setsPoint(DoublePoint sPoint) {
		this.points[0] = sPoint;
	}
	public DoublePoint getePoint() {
		return points[points.length-1];
	}
	public void setePoint(DoublePoint ePoint) {
		this.points[points.length-1] = ePoint;
	}
	public DoublePoint[] getPoints() {
		return points;
	}
	public void setPoints(DoublePoint[] points) {
		this.points = points;
	}
}
