package com.navinfo.dataservice.commons.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 2010-8-4 Time: 8:58:15
 * 地理坐标相关的工具类
 */

enum MeshLocateRelation {
	Top, Bottom, Left, Right, LeftTop, LeftBottom, RightTop, RightBottom, Inside,
};

/**
 * 
 */
public abstract class MeshUtils {

	public static void main(String[] args) throws Exception {
		String meshId = "595671";
		Set<String> meshIdSet = new HashSet<String>();
		meshIdSet.add("35672");
		Set<String> result = MeshUtils.getNeighborMeshSet(meshIdSet, 3);
		System.out.println(result.size());
		System.out.println(result);
		
//		double[] a = mesh2LocationLatLon("595671");
		
//		System.out.println(a[0]+","+a[1]);
//		
//		List<String> b = lonlat2MeshIds(a[0],a[1]);
//		
//		System.out.println(b.toString());
		
		// int meshid = 595673;
		// double[] a =mesh2LocationLatLon("24967");
		// System.out.println(a[0]+","+a[1]);
		//
		// System.out.println(lonlat2Mesh(109.875,1.833333));

		// String wkt = mesh2WKT("595651"); System.out.println(wkt);

//		String wkt1 = "LINESTRING (1 1, 4 3)";
//
//		String wkt2 = "POLYGON ((2 2,3 2,3 3,2 3,2 2))";
//
//		Geometry geom1 = new WKTReader().read(wkt1);
//
//		Geometry geom2 = new WKTReader().read(wkt2);
//
//		Geometry geom = linkInterMeshPolygon(geom1, geom2);
//
//		System.out.println(geom.getGeometryType());

		// (M3*10+M4)*3600+M6*450+60*3600
		// (M1*10+M2)*2400+M5*300
		// int x = (5 * 10 + 6) * 3600 + 3 * 450 + 60 * 3600;
		// int y = (5 * 10 + 9) * 2400 + 7 * 300;

		// System.out.println(decimal2Second(22.97195));
		// System.out.println(second2Decimal(decimal2Second(22.97195)));

		// System.out.println(x); //经度
		// System.out.println(y); //维度
		// System.out.println(location2Mesh(x + 225, y + 150));

		/*
		 * System.out.println("=================================================="
		 * ); String[] allMesh = get9NeighborMesh("595673"); for (int i = 0; i <
		 * allMesh.length; i++) { String s = allMesh[i];
		 * System.out.println("mesh" + (i + 1) + ":" + s); }
		 */
		// System.out
		// .println("==================================================");
		// Set<String> allMesh = get9NeighborMesh2("45172", 3);
		// int i = 0;
		// for (String s : allMesh) {
		// System.out.println("mesh" + (i + 1) + ":" + s);
		// i++;
		// }

		/*
		 * System.out.println("=================================================="
		 * ); int lo[] = mesh2Location("595662"); for (int i = 0; i < lo.length;
		 * i++) { int i1 = lo[i]; System.out.println(i1); }
		 * System.out.println("=================================================="
		 * ); lo = mesh2Location("605604"); for (int i = 0; i < lo.length; i++)
		 * { int i1 = lo[i]; System.out.println(i1); }
		 * 
		 * System.out.println("=================================================="
		 * ); allMesh = area2Meshes(418725, 143550, 419625, 144150); for (int i
		 * = 0; i < allMesh.length; i++) { String s = allMesh[i];
		 * System.out.println("mesh" + (i + 1) + ":" + s); }
		 */
		/*
		 * int a[] = mesh2Location("595651"); for (int m : a) {
		 * 
		 * // System.out.println(m); ; System.out.println(second2Decimal(m)); ;
		 * } String wkt = mesh2WKT("595651"); System.out.println(wkt);
		 */
	}

	/**
	 * 根据图幅号计算左下/中心坐标点/右上坐标点
	 * 
	 * @param meshId
	 * @return 左下/中心坐标点/右上坐标点
	 */
	public static int[] mesh2Location(String meshId) {

		if (meshId.length() < 6) {
			int length = 6 - meshId.length();
			for (int i = 0; i < length; i++) {
				meshId = "0" + meshId;
			}
		}

		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int lbX = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int lbY = (m1 * 10 + m2) * 2400 + m5 * 300;
		int cX = lbX + 450 / 2;
		int cY = lbY + 300 / 2;
		int rtX = lbX + 450;
		int rtY = lbY + 300;
		return new int[] { lbX, lbY, cX, cY, rtX, rtY };

	}

