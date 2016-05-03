package com.navinfo.navicommons.geo.computation;

import java.util.List;

import com.navinfo.dataservice.commons.util.GeometryUtils;

/** 
* @ClassName: CompLineUitl 
* @author Xiao Xiaowen 
* @date 2016年4月29日 下午2:06:01 
* @Description: TODO
*/
public class CompLineUitl {
	
	public static double getEucLength(DoublePoint point1,DoublePoint point2){
		return Math.sqrt(Math.pow((point2.getX()-point1.getX()), 2)+Math.pow((point2.getY()-point1.getY()), 2));
	}
	public static DoublePoint LineExtInters(DoubleLine line1,DoubleLine line2){
		double denominator = 0;
		return null;
	}
	/**
	 * 
	 * @param line:lat/lat,degree
	 * @param distance:positive for left or negative for right meters
	 * @return
	 */
	public static DoubleLine offset(DoubleLine line,double distance){
		DoublePoint sPoint = line.getsPoint();
		DoublePoint ePoint = line.getePoint();
		double lineEucLength = getEucLength(sPoint,ePoint);
		double degreeDist = GeometryUtils.convert2Degree(distance);
		double deltaX = 0;
		double deltaY = 0;
		//A为line作为向量时与X轴正方向夹角弧度
		//deltaX = degreeDist*cos(A+pi/2) = degreeDist*(-sin(A))
		//deltaY = degreeDist*sin(A+pi/2) = degreeDist*cos(A)
		//sin(A) = (ePoint.y-sPoint.y)/lineEucLength
		//cos(A) = (ePoint.x-sPoint.x)/lineEncLength
		deltaX = degreeDist*(-(ePoint.getY()-sPoint.getY())/lineEucLength);
		deltaY = degreeDist*(ePoint.getX()-sPoint.getX())/lineEucLength;
		
		return new DoubleLine(new DoublePoint((sPoint.getX()+deltaX),(sPoint.getY()+deltaY))
				,new DoublePoint((ePoint.getX()+deltaX),(ePoint.getY()+deltaY)));
	}

	/**
	 * 
	 * @param line:lat/lat,degree
	 * @param distance:positive meters
	 * @return DoubleLine[]:[0]-left line,[1]-right line
	 */
	public static DoubleLine[] offset(DoubleLine line,double distance,boolean isTwoWay){
		DoublePoint sPoint = line.getsPoint();
		DoublePoint ePoint = line.getePoint();
		double lineEucLength = getEucLength(sPoint,ePoint);
		double degreeDist = GeometryUtils.convert2Degree(distance);
		double deltaX = 0;
		double deltaY = 0;
		//A为line作为向量时与X轴正方向夹角弧度
		//deltaX = degreeDist*cos(A+pi/2) = degreeDist*(-sin(A))
		//deltaY = degreeDist*sin(A+pi/2) = degreeDist*cos(A)
		//sin(A) = (ePoint.y-sPoint.y)/lineEucLength
		//cos(A) = (ePoint.x-sPoint.x)/lineEncLength
		deltaX = degreeDist*(-(ePoint.getY()-sPoint.getY())/lineEucLength);
		deltaY = degreeDist*(ePoint.getX()-sPoint.getX())/lineEucLength;
		DoubleLine leftLine = new DoubleLine(new DoublePoint((sPoint.getX()+deltaX),(sPoint.getY()+deltaY))
				,new DoublePoint((ePoint.getX()+deltaX),(ePoint.getY()+deltaY)));
		DoubleLine rightLine = new DoubleLine(new DoublePoint((sPoint.getX()-deltaX),(sPoint.getY()-deltaY))
				,new DoublePoint((ePoint.getX()-deltaX),(ePoint.getY()-deltaY)));
		return new DoubleLine[]{leftLine,rightLine};
	}

	private static DoublePoint[] offset(DoublePoint[] points,double distance){
		DoublePoint[] newPoints = new DoublePoint[points.length];
		DoublePoint pre = points[0];
		DoublePoint now = points[1];
		//
		return null;
	}
	public static DoublePolyline offset(DoublePolyline polyline,double distance){
		
		return null;
	}
	public static List<DoublePolyline> offset(List<DoublePolyline> polylines,double distance){
		
		return null;
	}
}
