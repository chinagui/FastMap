package com.navinfo.navicommons.geo.computation;

import com.vividsolutions.jts.geom.LinearRing;

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.LineString;

/** 
* @ClassName: CompGeometryUtil 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:37:34 
* @Description: TODO
*/
public class CompGeometryUtil {
	/**
	 * [x1,y1,x2,y2]
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static boolean intersectLine(long[] line1,long[] line2){
		//先判断矩形是否相交
		if(!intersectRect(line2Rect(line1),line2Rect(line2))) 
			return false;
		//再判断叉积
		//如果两线段相交，则两线段必然相互跨立对方。
		//若P1P2跨立Q1Q2 ，则矢量 ( P1 - Q1 ) 和( P2 - Q1 )位于矢量( Q2 - Q1 ) 的两侧，即( P1 - Q1 ) × ( Q2 - Q1 ) * ( P2 - Q1 ) × ( Q2 - Q1 ) < 0。上式可改写成( P1 - Q1 ) × ( Q2 - Q1 ) * ( Q2 - Q1 ) × ( P2 - Q1 ) > 0。当 ( P1 - Q1 ) × ( Q2 - Q1 ) = 0 时，说明 ( P1 - Q1 ) 和 ( Q2 - Q1 )共线，但是因为已经通过快速排斥试验，所以 P1 一定在线段 Q1Q2上；同理，( Q2 - Q1 ) ×(P2 - Q1 ) = 0 说明 P2 一定在线段 Q1Q2上。所以判断P1P2跨立Q1Q2的依据是：( P1 - Q1 ) × ( Q2 - Q1 ) * ( Q2 - Q1 ) × ( P2 - Q1 ) >= 0。
		//同理判断Q1Q2跨立P1P2的依据是：( Q1 - P1 ) × ( P2 - P1 ) * ( P2 - P1 ) × ( Q2 - P1 ) >= 0。
		if(((line1[0]-line2[0])*(line2[3]-line2[1])-(line2[2]-line2[0])*(line1[1]-line2[1]))
				*((line2[2]-line2[0])*(line1[3]-line2[1])-(line1[2]-line2[0])*(line2[3]-line2[1]))<0
		   ||((line2[0]-line1[0])*(line1[3]-line1[1])-(line1[2]-line1[0])*(line2[1]-line1[1]))
			*((line1[2]-line1[0])*(line2[3]-line1[1])-(line2[2]-line1[0])*(line1[3]-line1[1]))<0)
			return false;
		return true;
	}
	/**
	 * 如果线段有重合，算包含
	 * [minx,miny,maxx,maxy]
	 * @param rect1:包含对象
	 * @param rect2：被包含对象
	 * @return
	 */
	public static boolean containsRect(long[] rect1,long[] rect2){
		//计算rect2的minx, miny,maxx,maxy都在rect1内则包含；
		if((rect2[0]>=rect1[0]&&rect2[0]<=rect1[2])
				&&(rect2[1]>=rect1[1]&&rect2[1]<=rect1[3])
				&&(rect2[2]>=rect1[0]&&rect2[2]<=rect1[2])
				&&(rect2[3]>=rect1[1]&&rect2[3]<=rect1[3])) return true;
		return false;
	}
	/**
	 * 只边框重合不算相交
	 * [minx,miny,maxx,maxy]
	 * @param rect1
	 * @param rect2
	 * @return
	 */
	public static boolean intersectRect(long[] rect1,long[] rect2){
//		if(Math.min(rect1[2],rect2[2])>Math.max(rect1[0], rect2[0])
//				||Math.min(rect1[1], rect2[1])>Math.max(rect1[3], rect2[3])) return false;
//		return true;
		//采用中心点长度算法
		return ( Math.abs((rect1[0]+rect1[2])-(rect2[0]+rect2[2]))<(rect1[2]-rect1[0]+rect2[2]-rect2[0])
		        && Math.abs((rect1[1]+rect1[3])-(rect2[1]+rect2[3]))<(rect1[3]-rect1[1]+rect2[3]-rect1[1]) );
	}
	/**
	 * 
	 * @param line:[x1,y1,x2,y2]
	 * @param rect:[minx,miny,maxx,maxy]
	 * @return
	 */
	public static boolean intersectLineRect(long[] line,long[] rect){
		//先判断是否rect包含line
		if(containsRect(rect,line2Rect(line))) return true;
		//和矩形的四条边任意一条相交，则线和矩形相交
		long[] rectLine=new long[]{rect[0],rect[1],rect[2],rect[1]};
		if(intersectLine(line,rectLine))
			return true;
		rectLine = new long[]{rect[2],rect[1],rect[2],rect[3]};
		if(intersectLine(line,rectLine))
			return true;
		rectLine = new long[]{rect[2],rect[3],rect[0],rect[3]};
		if(intersectLine(line,rectLine)){
			return true;
		}
		rectLine = new long[]{rect[0],rect[3],rect[0],rect[1]};
		if(intersectLine(line,rectLine))
			return true;
		return false;
	}
	/**
	 * 生成line的外接矩形
	 * @param line：[x1,y1,x2,y2]
	 * @return rect:[minx,miny,maxx,maxy]
	 */
	public static long[] line2Rect(long[] line){
		long[] rect = new long[4];
		if(line[0]<line[2]){
			rect[0]=line[0];
			rect[2]=line[2];
		}else{
			rect[0]=line[2];
			rect[2]=line[0];
		}
		if(line[1]<line[3]){
			rect[1]=line[1];
			rect[3]=line[3];
		}else{
			rect[1]=line[3];
			rect[3]=line[1];
		}
		return rect;
	}
	
	/**
	 * 射线法判断点是否在多边形内部
	 * @param point 待判断的点
	 * @param face 闭合多边形顶点
	 * @return
	 */
	public static boolean pointInFace(double[] point, double[] face) {

		double px = point[0];
		double py = point[1];

		boolean flag = false;

		int pointCount = face.length / 2;

		for (int i = 0; i < pointCount-1; i++) {
			double sx = face[2 * i];
			double sy = face[2 * i + 1];
			double tx = face[2 * i + 2];
			double ty = face[2 * i + 3];

			// 点与多边形顶点重合
			if ((sx == px && sy == py) || (tx == px && ty == py)) {
				return true;
			}

			// 判断线段两端点是否在射线两侧
			if ((sy < py && ty >= py) || (sy >= py && ty < py)) {
				// 线段上与射线 Y 坐标相同的点的 X 坐标
				double x = sx + (py - sy) * (tx - sx) / (ty - sy);

				// 点在多边形的边上
				if (x == px) {
					return true;
				}

				// 射线穿过多边形的边界
				if (x > px) {
					flag = !flag;
				}
			}
		}

		// 射线穿过多边形边界的次数为奇数时点在多边形内
		return flag;

	}
	/**
	 * 图廓线打断多边形
	 * @param lines:组成闭合简单多边形的多条lineString,如果只有一条，则该条线的首尾coordinate必须equal
	 * @param meshes：这些lineString跨越的图幅号
	 * @return：map，key:图幅号，value：该图幅号内的多边形的所有边linestring
	 */
	public static Map<String,LineString[]> cut(LineString[] lines,String[] meshes){
		Map<String,LineString[]> result = new HashMap<String,LineString[]>();
		
		return null;
	}
}
