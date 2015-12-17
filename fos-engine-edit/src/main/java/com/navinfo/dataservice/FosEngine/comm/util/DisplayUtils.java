package com.navinfo.dataservice.FosEngine.comm.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.spatial.geometry.JGeometry;

import com.navinfo.dataservice.FosEngine.comm.mercator.MercatorProjection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

public class DisplayUtils {

	public static int kind2Color(int kind) {
		if (kind == 13) {
			return 13;
		} else if (kind == 15) {
			return 14;
		} else {
			return kind + 1;
		}
	}
	
	
	//获取按照道路通行方向取四分之一位置
	public static double[] get1_4Point(JGeometry geom, int direct) {

		double linkPoints[][] = getLinkPoints(geom, direct);

		double linkLength = getLinkLength(linkPoints);

		double len1_4 = linkLength / 4;

		double[] point = lookFor1_4Point(linkPoints, len1_4);

		return point;

	}

	private static double[][] getLinkPoints(JGeometry geom, int direct) {

		double[] ps = geom.getOrdinatesArray();

		double[][] points = new double[ps.length / 2][];

		int num = 0;

		if (direct == 1 || direct == 2) {
			for (int i=0;i<ps.length;i += 2) {

				double lng = ps[i];

				double lat = ps[i+1];

				double lngMer = MercatorProjection.longitudeToMetersX(lng);

				double latMer = MercatorProjection.latitudeToMetersY(lat);

				points[num++] = new double[]{lngMer,latMer};
			}
		} else {
			for(int i=ps.length - 1;i>=0;i -= 2){
								
				double lat = ps[i];

				double lng = ps[i-1];

				double lngMer = MercatorProjection.longitudeToMetersX(lng);

				double latMer = MercatorProjection.latitudeToMetersY(lat);

				points[num++] = new double[]{lngMer,latMer};
			}
		}

		return points;

	}

	private static double getLinkLength(double[][] points) {
		double length = 0;

		for (int i = 1; i < points.length; i++) {
			double prePoint[] = points[i - 1];

			double currentPoint[] = points[i];

			double len = Math.sqrt(Math.pow(currentPoint[0] - prePoint[0], 2)
					+ Math.pow(currentPoint[1] - prePoint[1], 2));

			length += len;
		}

		return length;
	}

	private static double[] lookFor1_4Point(double[][] points, double len1_4) {
		double point[] = new double[2];

		for (int i = 1; i < points.length; i++) {
			double prePoint[] = points[i - 1];

			double currentPoint[] = points[i];

			double len = Math.sqrt(Math.pow(currentPoint[0] - prePoint[0], 2)
					+ Math.pow(currentPoint[1] - prePoint[1], 2));

			if (len >= len1_4) {

				point[0] = prePoint[0] + len1_4 / len
						* (currentPoint[0] - prePoint[0]);

				point[1] = prePoint[1] + len1_4 / len
						* (currentPoint[1] - prePoint[1]);

				break;
			}

			len1_4 -= len;
		}

		point[0] = MercatorProjection.metersXToLongitude(point[0]);

		point[1] = MercatorProjection.metersYToLatitude(point[1]);

		return point;
	}
	
	
	/***********************************************************************/
	