	/**
	 * 根据图幅号计算左下/中心坐标点/右上坐标点的经纬度
	 * 
	 * @param meshId
	 * @return 左下/中心坐标点/右上坐标点经纬度
	 */
	public static double[] mesh2LocationLatLon(String meshId) {
		int[] data = mesh2Location(meshId);

		double[] result = new double[data.length];

		for (int i = 0; i < data.length; i++) {
			result[i] = second2Decimal(data[i]);
		}

		return result;
	}

	/**
	 * 图幅号转换成POLYGON的wkt
	 * 
	 * @param meshId
	 * @return
	 */
	public static String mesh2WKT(String meshId) {
		int a[] = mesh2Location(meshId);
		double lbX = second2Decimal(a[0]);
		double lbY = second2Decimal(a[1]);
		double rtX = second2Decimal(a[4]);
		double rtY = second2Decimal(a[5]);
		return "POLYGON ((" + lbX + " " + lbY + ", " + lbX + " " + rtY + ", "
				+ rtX + " " + rtY + ", " + rtX + " " + lbY + "))";

	}

	/**
	 * 图幅号转换成POLYGON的JTS对象
	 * 
	 * @param meshId
	 * @return
	 * @throws ParseException
	 */
	public static Geometry mesh2Jts(String meshId) throws ParseException {

		String wkt = mesh2WKT(meshId);

		Geometry jts = new WKTReader().read(wkt);

		return jts;

	}

	public static Geometry linkInterMeshPolygon(Geometry linkGeom,
			Geometry meshGeom) {

		return meshGeom.intersection(linkGeom);

	}

	/**
	 * 给定坐标范围（秒），计算范围内包含的图幅
	 * 
	 * @param lbX
	 *            左下经度 (秒)
	 * @param lbY
	 *            左下维度 (秒)
	 * @param rtX
	 *            右上经度 (秒)
	 * @param rtY
	 *            右上维度 (秒)
	 * @return
	 */
	public static String[] area2Meshes(double lbX, double lbY, double rtX,
			double rtY) {
		// 计算左下坐标位于图幅
		String lbMesh = location2Mesh(lbX, lbY);
		// 计算右上坐标位于图幅
		String rtMesh = location2Mesh(rtX, rtY);
		if (lbMesh.equals(rtMesh)) {
			return new String[] { lbMesh };
		} else {
			// 跨多个图幅
			int[] lbLocations = mesh2Location(lbMesh);
			int[] rtLocations = mesh2Location(rtMesh);
			int lbcX = lbLocations[2]; // 左下图幅中心点X
			int lbcY = lbLocations[3]; // 左下图幅中心点Y
			int rtcX = rtLocations[2]; // 右上图幅中心点X
			int rtcY = rtLocations[3]; // 右上图幅中心点Y
			// 计算横向跨多少个图幅
			int hSize = (rtcX - lbcX) / 450 + 1;
			int vSize = (rtcY - lbcY) / 300 + 1;
			int meshSize = hSize * vSize; // 图幅数
			// 从左下角开始计算图幅
			String allMesh[] = new String[meshSize];
			int k = 0;
			for (int i = 0; i < hSize; i++) {
				for (int j = 0; j < vSize; j++) {
					allMesh[k] = location2Mesh(lbcX + i * 450, lbcY + j * 300);
					k++;
				}
			}

			return allMesh;

		}
	}

	/**
	 * 给定左下图幅，右上图幅，计算其外接矩形包含图幅
	 * 
	 * @param lbMesh
	 *            左下图幅
	 * @param rtMesh
	 *            右上图幅
	 * @return
	 */
	public static String[] getBetweenMeshes(String lbMesh, String rtMesh) {
		if (lbMesh.equals(rtMesh)) {
			return new String[] { lbMesh };
		} else {
			// 跨多个图幅
			int[] lbLocations = mesh2Location(lbMesh);
			int[] rtLocations = mesh2Location(rtMesh);
			int lbcX = lbLocations[2]; // 左下图幅中心点X
			int lbcY = lbLocations[3]; // 左下图幅中心点Y
			int rtcX = rtLocations[2]; // 右上图幅中心点X
			int rtcY = rtLocations[3]; // 右上图幅中心点Y
			// 计算横向跨多少个图幅
			int hSize = (rtcX - lbcX) / 450 + 1;
			int vSize = (rtcY - lbcY) / 300 + 1;
			int meshSize = hSize * vSize; // 图幅数
			// 从左下角开始计算图幅
			String allMesh[] = new String[meshSize];
			int k = 0;
			for (int i = 0; i < hSize; i++) {
				for (int j = 0; j < vSize; j++) {
					allMesh[k] = location2Mesh(lbcX + i * 450, lbcY + j * 300);
					k++;
				}
			}

			return allMesh;

		}
	}

