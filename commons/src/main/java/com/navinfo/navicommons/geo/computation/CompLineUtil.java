package com.navinfo.navicommons.geo.computation;

import com.navinfo.dataservice.commons.util.DoubleUtil;

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
	/**
	 * 获取两条线段或者线段延长线的交点
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static DoublePoint LineExtIntersect(DoubleLine line1,DoubleLine line2){
		double k1 = line1.getSlope();
		double k2 = line2.getSlope();
		if(DoubleUtil.equalsBigDecimal(k1,k2)){//斜率相同
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
			
			return new DoublePoint(DoubleUtil.keepSpecDecimal(x),DoubleUtil.keepSpecDecimal(y));

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
		DoubleLine leftLine = new DoubleLine(new DoublePoint(DoubleUtil.keepSpecDecimal(s.getX()+deltaX),DoubleUtil.keepSpecDecimal(s.getY()+deltaY))
				,new DoublePoint(DoubleUtil.keepSpecDecimal(e.getX()+deltaX),DoubleUtil.keepSpecDecimal(e.getY()+deltaY)));
		if(isTwoWay){
			DoubleLine rightLine = new DoubleLine(new DoublePoint(DoubleUtil.keepSpecDecimal(s.getX()-deltaX),DoubleUtil.keepSpecDecimal(s.getY()-deltaY))
					,new DoublePoint(DoubleUtil.keepSpecDecimal(e.getX()-deltaX),DoubleUtil.keepSpecDecimal(e.getY()-deltaY)));
			return new DoubleLine[]{leftLine,rightLine};
		}else{
			return new DoubleLine[]{leftLine};
		}
	}
	public static boolean isRightSide(DoubleLine startLine,DoubleLine endLine,DoubleLine adjacentLine){
		if(angleAnticlockwise(startLine,endLine)>=angleAnticlockwise(startLine,adjacentLine)) return true;
		return false;
	}
	/**
	 * 计算point是否在有向线段line的右侧（包含在线上情况）
	 * 算法：line由p0p1组成，point为p2，构造(p2 - p0) 和 (p1 - p0)向量，判断
	 * @param line
	 * @param point
	 * @return
	 */
	public static boolean isRightSide(DoubleLine line,DoublePoint point){
		DoublePoint p20 = CompPointUtil.minus(point, line.getSpoint());
		DoublePoint p10 = CompPointUtil.minus(line.getEpoint(), line.getSpoint());
		double crossValue = CompPointUtil.cross(p20, p10);
		if(crossValue>=0){
			return true;
		}
		return false;
	}
	/**
	 * 两条线段间夹角，0~PI
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static double angleNoDirection(DoubleLine line1,DoubleLine line2){
		DoublePoint p1 = line1.pan2OriginPoint();
		DoublePoint p2 = line2.pan2OriginPoint();
		return CompPointUtil.angle(p1, p2);
	}
	/**
	 * 有向线段line1到line2逆时针方向的角度
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static double angleAnticlockwise(DoubleLine line1,DoubleLine line2){
		DoublePoint p1 = line1.pan2OriginPoint();
		DoublePoint p2 = line2.pan2OriginPoint();
		if(isRightSide(line1, line2.getEpoint())){
			return Math.PI-CompPointUtil.angle(p1, p2);
		}else{
			return Math.PI+CompPointUtil.angle(p1, p2);
		}
	}
	public static boolean intersectant(DoubleLine l1,DoubleLine l2){
		return LongLineUtil.intersectant(MyGeoConvertor.degree2Millisec(l1)
				,MyGeoConvertor.degree2Millisec(l2));
	}
}