	//获取进入线3米再向右找6米的位置
	public static double[] getTipPoint(JGeometry geom, double inPointLng,
			double inPointLat) {

		double point[] = new double[2];

		double ps[] = geom.getOrdinatesArray();

		double endTwoPointMers[][] = findEndTwoPointMercator(ps, inPointLng, inPointLat);
		
		double k = 0;
		
		double c = 0;
		
		boolean isVerticalX = false;
		
		if (endTwoPointMers[0][0] != endTwoPointMers[1][0]){
			
			if (endTwoPointMers[0][1] != endTwoPointMers[1][1]){
				
				k = (endTwoPointMers[1][1] - endTwoPointMers[0][1]) / (endTwoPointMers[1][0] - endTwoPointMers[0][0]);
				
				c = endTwoPointMers[0][1] - k * endTwoPointMers[0][0];
				
			}else{
				k = 0;
				
				c = endTwoPointMers[0][1];
			}
			
		}else{
			isVerticalX = true;
		}
		
		if (!isVerticalX){
			
			double[] pointMer = get_3_6_MeterPoint(endTwoPointMers, k, c);
			
			point[0] = MercatorProjection.metersXToLongitude(pointMer[0]);
			
			point[1] = MercatorProjection.metersYToLatitude(pointMer[1]);
			
		}else{
			double[] tmpPoint = new double[2];
			
			tmpPoint[0] = endTwoPointMers[0][0];
			
			if (endTwoPointMers[0][1] > endTwoPointMers[1][1]){
				tmpPoint[1] = endTwoPointMers[0][1] - 3;
				
				point[0] = MercatorProjection.metersXToLongitude(tmpPoint[0] + 6);
				
				point[1] = MercatorProjection.metersYToLatitude(tmpPoint[1]);
			}else{
				tmpPoint[1] = endTwoPointMers[0][1] + 3;
				
				point[0] = MercatorProjection.metersXToLongitude(tmpPoint[0] - 6);
				
				point[1] = MercatorProjection.metersYToLatitude(tmpPoint[1]);
			}
		}

		return point;
	}

	private static double[][] findEndTwoPointMercator(double ps[],
			double inPointLng, double inPointLat) {

		double endTwoPointMers[][] = new double[2][];

		endTwoPointMers[0] = new double[] {
				MercatorProjection.longitudeToMetersX(inPointLng),
				MercatorProjection.latitudeToMetersY(inPointLat) };

		if (ps[0] == inPointLng && ps[1] == inPointLat) {

			endTwoPointMers[1] = new double[] {
					MercatorProjection.longitudeToMetersX(ps[2]),
					MercatorProjection.latitudeToMetersY(ps[3]) };
		} else {

			endTwoPointMers[1] = new double[] {
					MercatorProjection.longitudeToMetersX(ps[ps.length - 2]),
					MercatorProjection.latitudeToMetersY(ps[ps.length - 1]) };
		}

		return endTwoPointMers;

	}
	
	private static double[] get_3_6_MeterPoint(double endTwoPointMers[][],double k,double c){
		double[] point = new double[2];
		
		//寻找方向
		if (k > 0){
			if (endTwoPointMers[0][0] < endTwoPointMers[1][0]){
				point[0] = endTwoPointMers[0][0] + k * 3;
			}else{
				point[0] = endTwoPointMers[0][0] - k * 3;
			}
		}else if (k < 0){
			if (endTwoPointMers[0][0] > endTwoPointMers[1][0]){
				point[0] = endTwoPointMers[0][0] + k * 3;
			}else{
				point[0] = endTwoPointMers[0][0] - k * 3;
			}
		}else{
			if (endTwoPointMers[0][0] > endTwoPointMers[1][0]){
				point[0] = endTwoPointMers[0][0] - 3;
			}else{
				point[0] = endTwoPointMers[0][0] + 3;
			}
		}
		
		point[1] = k * point[0] + c;
		
		if (k !=0){
			double k1 = 1 / k;
			
			double c1 = point[1] - k1 * point[0];
			
			//求出左右两个点
			
			double px1 = point[0] - k1 * 6;
			
			double py1 = k1 * px1 + c1;
			
			double px2 = point[0] + k1 * 6;
			
			double py2 = k1 * px2 + c1;
			
			//寻找右边点
			if (py1 < (k * px1 + c)){
				point[0] = px1;
				
				point[1] = py1;
			}else{
				point[0] = px2;
				
				point[1] = py2;
			}
		}else{
			if (endTwoPointMers[0][0] < point[0]){
			
				point[1] += 6;
			}else{
				point[1] -= 6;
			}
		}
		
		
		return point;
	}
	
	/*********************************************************************/
	
