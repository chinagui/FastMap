package com.navinfo.dataservice.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.navicommons.geo.computation.DoubleLine;
import com.navinfo.navicommons.geo.computation.DoublePoint;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.JtsGeometryConvertor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import oracle.spatial.geometry.JGeometry;

public class DisplayUtils {

	private static final GeometryFactory geometryFactory = new GeometryFactory();

	public static int kind2Color(int kind) {
		if (kind == 13) {
			return 13;
		} else if (kind == 15) {
			return 14;
		} else {
			return kind + 1;
		}
	}

	// 获取按照道路通行方向取四分之一位置
	public static double[] get1_4Point(JGeometry geom, int direct) {

		double linkPoints[][] = getLinkPoints(geom, direct);

		double linkLength = getLinkLength(linkPoints);

		double len1_4 = linkLength / 4;

		double[] point = lookForRatioPoint(linkPoints, len1_4);

		return point;

	}

	/**
	 * 按比例获取道路通行方向的点位坐标。n为小数，小于等于0按0.1处理，大于1按1处理。
	 * 
	 * @param geom
	 * @param direct
	 * @param nValue
	 * @return
	 */
	public static double[] getRatioPointForLink(JGeometry geom, int direct, double ratioValue) {

		if (ratioValue <= 0) {

			ratioValue = 0.1;
		}
		if (ratioValue > 1) {

			ratioValue = 1;
		}

		double linkPoints[][] = getLinkPoints(geom, direct);

		double linkLength = getLinkLength(linkPoints);

		double lenRefer = linkLength * ratioValue;

		double[] point = lookForRatioPoint(linkPoints, lenRefer);

		return point;

	}

	private static double[][] getLinkPoints(JGeometry geom, int direct) {

		double[] ps = geom.getOrdinatesArray();

		double[][] points = new double[ps.length / 2][];

		int num = 0;

		if (direct == 1 || direct == 2) {
			for (int i = 0; i < ps.length; i += 2) {

				double lng = ps[i];

				double lat = ps[i + 1];

				double lngMer = MercatorProjection.longitudeToMetersX(lng);

				double latMer = MercatorProjection.latitudeToMetersY(lat);

				points[num++] = new double[] { lngMer, latMer };
			}
		} else {
			for (int i = ps.length - 1; i >= 0; i -= 2) {

				double lat = ps[i];

				double lng = ps[i - 1];

				double lngMer = MercatorProjection.longitudeToMetersX(lng);

				double latMer = MercatorProjection.latitudeToMetersY(lat);

				points[num++] = new double[] { lngMer, latMer };
			}
		}

		return points;

	}

	private static double[] lookForRatioPoint(double[][] points, double lenRefer) {
		double point[] = new double[2];

		for (int i = 1; i < points.length; i++) {
			double prePoint[] = points[i - 1];

			double currentPoint[] = points[i];

			double len = Math
					.sqrt(Math.pow(currentPoint[0] - prePoint[0], 2) + Math.pow(currentPoint[1] - prePoint[1], 2));

			if (len >= lenRefer) {

				point[0] = prePoint[0] + lenRefer / len * (currentPoint[0] - prePoint[0]);

				point[1] = prePoint[1] + lenRefer / len * (currentPoint[1] - prePoint[1]);

				break;
			}

			lenRefer -= len;
		}

		point[0] = MercatorProjection.metersXToLongitude(point[0]);

		point[1] = MercatorProjection.metersYToLatitude(point[1]);

		return point;
	}

	public static double[][] getTipsPointPos(String linkWkt, String pointWkt, int seqNum) throws Exception {

		return getLinkPointPos(linkWkt, pointWkt, 3, seqNum, 3, 5, 6);
	}

	public static double[][] getGdbPointPos(String linkWkt, String pointWkt, int seqNum) throws Exception {
		return getLinkPointPos(linkWkt, pointWkt, 3, seqNum, 4.5, 4, 3);
	}

	public static double[][] getGdbPointPos(String linkWkt, String pointWkt, int seqNum, double base, int veritUnit) throws Exception {
		return getLinkPointPos(linkWkt, pointWkt, 3, seqNum, base, 4, veritUnit);
	}

