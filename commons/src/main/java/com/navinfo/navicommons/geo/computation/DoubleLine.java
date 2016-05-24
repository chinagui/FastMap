package com.navinfo.navicommons.geo.computation;

import com.navinfo.dataservice.commons.util.DoubleUtil;

/** 
* @ClassName: Line 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:52:42 
* @Description: TODO
*/
public class DoubleLine {
	private DoublePoint spoint;
	private DoublePoint epoint;
	public DoubleLine(DoublePoint spoint,DoublePoint epoint){
		this.spoint=spoint;
		this.epoint=epoint;
	}
	public DoublePoint getSpoint() {
		return spoint;
	}
	public void setSpoint(DoublePoint spoint) {
		this.spoint = spoint;
	}
	public DoublePoint getEpoint() {
		return epoint;
	}
	public void setEpoint(DoublePoint epoint) {
		this.epoint = epoint;
	}
	/**
	 * 计算线段的斜率，如果垂直X轴，那么返回DoubleUtil.INFINITY；
	 * @return 
	 */
	public double getSlope(){
		if(epoint.getX()==spoint.getX()){
			return DoubleUtil.INFINITY;
		}else{
			return (epoint.getY()-spoint.getY())/(epoint.getX()-spoint.getX());
		}
	}
	public void reverse(){
		DoublePoint temp = epoint;
		epoint=spoint;
		spoint=temp;
	}
	public double getDeltaX(){
		return epoint.getX()-spoint.getX();
	}
	public double getDeltaY(){
		return epoint.getY()-spoint.getY();
	}
	/**
	 * 将line平移到原点生成一个向量
	 * @return
	 */
	public DoublePoint pan2OriginPoint(){
		return new DoublePoint(getDeltaX(),getDeltaY());
	}
	
	public String toString(){
		return "("+spoint+","+epoint+")";
	}
}