	//获取路口主点挂接LINK角平分线30米位置点
	public static double[] getCrossPoint(String inLinkWkt, String outLinkWkt,
			String pointWkt) throws Exception {
		double[] point = new double[2];

		WKTReader reader = new WKTReader();

		Geometry inLink = reader.read(inLinkWkt);

		Geometry outLink = reader.read(outLinkWkt);

		Geometry inPoint = reader.read(pointWkt);

		double[] psInLink = new double[4];

		double[] psOutLink = new double[4];

		fillInOutPoints(inLink, outLink, inPoint, psInLink, psOutLink);

		double[] ps = new double[2];

		ps[0] = MercatorProjection
				.longitudeToMetersX(inPoint.getCoordinate().x);

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

	private static void fillInOutPoints(Geometry inLink, Geometry outLink,
			Geometry ps, double[] psInLink, double[] psOutLink) {

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
	
	/*************************************************************************/
	
	//求跨图幅的LINK，被图幅边界打断后的WKT列表
	public static List<String> getSplitLinkByMeshs(String wkt) throws ParseException{
		
		WKTReader reader = new WKTReader();
		
		WKTWriter writer = new WKTWriter();
		
		Geometry geomLink = reader.read(wkt);
		
		Geometry geomBound = geomLink.getBoundary();
		
		double minLon = 180,minLat = 90,maxLon = 0,maxLat = 0;
		
		Coordinate[] csBound = geomBound.getCoordinates();
		
		for(Coordinate c: csBound){
			if (minLon > c.x){
				minLon = c.x;
			}
			
			if (minLat > c.y){
				minLat = c.y;
			}
			
			if (maxLon < c.x){
				maxLon = c.x;
			}
			
			if (maxLat < c.y){
				maxLat = c.y;
			}
		}
		
//		String[] meshs = MeshUtils.area2Meshes(minLon, minLat, maxLon, maxLat);
		
		Set<String> meshs = new HashSet<String>();
		
		meshs.add(MeshUtils.lonlat2Mesh(minLon, minLat));
		
		meshs.add(MeshUtils.lonlat2Mesh(maxLon, minLat));
		
		meshs.add(MeshUtils.lonlat2Mesh(maxLon, maxLat));
		
		meshs.add(MeshUtils.lonlat2Mesh(minLon, maxLat));
		
		if (meshs.size() > 1){
			
			List<String> list = new ArrayList<String>();
			
			for(String mesh : meshs){

				String meshWkt = MeshUtils.mesh2WKT(mesh);
		
				Geometry geomMesh = reader.read(meshWkt);
				
				Geometry geomInter = geomMesh.intersection(geomLink);
				
				
				list.add(writer.write(geomInter));
			}
			
			return list;
		}else{
			return null;
		}
		
		
	}
	
	
	/**********************************************************************************/
	
	// 计算LINK与正北方向的夹角
		public static double calIncloudedAngle(String wkt, int direct)
				throws Exception {
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
					includedAngle = Math.atan((stopY - startY) / (stopX - startX))
							* 180 / Math.PI;
					break;
				case 2:
					includedAngle = Math.atan((stopY - startY) / (startX - stopX))
							* 180 / Math.PI + 270;
					break;
				case 3:
					includedAngle = Math.atan((stopY - startY) / (stopX - startX))
							* 180 / Math.PI + 180;
					break;
				case 4:
					includedAngle = Math.atan((startY - stopY) / (stopX - startX))
							* 180 / Math.PI + 90;
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
				points[0] = cs[0].x;

				points[1] = cs[0].y;

				points[2] = cs[1].x;

				points[3] = cs[1].y;
			} else {

				int len = cs.length;

				points[0] = cs[len - 1].x;

				points[1] = cs[len - 1].y;

				points[2] = cs[len - 2].x;

				points[3] = cs[len - 2].y;
			}

			return points;
		}

		private static int getQuadrant(double startX, double startY, double stopX,
				double stopY) {

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
	
}