	/**
	 * @param linkWkt
	 *            进入线几何
	 * @param pointWkt
	 *            进入点几何
	 * @param totalCnt
	 *            总共要素类型个数
	 * @param seqNum
	 *            要素类型序号
	 * @param base
	 *            基础距离
	 * @param unit
	 *            递增距离
	 * @param vertiUnit
	 *            线垂直方向移动距离
	 * @return 显示点位
	 * @throws Exception
	 */
	private static double[][] getLinkPointPos(String linkWkt, String pointWkt, int totalCnt, int seqNum, double base,
			double unit, double vertiUnit) throws Exception {
		double[][] position = new double[2][2];

		/*
		 * 1、对线、点进行墨卡托数组转 2、根据点所处线的位置，转换线数组顺序 3、求出线的长度，判断按照线上挂的tips个数，是否超过线的长度
		 * 4、如果未超出，则按照3米为单位，向线通行方向的逆向扩展 5、如果超出，则按照线长度与tips个数比例，向线通行方向的逆向扩展
		 * 6、找出seqNum * 3米（或者新比例值）位置，作为引导坐标位置 7、按照引导坐标位置和线通行方向向右找6米位置作为显示坐标位置
		 * 8、转换墨卡托坐标为经纬度坐标返回
		 */

		// 默认是3米，如果按照tips个数 * 3 米超出了LINK的长度，则重新计算这个值

		// 1、对线、点进行墨卡托数组转
		double[][] linkMerArray = convertLinkToMerArray(linkWkt);

		double[] pointMerArray = convertPointToMerArray(pointWkt);

		// 2、根据点所处线的位置，转换线数组顺序
		isReverseLinkOrder(linkMerArray, pointMerArray);

		// 3、求出线的长度，判断按照线上挂的tips个数，是否超过线的长度
		double linkLength = GeometryUtils.getLinkLength(linkWkt);

		boolean isExceedLink = false;

		double total = base + unit * (totalCnt - 1);

		if (total > linkLength) {
			isExceedLink = true;
		}

		// 5、如果超出，则按照线长度与tips个数比例，向线通行方向的逆向扩展
		if (isExceedLink) {
			unit = linkLength / (totalCnt + 1);
		}

		// 6、找出seqNum * 3米（或者新比例值）位置，作为引导坐标位置
		double[] guidePosition = new double[2];

		double guidePointDistance = base + unit * seqNum;

		if (guidePointDistance > linkLength) {

			guidePointDistance = unit * seqNum;
		}

		if (guidePointDistance > linkLength) {

			guidePointDistance = linkLength;
		}

		// 返回值为引导坐标所处的LINK形状段上的第几段，从0开始
		int guideSeqNum = getGuidePosition(linkMerArray, guidePointDistance, guidePosition);

		// 按照引导坐标位置和线通行方向向右找6米位置作为显示坐标位置
		double[] displayPosition = getDisplayPosition(linkMerArray, guidePosition, guideSeqNum, vertiUnit);

		// 转换墨卡托坐标为经纬度坐标返回

		guidePosition[0] = MercatorProjection.metersXToLongitude(guidePosition[0]);

		guidePosition[1] = MercatorProjection.metersYToLatitude(guidePosition[1]);

		position[0] = guidePosition;

		displayPosition[0] = MercatorProjection.metersXToLongitude(displayPosition[0]);

		displayPosition[1] = MercatorProjection.metersYToLatitude(displayPosition[1]);

		position[1] = displayPosition;

		return position;
	}

	// 转换线经纬度wkt为以米为单位的二维数组
	public static double[][] convertLinkToMerArray(String linkWkt) throws Exception {

		WKTReader reader = new WKTReader();

		Geometry geom = reader.read(linkWkt);

		return convertLinkToMerArray(geom);

	}

	// 转换线经纬度wkt为以米为单位的二维数组
	public static double[][] convertLinkToMerArray(Geometry geom) throws Exception {

		Coordinate[] cs = geom.getCoordinates();

		double[][] linkMerArray = new double[cs.length][2];

		int num = 0;

		for (Coordinate c : cs) {
			double[] p = new double[2];

			// 此处需要进行对经纬度转换为墨卡托值
			p[0] = MercatorProjection.longitudeToMetersX(c.x);

			p[1] = MercatorProjection.latitudeToMetersY(c.y);

			linkMerArray[num++] = p;
		}

		return linkMerArray;

	}

	// 转换点经纬度wkt为以米为单位的数组
	private static double[] convertPointToMerArray(String pointWkt) throws Exception {

		double[] pointMerArray = new double[2];

		WKTReader reader = new WKTReader();

		Geometry geom = reader.read(pointWkt);

		// 此处需要进行对经纬度转换为墨卡托值
		pointMerArray[0] = geom.getCentroid().getX();

		pointMerArray[1] = geom.getCentroid().getY();

		pointMerArray[0] = MercatorProjection.longitudeToMetersX(pointMerArray[0]);

		pointMerArray[1] = MercatorProjection.latitudeToMetersY(pointMerArray[1]);

		return pointMerArray;
	}

	// 根据点所处线的位置，转换线数组顺序
	private static void isReverseLinkOrder(double[][] linkMerArray, double[] pointMerArray) {

		if (linkMerArray[0][0] != pointMerArray[0] || linkMerArray[0][1] != pointMerArray[1]) {
			int lenLinkArray = linkMerArray.length;

			int len = lenLinkArray / 2;

			double[] temp = null;

			for (int i = 0; i < len; i++) {
				temp = linkMerArray[i];
				linkMerArray[i] = linkMerArray[lenLinkArray - 1 - i];
				linkMerArray[lenLinkArray - 1 - i] = temp;
			}
		}
	}

