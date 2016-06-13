package com.navinfo.navicommons.geo.computation;

/** 
* @ClassName: LongPointUtil 
* @author Xiao Xiaowen 
* @date 2016年5月10日 下午9:23:43 
* @Description: TODO
*/
public class LongPointUtil {
	
	public static LongPoint plus(LongPoint point1,LongPoint point2){
		return new LongPoint((point1.getX()+point2.getX()),(point1.getY()+point2.getY()));
	}
	public static LongPoint minus(LongPoint point1,LongPoint point2){
		return new LongPoint((point1.getX()-point2.getX()),(point1.getY()-point2.getY()));
	}
	/**
	 * 计算坐标（0,0）到point的向量的模（长度）
	 * @param point
	 * @return
	 */
	public static double norm(LongPoint point){
		return Math.sqrt(Math.pow(point.getX(), 2)+Math.pow(point.getY(), 2));
	}
	/**
	 * 计算坐标（0,0）到两点坐标的向量之间夹角cos值
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static double cosAngle(LongPoint point1,LongPoint point2){
		return (point1.getX()*point2.getX()+point1.getY()*point2.getY())/(norm(point1)*norm(point2));
	}
	/**
	 * 计算坐标（0,0）到两点坐标的向量之间夹角cos值
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static double angle(LongPoint point1,LongPoint point2){
		return Math.acos(cosAngle(point1,point2));
	}
	/**
	 *  计算坐标（0,0）到两点坐标的向量的叉积
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static double cross(LongPoint point1,LongPoint point2){
		return (point1.getX()*point2.getY())-(point1.getY()*point2.getX());
	}
	
}
