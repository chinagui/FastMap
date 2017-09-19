package com.navinfo.dataservice.engine.limit.commons.geom.computation;

import java.util.Arrays;
import java.util.List;

/** 
* @ClassName: LongLineUtil 
* @author Xiao Xiaowen 
* @date 2016年6月2日 下午4:25:10 
* @Description: TODO
*/
public class LongLineUtil {
	
	/**
	 * 判断p和l的两个端点是否共线，即是否在线上或者延长线上
	 * 算法：( Q - P1 ) × ( P2 - P1 ) = 0 
	 * @param l
	 * @param p
	 * @return
	 */
	public static boolean collinear(LongLine l,LongPoint p){
		long r = (p.getX()-l.getSpoint().getX())*(l.getEpoint().getY()-l.getSpoint().getY())
				-(l.getEpoint().getX()-l.getSpoint().getX())*(p.getY()-l.getSpoint().getY());
		return r==0;
	}
	/**
	 * 点在另一条线上，算相交
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static boolean intersectant(LongLine l1,LongLine l2){
		//先判断矩形是否相交
		if(!LongRectUtil.intersectant(MyGeoConvertor.line2Rect(l1),MyGeoConvertor.line2Rect(l2))) 
			return false;
		//再判断叉积
		//如果两线段相交，则两线段必然"相互"跨立对方。
		//若P1P2跨立Q1Q2 ，则矢量 ( P1 - Q1 ) 和( P2 - Q1 )位于矢量( Q2 - Q1 ) 的两侧，即( P1 - Q1 ) × ( Q2 - Q1 ) * ( P2 - Q1 ) × ( Q2 - Q1 ) < 0。
		//上式可改写成( P1 - Q1 ) × ( Q2 - Q1 ) * ( Q2 - Q1 ) × ( P2 - Q1 ) > 0。当 ( P1 - Q1 ) × ( Q2 - Q1 ) = 0 时，说明 ( P1 - Q1 ) 和 ( Q2 - Q1 )共线，但是因为已经通过快速排斥试验，所以 P1 一定在线段 Q1Q2上；
		//同理，( Q2 - Q1 ) ×(P2 - Q1 ) = 0 说明 P2 一定在线段 Q1Q2上。
		//所以判断P1P2跨立Q1Q2的依据是：( P1 - Q1 ) × ( Q2 - Q1 ) * ( Q2 - Q1 ) × ( P2 - Q1 ) >= 0。
		//同理判断Q1Q2跨立P1P2的依据是：( Q1 - P1 ) × ( P2 - P1 ) * ( P2 - P1 ) × ( Q2 - P1 ) >= 0。
		//( P1 - Q1 ) × ( Q2 - Q1 )
		long r1 = ((l1.getSpoint().getX()-l2.getSpoint().getX())*(l2.getEpoint().getY()-l2.getSpoint().getY())
				-(l2.getEpoint().getX()-l2.getSpoint().getX())*(l1.getSpoint().getY()-l2.getSpoint().getY())) >0 ? 1 :-1;
		//( Q2 - Q1 ) × ( P2 - Q1 )
		long r2 = ((l2.getEpoint().getX()-l2.getSpoint().getX())*(l1.getEpoint().getY()-l2.getSpoint().getY())
				-(l1.getEpoint().getX()-l2.getSpoint().getX())*(l2.getEpoint().getY()-l2.getSpoint().getY())) >0 ? 1 :-1;
		//( Q1 - P1 ) × ( P2 - P1 )
		long r3 = ((l2.getSpoint().getX()-l1.getSpoint().getX())*(l1.getEpoint().getY()-l1.getSpoint().getY())
				-(l1.getEpoint().getX()-l1.getSpoint().getX())*(l2.getSpoint().getY()-l1.getSpoint().getY())) >0 ? 1 :-1;
		//( P2 - P1 ) × ( Q2 - P1 )
		long r4 = ((l1.getEpoint().getX()-l1.getSpoint().getX())*(l2.getEpoint().getY()-l1.getSpoint().getY())
				-(l2.getEpoint().getX()-l1.getSpoint().getX())*(l1.getEpoint().getY()-l1.getSpoint().getY())) >0 ? 1 :-1;
		if(r1*r2<0
		   ||r3*r4<0)
			return false;
		return true;
	}

	/**
	 * 1.只要有任意一点在内部,2.两点都在线上的 为true;其余为false
	 * @param line
	 * @param rect
	 * @return
	 */
	public static boolean intersectant(LongLine line,LongRect rect){
		//先判断是否rect包含line
		if(LongRectUtil.contained(rect,MyGeoConvertor.line2Rect(line))) return true;
		//和矩形的四条边任意一条相交，则线和矩形相交
		LongPoint rb = new LongPoint(rect.getMaxX(),rect.getMinY());
		LongPoint lt = new LongPoint(rect.getMinX(),rect.getMaxY());
		//下边线
		if(collinear(line,rect.getLbPoint())==collinear(line,rb)//排除只有一个点在线上的这种相交情况
			&&intersectant(line,new LongLine(rect.getLbPoint(),rb)))
			return true;
		//右边线
		if(collinear(line,rb)==collinear(line,rect.getRtPoint())//排除只有一个点在线上的这种相交情况
			&&intersectant(line,new LongLine(rb,rect.getRtPoint())))
			return true;		
		//上边线
		if(collinear(line,rect.getRtPoint())==collinear(line,lt)//排除只有一个点在线上的这种相交情况
				&&intersectant(line,new LongLine(rect.getRtPoint(),lt)))
			return true;		
		//左边线
		if(collinear(line,lt)==collinear(line,rect.getLbPoint())//排除只有一个点在线上的这种相交情况
				&&intersectant(line,new LongLine(lt,rect.getLbPoint())))
			return true;
		return false;
	}

	public static void main(String[] args){
		//intersectant(new LongLine(new LongPoint(0,0),new LongPoint(3,3))
		//		,new LongLine(new LongPoint(1,1),new LongPoint(3,1)));
		
		List<String> s1 = Arrays.asList(new String[]{"AAA","BBB"});
		List<String> s2 = Arrays.asList(new String[]{"BBB"});
		System.out.println(s1.contains(s2.get(0)));
	}
}