	// 求出线的长度
	public static double getLinkLength(double[][] linkMerArray) {

		double length = 0;

		for (int i = 0; i <= linkMerArray.length - 2; i++) {

			double[] curPoint = linkMerArray[i];

			double[] nextPoint = linkMerArray[i + 1];

			length += Math.sqrt(Math.pow(nextPoint[0] - curPoint[0], 2) + Math.pow(nextPoint[1] - curPoint[1], 2));
		}

		length = Math.round(length * 100) / 100.0;

		return length;
	}

	// 求出引导坐标位置
	/**
	 * 
	 * @param linkMerArray
	 * @param unit
	 * @param seqNum
	 * @return 返回值为引导坐标所处的LINK形状段上的第几段，从0开始
	 */
	private static int getGuidePosition(double[][] linkMerArray, double guidePointDistance, double[] guidePosition) {

		int guideSeqNum = 0;

		for (int i = 0; i <= linkMerArray.length - 2; i++) {

			double[] curPoint = linkMerArray[i];

			double[] nextPoint = linkMerArray[i + 1];

			guideSeqNum = i;

			double ppDistance = Math
					.sqrt(Math.pow(nextPoint[0] - curPoint[0], 2) + Math.pow(nextPoint[1] - curPoint[1], 2));

			if (ppDistance > guidePointDistance) {

				if (curPoint[0] != nextPoint[0]) {
					double k = (curPoint[1] - nextPoint[1]) / (curPoint[0] - nextPoint[0]);

					double c = curPoint[1] - k * curPoint[0];

					if (curPoint[0] < nextPoint[0]) {
						// 递增
						guidePosition[0] = curPoint[0]
								+ (guidePointDistance / ppDistance) * (nextPoint[0] - curPoint[0]);

					} else {
						// 递减
						guidePosition[0] = curPoint[0]
								+ (guidePointDistance / ppDistance) * (nextPoint[0] - curPoint[0]);
					}

					guidePosition[1] = k * guidePosition[0] + c;
				} else {
					// 与Y轴垂直
					guidePosition[0] = curPoint[0];

					if (curPoint[1] < nextPoint[1]) {
						guidePosition[1] = curPoint[1] + guidePointDistance;
					} else {
						guidePosition[1] = curPoint[1] - guidePointDistance;
					}
				}

				break;
			} else {
				guidePointDistance -= ppDistance;
			}
		}

		return guideSeqNum;
	}

	// 按照引导坐标位置和线通行方向向右找vertiUnit=6米位置作为显示坐标位置
	public static double[] getDisplayPosition(double[][] linkMerArray, double[] guidePosition, int guideSeqNum,
			double vertiUnit) {

		double[] displayPosition = new double[2];

		double[] startPoint = linkMerArray[guideSeqNum];

		double[] stopPoint = linkMerArray[guideSeqNum + 1];

		if (startPoint[0] != stopPoint[0]) {
			// 不与Y轴垂直

			double k = (startPoint[1] - stopPoint[1]) / (startPoint[0] - stopPoint[0]);

			if (k != 0) {
				// 不与X轴平行
				double k1 = -1 / k;
				double c1 = guidePosition[1] - (guidePosition[0] * k1);
				double x1 = guidePosition[0] + (vertiUnit / Math.sqrt(1 + k1 * k1));
				double y1 = k1 * x1 + c1;
				double x2 = guidePosition[0] - (vertiUnit / Math.sqrt(1 + k1 * k1));
				double y2 = k1 * x2 + c1;
				if (k > 0) {
					if (startPoint[0] < stopPoint[0]) {
						displayPosition[0] = x2;

						displayPosition[1] = y2;
					} else {
						displayPosition[0] = x1;

						displayPosition[1] = y1;
					}
				} else {
					if (startPoint[0] < stopPoint[0]) {
						displayPosition[0] = x1;

						displayPosition[1] = y1;
					} else {
						displayPosition[0] = x2;

						displayPosition[1] = y2;
					}
				}
			} else {
				// 与X轴平行

				if (startPoint[0] < stopPoint[0]) {

					displayPosition[0] = guidePosition[0];

					displayPosition[1] = guidePosition[1] + vertiUnit;
				} else {
					displayPosition[0] = guidePosition[0];

					displayPosition[1] = guidePosition[1] - vertiUnit;
				}
			}

		} else {
			// 与Y轴平行

			if (startPoint[1] < stopPoint[1]) {

				displayPosition[0] = guidePosition[0] - vertiUnit;

				displayPosition[1] = guidePosition[1];
			} else {
				displayPosition[0] = guidePosition[0] + vertiUnit;

				displayPosition[1] = guidePosition[1];
			}
		}

		return displayPosition;
	}

	/*********************************************************************/

