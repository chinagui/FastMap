package com.navinfo.navicommons.geo.computation;

/** 
* @ClassName: Line 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:52:42 
* @Description: TODO
*/
public class DoubleLine {
	private DoublePoint[] points;
	public DoubleLine(DoublePoint sPoint,DoublePoint ePoint){
		this.points=new DoublePoint[]{sPoint,ePoint};
	}
	public DoublePoint getsPoint() {
		return points[0];
	}
	public void setsPoint(DoublePoint sPoint) {
		this.points[0] = sPoint;
	}
	public DoublePoint getePoint() {
		return points[1];
	}
	public void setePoint(DoublePoint ePoint) {
		this.points[1] = ePoint;
	}
}
