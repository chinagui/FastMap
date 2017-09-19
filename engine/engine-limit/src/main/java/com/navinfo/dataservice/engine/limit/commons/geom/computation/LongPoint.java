package com.navinfo.dataservice.engine.limit.commons.geom.computation;

/** 
* @ClassName: LongPoint 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:47:19 
* @Description: TODO
*/
public class LongPoint {
	private long x;
	private long y;
	public LongPoint(long x, long y){
		this.x=x;
		this.y=y;
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
	public LongPoint clone(){
		return new LongPoint(this.x,this.y);
	}
	public String toString(){
		return x+" "+y;
	}
	public int hashCode(){
		return (x+" "+y).hashCode();
	}
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof LongPoint
				&&this.getX()==((LongPoint) anObject).getX()
				&&this.getY()==((LongPoint) anObject).getY()){
			return true;
		}else{
			return false;
		}
	}
	
}