	// 获取路口主点挂接LINK角平分线30米位置点
	public static double[] getCrossPoint(String inLinkWkt, String outLinkWkt, String pointWkt) throws Exception {
		double[] point = new double[2];

		WKTReader reader = new WKTReader();

		Geometry inLink = reader.read(inLinkWkt);

		Geometry outLink = reader.read(outLinkWkt);

		Geometry inPoint = reader.read(pointWkt);

		double[] psInLink = new double[4];

		double[] psOutLink = new double[4];

		fillInOutPoints(inLink, outLink, inPoint, psInLink, psOutLink);

		double[] ps = new double[2];

		ps[0] = MercatorProjection.longitudeToMetersX(inPoint.getCoordinate().x);

		ps[1] = MercatorProjection.latitudeToMetersY(inPoint.getCoordinate().y);

		// 交换进入线点位置
		double temp = psInLink[0];

		psInLink[0] = psInLink[2];

		psInLink[2] = temp;

		temp = psInLink[1];

		psInLink[1] = psInLink[3];

		psInLink[3] = temp;

		double[] inLinkPoint = getDistance100mPoint(psInLink);

		double[] outLinkPoint = getDistance100mPoint(psOutLink);

		double midX = (inLinkPoint[0] + outLinkPoint[0]) / 2;

		double midY = (inLinkPoint[1] + outLinkPoint[1]) / 2;

		if (midX != ps[0]) {
			double k = (midY - ps[1]) / (midX - ps[0]);

			double c = ps[1] - k * ps[0];

			if (ps[0] < midX) {
				point[0] = ps[0] + 30;

			} else {
				point[0] = ps[0] - 30;
			}

			point[1] = k * point[0] + c;
		} else {
			point[0] = ps[0];

			if (ps[1] < midY) {
				point[1] = ps[1] + 30;
			} else {
				point[1] = ps[1] - 30;
			}
		}

		point[0] = MercatorProjection.metersXToLongitude(point[0]);

		point[1] = MercatorProjection.metersYToLatitude(point[1]);

		return point;
	}

	private static double[] getDistance100mPoint(double link[]) {
		double[] p = new double[2];

		if (link[0] != link[2]) {
			double k = (link[3] - link[1]) / (link[2] - link[0]);

			double c = link[1] - k * link[0];

			if (link[0] < link[2]) {
				p[0] = link[0] + 100;
			} else {
				p[0] = link[0] - 100;
			}

			p[1] = k * p[0] + c;
		} else {
			p[0] = link[0];

			if (link[1] < link[3]) {
				p[1] = link[1] + 100;
			} else {
				p[1] = link[1] - 100;
			}
		}

		return p;
	}

	private static void fillInOutPoints(Geometry inLink, Geometry outLink, Geometry ps, double[] psInLink,
			double[] psOutLink) {

		Coordinate[] csIn = inLink.getCoordinates();

		Coordinate[] csOut = outLink.getCoordinates();

		Point point = (Point) ps;

		if (csIn[0].x == point.getX() && csIn[0].y == point.getY()) {

			psInLink[0] = csIn[1].x;

			psInLink[1] = csIn[1].y;

			psInLink[2] = csIn[0].x;

			psInLink[3] = csIn[0].y;

			if (csOut[0].x == point.getX() && csOut[0].y == point.getY()) {

				psOutLink[0] = csOut[0].x;

				psOutLink[1] = csOut[0].y;

				psOutLink[2] = csOut[1].x;

				psOutLink[3] = csOut[1].y;
			} else {
				int len = csOut.length;

				psOutLink[0] = csOut[len - 1].x;

				psOutLink[1] = csOut[len - 1].y;

				psOutLink[2] = csOut[len - 2].x;

				psOutLink[3] = csOut[len - 2].y;
			}

		} else {

			int len = csIn.length;

			psInLink[0] = csIn[len - 2].x;

			psInLink[1] = csIn[len - 2].y;

			psInLink[2] = csIn[len - 1].x;

			psInLink[3] = csIn[len - 1].y;

			if (csOut[0].x == point.getX() && csOut[0].y == point.getY()) {

				psOutLink[0] = csOut[0].x;

				psOutLink[1] = csOut[0].y;

				psOutLink[2] = csOut[1].x;

				psOutLink[3] = csOut[1].y;
			} else {
				len = csOut.length;

				psOutLink[0] = csOut[len - 1].x;

				psOutLink[1] = csOut[len - 1].y;

				psOutLink[2] = csOut[len - 2].x;

				psOutLink[3] = csOut[len - 2].y;
			}
		}

		psInLink[0] = MercatorProjection.longitudeToMetersX(psInLink[0]);

		psInLink[1] = MercatorProjection.latitudeToMetersY(psInLink[1]);

		psInLink[2] = MercatorProjection.longitudeToMetersX(psInLink[2]);

		psInLink[3] = MercatorProjection.latitudeToMetersY(psInLink[3]);

		psOutLink[0] = MercatorProjection.longitudeToMetersX(psOutLink[0]);

		psOutLink[1] = MercatorProjection.latitudeToMetersY(psOutLink[1]);

		psOutLink[2] = MercatorProjection.longitudeToMetersX(psOutLink[2]);

		psOutLink[3] = MercatorProjection.latitudeToMetersY(psOutLink[3]);
	}

	/**********************************************************************************/

