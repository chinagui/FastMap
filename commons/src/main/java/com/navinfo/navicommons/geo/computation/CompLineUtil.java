package com.navinfo.navicommons.geo.computation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/** 
* @ClassName: CompLineUitl 
* @author Xiao Xiaowen 
* @date 2016年4月29日 下午2:06:01 
* @Description: TODO
*/
public class CompLineUtil {
	public static double getEucLength(DoublePoint point1,DoublePoint point2){
		return Math.sqrt(Math.pow((point2.getX()-point1.getX()), 2)+Math.pow((point2.getY()-point1.getY()), 2));
	}
	public static DoublePoint LineExtIntersect(DoubleLine line1,DoubleLine line2){
		double k1 = line1.getSlope();
		double k2 = line2.getSlope();
		if(DoubleUtil.equals(k1,k2)){//斜率相同
			if (line1.getEpoint().equals(line2.getSpoint())
					||line1.getEpoint().equals(line2.getEpoint())){
				return line1.getEpoint().clone();
			}else if(line1.getSpoint().equals(line2.getSpoint())
					||line1.getSpoint().equals(line2.getEpoint())){
				return line1.getSpoint().clone();
			}else{
				return null;//线段共线且首尾不同，则无交点或者无穷多个交点
			}
		}else{//斜率不同，则有且只有一个交点
			/**
			 * 算法：
			 * 先计算垂直线的特殊情况
			 * 然后计算：
			 * line1直线方程：y=k1*x+b1
			 * line1两点坐标(1s.x,1s.y)(1e.x,1e.y)
			 * line1直线方程k1=(1e.y-1s.y)/(1e.x-1s.x)
			 * line1直线方程b1=(1e.x*1s.y-1s.x*1e.y)/(1e.x-1s.x)
			 * line2直线方程：y=k2*x+b2
			 * line2两点坐标(2s.x,2s.y)(2e.x,2e.y)
			 * line2直线方程k2=(2e.y-2s.y)/(2e.x-2s.x)
			 * line2直线方程b2=(2e.x*2s.y-2s.x*2e.y)/(2e.x-2s.x)
			 * 交点解x=(b2-b1)/(k1-k2)
			 * 交点解y=(k1*b2-k2*b1)/(k1-k2)
			 */
			double x = 0.0;
			double y = 0.0;
			double b1 = 0.0;
			double b2 = 0.0;
			//两者斜率都为无穷大则在第一个if中已经处理了
			if(k1==DoubleUtil.INFINITY){
				x = line1.getEpoint().getX();//line1.getSpoint().getX() 都行
				b2 = (line2.getEpoint().getX()*line2.getSpoint().getY()-line2.getSpoint().getX()*line2.getEpoint().getY())/(line2.getEpoint().getX()-line2.getSpoint().getX());
				y = k2*x+b2;
			}else if(k2==DoubleUtil.INFINITY){
				x = line2.getEpoint().getX();
				b1 = (line1.getEpoint().getX()*line1.getSpoint().getY()-line1.getSpoint().getX()*line1.getEpoint().getY())/(line1.getEpoint().getX()-line1.getSpoint().getX());
				y = k1*x+b1;
			}else{
				b1 = (line1.getEpoint().getX()*line1.getSpoint().getY()-line1.getSpoint().getX()*line1.getEpoint().getY())/(line1.getEpoint().getX()-line1.getSpoint().getX());

				b2 = (line2.getEpoint().getX()*line2.getSpoint().getY()-line2.getSpoint().getX()*line2.getEpoint().getY())/(line2.getEpoint().getX()-line2.getSpoint().getX());
				x = (b2-b1)/(k1-k2);
				y=(k1*b2-k2*b1)/(k1-k2);
			}
			
			return new DoublePoint(DoubleUtil.keep5Decimal(x),DoubleUtil.keep5Decimal(y));

		}
	}

