package com.navinfo.dataservice.engine.edit;  
import java.util.ArrayList; 
import java.util.List; 
/**  
* @Description:TODO
计算多边形的面积根据网上的地理测绘面积服务算法改写
* @package com.test  
* @version v1.0 2012-10-10 
*/  

public class PolygonArea { 
	// public static readonly double WGS84LongAxle = 6378137; //WGS84地球椭球体的长半轴
	 //public static readonly double WGS84LongAxle = 6378137; //WGS84地球椭球体的长半轴

     //public static readonly double WGS84ShortAxle = 6356752.3142;
     //public static readonly double WGS84ShortAxle = 6356752.3142;
	private static double earthRadiusMeters =6367444.0; //源码中使用半径为6367460.0; 
	private double metersPerDegree = 2.0 * Math.PI * earthRadiusMeters / 360.0; 
	private double radiansPerDegree = Math.PI / 180.0; 
	private double degreesPerRadian = 180.0 / Math.PI;
	private void calculateArea(List<double[]> points) { 
		if (points.size() > 2) {  
			double areaMeters2 = PlanarPolygonAreaMeters2(points); 
			System.out.println(areaMeters2);
			if (areaMeters2 > 1000000.0) areaMeters2 = SphericalPolygonAreaMeters2(points); 
			System.out.println("面积为："+areaMeters2+"（平方米）"); 
		} 
} 
/** 
* @Description:TODO
球面多边形面积计算
* @param points 
* @return
*/ 
private double SphericalPolygonAreaMeters2(List<double[]> points) { 
	double totalAngle = 0.0;  
	for (int i = 0; i < points.size(); ++i) { 
			int j = (i + 1) % points.size(); 
			int k = (i + 2) % points.size();  
			totalAngle += Angle(points.get(i), points.get(j), points.get(k)); 
		} 
	double planarTotalAngle = (points.size() - 2) * 180.0; 
	double sphericalExcess = totalAngle - planarTotalAngle; 
	if (sphericalExcess > 420.0) { 
		totalAngle = points.size() * 360.0 - totalAngle; 
		sphericalExcess = totalAngle - planarTotalAngle; 
	}else if (sphericalExcess > 300.0 && sphericalExcess < 420.0){
		sphericalExcess = Math.abs(360.0 - sphericalExcess);
	} 
	System.out.println(sphericalExcess+":sphericalExcess");
	System.out.println(radiansPerDegree * earthRadiusMeters * earthRadiusMeters);
	return sphericalExcess * radiansPerDegree * earthRadiusMeters * earthRadiusMeters; 
} 

/** 

* @Description:TODO

角度

* @param p1 

* @param p2 


* @param p3 

* @return 

*/ 

private double Angle(double[] p1, double []p2, double[] p3) { 
	double bearing21 = Bearing(p2, p1); 
	double bearing23 = Bearing(p2, p3); 
	double angle = bearing21 - bearing23; 
	if (angle < 0.0) angle += 360.0; 
	return angle; 
}

/** 

* @Description:TODO

方向


* @param from 

* @param to 

* @return 

*/ 

 

private double Bearing(double []from, double[] to) { 
	double lat1 = from[1] * radiansPerDegree; 
	double lon1 = from[0] * radiansPerDegree; 
	double lat2 = to[1] * radiansPerDegree; 
	double lon2 = to[0] * radiansPerDegree; 
	double angle = -Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2), Math.cos(lat1) * 
	Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2)); 
	if (angle < 0.0) angle += Math.PI * 2.0; 
	angle = angle * degreesPerRadian; 
	return angle; 
} 

/** 

* @Description:TODO

平面多边形面积

* @param points double[0] longitude; 


double[1] latitude 

* @return 


*/ 

private double PlanarPolygonAreaMeters2(List<double[]> points) { 

	double a = 0.0;  
	for (int i = 0; i < points.size(); ++i) { 
		int j = (i + 1) % points.size();  
		double xi = points.get(i)[0] * metersPerDegree * Math.cos(points.get(i)[1] * 
		radiansPerDegree);  
		double yi = points.get(i)[1] * metersPerDegree;  
		double xj = points.get(j)[0] * metersPerDegree * Math.cos(points.get(j)[1] * 
		radiansPerDegree);  
		double yj = points.get(j)[1] * metersPerDegree; 
	a += xi * yj - xj * yi; 
	}  
	return Math.abs(a / 2.0); 
} 
public static void main(String[] args) { 
	List<double[]> points = new ArrayList<double[]>();
	//[5] POLYGON ((116.20670 39.96820, 116.20487 39.96644, 116.20495 39.96637, 116.20702 39.96634, 116.20670 39.96820))
	//String s = "116.20670,39.96820;116.20487,39.96644;116.20495,39.96637;116.20702,39.96634;116.20670,39.96820";
	//String s =  "116.17659,39.97508;116.16144,39.94844;116.20427,39.94322;116.17659,39.97508";
	String s =   "112.52931,37.86889;112.51706,37.86058;112.52099,37.84985;112.54137,37.85125;112.53511,37.85869"; 
	String[] s1 = s.split(";"); 
	for(String ss : s1 ) { 
		String[] temp = ss.split(","); 
		double[] point = {Double.parseDouble(temp[0]) , Double.parseDouble(temp[1])}; 
		points.add(point); 
		System.out.println(temp[1]+","+temp[0]); 
	} 
	System.out.println(points);
	PolygonArea tp = new PolygonArea(); 
	tp.calculateArea(points);
 } 

}  