	// 计算LINK与正北方向的夹角
	public static double calIncloudedAngle(String wkt, int direct) throws Exception {
		double includedAngle = 0;

		WKTReader reader = new WKTReader();

		Geometry link = reader.read(wkt);

		double startX = 0, startY = 0, stopX = 0, stopY = 0;

		double[] points = getTraffic2Points(link, direct);

		startX = points[0];

		startY = points[1];

		stopX = points[2];

		stopY = points[3];

		if (startX != stopX && startY != stopY) {

			int quadrant = getQuadrant(startX, startY, stopX, stopY);

			switch (quadrant) {
			case 1:
				includedAngle = 90 - Math.atan((stopY - startY) / (stopX - startX)) * 180 / Math.PI;
				break;
			case 2:
				includedAngle = Math.atan((stopY - startY) / (startX - stopX)) * 180 / Math.PI + 270;
				break;
			case 3:
				includedAngle = Math.atan((startX - stopX) / (startY - stopY)) * 180 / Math.PI + 180;
				break;
			case 4:
				includedAngle = Math.atan((startY - stopY) / (stopX - startX)) * 180 / Math.PI + 90;
				break;
			default:
				break;
			}

		} else {

			if (startX == stopX) {
				if (startY < stopY) {
					includedAngle = 0;
				} else {
					includedAngle = 180;
				}
			} else {
				if (startX < stopX) {
					includedAngle = 90;
				} else {
					includedAngle = 270;
				}
			}

		}

		return includedAngle;
	}

	private static double[] getTraffic2Points(Geometry link, int direct) {

		double[] points = new double[4];

		Coordinate[] cs = link.getCoordinates();

		if (direct == 1 || direct == 2) {
			int len = cs.length;

			points[0] = cs[len - 2].x;

			points[1] = cs[len - 2].y;

			points[2] = cs[len - 1].x;

			points[3] = cs[len - 1].y;

		} else {

			points[0] = cs[1].x;

			points[1] = cs[1].y;

			points[2] = cs[0].x;

			points[3] = cs[0].y;
		}

		return points;
	}

	private static int getQuadrant(double startX, double startY, double stopX, double stopY) {

		if (startX < stopX) {
			if (startY < stopY) {
				// 第一象限
				return 1;
			} else {
				return 4;
			}
		} else {
			if (startY < stopY) {
				return 2;
			} else {
				return 3;
			}
		}
	}

	// 求限速tips显示坐标位置
	public static double[] calSpeedLimitPos(String linkWkt, String pointWkt, int direct) throws Exception {
		double[] position = new double[2];

		double[][] linkMerArray = convertLinkToMerArray(linkWkt);

		double[] pointMerArray = convertPointToMerArray(pointWkt);

		double startx, starty, stopx, stopy;

		double range[] = findLinkRange(linkMerArray, pointMerArray, direct);

		startx = range[0];

		starty = range[1];

		stopx = range[2];

		stopy = range[3];

		if (startx != stopx) {
			double k = (starty - stopy) / (startx - stopx);

			if (starty != stopy) {
				double c1 = starty - (-1 / k) * startx;

				if (k > 0) {
					if (startx < stopx) {

						position[0] = startx + 6 / (Math.pow(1 + ((1 / k) * (1 + 1 / k)), 2));

						position[1] = position[0] * (-1 / k) + c1;
					} else {
						position[0] = startx - 6 / (Math.pow(1 + ((1 / k) * (1 + 1 / k)), 2));

						position[1] = position[0] * (-1 / k) + c1;
					}
				} else {
					if (startx < stopx) {

						position[0] = startx - 6 / (Math.pow(1 + ((1 / k) * (1 + 1 / k)), 2));

						position[1] = position[0] * (-1 / k) + c1;
					} else {
						position[0] = startx + 6 / (Math.pow(1 + ((1 / k) * (1 + 1 / k)), 2));

						position[1] = position[0] * (-1 / k) + c1;
					}
				}
			} else {
				position[0] = startx;

				if (startx < stopx) {

					position[1] = starty - 6;
				} else {
					position[1] = starty + 6;
				}
			}
		} else {
			position[1] = starty;

			if (starty < stopy) {
				position[0] = startx + 6;
			} else {
				position[0] = startx - 6;
			}
		}

		position[0] = MercatorProjection.metersXToLongitude(position[0]);

		position[1] = MercatorProjection.metersYToLatitude(position[1]);

		return position;
	}

