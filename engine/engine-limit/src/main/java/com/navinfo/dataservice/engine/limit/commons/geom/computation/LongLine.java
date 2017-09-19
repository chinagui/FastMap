package com.navinfo.dataservice.engine.limit.commons.geom.computation;

import com.navinfo.dataservice.engine.limit.commons.util.DoubleUtil;

/** 
* @ClassName: Line 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:52:42 
* @Description: TODO
*/
public class LongLine {
	private LongPoint spoint;
	private LongPoint epoint;
	public LongLine(LongPoint spoint, LongPoint epoint){
		this.spoint=spoint;
		this.epoint=epoint;
	}
	public LongPoint getSpoint() {
		return spoint;
	}
	public void setSpoint(LongPoint spoint) {
		this.spoint = spoint;
	}
	public LongPoint getEpoint() {
		return epoint;
	}
	public void setEpoint(LongPoint epoint) {
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
		LongPoint temp = epoint;
		epoint=spoint;
		spoint=temp;
	}
	public long getDeltaX(){
		return epoint.getX()-spoint.getX();
	}
	public long getDeltaY(){
		return epoint.getY()-spoint.getY();
	}
	/**
	 * 将line平移到原点生成一个向量
	 * @return
	 */
	public LongPoint pan2OriginPoint(){
		return new LongPoint(getDeltaX(),getDeltaY());
	}
	

	public long getMinX(){
		return spoint.getX()<epoint.getX()?spoint.getX():epoint.getX();
	}
	public long getMaxX(){
		return spoint.getX()<epoint.getX()?epoint.getX():spoint.getX();
	}
	public long getMinY(){
		return spoint.getY()<epoint.getY()?spoint.getY():epoint.getY();
	}
	public long getMaxY(){
		return spoint.getY()<epoint.getY()?epoint.getY():spoint.getY();
	}
	
	public String toString(){
		return "("+spoint+","+epoint+")";
	}
}
