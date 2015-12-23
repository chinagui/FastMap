package com.navinfo.dataservice.commons.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 2010-8-4 Time: 8:58:15
 * 地理坐标相关的工具类
 */
public abstract class MeshUtils {

	public static void main(String[] args) {
		int meshid = 595673;
		
		System.out.println(lonlat2Mesh(136.31313, 39.3131));
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
//		System.out
//				.println("==================================================");
//		Set<String> allMesh = get9NeighborMesh2("45172", 3);
//		int i = 0;
//		for (String s : allMesh) {
//			System.out.println("mesh" + (i + 1) + ":" + s);
//			i++;
//		}

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
	 * @param meshId
	 * @return 左下/中心坐标点/右上坐标点经纬度
	 */
	public static double[] mesh2LocationLatLon(String meshId) {
		int[] data = mesh2Location(meshId);
		
		double[] result = new double[data.length];
		
		for( int i=0; i<data.length; i++){
			result[i]=second2Decimal(data[i]);
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
	private static Set<String> get8NeighborMesh(String meshId) {
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
		meshes.add(location2Mesh(x, y + 300));
		meshes.add(location2Mesh(x + 450, y + 300));
		meshes.add(location2Mesh(x - 450, y));
		// meshes.put(location2Mesh(x, y));
		meshes.add(location2Mesh(x + 450, y));
		meshes.add(location2Mesh(x - 450, y - 300));
		meshes.add(location2Mesh(x, y - 300));
		meshes.add(location2Mesh(x + 450, y - 300));
		return meshes;
	}

	/**
	 * 
	 * @param meshIds
	 *            ：已半角逗号分隔开的图符号序列，如"595661,595662"
	 * @return 不包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
	 * @author XXW
	 */
	public static Set<String> getNeighborMeshSetButSelves(String[] meshIdArray) {
		if (meshIdArray != null && meshIdArray.length > 0) {
			Set<Set<String>> meshSets = new HashSet<Set<String>>();
			System.out.println(meshIdArray.length);
			for (String meshId : meshIdArray) {
				meshSets.add(get8NeighborMesh(meshId));
			}
			Set<String> meshSet = getNonDupMeshSet(meshSets);
			// 去除自身
			Iterator<String> it = meshSet.iterator();
			while (it.hasNext()) {
				String mesh = it.next();
				for (int i = 0; i < meshIdArray.length; i++) {
					if (mesh.equals(meshIdArray[i])) {
						it.remove();
						break;
					}
				}
			}
			return meshSet;
		} else {
			return null;
		}
	}

	/**
	 * 图幅列表去重
	 * 
	 * @param meshSets
	 * @return
	 * @author XXW
	 */
	public static Set<String> getNonDupMeshSet(Set<Set<String>> meshSets) {
		Set<String> targetMeshSet = new HashSet<String>();
		for (Set<String> meshSet : meshSets) {
			for (String mesh : meshSet) {
				if (!isExistsInMeshSet(mesh, targetMeshSet)) {
					targetMeshSet.add(mesh);
				}
			}
		}
		return targetMeshSet;
	}

	/**
	 * 图幅是否在图幅列表中存在
	 * 
	 * @param targetMesh
	 * @param meshSet
	 * @return
	 * @author XXW
	 */
	private static boolean isExistsInMeshSet(String targetMesh,
			Set<String> meshSet) {
		for (String mesh : meshSet) {
			if (targetMesh.equals(mesh)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取某图幅某等级下周边9个图幅
	 * 
	 * @param meshId
	 *            图幅ID
	 * @param level
	 *            等级
	 * @return 周边图幅列表
	 */
	public static Set<String> get9NeighborMesh2(String meshId, int level) {
		meshId = StringUtils.leftPad(meshId, 6, '0');
		Set<String> outMeshes = new HashSet<String>();
		if (level == 1) {
			outMeshes = get8NeighborMesh(meshId);
			outMeshes.add(meshId);
			return outMeshes;
		} else {
			Set<String> meshes = get9NeighborMesh2(meshId, level - 1);
			for (String mesh : meshes) {
				outMeshes.addAll(get8NeighborMesh(mesh));
				outMeshes.add(mesh);
			}
			return outMeshes;
		}
	}
}
