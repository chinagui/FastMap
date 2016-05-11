package com.navinfo.navicommons.geo.computation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.navinfo.dataservice.commons.util.JtsGeometryUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

/** 
* @ClassName: CompPolylineUtil 
* @author Xiao Xiaowen 
* @date 2016年5月10日 下午9:12:51 
* @Description: TODO
*/
public class CompPolylineUtil {

	public static DoublePolyline offset(DoublePolyline polyline,double distance){
		
		return null;
	}
	/**
	 * 
	 * @param polylines：首尾相连的polyline组成的线串
	 * @param distance
	 * @return
	 */
	public static DoublePolyline[] offset(DoublePolyline[] polylines,double distance){
		if(polylines!=null&&polylines.length>0){
			int linesLen = polylines.length;
			DoublePolyline[] leftResults = new DoublePolyline[linesLen];
			DoublePolyline[] rightResults = new DoublePolyline[linesLen];
			//先遍历polylines
			for(int i=0;i<linesLen;i++){
				DoublePolyline polyline = polylines[i];
				int lineLen = polyline.getLineSize();
				DoubleLine[] leftLines= new DoubleLine[lineLen];
				DoubleLine[] rightLines= new DoubleLine[lineLen];
				//每条polyline有若干line组成，遍历lines
				for(int j=0;j<lineLen;j++){
					DoubleLine line = polyline.getLines()[j];
					DoubleLine[] offSets = CompLineUtil.offset(line,distance,true);
					leftLines[j]=offSets[0];
					rightLines[j]=offSets[1];
					//从第二条line开始，计算与前一条line的交点
					if(j>0){
						DoublePoint leftMidPoint = CompLineUtil.LineExtIntersect(leftLines[j-1],leftLines[j]);
						leftLines[j-1].setEpoint(leftMidPoint);
						leftLines[j].setSpoint(leftMidPoint);
						DoublePoint rightMidPoint = CompLineUtil.LineExtIntersect(rightLines[j-1],rightLines[j]);
						rightLines[j-1].setEpoint(rightMidPoint);
						rightLines[j].setSpoint(rightMidPoint);
					}
				}
				leftResults[i]=new DoublePolyline(leftLines);
				rightResults[i]=new DoublePolyline(rightLines);
				//从第二条polyline开始，计算前一条polyline的最后一条line与当前polyline的第一条line的交点
				if(i>0){
					DoubleLine leftPreLastLine = leftResults[i-1].getLastLine();
					DoubleLine leftCurFirstLine = leftResults[i].getFirstLine();
					DoublePoint leftMidNode = CompLineUtil.LineExtIntersect(leftPreLastLine,leftCurFirstLine);
					leftPreLastLine.setEpoint(leftMidNode);
					leftCurFirstLine.setSpoint(leftMidNode);
					DoubleLine rightPreLastLine = rightResults[i-1].getLastLine();
					DoubleLine rightCurFirstLine = rightResults[i].getFirstLine();
					DoublePoint rightMidNode = CompLineUtil.LineExtIntersect(rightPreLastLine,rightCurFirstLine);
					rightPreLastLine.setEpoint(rightMidNode);
					rightCurFirstLine.setSpoint(rightMidNode);
				}
			}
			//reverse left polylines
			List<DoublePolyline> ls = Arrays.asList(leftResults);
			Collections.reverse(ls);
			leftResults = ls.toArray(new DoublePolyline[0]);
			for(DoublePolyline polyline:leftResults){
				polyline.reverse();
			}
			List<DoublePolyline> resultsList = new ArrayList(Arrays.asList(rightResults));
			resultsList.addAll(Arrays.asList(leftResults));
			return resultsList.toArray(new DoublePolyline[0]);
		}
		return null;
	}
	public static LineString[] separate(Point startPoint,LineString[] lines,double distance){
		if(lines!=null&&lines.length>0){
			int length =lines.length;
			DoublePolyline[] polylines = new DoublePolyline[length];
			DoublePoint start = MyGeometryConvertor.convert(startPoint.getCoordinate());//保留起点
			DoublePoint end = null;
			DoublePoint curStart = start;
			for(int i=0;i<length;i++){
				polylines[i]=MyGeometryConvertor.convert(lines[i]);
				if(!curStart.equals(polylines[i].getSpoint())){
					polylines[i].reverse();
				}
				curStart = polylines[i].getEpoint();
			}
			end = polylines[length-1].getEpoint();//得到终点
			//获取右左偏移平行线
			DoublePolyline[] rawResults = offset(polylines,distance);
			//构造闭环
			rawResults[0].extend(start, false);
			rawResults[length-1].extend(end, true);
			rawResults[length].extend(end, false);
			rawResults[2*length-1].extend(start, true);
			//转换
			LineString[] results = new LineString[rawResults.length];
			for(int j=0;j<rawResults.length;j++){
				results[j]=MyGeometryConvertor.convert(rawResults[j]);
			}
			return results;
		}
		return null;
	}
	/**
	 * 首尾相连的两条线，获取连接点mid，起始线离mid最近的形状点为start，终点线离mid最近的形状点为end
	 * @param startLine
	 * @param endLine
	 * @return [start,mid,end]
	 * @throws Exception
	 */
	private static DoublePoint[] getStartMidEnd(LineString startLine,LineString endLine)throws Exception{
		//先判断起点
		DoublePoint start = null;
		DoublePoint mid = null;
		DoublePoint end = null;
		DoublePoint startPoint1 = MyGeometryConvertor.convert(startLine.getCoordinateN(0));
		DoublePoint startPoint2 = MyGeometryConvertor.convert(endLine.getCoordinateN(0));
		DoublePoint endPoint2 = MyGeometryConvertor.convert(endLine.getCoordinateN(endLine.getNumPoints() - 1));
		if(startPoint1.equals(startPoint2)){
			start = MyGeometryConvertor.convert(startLine.getCoordinateN(1));
			mid = startPoint1;
			end = MyGeometryConvertor.convert(endLine.getCoordinateN(1));
		}else if(startPoint1.equals(endPoint2)){
			start = MyGeometryConvertor.convert(startLine.getCoordinateN(1));
			mid = startPoint1;
			end = MyGeometryConvertor.convert(endLine.getCoordinateN(endLine.getNumPoints() - 2));
		}else{
			DoublePoint endPoint1 = MyGeometryConvertor.convert(startLine.getCoordinateN(startLine.getNumPoints() - 1));
			if(endPoint1.equals(startPoint2)){
				start = MyGeometryConvertor.convert(startLine.getCoordinateN(startLine.getNumPoints() - 2));
				mid = endPoint1;
				end = MyGeometryConvertor.convert(endLine.getCoordinateN(1));
			}else if(endPoint1.equals(endPoint2)){
				start = MyGeometryConvertor.convert(startLine.getCoordinateN(startLine.getNumPoints() - 2));
				mid = endPoint1;
				end = MyGeometryConvertor.convert(endLine.getCoordinateN(endLine.getNumPoints() - 2));
			}else{
				throw new Exception("起始线和终点线不相连，无法判断。");
			}
		}
		return new DoublePoint[]{start,mid,end};
	}
	
