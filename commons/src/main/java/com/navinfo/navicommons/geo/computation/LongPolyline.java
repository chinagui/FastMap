package com.navinfo.navicommons.geo.computation;

import java.util.ArrayList;
import java.util.List;

/** 
* @ClassName: LongPolyline 
* @author Xiao Xiaowen 
* @date 2016年4月29日 上午11:24:17 
* @Description: TODO
*/
public class LongPolyline {
	private LongPoint[] points;
	public LongPolyline(LongPoint[] points){
		this.points=points;
	}
	
	public LongPoint getsPoint() {
		return points[0];
	}
	public void setsPoint(LongPoint sPoint) {
		this.points[0] = sPoint;
	}
	public LongPoint getePoint() {
		return points[points.length-1];
	}
	public void setePoint(LongPoint ePoint) {
		this.points[points.length-1] = ePoint;
	}
	public LongPoint[] getPoints() {
		return points;
	}
	public void setPoints(LongPoint[] points) {
		this.points = points;
	}
}