	/**
	 * 
	 * @param line:lat/lat,degree
	 * @param distance:positive meters
	 * @return DoubleLine[]:[0]-left line,[1]-right line
	 * 特殊说明：当取单方向偏移线是，distance可以正负值，正数取得是原始线的左边偏移线，负数值取得是右边偏移线
	 */
	public static DoubleLine[] offset(DoubleLine line,double distance,boolean isTwoWay){
		DoublePoint s = line.getSpoint();
		DoublePoint e = line.getEpoint();
		double lineEucLength = getEucLength(s,e);
		double degreeDist = GeometryUtils.convert2Degree(distance);
		double deltaX = 0;
		double deltaY = 0;
		if(s.getX()==e.getX()){//垂直线，offset只要平移X轴坐标
			if(e.getY()>s.getY()){
				deltaX = -degreeDist;// PI/2
			}else{
				deltaX = degreeDist;// PI*3/2
			}
		}else if(s.getY()==e.getY()){//水平线
			if(e.getX()>s.getX()){
				deltaY = degreeDist;// 0
			}else{
				deltaY = -degreeDist;// PI
			}
		}else{
			/**
			//A为line作为向量时与X轴正方向夹角弧度
			//deltaX = degreeDist*cos(A+pi/2) = degreeDist*(-sin(A))
			//deltaY = degreeDist*sin(A+pi/2) = degreeDist*cos(A)
			//sin(A) = (e.y-s.y)/lineEucLength
			//cos(A) = (e.x-s.x)/lineEncLength
			 * */
			deltaX = degreeDist*(-(e.getY()-s.getY())/lineEucLength);
			deltaY = degreeDist*(e.getX()-s.getX())/lineEucLength;
		}
		DoubleLine leftLine = new DoubleLine(new DoublePoint(DoubleUtil.keep5Decimal(s.getX()+deltaX),DoubleUtil.keep5Decimal(s.getY()+deltaY))
				,new DoublePoint(DoubleUtil.keep5Decimal(e.getX()+deltaX),DoubleUtil.keep5Decimal(e.getY()+deltaY)));
		if(isTwoWay){
			DoubleLine rightLine = new DoubleLine(new DoublePoint(DoubleUtil.keep5Decimal(s.getX()-deltaX),DoubleUtil.keep5Decimal(s.getY()-deltaY))
					,new DoublePoint(DoubleUtil.keep5Decimal(e.getX()-deltaX),DoubleUtil.keep5Decimal(e.getY()-deltaY)));
			return new DoubleLine[]{leftLine,rightLine};
		}else{
			return new DoubleLine[]{leftLine};
		}
	}
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
					DoubleLine[] offSets = offset(line,distance,true);
					leftLines[j]=offSets[0];
					rightLines[j]=offSets[1];
					//从第二条line开始，计算与前一条line的交点
					if(j>0){
						DoublePoint leftMidPoint = LineExtIntersect(leftLines[j-1],leftLines[j]);
						leftLines[j-1].setEpoint(leftMidPoint);
						leftLines[j].setSpoint(leftMidPoint);
						DoublePoint rightMidPoint = LineExtIntersect(rightLines[j-1],rightLines[j]);
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
					DoublePoint leftMidNode = LineExtIntersect(leftPreLastLine,leftCurFirstLine);
					leftPreLastLine.setEpoint(leftMidNode);
					leftCurFirstLine.setSpoint(leftMidNode);
					DoubleLine rightPreLastLine = rightResults[i-1].getLastLine();
					DoubleLine rightCurFirstLine = rightResults[i].getFirstLine();
					DoublePoint rightMidNode = LineExtIntersect(rightPreLastLine,rightCurFirstLine);
					rightPreLastLine.setEpoint(rightMidNode);
					rightCurFirstLine.setSpoint(rightMidNode);
				}
			}
			//reverse right polylines
			List<DoublePolyline> ls = Arrays.asList(rightResults);
			Collections.reverse(ls);
			rightResults = ls.toArray(new DoublePolyline[0]);
			for(DoublePolyline polyline:rightResults){
				polyline.reverse();
			}
			List<DoublePolyline> resultsList = new ArrayList(Arrays.asList(leftResults));
			resultsList.addAll(Arrays.asList(rightResults));
			return resultsList.toArray(new DoublePolyline[0]);
		}
		return null;
	}
	public static LineString[] offset(LineString[] lines,double distance){
		if(lines!=null&&lines.length>0){
			DoublePolyline[] polylines = new DoublePolyline[lines.length];
			for(int i=0;i<lines.length;i++){
				polylines[i]=MyGeometryConvertor.convert(lines[i]);
			}
			DoublePolyline[] rawResults = offset(polylines,distance);
			LineString[] results = new LineString[rawResults.length];
			for(int j=0;j<rawResults.length;j++){
				results[j]=MyGeometryConvertor.convert(rawResults[j]);
			}
			return results;
		}
		return null;
	}
}
