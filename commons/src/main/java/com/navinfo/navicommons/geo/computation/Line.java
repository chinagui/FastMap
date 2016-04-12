package com.navinfo.navicommons.geo.computation;

/** 
* @ClassName: Line 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:52:42 
* @Description: TODO
*/
public class Line {
	private Point sPoint;
	private Point ePoint;
	public Line(Point sPoint,Point ePoint){
		this.sPoint=sPoint;
		this.ePoint=ePoint;
	}
	public Point getsPoint() {
		return sPoint;
	}
	public void setsPoint(Point sPoint) {
		this.sPoint = sPoint;
	}
	public Point getePoint() {
		return ePoint;
	}
	public void setePoint(Point ePoint) {
		this.ePoint = ePoint;
	}
}