	private static double[] findLinkRange(double[][] linkMerArray, double[] pointMerArray, int direct) {
		double[] interPoint = new double[2];

		double range[] = new double[4];

		double startx = 0, starty = 0, stopx = 0, stopy = 0;

		if (direct == 0 || direct == 2) {

			for (int i = 0; i < linkMerArray.length - 1; i++) {

				if (((linkMerArray[i][0] <= pointMerArray[0] && linkMerArray[i + 1][0] >= pointMerArray[0])
						|| (linkMerArray[i][0] >= pointMerArray[0] && linkMerArray[i + 1][0] <= pointMerArray[0]))
						&& ((linkMerArray[i][1] <= pointMerArray[1] && linkMerArray[i + 1][1] >= pointMerArray[1])
								|| (linkMerArray[i][1] >= pointMerArray[1]
										&& linkMerArray[i + 1][1] <= pointMerArray[1]))) {

					startx = linkMerArray[i][0];

					starty = linkMerArray[i][1];

					stopx = linkMerArray[i + 1][0];

					stopy = linkMerArray[i + 1][1];

					break;
				}

			}

		} else {

			for (int i = linkMerArray.length - 1; i > 0; i--) {

				if (((linkMerArray[i][0] <= pointMerArray[0] && linkMerArray[i - 1][0] >= pointMerArray[0])
						|| (linkMerArray[i][0] >= pointMerArray[0] && linkMerArray[i - 1][0] <= pointMerArray[0]))
						&& ((linkMerArray[i][1] <= pointMerArray[1] && linkMerArray[i - 1][1] >= pointMerArray[1])
								|| (linkMerArray[i][1] >= pointMerArray[1]
										&& linkMerArray[i - 1][1] <= pointMerArray[1]))) {

					startx = linkMerArray[i][0];

					starty = linkMerArray[i][1];

					stopx = linkMerArray[i - 1][0];

					stopy = linkMerArray[i - 1][1];

					break;
				}

			}
		}

		if ((starty - pointMerArray[1]) * (pointMerArray[0] - stopx) != (pointMerArray[1] - stopy)
				* (startx - pointMerArray[0])) {

			if (startx != stopx) {

				if (starty != stopy) {

					double k1 = (starty - stopy) / (startx - stopx);

					double c1 = starty - k1 * startx;

					double k2 = -1 / k1;

					double c2 = pointMerArray[1] - k2 * pointMerArray[0];

					interPoint[0] = (c2 - c1) / (k1 - k2);

					interPoint[1] = k1 * interPoint[0] + c1;

				} else {
					interPoint[0] = pointMerArray[0];

					interPoint[1] = starty;
				}

			} else {
				interPoint[0] = startx;

				interPoint[1] = pointMerArray[1];
			}

		} else {
			interPoint[0] = pointMerArray[0];

			interPoint[1] = pointMerArray[1];
		}

		range[0] = interPoint[0];

		range[1] = interPoint[1];

		range[2] = stopx;

		range[3] = stopy;

		return range;
	}

	// 显示坐标：
	// 取link中点坐标，限速的通行方向的右侧2米
	public static double[] getMid2MPosition(String linkWkt, int direct) throws Exception {
		double[] position = new double[2];

		double[][] linkMerArray = convertLinkToMerArray(linkWkt);

		if (direct == 3) {
			int len = linkMerArray.length;

			for (int i = 0; i < len / 2; i++) {
				double tmpPoint[] = linkMerArray[i];
				linkMerArray[i] = linkMerArray[len - i - 1];
				linkMerArray[len - i - 1] = tmpPoint;
			}
		}

		double linkLength = GeometryUtils.getLinkLength(linkWkt);

		double[] midPoint = new double[2];

		double[] range = getMidPointRange(linkMerArray, linkLength, midPoint);

		if (range[0] != range[2]) {

			if (range[1] != range[3]) {

				double k = (range[3] - range[1]) / (range[2] - range[0]);

				if (k > 0) {
					if (range[2] > range[0]) {
						// 递增
						position[0] = midPoint[0] + (2 / (1 + k * k));

						position[1] = midPoint[1] - (2 * k / (1 + k * k));
					} else {
						position[0] = midPoint[0] - (2 / (1 + k * k));

						position[1] = midPoint[1] + (2 * k / (1 + k * k));
					}
				} else {
					if (range[3] > range[1]) {
						// 递增
						position[0] = midPoint[0] - (2 / (1 + k * k));

						position[1] = midPoint[1] - (2 * k / (1 + k * k));
					} else {
						position[0] = midPoint[0] - (2 / (1 + k * k));

						position[1] = midPoint[1] + (2 * k / (1 + k * k));
					}
				}

			} else {
				// 与X轴平行
				if (range[2] > range[0]) {
					position[0] = midPoint[0];

					position[1] = midPoint[1] - 2;
				} else {
					position[0] = midPoint[0];

					position[1] = midPoint[1] + 2;
				}
			}

		} else {
			// 与Y轴平行
			if (range[3] > range[1]) {
				position[0] = midPoint[0] + 2;

				position[1] = midPoint[1];
			} else {
				position[0] = midPoint[0] - 2;

				position[1] = midPoint[1];
			}
		}

		position[0] = MercatorProjection.metersXToLongitude(position[0]);

		position[1] = MercatorProjection.metersYToLatitude(position[1]);

//		// 按照引导坐标位置和线通行方向向右找6米位置作为显示坐标位置
//		double[] displayPosition = getDisplayPosition(linkMerArray, position, guideSeqNum, vertiUnit);

		return position;
	}

