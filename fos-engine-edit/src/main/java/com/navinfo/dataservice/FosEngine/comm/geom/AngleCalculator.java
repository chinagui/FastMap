package com.navinfo.dataservice.FosEngine.comm.geom;

import oracle.spatial.geometry.JGeometry;

import com.vividsolutions.jts.geom.LineSegment;

/**
 * 计算角度的帮助类
 */
public class AngleCalculator {
	/**
	 * 根据已知点A，距离和夹角求B点经纬度
	 * 
	 * @param A
	 *            已知点的经纬度
	 * @param distance
	 *            AB两地的距离 单位km
	 * @param angle
	 *            AB连线与正北方向的夹角（0~360）
	 * @return B点的经纬度
	 */
	public static LngLatPoint getMyLatLng(LngLatPoint A, double distance, double angle) {

		double dx = distance * 1000 * Math.sin(Math.toRadians(angle));
		double dy = distance * 1000 * Math.cos(Math.toRadians(angle));

		double bjd = (dx / A.Ed + A.m_RadLo) * 180. / Math.PI;
		double bwd = (dy / A.Ec + A.m_RadLa) * 180. / Math.PI;

		return new LngLatPoint(bjd, bwd);
	}

	/**
	 * 获取AB连线与正北方向的角度
	 * 
	 * @param A
	 *            A点的经纬度
	 * @param B
	 *            B点的经纬度
	 * @return AB连线与正北方向的角度
	 */
	public static double getAngle(LngLatPoint A, LngLatPoint B) {
		double dx = (B.m_RadLo - A.m_RadLo) * A.Ed;

		double dy = (B.m_RadLa - A.m_RadLa) * A.Ec;

		double angle = 0.0;

		angle = Math.atan(Math.abs(dx / dy)) * 180. / Math.PI;

		double dLo = B.m_Longitude - A.m_Longitude;

		double dLa = B.m_Latitude - A.m_Latitude;

		if (dLo > 0 && dLa <= 0) {
			angle = (90. - angle) + 90;
		} else if (dLo <= 0 && dLa < 0) {
			angle = angle + 180.;
		} else if (dLo < 0 && dLa >= 0) {
			angle = (90. - angle) + 270;
		}

		// double degree = angle*Math.PI/180.;
		//
		// DecimalFormat df =new DecimalFormat("#.000");
		//
		// degree = Double.valueOf(df.format(degree));

		return angle;
	}

	/**
	 * 求线和正北方向的夹角
	 * 
	 * @param nodePid
	 *            进入点的pid
	 * @param sNodePid
	 *            线的起点pid
	 * @param eNodePid
	 *            线的中点pid
	 * @param geom
	 *            线几何
	 * @return 线和正北方向夹角
	 */
	public static double getDisplayAngle(int nodePid, int sNodePid,
			int eNodePid, JGeometry geom) {
		double angle = 0.0;

		double[] points = geom.getOrdinatesArray();

		if (nodePid == sNodePid) {
			LngLatPoint startPoint = new LngLatPoint(points[0], points[1]);

			LngLatPoint endPoint = new LngLatPoint(points[2], points[3]);

			angle = getAngle(startPoint, endPoint);
		} else {
			int size = points.length;

			LngLatPoint startPoint = new LngLatPoint(points[size - 2],
					points[size - 1]);

			LngLatPoint endPoint = new LngLatPoint(points[size - 4], points[size - 3]);

			angle = getAngle(startPoint, endPoint);
		}

		return angle;
	}
	
	/**
	 * 以link1为正北方向，计算link2和link1的夹角
	 * @param link1
	 * @param link2
	 * @return
	 */
	public static double getAngle(LineSegment link1, LineSegment link2){
		
		double a1 = getAngle(new LngLatPoint(link1.p0.x, link1.p0.y),new LngLatPoint(link1.p1.x, link1.p1.y));
		
		double a2 = getAngle(new LngLatPoint(link2.p0.x, link2.p0.y),new LngLatPoint(link2.p1.x, link2.p1.y));
		
		if (a2>=a1){
			return Math.round(a2-a1); 
		}
		else{
			return Math.round(a2-a1+360);
		}
	}
	
	/**
	 * 计算大地距离的辅助类
	 */
	static class LngLatPoint {

		final static double Rc = 6378137;
		final static double Rj = 6356725;
		double m_LoDeg, m_LoMin, m_LoSec;
		double m_LaDeg, m_LaMin, m_LaSec;
		double m_Longitude, m_Latitude;
		double m_RadLo, m_RadLa;
		double Ec;
		double Ed;

		public LngLatPoint(double longitude, double latitude) {
			m_LoDeg = (int) longitude;
			m_LoMin = (int) ((longitude - m_LoDeg) * 60);
			m_LoSec = (longitude - m_LoDeg - m_LoMin / 60.) * 3600;

			m_LaDeg = (int) latitude;
			m_LaMin = (int) ((latitude - m_LaDeg) * 60);
			m_LaSec = (latitude - m_LaDeg - m_LaMin / 60.) * 3600;

			m_Longitude = longitude;
			m_Latitude = latitude;
			m_RadLo = longitude * Math.PI / 180.;
			m_RadLa = latitude * Math.PI / 180.;
			Ec = Rj + (Rc - Rj) * (90. - m_Latitude) / 90.;
			Ed = Ec * Math.cos(m_RadLa);
		}

	}


	public static void main(String[] args) {
		LngLatPoint A = new LngLatPoint(113.249648, 23.401553);
		LngLatPoint B = new LngLatPoint(113.246033, 23.403362);
		System.out.println(getAngle(A, B));
	}
}
