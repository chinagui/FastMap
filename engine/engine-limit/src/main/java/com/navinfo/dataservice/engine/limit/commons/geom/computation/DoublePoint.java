package com.navinfo.dataservice.engine.limit.commons.geom.computation;


/** 
 * 平面上一个点，也是一个向量
 * x,y如果超过5位小数精度，需要四舍五入只保留5位小数，请使用DoubleUtil.keep5Decimal()
* @ClassName: Point 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:47:19 
* @Description: TODO
*/
public class DoublePoint {
	private double x;
	private double y;
	public DoublePoint(double x,double y){
//		this.x=DoubleUtil.keep5Decimal(x);
//		this.y=DoubleUtil.keep5Decimal(y);
		this.x=x;
		this.y=y;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public DoublePoint clone(){
		return new DoublePoint(this.x,this.y);
	}
	public String toString(){
		return x+" "+y;
	}
	public int hashCode(){
		return (x+" "+y).hashCode();
	}
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof DoublePoint
				&&this.getX()==((DoublePoint) anObject).getX()
				&&this.getY()==((DoublePoint) anObject).getY()){
			return true;
		}else{
			return false;
		}
	}
}
