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
	public static DoublePoint LineExtIntersect(DoubleLine line1,DoubleLine line2){
		double slope1 = line1.getSlope();
		double slope2 = line2.getSlope();
		if(DoubleUtil.equals(slope1,slope2)){//斜率相同
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
			double k1 = (line1.getEpoint().getY()-line1.getSpoint().getY())/(line1.getEpoint().getX()-line1.getSpoint().getX());
			double b1 = (line1.getEpoint().getX()*line1.getSpoint().getY()-line1.getSpoint().getX()*line1.getEpoint().getY())/(line1.getEpoint().getX()-line1.getSpoint().getX());

			double k2 = (line2.getEpoint().getY()-line2.getSpoint().getY())/(line2.getEpoint().getX()-line2.getSpoint().getX());
			double b2 = (line2.getEpoint().getX()*line2.getSpoint().getY()-line2.getSpoint().getX()*line2.getEpoint().getY())/(line2.getEpoint().getX()-line2.getSpoint().getX());
			
			double x = (b2-b1)/(k1-k2);
			double y=(k1*b2-k2*b1)/(k1-k2);
			
			return new DoublePoint(DoubleUtil.keep5Decimal(x),DoubleUtil.keep5Decimal(y));

		}
	}
	/**
	 * 
	 * @param line:lat/lat,degree
	 * @param distance:positive for left or negative for right meters
	 * @return
	 */
	public static DoubleLine offset(DoubleLine line,double distance){
		DoublePoint sPoint = line.getSpoint();
		DoublePoint ePoint = line.getEpoint();
		double lineEucLength = getEucLength(sPoint,ePoint);
		double degreeDist = GeometryUtils.convert2Degree(distance);
		double deltaX = 0;
		double deltaY = 0;
		if(sPoint.getX()==ePoint.getX()){//垂直线，offset只要平移X轴坐标
			deltaX = degreeDist;
		}else if(sPoint.getY()==ePoint.getY()){//水平线
			deltaY = degreeDist;
		}else{
			/**
			//A为line作为向量时与X轴正方向夹角弧度
			//deltaX = degreeDist*cos(A+pi/2) = degreeDist*(-sin(A))
			//deltaY = degreeDist*sin(A+pi/2) = degreeDist*cos(A)
			//sin(A) = (ePoint.y-sPoint.y)/lineEucLength
			//cos(A) = (ePoint.x-sPoint.x)/lineEncLength
			 * */
			deltaX = degreeDist*(-(ePoint.getY()-sPoint.getY())/lineEucLength);
			deltaY = degreeDist*(ePoint.getX()-sPoint.getX())/lineEucLength;
		}
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
		DoublePoint sPoint = line.getSpoint();
		DoublePoint ePoint = line.getEpoint();
		double lineEucLength = getEucLength(sPoint,ePoint);
		double degreeDist = GeometryUtils.convert2Degree(distance);
		double deltaX = 0;
		double deltaY = 0;
		if(sPoint.getX()==ePoint.getX()){//垂直线，offset只要平移X轴坐标
			deltaX = degreeDist;
		}else if(sPoint.getY()==ePoint.getY()){//水平线
			deltaY = degreeDist;
		}else{
			/**
			//A为line作为向量时与X轴正方向夹角弧度
			//deltaX = degreeDist*cos(A+pi/2) = degreeDist*(-sin(A))
			//deltaY = degreeDist*sin(A+pi/2) = degreeDist*cos(A)
			//sin(A) = (ePoint.y-sPoint.y)/lineEucLength
			//cos(A) = (ePoint.x-sPoint.x)/lineEncLength
			 * */
			deltaX = degreeDist*(-(ePoint.getY()-sPoint.getY())/lineEucLength);
			deltaY = degreeDist*(ePoint.getX()-sPoint.getX())/lineEucLength;
		}
		DoubleLine leftLine = new DoubleLine(new DoublePoint((sPoint.getX()+deltaX),(sPoint.getY()+deltaY))
				,new DoublePoint((ePoint.getX()+deltaX),(ePoint.getY()+deltaY)));
		DoubleLine rightLine = new DoubleLine(new DoublePoint((sPoint.getX()-deltaX),(sPoint.getY()-deltaY))
				,new DoublePoint((ePoint.getX()-deltaX),(ePoint.getY()-deltaY)));
		return new DoubleLine[]{leftLine,rightLine};
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
			DoublePolyline[] results = new DoublePolyline[linesLen*2];
			for(int i=0;i<linesLen;i++){
				DoublePolyline polyline = polylines[i];
				int lineLen = polyline.getLineSize();
//				DoublePoint[] leftPolylinePoints= new
				
			}
		}
		return null;
	}
}
