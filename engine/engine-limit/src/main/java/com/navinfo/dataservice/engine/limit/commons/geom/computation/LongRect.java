package com.navinfo.dataservice.engine.limit.commons.geom.computation;


/** 
* @ClassName: LongRect 
* @author Xiao Xiaowen 
* @date 2016年6月2日 下午3:50:09 
* @Description: TODO
*  
*/
public class LongRect {
	private LongPoint lbPoint;
	private LongPoint rtPoint;
	public LongRect(LongPoint lbPoint, LongPoint rtPoint){
		this.lbPoint=lbPoint;
		this.rtPoint=rtPoint;
	}

	public LongPoint getLbPoint() {
		return lbPoint;
	}

	public void setLbPoint(LongPoint lbPoint) {
		this.lbPoint = lbPoint;
	}

	public LongPoint getRtPoint() {
		return rtPoint;
	}

	public void setRtPoint(LongPoint rtPoint) {
		this.rtPoint = rtPoint;
	}
	
	public long getMinX(){
		return lbPoint.getX();
	}
	public long getMaxX(){
		return rtPoint.getX();
	}
	public long getMinY(){
		return lbPoint.getY();
	}
	public long getMaxY(){
		return rtPoint.getY();
	}
	
	public String toString(){
		return "Rect("+lbPoint+","+rtPoint+")";
	}
}