	/**
	 * 传入rectangle的坐标字符串组，x1 y1,x2 y2,x3 y3,x4 y4,x1 y1
	 * 
	 * @param polygon
	 * @return
	 */
	public static String[] rectangle2Meshes(String rect) {
		if (StringUtils.isNotEmpty(rect)) {
			float lbX = 999;
			float lbY = 999;
			float rtX = -999;
			float rtY = -999;
			for (String point : rect.split(",")) {
				String pointTrim = point.trim();
				String[] pointArray = pointTrim.split(" ");
				if (pointArray != null && pointArray.length == 2) {
					float x = Float.parseFloat(pointArray[0]);
					float y = Float.parseFloat(pointArray[1]);
					if (x < lbX)
						lbX = x;
					if (x > rtX)
						rtX = x;
					if (y < lbY)
						lbY = y;
					if (y > rtY)
						rtY = y;
				}
			}
			if (lbX != 999 && lbY != 999 && rtX != -999 && rtY != -999) {
				return area2Meshes(lbX * 3600, lbY * 3600, rtX * 3600,
						rtY * 3600);
			}
		}
		return null;

	}

	/**
	 * 经纬度小数形式转换成秒
	 * 
	 * @param x
	 *            //小数
	 * @return
	 */
	public static double decimal2Second(double x) {

		return x * 3600;

	}

	/**
	 * 经纬度秒转换成小数
	 * 
	 * @param x
	 *            //秒
	 * @return
	 */
	public static double second2Decimal(double x) {
		return Double.parseDouble(new java.text.DecimalFormat("#.000000")
				.format(x / 3600));
	}

	/**
	 * 位置转换成图幅号
	 * 
	 * @param x
	 *            //经度 秒
	 * @param y
	 *            //维度 秒
	 * @return
	 */
	public static String location2Mesh(double x, double y) {
		x /= 3600.0;
		y /= 3600.0;

		double W;
		int M1M2;
		int J1;
		double J2;
		int M3M4;
		int M5;
		int M6;

		W = y;
		M1M2 = (int) (W * 1.5);

		J1 = (int) x;
		J2 = x - J1;

		M3M4 = J1 - 60;
		M5 = (int) ((W - M1M2 / 1.5) * 12.0);
		M6 = (int) (J2 * 8.0);
		StringBuilder builder = new StringBuilder();
		builder.append(M1M2);
		builder.append(M3M4);
		builder.append(M5);
		builder.append(M6);
		String meshId = builder.toString();
		if (meshId.length() == 5) {
			meshId = "0" + meshId;
		}
		return meshId;
	}

	/**
	 * 经纬度转图幅号
	 * 
	 * @param lon
	 * @param lat
	 * @return
	 */
	public static String lonlat2Mesh(double lon, double lat) {
		double W;
		int M1M2;
		int J1;
		double J2;
		int M3M4;
		int M5;
		int M6;

		W = lat;
		M1M2 = (int) (W * 1.5);

		J1 = (int) lon;
		J2 = lon - J1;

		M3M4 = J1 - 60;
		M5 = (int) ((W - M1M2 / 1.5) * 12.0);
		M6 = (int) (J2 * 8.0);
		StringBuilder builder = new StringBuilder();
		builder.append(M1M2);
		builder.append(M3M4);
		builder.append(M5);
		builder.append(M6);
		String meshId = builder.toString();
		if (meshId.length() == 5) {
			meshId = "0" + meshId;
		}
		return meshId;
	}

