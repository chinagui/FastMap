package com.navinfo.dataservice.commons.geom;

import oracle.spatial.geometry.JGeometry;

import com.vividsolutions.jts.geom.Coordinate;
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
	public static LngLatPoint getMyLatLng(LngLatPoint A, double distance,
			double angle) {

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

			LngLatPoint endPoint = new LngLatPoint(points[size - 4],
					points[size - 3]);

			angle = getAngle(startPoint, endPoint);
		}

		return angle;
	}

	/**
	 * 以link1为正北方向，计算link2和link1的夹角
	 * 
	 * @param link1
	 * @param link2
	 * @return
	 */
	public static double getAngle(LineSegment link1, LineSegment link2) {

		double a1 = getAngle(new LngLatPoint(link1.p0.x, link1.p0.y),
				new LngLatPoint(link1.p1.x, link1.p1.y));

		double a2 = getAngle(new LngLatPoint(link2.p0.x, link2.p0.y),
				new LngLatPoint(link2.p1.x, link2.p1.y));

		if (a2 >= a1) {
			return Math.round(a2 - a1);
		} else {
			return Math.round(a2 - a1 + 360);
		}
	}

	/**
	 * 以link1为正北方向，计算link2和link1的夹角
	 * 
	 * @param link1
	 * @param link2
	 * @return
	 */
	public static double getnMinAngle(LineSegment link1, LineSegment link2) {

		double a1 = getAngle(new LngLatPoint(link1.p0.x, link1.p0.y),
				new LngLatPoint(link1.p1.x, link1.p1.y));

		double a2 = getAngle(new LngLatPoint(link2.p0.x, link2.p0.y),
				new LngLatPoint(link2.p1.x, link2.p1.y));

		return Math.abs(a2 - a1);
	}

	/**
	 * 计算两个线段之间的夹角
	 * 
	 * @param preLink
	 * @param nextLink
	 * @return 两线段 挂接返回两线段的夹角，不挂接返回9999；
	 */
	public static double getConnectLinksAngle(LineSegment preLink,
			LineSegment nextLink) {

		Coordinate preS = preLink.getCoordinate(0);
		Coordinate preE = preLink.getCoordinate(1);

		Coordinate nextS = nextLink.getCoordinate(0);
		Coordinate nextE = nextLink.getCoordinate(1);

		double rad = 0;

		if (preS.equals(nextS)) {

			rad = radianOfCounterclockwise(preE, preS, nextE);

		} else if (preS.equals(nextE)) {

			rad = radianOfCounterclockwise(preE, preS, nextS);

		} else if (preE.equals(nextS)) {

			rad = radianOfCounterclockwise(preS, preE, nextE);

		} else if (preE.equals(nextE)) {

			rad = radianOfCounterclockwise(preS, preE, nextS);

		} else {
			// 两线段不连接
			return 9999;
		}

		double angle = (rad * 180) / Math.PI;

		if (angle > 180)
			angle = 360 - angle;

		return angle;
	}

	/**
	 * 计算3点之间弧度
	 * 
	 * @param shapePoint1
	 * @param nodePoint
	 * @param shapePoint3
	 * @return
	 */
	private static double radianOfCounterclockwise(Coordinate shapePoint1,
			Coordinate nodePoint, Coordinate shapePoint3) {

		double PI = Math.PI;

		double dy1 = 0.0, dy2 = 0.0, dx1 = 0.0, dx2 = 0.0, A1 = 0.0, A2 = 0.0, A = 0.0;

		Coordinate point2 = new Coordinate(nodePoint.x, nodePoint.y, 0);

		dy1 = (double) point2.y - (double) shapePoint1.y;

		dy2 = (double) shapePoint3.y - (double) point2.y;

		if (point2.x != shapePoint1.x) {
			dx1 = (double) point2.x - (double) shapePoint1.x;

			A1 = Math.atan(dy1 / dx1);
		} else {
			A1 = PI / 2;
		}

		if (point2.x != shapePoint3.x) {
			dx2 = (double) shapePoint3.x - (double) point2.x;

			A2 = Math.atan(dy2 / dx2);
		} else {
			A2 = PI / 2;
		}

		// 判断A1的角度
		if ((A1 >= 0) && (0 >= dy1) && (0 >= dx1)) {
			A1 += 0; // 第一象限
		} else if ((A1 > 0) && (dy1 >= 0) && (dx1 >= 0)) {
			A1 += PI; // 第三象限
		} else if ((0 >= A1) && (0 >= dy1) && (dx1 > 0)) {
			A1 += PI; // 第二象限
		} else if ((A1 < 0) && (dy1 >= 0) && (dx1 < 0)) {
			A1 += 2 * PI; // 第四象限
		}

		// 判断A2的角度
		if ((A2 >= 0) && (0 >= dy2) && (0 >= dx2)) {
			A2 += PI; // 第三象限
		} else if ((A2 > 0) && (dy2 >= 0) && (dx2 >= 0)) {
			A2 += 0; // 第一象限
		} else if ((0 >= A2) && (0 >= dy2) && (dx2 > 0)) {
			A2 += 2 * PI; // 第四象限
		} else if ((A2 < 0) && (dy2 >= 0) && (dx2 < 0)) {
			A2 += PI; // 第二象限
		}

		// 求夹角
		if (A1 == 0) {
			A = A2;
		} else if (A1 > A2) // && A2 >= 0)
		{
			A = 2 * PI + A2 - A1;
		} else if (A2 > A1) {
			A = A2 - A1;
		} else if (A2 == A1) {
			A = 2 * PI;
		}
		return A;
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
