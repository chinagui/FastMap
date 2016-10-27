package com.navinfo.navicommons.geo.computation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

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
					System.out.println("LINESTRING "+line+"\t"+(j*3+1));
					DoubleLine[] offSets = CompLineUtil.offset(line,distance,true);
					leftLines[j]=offSets[0];
					rightLines[j]=offSets[1];
					System.out.println("LINESTRING "+offSets[0]+"\t"+(j*3+1));
					System.out.println("LINESTRING "+offSets[1]+"\t"+(j*3+1));
					//从第二条line开始，计算与前一条line的交点
					if(j>0){
						if(leftLines[j-1]==null||leftLines[j-1].getSpoint()==null||leftLines[j-1].getEpoint()==null
								||leftLines[j]==null||leftLines[j].getSpoint()==null||leftLines[j].getEpoint()==null
								||rightLines[j-1]==null||rightLines[j-1].getSpoint()==null||rightLines[j-1].getEpoint()==null
								||rightLines[j]==null||rightLines[j].getSpoint()==null||rightLines[j].getEpoint()==null){
							System.out.println("***");
						}
						if(CompLineUtil.angleNoDirection(leftLines[j-1], leftLines[j])<DoubleUtil.MIN_ANGLE4LINE_INT){
							leftLines[j].setSpoint(leftLines[j-1].getEpoint());
						}else{
							DoublePoint leftMidPoint = CompLineUtil.LineExtIntersect(leftLines[j-1],leftLines[j]);
							leftLines[j-1].setEpoint(leftMidPoint);
							leftLines[j].setSpoint(leftMidPoint);
						}
						if(CompLineUtil.angleNoDirection(rightLines[j-1], rightLines[j])<DoubleUtil.MIN_ANGLE4LINE_INT){
							rightLines[j].setSpoint(rightLines[j-1].getEpoint());
						}else{
							DoublePoint rightMidPoint = CompLineUtil.LineExtIntersect(rightLines[j-1],rightLines[j]);
							rightLines[j-1].setEpoint(rightMidPoint);
							rightLines[j].setSpoint(rightMidPoint);
						}
					}
				}
				leftResults[i]=new DoublePolyline(leftLines);
				rightResults[i]=new DoublePolyline(rightLines);
				//从第二条polyline开始，计算前一条polyline的最后一条line与当前polyline的第一条line的交点
				if(i>0){
					DoubleLine leftPreLastLine = leftResults[i-1].getLastLine();
					DoubleLine leftCurFirstLine = leftResults[i].getFirstLine();
					if(CompLineUtil.angleNoDirection(leftPreLastLine, leftCurFirstLine)<DoubleUtil.MIN_ANGLE4LINE_INT){
						leftCurFirstLine.setSpoint(leftPreLastLine.getEpoint());
					}else{
						DoublePoint leftMidNode = CompLineUtil.LineExtIntersect(leftPreLastLine,leftCurFirstLine);
						leftPreLastLine.setEpoint(leftMidNode);
						leftCurFirstLine.setSpoint(leftMidNode);
					}
					
					DoubleLine rightPreLastLine = rightResults[i-1].getLastLine();
					DoubleLine rightCurFirstLine = rightResults[i].getFirstLine();
					if(CompLineUtil.angleNoDirection(rightPreLastLine, rightCurFirstLine)<DoubleUtil.MIN_ANGLE4LINE_INT){
						rightCurFirstLine.setSpoint(rightPreLastLine.getEpoint());
					}else{
						DoublePoint rightMidNode = CompLineUtil.LineExtIntersect(rightPreLastLine,rightCurFirstLine);
						rightPreLastLine.setEpoint(rightMidNode);
						rightCurFirstLine.setSpoint(rightMidNode);
					}
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
			DoublePoint start = JtsGeometryConvertor.convert(startPoint.getCoordinate());//保留起点
			DoublePoint end = null;
			DoublePoint curStart = start;
			for(int i=0;i<length;i++){
				polylines[i]=JtsGeometryConvertor.convert(lines[i]);
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
				//舍弃多余小数位数
				results[j]=JtsGeometryConvertor.convertWithSpecDecimal(rawResults[j]);
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
		DoublePoint startPoint1 = JtsGeometryConvertor.convert(startLine.getCoordinateN(0));
		DoublePoint startPoint2 = JtsGeometryConvertor.convert(endLine.getCoordinateN(0));
		DoublePoint endPoint2 = JtsGeometryConvertor.convert(endLine.getCoordinateN(endLine.getNumPoints() - 1));
		if(startPoint1.equals(startPoint2)){
			start = JtsGeometryConvertor.convert(startLine.getCoordinateN(1));
			mid = startPoint1;
			end = JtsGeometryConvertor.convert(endLine.getCoordinateN(1));
		}else if(startPoint1.equals(endPoint2)){
			start = JtsGeometryConvertor.convert(startLine.getCoordinateN(1));
			mid = startPoint1;
			end = JtsGeometryConvertor.convert(endLine.getCoordinateN(endLine.getNumPoints() - 2));
		}else{
			DoublePoint endPoint1 = JtsGeometryConvertor.convert(startLine.getCoordinateN(startLine.getNumPoints() - 1));
			if(endPoint1.equals(startPoint2)){
				start = JtsGeometryConvertor.convert(startLine.getCoordinateN(startLine.getNumPoints() - 2));
				mid = endPoint1;
				end = JtsGeometryConvertor.convert(endLine.getCoordinateN(1));
			}else if(endPoint1.equals(endPoint2)){
				start = JtsGeometryConvertor.convert(startLine.getCoordinateN(startLine.getNumPoints() - 2));
				mid = endPoint1;
				end = JtsGeometryConvertor.convert(endLine.getCoordinateN(endLine.getNumPoints() - 2));
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
		DoublePoint adjStart = JtsGeometryConvertor.convert(adjacentLine.getCoordinateN(0));
		if(adjStart.equals(mid)){
			adj = JtsGeometryConvertor.convert(adjacentLine.getCoordinateN(1));
		}else{
			DoublePoint adjEnd = JtsGeometryConvertor.convert(adjacentLine.getCoordinateN(adjacentLine.getNumPoints() - 1));
			if(adjEnd.equals(mid)){
				adj = JtsGeometryConvertor.convert(adjacentLine.getCoordinateN(adjacentLine.getNumPoints() - 2));
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
		//获取中点连接线start，mid，end
		DoublePoint[] connector = getStartMidEnd(startLine,endLine);
		DoublePoint start = connector[0];
		DoublePoint mid = connector[1];
		DoublePoint end = connector[2];
		DoublePoint fromP = JtsGeometryConvertor.convert(fromPoint.getCoordinate());
		DoublePoint tarStart = JtsGeometryConvertor.convert(targetLine.getCoordinateN(0));
		Coordinate[] coors = targetLine.getCoordinates();
		DoubleLine sLine = new DoubleLine(start,mid);
		DoubleLine eLine = new DoubleLine(mid,end);
		int fromIndex = 0;
		int endIndex = coors.length-1;
		//endIndex-fromIndex+1为截取剩下的长度
		//+1是增加start和end中间点作为截取剩下的线的一个端点
		Coordinate[] newCoors = null;
		if(tarStart.equals(fromP)){//targetLine的画线方向是从fromPoint往外画的
			for(;fromIndex<coors.length;fromIndex++){
				DoublePoint temp = JtsGeometryConvertor.convert(coors[fromIndex]);
				if(temp.equals(mid)){//如果有重合点，直接取此点作为切割起始点
					break;
				}
				DoubleLine adjLine = new DoubleLine(mid,temp);
				if(isRightSide==CompLineUtil.isRightSide(sLine,eLine,adjLine)){
					break;
				}
			}
			if(fromIndex>endIndex){//如果整条线都在里头，则只保留末端点与mid组成的直线
				newCoors = new Coordinate[2];
				newCoors[0]=JtsGeometryConvertor.convert(mid);
				newCoors[1]=coors[endIndex];
			}else{
				newCoors = new Coordinate[endIndex-fromIndex+1+1];
				newCoors[0]=JtsGeometryConvertor.convert(mid);
				int i = 1;
				for(;fromIndex<=endIndex;fromIndex++){
					newCoors[i]=coors[fromIndex];
					i++;
				}
			}
		}else{//targetLine的画线方向是从外向fromPoint画的
			for(;endIndex>=0;endIndex--){
				DoublePoint temp = JtsGeometryConvertor.convert(coors[endIndex]);
				if(temp.equals(mid)){//如果有重合点，直接取此点作为切割起始点
					break;
				}
				DoubleLine adjLine = new DoubleLine(mid,temp);
				if(isRightSide==CompLineUtil.isRightSide(sLine,eLine,adjLine)){
					break;
				}
			}
			if(endIndex<0){//如果整条线都在里头，则只保留末端点与mid组成的直线
				newCoors = new Coordinate[2];
				newCoors[0]=JtsGeometryConvertor.convert(mid);
				newCoors[1]=coors[0];
			}else{
				newCoors = new Coordinate[endIndex-fromIndex+1+1];
				int i = 0;
				for(;fromIndex<=endIndex;fromIndex++){
					newCoors[i]=coors[fromIndex];
					i++;
				}
				newCoors[i]=JtsGeometryConvertor.convert(mid);
			}
		}
		return JtsGeometryFactory.createLineString(newCoors);
	}
	
	public static void main(String[] args){
//		String[] a1 = new String[]{"AA","BB","CC","DD"};
//		String[] a2 = Arrays.copyOfRange(a1, 0, 1);
//		for(String s:a2){
//			System.out.println(s);
//		}
		LineString l = JtsGeometryFactory.createLineString(new Coordinate[0]);
		System.out.println(l.toText());
	}
}