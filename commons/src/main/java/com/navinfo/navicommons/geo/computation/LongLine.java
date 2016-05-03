package com.navinfo.navicommons.geo.computation;

/** 
* @ClassName: Line 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:52:42 
* @Description: TODO
*/
public class LongLine {
	private LongPoint[] points;//
	public LongLine(LongPoint sPoint,LongPoint ePoint){
		this.points=new LongPoint[]{sPoint,ePoint};
	}
	public LongPoint getsPoint() {
		return points[0];
	}
	public void setsPoint(LongPoint sPoint) {
		this.points[0] = sPoint;
	}
	public LongPoint getePoint() {
		return points[1];
	}
	public void setePoint(LongPoint ePoint) {
		this.points[1] = ePoint;
	}
}
