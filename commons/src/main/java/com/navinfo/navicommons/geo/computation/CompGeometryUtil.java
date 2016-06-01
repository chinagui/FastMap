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

import com.navinfo.dataservice.commons.util.DoubleUtil;
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
			Polygon p = (Polygon)sub.getGeometryN(i);
			//不需要再四舍五入，和图幅多边形intersection，不会产生超过5位精度的形状点
//			//保留指定精度
//			for(Coordinate co:p.getCoordinates()){
//				co.x=DoubleUtil.keepSpecDecimal(co.x);
//				co.y = DoubleUtil.keepSpecDecimal(co.y);
//			}
			result.add(parseLine(p,mesh));
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
	/**
	 * 计算一个闭合线是否是顺时针方向
	 * 算法：
	 * 找到一个凸点，按照线坐标方向，凸点前一个形状点组成第一条line，如果凸点下一个形状点在line的顺时针方向，则是顺时针，否则为逆时针
	 * @param line:线需要闭合，不闭合会抛异常
	 * @return
	 * @throws GeoComputationException
	 */
	public static boolean isClockwise(LineString line)throws GeoComputationException{
		if(!line.isClosed()){
			throw new GeoComputationException("线不闭合。");
		}
		Polygon bbox = (Polygon)line.getEnvelope();
		//minx,miny,maxx,maxy任意一个
		Coordinate minCo = bbox.getExteriorRing().getCoordinateN(0);
		Coordinate maxCo = bbox.getExteriorRing().getCoordinateN(2);
		double minx = minCo.x;
		double miny = minCo.y;
		double maxx = maxCo.x;
		double maxy = maxCo.y;
		Coordinate[] cos = line.getCoordinates();
		int pointSize = cos.length-1;
		for(int i=0;i<pointSize;i++){
			if(DoubleUtil.equals(cos[0].x, minx)
			||DoubleUtil.equals(cos[0].x, maxx)
			||DoubleUtil.equals(cos[0].y, miny)
			||DoubleUtil.equals(cos[0].y, maxy)){
				Coordinate preCo = null;
				if(i==0){
					preCo = cos[cos.length-2];
				}else{
					preCo = cos[i-1];
				}
				if(CompLineUtil.isRightSide(new DoubleLine(MyGeometryConvertor.convert(preCo)
						,MyGeometryConvertor.convert(cos[i]))
						,MyGeometryConvertor.convert(cos[i+1]))){
					return true;
				}else{
					return false;
				}
				
			}
		}
		return false;
	}
	/**
	 * 根据几何计算所属图幅号
	 * 点：可能属于1,2,4图幅
	 * 线：根据业务规则，如果形状点是图廓点，那么计算出来的图幅需要和至少一个形状点计算出来的图幅相交，才能算是所属图幅
	 *    也就是图廓点计算的图幅，如果只有本身所属，那么不能算为线的图幅，比如线有两个点，一个在图幅内，一个在图廓线上，那么最后计算所属图幅只有一个
	 *    再比如线有两个点，两个点在一个图幅的左下交点和右下角点，那么算出来是两个点所属图幅的交集，只有中间两个图幅
	 *    如果两个点在图幅的左下角点，右上角点，那么计算出来的只有一个图幅
	 * 面：同线
	 * 只实现了Point，LineString，Polygon三种类型
	 * @param geo
	 * @return
	 */
	public static String[] geo2MeshesWithoutBreak(Geometry geo){
		if(geo!=null){
			if(geo.getGeometryType().equals(GeometryTypeName.POINT)){
				return MeshUtils.point2Meshes(((Point)geo).getX(), ((Point)geo).getY());
			}else if(geo.getGeometryType().equals(GeometryTypeName.LINESTRING)
					||geo.getGeometryType().equals(GeometryTypeName.POLYGON)){
				Coordinate[] cs = geo.getCoordinates();
				int checkLength=cs.length;
				//判断是否闭合，如果是闭合去除最后一个闭合点
				if(cs[0].equals(cs[cs.length-1])){
					checkLength--;
				}
				Set<String> coreMeshes = new HashSet<String>();
				Set<String> noCoreMeshes = new HashSet<String>();
				for(int i=0;i<checkLength;i++){
					Coordinate c = cs[i];
					String[] result = MeshUtils.point2Meshes(c.x, c.y);
					if(result.length==1){
						coreMeshes.add(result[0]);
					}else{
						for(String mesh:result){
							if(noCoreMeshes.contains(mesh)){
								coreMeshes.add(mesh);
							}else{
								noCoreMeshes.add(mesh);
							}
						}
					}
				}
				return coreMeshes.toArray(new String[0]);
			}
		}
		return null;
	}
}
