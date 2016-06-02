package com.navinfo.navicommons.geo.computation;

/** 
* @ClassName: Point 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:47:19 
* @Description: TODO
*/
public class LongPoint {
	private long x;
	private long y;
	public LongPoint(long x,long y){
		this.x=x;
		this.y=y;
	}
	public LongPoint(double x,double y){
		this.x=Math.round(x*100000);
		this.y=Math.round(y*100000);
	}
	public long getX() {
		return x;
	}
	public void setX(long x) {
		this.x = x;
	}
	public long getY() {
		return y;
	}
	public void setY(long y) {
		this.y = y;
	}
	
}
