package com.navinfo.dataservice.engine.limit.commons.geom.computation;

/** 
* @ClassName: LongRectUtil 
* @author Xiao Xiaowen 
* @date 2016年6月2日 下午4:30:11 
* @Description: TODO
*/
public class LongRectUtil {
	
	/**
	 * 算法：p的x,y都>=min&&<=max
	 * @return 点在内部，和 在线框上返回true，外部返回false
	 */
	public static boolean contained(LongRect r,LongPoint p){
		if((p.getX()>=r.getMinX()&&p.getX()<=r.getMaxX())
				&&(p.getY()>=r.getMinY()&&p.getY()<=r.getMaxY())) return true;
		return false;
	}
	/**
	 * 如果线段有重合，算包含
	 * 算法：r2的x,y都>=min&&<=max
	 * @param r1:包含对象
	 * @param r2:被包含对象
	 * @return
	 */
	public static boolean contained(LongRect r1,LongRect r2){
		if(contained(r1,r2.getLbPoint())
				&&contained(r1,r2.getRtPoint())) return true;
				return false;
	}
	/**
	 * 只边框重合不算相交
	 * 算法：采用中心点长度算法
	 * @param r1
	 * @param r2
	 * @return
	 */
	public static boolean intersectant(LongRect r1,LongRect r2){
		return(Math.abs((r1.getMinX()+r1.getMaxX())-(r2.getMinX()+r2.getMaxX()))<(r1.getMaxX()-r1.getMinX()+r2.getMaxX()-r2.getMinX())
				&&
				Math.abs((r1.getMinY()+r1.getMaxY())-(r2.getMinY()+r2.getMaxY()))<(r1.getMaxY()-r1.getMinY()+r2.getMaxY()-r2.getMinY()));
	}
}