	private static double[] getMidPointRange(double[][] linkMerArray, double linkLength, double[] midPoint) {
		double[] range = new double[4];

		double halfLength = linkLength / 2;

		for (int i = 0; i < linkMerArray.length - 1; i++) {
			double len = Math.sqrt(Math.pow(linkMerArray[i][0] - linkMerArray[i + 1][0], 2)
					+ Math.pow(linkMerArray[i][1] - linkMerArray[i + 1][1], 2));

			if (len < halfLength) {
				halfLength -= len;
			} else {
				range[0] = linkMerArray[i][0];

				range[1] = linkMerArray[i][1];

				range[2] = linkMerArray[i + 1][0];

				range[3] = linkMerArray[i + 1][1];

				if (len > halfLength) {

					double scale = halfLength / len;

					midPoint[0] = linkMerArray[i][0] + scale * (linkMerArray[i + 1][0] - linkMerArray[i][0]);

					midPoint[1] = linkMerArray[i][1] + scale * (linkMerArray[i + 1][1] - linkMerArray[i][1]);

					break;
				} else {

					midPoint[0] = linkMerArray[i + 1][0];

					midPoint[1] = linkMerArray[i + 1][1];

					break;
				}
			}
		}

		return range;
	}

	/**
	 * 计算通行方向
	 * 
	 * @param linkWkt
	 *            进入线
	 * @param pointWkt
	 *            进入点
	 * @return
	 * @throws ParseException
	 */
	public static int getDirect(String linkWkt, String pointWkt) throws ParseException {

		int direct = 2;

		Geometry link = new WKTReader().read(linkWkt);

		Geometry point = new WKTReader().read(pointWkt);

		Coordinate[] csLink = link.getCoordinates();

		Coordinate cPoint = point.getCoordinate();

		if (csLink[0].x == cPoint.x && csLink[0].y == cPoint.y) {
			direct = 3;
		}

		return direct;
	}

	/**
	 * 计算在线上的距离端点距离为dist的点的坐标
	 * 
	 * @param coord
	 *            墨卡托坐标
	 * @param next
	 *            墨卡托坐标
	 * @param dist
	 *            要获取的点距离coord点的距离（米）
	 * @return 经纬度坐标
	 */
	private static Coordinate getPointOnLinkByDistance(Coordinate coord, Coordinate next, double dist) {
		Coordinate result = new Coordinate();

		double distance = Math.sqrt(Math.pow(next.x - coord.x, 2) + Math.pow(next.y - coord.y, 2));

		if (coord.x != next.x) {
			double k = (coord.y - next.y) / (coord.x - next.x);

			double c = coord.y - k * coord.x;

			result.x = coord.x + (dist / distance) * (next.x - coord.x);

			result.y = k * result.x + c;
		} else {
			// 与x轴垂直

			result.x = coord.x;

			if (coord.y < next.y) {
				result.y = coord.y + dist;
			} else {
				result.y = coord.y - dist;
			}
		}

		// 转换墨卡托坐标为经纬度坐标返回

		result.x = MercatorProjection.metersXToLongitude(result.x);

		result.y = MercatorProjection.metersYToLatitude(result.y);

		return result;
	}

	public static LineString getGscLine4Web(Geometry linkGeo, int startEnd, int seqNum, int z) throws Exception {

		double offset = 10;

		switch (z) {
		case 16:
		case 17:
			offset = 8;
			break;
		case 18:
			offset = 5;
			break;
		case 19:
			offset = 4;
			break;
		case 20:
			offset = 3;
			break;
		}

		double[][] linkMerArray = convertLinkToMerArray(linkGeo);

		Coordinate[] coords = linkGeo.getCoordinates();

		List<Coordinate> coordList = new ArrayList<Coordinate>();

		Coordinate coord = new Coordinate(linkMerArray[seqNum][0], linkMerArray[seqNum][1]);

		if (startEnd == 0) {

			if (seqNum - 1 >= 0) {
				Coordinate next = new Coordinate(linkMerArray[seqNum - 1][0], linkMerArray[seqNum - 1][1]);

				Coordinate result = getPointOnLinkByDistance(coord, next, offset);

				coordList.add(result);
			}

			coordList.add(coords[seqNum]);

			if ((seqNum + 1) < coords.length) {

				Coordinate next = new Coordinate(linkMerArray[seqNum + 1][0], linkMerArray[seqNum + 1][1]);

				Coordinate result = getPointOnLinkByDistance(coord, next, offset);

				coordList.add(result);
			}
		} else if (startEnd == 1) {

			coordList.add(coords[seqNum]);

			if ((seqNum + 1) < coords.length) {

				Coordinate next = new Coordinate(linkMerArray[seqNum + 1][0], linkMerArray[seqNum + 1][1]);

				Coordinate result = getPointOnLinkByDistance(coord, next, offset);

				coordList.add(result);
			}
		} else {

			coordList.add(coords[seqNum]);

			if (seqNum - 1 >= 0) {

				Coordinate next = new Coordinate(linkMerArray[seqNum - 1][0], linkMerArray[seqNum - 1][1]);

				// GeometryUtils.getPointOnLineStringDistance(new
				// LineSegment(coord, next),offset);
				Coordinate result = getPointOnLinkByDistance(coord, next, offset);

				coordList.add(result);
			}
		}

		Coordinate[] newCoords = new Coordinate[coordList.size()];

		for (int i = 0; i < coordList.size(); i++) {
			newCoords[i] = coordList.get(i);
		}

		LineString line = geometryFactory.createLineString(newCoords);

		return line;
	}