	/**
	 * 计算某图幅周报的9个图幅
	 * 
	 * @param meshId
	 * @return
	 */
	public static String[] get9NeighborMesh(String meshId) {
		meshId = StringUtils.leftPad(meshId, 6, '0');
		String allMesh[] = new String[9];
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int x = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int y = (m1 * 10 + m2) * 2400 + m5 * 300;
		x += 450.0 / 2;
		y += 300.0 / 2;
		allMesh[0] = location2Mesh(x - 450, y + 300);
		allMesh[1] = location2Mesh(x, y + 300);
		allMesh[2] = location2Mesh(x + 450, y + 300);
		allMesh[3] = location2Mesh(x - 450, y);
		allMesh[4] = location2Mesh(x, y);
		allMesh[5] = location2Mesh(x + 450, y);
		allMesh[6] = location2Mesh(x - 450, y - 300);
		allMesh[7] = location2Mesh(x, y - 300);
		allMesh[8] = location2Mesh(x + 450, y - 300);
		return allMesh;

	}

	/**
	 * @param meshId
	 * @return 获取除了中心图幅之外的阔圈图幅
	 */
	private static  String[] get8NeighborMesh(String meshId) {
		meshId = StringUtils.leftPad(meshId, 6, '0');
		String allMesh[] = new String[8];
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int x = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int y = (m1 * 10 + m2) * 2400 + m5 * 300;
		x += 450.0 / 2;
		y += 300.0 / 2;
		allMesh[0] = location2Mesh(x - 450, y + 300);
		allMesh[1] = location2Mesh(x, y + 300);
		allMesh[2] = location2Mesh(x + 450, y + 300);
		allMesh[3] = location2Mesh(x - 450, y);
		//allMesh[4] = location2Mesh(x, y);
		allMesh[4] = location2Mesh(x + 450, y);
		allMesh[5] = location2Mesh(x - 450, y - 300);
		allMesh[6] = location2Mesh(x, y - 300);
		allMesh[7] = location2Mesh(x + 450, y - 300);
		return allMesh;

	}
	
	/**
	 * 计算单个图幅邻接的9个图
	 * @param meshId
	 * @return 9邻接图幅set
	 */
	private static Set<String> generate9NeighborMeshSet(String meshId){
		Set<String> meshes = new HashSet<String>();
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int x = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int y = (m1 * 10 + m2) * 2400 + m5 * 300;
		x += 450.0 / 2;
		y += 300.0 / 2;
		meshes.add(location2Mesh(x - 450, y + 300));
		meshes.add( location2Mesh(x, y + 300));
		meshes.add( location2Mesh(x + 450, y + 300));
		meshes.add( location2Mesh(x - 450, y));
		meshes.add(location2Mesh(x, y));
		meshes.add( location2Mesh(x + 450, y));
		meshes.add(location2Mesh(x - 450, y - 300));
		meshes.add(location2Mesh(x, y - 300));
		meshes.add(location2Mesh(x + 450, y - 300));
		return meshes;
	}

	/**
	 * 计算单个图幅邻接的8个图
	 * @param meshId
	 * @return 8邻接图幅set
	 */
	private static Set<String> generate8NeighborMeshSet(String meshId){
		Set<String> meshes = new HashSet<String>();
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int x = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int y = (m1 * 10 + m2) * 2400 + m5 * 300;
		x += 450.0 / 2;
		y += 300.0 / 2;
		meshes.add(location2Mesh(x - 450, y + 300));
		meshes.add( location2Mesh(x, y + 300));
		meshes.add( location2Mesh(x + 450, y + 300));
		meshes.add( location2Mesh(x - 450, y));
		meshes.add( location2Mesh(x + 450, y));
		meshes.add(location2Mesh(x - 450, y - 300));
		meshes.add(location2Mesh(x, y - 300));
		meshes.add(location2Mesh(x + 450, y - 300));
		return meshes;
	}
	
	/**
	 * 计算1圈的扩圈图幅
	 * 使用set可以保证输入和输出不会有重复
	 * @param meshSet
	 * @return 包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
	 * @author XXW
	 */
	private static Set<String> generateNeighborMeshSet(Set<String> meshSet){
		Set<String> neiMeshSet=new HashSet<String>();
		for(String meshId:meshSet){
			neiMeshSet.addAll(generate9NeighborMeshSet(meshId));
		}
		return neiMeshSet;
	}