	public static boolean isRightSide(LineString startLine,LineString endLine,LineString adjacentLine)throws Exception{
		//获取中点连接线start，mid，end
		DoublePoint[] connector = getStartMidEnd(startLine,endLine);
		DoublePoint start = connector[0];
		DoublePoint mid = connector[1];
		DoublePoint end = connector[2];
		DoublePoint adj = null;
		DoublePoint adjStart = MyGeometryConvertor.convert(adjacentLine.getCoordinateN(0));
		if(adjStart.equals(mid)){
			adj = MyGeometryConvertor.convert(adjacentLine.getCoordinateN(1));
		}else{
			DoublePoint adjEnd = MyGeometryConvertor.convert(adjacentLine.getCoordinateN(adjacentLine.getNumPoints() - 1));
			if(adjEnd.equals(mid)){
				adj = MyGeometryConvertor.convert(adjacentLine.getCoordinateN(adjacentLine.getNumPoints() - 2));
			}else{
				throw new Exception("挂接线和起始线和终点线连接点不相连，无法判断。");
			}
		}
		return CompLineUtil.isRightSide(new DoubleLine(start,mid),new DoubleLine(mid,end),new DoubleLine(mid,adj));
	}

	/**
	 * 舍弃targetLine不在指定side的所有形状点
	 * 注意：此方法忽略了很多特殊情况，只针对上下线分离时修改挂接link形状时使用
	 * @param startLine
	 * @param endLine
	 * @param targetLine：切割的目标线
	 * @param fromPoint:targetLine从这点开始判断是否在指定side
	 * @param isRightSide
	 * @return
	 * @throws Exception
	 */
	public static LineString cut(LineString startLine,LineString endLine,LineString targetLine,Point fromPoint,boolean isRightSide)throws Exception{
		int newSize = 0;
		//获取中点连接线start，mid，end
		DoublePoint[] connector = getStartMidEnd(startLine,endLine);
		DoublePoint start = connector[0];
		DoublePoint mid = connector[1];
		DoublePoint end = connector[2];
		DoublePoint fromP = MyGeometryConvertor.convert(fromPoint.getCoordinate());
		DoublePoint tarStart = MyGeometryConvertor.convert(targetLine.getCoordinateN(0));
		Coordinate[] coors = targetLine.getCoordinates();
		DoubleLine sLine = new DoubleLine(start,mid);
		DoubleLine eLine = new DoubleLine(mid,end);
		int fromIndex = 0;
		int endIndex = coors.length-1;
		if(tarStart.equals(fromP)){//targetLine的画线方向是从fromPoint往外画的
			for(;fromIndex<coors.length;fromIndex++){
				DoublePoint temp = MyGeometryConvertor.convert(coors[fromIndex]);
				if(temp.equals(mid)){//如果有重合点，直接取此点作为切割起始点
					break;
				}
				DoubleLine adjLine = new DoubleLine(mid,temp);
				if(!(isRightSide&&CompLineUtil.isRightSide(sLine,eLine,adjLine))){
					break;
				}
			}
		}else{//targetLine的画线方向是从外向fromPoint画的
			for(;endIndex>=0;endIndex--){
				DoublePoint temp = MyGeometryConvertor.convert(coors[endIndex]);
				if(temp.equals(mid)){//如果有重合点，直接取此点作为切割起始点
					break;
				}
				DoubleLine adjLine = new DoubleLine(mid,temp);
				if(!(isRightSide&&CompLineUtil.isRightSide(sLine,eLine,adjLine))){
					break;
				}
			}
		}
		Coordinate[] newCoors = new Coordinate[endIndex-fromIndex+1];
		int i = 0;
		for(;fromIndex<=endIndex;fromIndex++){
			newCoors[i]=coors[fromIndex];
		}
		return JtsGeometryUtil.createLineString(newCoors);
	}
	
	public static void main(String[] args){
		String[] a1 = new String[]{"AA","BB","CC","DD"};
		String[] a2 = Arrays.copyOfRange(a1, 0, 1);
		for(String s:a2){
			System.out.println(s);
		}
	}
}
