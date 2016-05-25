package com.navinfo.navicommons.geo.computation;

import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.navicommons.exception.GeoComputationException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Geometry;

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
	 * @return：map，key:图幅号，value：所有切割后生成线数组（每个数组能够组成一个一个面且是逆时针方向）的集合
	 * 注意：不同图幅内部lineString有重复，即图廓线会属于两个图幅
	 */
	public static Map<String,Set<LineString[]>> cut(Polygon polygon,String[] meshes)throws GeoComputationException{
		
		Map<String,Set<LineString[]>> result = new HashMap<String,Set<LineString[]>>();
		for(String meshId:meshes){
			result.put(meshId, cut(polygon,meshId));
		}
		return result;
	}
	public static Set<LineString[]> cut(Polygon polygon,String mesh)throws GeoComputationException{
		Set<LineString[]> result = new HashSet<LineString[]>();
		//找到需要生成的面
		Polygon meshPolygon = MyGeometryConvertor.convert(MeshUtils.mesh2Rect(mesh));
		Geometry sub = polygon.intersection(meshPolygon);//sub 可能是polygon，也可能是multipolygon
		int geoNum = sub.getNumGeometries();
		for(int i=0;i<geoNum;i++){
			result.add(parseLine((Polygon)sub.getGeometryN(i),mesh));
		}
		return result;
	}
	/**
	 * 多边形只能和图廓线相交，不能超越图幅范围
	 * @param polygon:多边形，无hole
	 * @param mesh：多边形所属图幅号
	 * @return 被图幅边框打断后生成的lineString数组
	 * @throws GeoComputationException
	 */
	public static LineString[] parseLine(Polygon polygon,String mesh)throws GeoComputationException{
		LineString line = polygon.getExteriorRing();
		if(isClockwise(line)){
			line = (LineString)line.reverse();
		}
		Coordinate[] cos = line.getCoordinates();
		List<LineString> resultList = new LinkedList<LineString>();
		int startIndex = 0;
		for(int i=1;i<cos.length;i++){
			/**
			 * 两种情况：
			 * 1. 当起点在图廓线上，则要么下一个点也在border上，这时直接组成一条线，要么下一个点不在border上，那么要直到找到下一个在border上的点才组成一条线
			 * 2. 当起点不在图廓线上，则找到下一个点在border上，线就被打断了，直接生成一条线
			 * 所以不管起点是在不在图廓线上，只要下一个点在图廓线上，那么线就会打断
			 */
			if(MeshUtils.locateMeshBorder(cos[i].x, cos[i].y, mesh)//被打断的情况
					||(i==(cos.length-1)))//最后一个点了，和前面的组成线
			{
				Coordinate[] sub = new Coordinate[i-startIndex+1];
				for(int j=0;j<=(i-startIndex);j++){
					sub[j]=cos[startIndex+j];
				}
				resultList.add(JtsGeometryFactory.createLineString(sub));
				startIndex = i;
			}
		}
		return resultList.toArray(new LineString[0]);
	}
	
	public static boolean isClockwise(LineString line)throws GeoComputationException{
		if(!line.isClosed()){
			throw new GeoComputationException("线不闭合。");
		}
		Coordinate[] cos = line.getCoordinates();
		int clockNum = 0;
		int anticlockNum = 0;
		//
		if(CompLineUtil.isRightSide(new DoubleLine(MyGeometryConvertor.convert(cos[cos.length-2])
				,MyGeometryConvertor.convert(cos[0])), MyGeometryConvertor.convert(cos[1]))){
			clockNum++;
		}else{
			anticlockNum++;
		}
		for(int i=2;i<cos.length;i++){
			DoubleLine dl = new DoubleLine(MyGeometryConvertor.convert(cos[i-2]),
					MyGeometryConvertor.convert(cos[i-1]));
			DoublePoint dp = MyGeometryConvertor.convert(cos[i]);
			if(CompLineUtil.isRightSide(dl,dp)){
				clockNum++;
			}else{
				anticlockNum++;
			}
		}
		if(clockNum>anticlockNum){
			return true;
		}else{
			return false;
		}
	}
}
