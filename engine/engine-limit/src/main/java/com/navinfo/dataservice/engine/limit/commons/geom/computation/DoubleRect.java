package com.navinfo.dataservice.engine.limit.commons.geom.computation;

/**
* @ClassName: DoubleRect 
* @author Xiao Xiaowen 
* @date 2016年6月2日 下午3:50:09 
* @Description: TODO
*  
*/
public class DoubleRect {
	private DoublePoint lbPoint;
	private DoublePoint rtPoint;
	public DoubleRect(DoublePoint lbPoint, DoublePoint rtPoint){
		this.lbPoint=lbPoint;
		this.rtPoint=rtPoint;
	}

	public DoublePoint getLbPoint() {
		return lbPoint;
	}

	public void setLbPoint(DoublePoint lbPoint) {
		this.lbPoint = lbPoint;
	}

	public DoublePoint getRtPoint() {
		return rtPoint;
	}

	public void setRtPoint(DoublePoint rtPoint) {
		this.rtPoint = rtPoint;
	}
	
	public String toString(){
		return "Rect("+lbPoint+","+rtPoint+")";
	}
}