	/**
	 * 计算1圈的扩圈图幅
	 * 使用set可以保证输入和输出不会有重复
	 * @param meshSet
	 * @return 包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
	 * @author XXW
	 */
	public static Set<String> getNeighborMeshSet(Set<String> meshSet){
		//check
		if(meshSet!=null){
			Set<String> checkedMeshSet = new HashSet<String>();
			for(String meshId:meshSet){
				checkedMeshSet.add(StringUtils.leftPad(meshId, 6, '0'));
			}
			return generateNeighborMeshSet(checkedMeshSet);
		}
		return null;
	}
	
	/**
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	private static Set<String> generateNeighborMeshSet(String meshId,int extendCount){
		Set<String> outMeshes = new HashSet<String>();
		if(extendCount==1){
			outMeshes= generate9NeighborMeshSet(meshId);
			return outMeshes;
		}else{
			Set<String> meshes = generateNeighborMeshSet(meshId,extendCount-1);
			outMeshes = generateNeighborMeshSet(meshes);
			return outMeshes;
		}
	}
	/**
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	public static Set<String> getNeighborMeshSet(String meshId,int extendCount){
		if(StringUtils.isNotEmpty(meshId)&&extendCount>0){
			meshId = StringUtils.leftPad(meshId, 6, '0');
			return generateNeighborMeshSet(meshId,extendCount);
		}
		return null;
	}
	

	/**
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	private static Set<String> generateNeighborMeshSet(Set<String> meshSet,int extendCount){
		Set<String> outMeshes = new HashSet<String>();
		if(extendCount==1){
			outMeshes= generateNeighborMeshSet(meshSet);
			return outMeshes;
		}else{
			Set<String> meshes = generateNeighborMeshSet(meshSet,extendCount-1);
			outMeshes = generateNeighborMeshSet(meshes);
			return outMeshes;
		}
	}

	/**
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	public static Set<String> getNeighborMeshSet(Set<String> meshSet,int extendCount){
		if(meshSet!=null&&extendCount>0){
			Set<String> checkedMeshSet = new HashSet<String>();
			for(String meshId:meshSet){
				checkedMeshSet.add(StringUtils.leftPad(meshId, 6, '0'));
			}
			return generateNeighborMeshSet(checkedMeshSet,extendCount);
		}
		return null;
	}

	/**
	 * 点是否在图框线上
	 * 
	 * @param dLongitude
	 * @param dLatitude
	 * @return
	 */
	public static boolean isPointAtMeshBorder(double dLongitude,
			double dLatitude) {
		int model = 0;

		int[] result = calculateIdealRowIndex(dLatitude);

		int rowIndex = result[0];
		int remainder = result[1];

		switch (rowIndex % 3) {
		case 0: // 第一行
		{
			if (300000 - remainder == 12) // 余数距离上框等于0.012秒
				model |= 0x01;
			else if (remainder == 0)
				model |= 0x01;
		}
			break;
		case 1: // 第二行由于上下边框均不在其内，因此不在图框上
			break;
		case 2: // 第三行
		{
			if (remainder == 12) // 余数距离下框等于0.012秒
				model |= 0x01;
		}
			break;
		}

		result = calculateIdealColumnIndex(dLongitude);

		if (0 == result[1])
			model |= 0x10;

		if (model == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 根据纬度计算该点位于理想图幅分割的行序号
	 * 
	 * @param dLatitude
	 * @return 行序号，余数
	 */
	private static int[] calculateIdealRowIndex(double dLatitude) {
		int[] result = new int[2];

		// 相对区域纬度 = 绝对纬度 - 0.0
		double regionLatitude = dLatitude - 0.0;

		// 相对的以秒为单位的纬度
		double secondLatitude = regionLatitude * 3600;

		// 为避免浮点数的内存影响，将秒*10的三次方(由于0.00001度为0.036秒)
		long longsecond = (int) Math.floor(secondLatitude * 1000);

		result[0] = (int) (longsecond / 300000);

		result[1] = (int) (longsecond % 300000);

		return result;
	}

	/**
	 * 根据经度计算该点位于理想图幅分割的列序号
	 * 
	 * @param dLongitude
	 * @return 列序号，余数
	 */
	private static int[] calculateIdealColumnIndex(double dLongitude) {
		int[] result = new int[2];

		// 相对区域经度 = 绝对经度 - 60.0
		double regionLongitude = dLongitude - 60.0;

		// 相对的以秒为单位的经度
		double secondLongitude = regionLongitude * 3600;

		// 为避免浮点数的内存影响，将秒*10的三次方(由于0.00001度为0.036秒)
		long longsecond = (int) Math.floor(secondLongitude * 1000);

		result[0] = (int) (longsecond / 450000);

		result[1] = (int) (longsecond % 450000);

		return result;
	}

	/**
	 *  计算点所在的图幅号,如果点在图幅边界上,返回所有的图幅号
	 *  返回顺序,先上再下,先右再左
	 * @param lon
	 * @param lat
	 * @return
	 */
	public static List<String> lonlat2MeshIds(double lon, double lat) {

		List<String> rst = new ArrayList<String>();

		int pos = which25TMeshBorderPointAt(lon, lat);

		String sMeshId = lonlat2Mesh(lon, lat);

		rst.add(sMeshId);

		if (0 == pos) // 不在边框上
			return rst;

		if (0x01 == (pos & 0x0F)) // 上图框
			rst.add(meshLocator_25T(sMeshId, MeshLocateRelation.Bottom));

		if (0x10 == (pos & 0xF0)) // 右图框
		{
			String mesh = meshLocator_25T(sMeshId, MeshLocateRelation.Left);
			rst.add(mesh);
			if (0x01 == (pos & 0x0F)) // 上图框
				rst.add(meshLocator_25T(mesh, MeshLocateRelation.Bottom));
		}

		return rst;
	}

	/**
	 * 误差精度范围内,点是否在图廓边界上
	 * 
	 * @param 经纬度
	 * @return 0x01--上下图框，0x10--左右图框
	 */
	private static int which25TMeshBorderPointAt(double dLongitude,
			double dLatitude) {
		int model = 0;

		int[] result = calculateIdealRowIndex(dLatitude);

		int rowIndex = result[0];
		int remainder = result[1];

		switch (rowIndex % 3) {
		case 0: // 第一行
		{
			if (300000 - remainder == 12) // 余数距离上框等于0.012秒
				model |= 0x01;
			else if (remainder == 0)
				model |= 0x01;
		}
			break;
		case 1: // 第二行由于上下边框均不在其内，因此不在图框上
			break;
		case 2: // 第三行
		{
			if (remainder == 12) // 余数距离下框等于0.012秒
				model |= 0x01;
		}
			break;
		}

		result = calculateIdealColumnIndex(dLongitude);

		if (0 == result[1])
			model |= 0x10;

		return model;
	}

	/**
	 * 计算目标图幅的周边方位的图幅
	 * 
	 * @param mesh
	 * @param loc
	 * @return
	 */
	private static String meshLocator_25T(String mesh, MeshLocateRelation loc) {
		if (loc == MeshLocateRelation.Inside)
			return mesh;

		int M1 = Integer.valueOf(mesh.substring(0, 1));
		int M2 = Integer.valueOf(mesh.substring(1, 2));
		int M3 = Integer.valueOf(mesh.substring(2, 3));
		int M4 = Integer.valueOf(mesh.substring(3, 4));
		int M5 = Integer.valueOf(mesh.substring(4, 5));
		int M6 = Integer.valueOf(mesh.substring(5, 6));

		// 该图幅的左下角点
		double x = (M3 * 10 + M4) * 3600 + M6 * 450 + 60 * 3600;
		double y = (M1 * 10 + M2) * 2400 + M5 * 300;

		// 该图幅的中间点
		x += 450.0 / 2;
		y += 300.0 / 2;

		switch (loc) {
		case Top: {
			return lonlat2Mesh(x / 3600.0, (y + 300) / 3600.0);
		}
		case Bottom: {
			return lonlat2Mesh(x / 3600.0, (y - 300) / 3600.0);
		}
		case Left: {
			return lonlat2Mesh((x - 450) / 3600.0, y / 3600.0);
		}
		case Right: {
			return lonlat2Mesh((x + 450) / 3600.0, y / 3600.0);
		}
		case LeftTop: {
			return lonlat2Mesh((x - 450) / 3600.0, (y + 300) / 3600.0);
		}
		case LeftBottom: {
			return lonlat2Mesh((x - 450) / 3600.0, (y - 300) / 3600.0);
		}
		case RightTop: {
			return lonlat2Mesh((x + 450) / 3600.0, (y + 300) / 3600.0);
		}
		case RightBottom: {
			return lonlat2Mesh((x + 450) / 3600.0, (y - 300) / 3600.0);
		}
		default: {
			return mesh;
		}
		}
	}
}