	/**
	 * 计算立交的线几何
	 * 
	 * @param linkGeo
	 * @param startEnd
	 * @param seqNum
	 * @return
	 */
	public static LineString getGscLine(Geometry linkGeo, int startEnd, int seqNum) {
		Coordinate[] coords = linkGeo.getCoordinates();

		List<Coordinate> coordList = new ArrayList<Coordinate>();

		double degree = GeometryUtils.convert2Degree(2);// 2 meter

		if (startEnd == 0) {
			// back
			List<Coordinate> coorsBack = new ArrayList<Coordinate>();
			double distBack = 0;

			int current = seqNum;
			Coordinate coord = coords[current];
			DoublePoint curPoint = JtsGeometryConvertor.convert(coord);
			coorsBack.add(coord);

			while (current - 1 > -1) {
				Coordinate next = coords[current - 1];
				DoublePoint nextPoint = JtsGeometryConvertor.convert(next);
				DoubleLine li = new DoubleLine(curPoint, nextPoint);
				DoublePoint op = li.split(degree - distBack);
				if (op == null) {
					coorsBack.add(next);
					distBack += li.getEncLength();
				} else {
					coorsBack.add(JtsGeometryConvertor.convert(op));
					break;
				}
				curPoint = nextPoint;
				current--;
			}
			// fore
			List<Coordinate> coorsFore = new ArrayList<Coordinate>();
			double distFore = 0;

			int currentFore = seqNum;
			Coordinate coordFore = coords[currentFore];
			DoublePoint curPointFore = JtsGeometryConvertor.convert(coordFore);
			coorsFore.add(coordFore);

			while (currentFore + 1 < coords.length) {
				Coordinate nextFore = coords[currentFore + 1];
				DoublePoint nextPointFore = JtsGeometryConvertor.convert(nextFore);
				DoubleLine li = new DoubleLine(curPointFore, nextPointFore);
				DoublePoint op = li.split(degree - distFore);
				if (op == null) {
					coorsFore.add(nextFore);
					distFore += li.getEncLength();
				} else {
					coorsFore.add(JtsGeometryConvertor.convert(op));
					break;
				}
				curPointFore = nextPointFore;
				currentFore++;
			}
			Collections.reverse(coorsBack);
			coordList.addAll(coorsBack);
			coordList.addAll(coorsFore);
		} else if (startEnd == 1) {
			double distFore = 0;

			int currentFore = 0;
			Coordinate coordFore = coords[currentFore];
			DoublePoint curPointFore = JtsGeometryConvertor.convert(coordFore);
			coordList.add(coordFore);

			while (currentFore + 1 < coords.length) {
				Coordinate nextFore = coords[currentFore + 1];
				DoublePoint nextPointFore = JtsGeometryConvertor.convert(nextFore);
				DoubleLine li = new DoubleLine(curPointFore, nextPointFore);
				DoublePoint op = li.split(degree - distFore);
				if (op == null) {
					coordList.add(nextFore);
					distFore += li.getEncLength();
				} else {
					coordList.add(JtsGeometryConvertor.convert(op));
					break;
				}
				curPointFore = nextPointFore;
				currentFore++;
			}
		} else {
			double distBack = 0;

			int current = coords.length - 1;
			Coordinate coord = coords[current];
			DoublePoint curPoint = JtsGeometryConvertor.convert(coord);
			coordList.add(coord);

			while (current - 1 > -1) {
				Coordinate next = coords[current - 1];
				DoublePoint nextPoint = JtsGeometryConvertor.convert(next);
				DoubleLine li = new DoubleLine(curPoint, nextPoint);
				DoublePoint op = li.split(degree - distBack);
				if (op == null) {
					coordList.add(next);
					distBack += li.getEncLength();
				} else {
					coordList.add(JtsGeometryConvertor.convert(op));
					break;
				}
				curPoint = nextPoint;
				current--;
			}
			Collections.reverse(coordList);
		}

		Coordinate[] newCoords = new Coordinate[coordList.size()];

		for (int i = 0; i < coordList.size(); i++) {
			newCoords[i] = coordList.get(i);
		}

		LineString line = geometryFactory.createLineString(newCoords);

		return line;
	}

	public static void main(String[] args) throws Exception {
		String wkt = "LINESTRING(116.48686 40.01237, 116.48676 40.01244,116.48661 40.01244)";
		LineString ls = getGscLine(JtsGeometryFactory.read(wkt), 0, 1);
		System.out.println(ls.toText());
	}
}