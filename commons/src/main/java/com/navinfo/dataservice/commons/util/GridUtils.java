package com.navinfo.dataservice.commons.util;

import java.util.List;

import com.navinfo.navicommons.geo.computation.CompGridUtil;

import net.sf.json.JSONArray;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/**
 * 网格的帮助类
 */
public class GridUtils {

	/**
	 * 获取网格的左下、右上端点的经纬度
	 * 
	 * @param gridId
	 * @return 左下经度、左下纬度、右上经度、右上纬度
	 */
	public static double[] grid2Location(String gridId) {

		double[] loc = MeshUtils.mesh2LocationLatLon(gridId.substring(0, 6));

		String last = gridId.substring(7, 8);

		double result[] = new double[4];

		if (last.equals("1")) {
			result[0] = loc[0];

			result[1] = loc[3];

			result[2] = loc[2];

			result[3] = loc[5];
		} else if (last.equals("2")) {
			result[0] = loc[2];

			result[1] = loc[3];

			result[2] = loc[4];

			result[3] = loc[5];
		} else if (last.equals("3")) {
			result[0] = loc[0];

			result[1] = loc[1];

			result[2] = loc[2];

			result[3] = loc[3];
		} else if (last.equals("4")) {
			result[0] = loc[2];

			result[1] = loc[1];

			result[2] = loc[4];

			result[3] = loc[3];
		} else {
			return null;
		}

		return result;
	}

	/**
	 * grid转wkt
	 * @param gridId
	 * @return
	 */
	public static String grid2Wkt(String gridId) {

		double[] loc = CompGridUtil.grid2Rect(gridId);

		double lbX = loc[0];
		double lbY = loc[1];
		double rtX = loc[2];
		double rtY = loc[3];
		return "POLYGON ((" + lbX + " " + lbY + ", " + lbX + " " + rtY + ", "
				+ rtX + " " + rtY + ", " + rtX + " " + lbY  + ", " + + lbX + " " + lbY + "))";

	}
	
	/**
	 * grid转wkt
	 * @param gridId
	 * @return
	 */
	public static String grids2Wkt(JSONArray grids) {

		double minLon = 180;

		double minLat = 90;

		double maxLon = -180;

		double maxLat = -90;

		for (int i = 0; i < grids.size(); i++) {
			int gridId = grids.getInt(i);

			double[] loc = CompGridUtil.grid2Rect(gridId);

			if (loc[0] < minLon) {
				minLon = loc[0];
			}

			if (loc[1] < minLat) {
				minLat = loc[1];
			}

			if (loc[2] > maxLon) {
				maxLon = loc[2];
			}

			if (loc[3] > maxLat) {
				maxLat = loc[3];
			}
		}

		return "POLYGON ((" + minLon + " " + minLat + ", " + minLon + " " + maxLat + ", "
				+ maxLon + " " + maxLat + ", " + maxLon + " " + minLat  + ", " + + minLon + " " + minLat + "))";

	}

	/**
	 * 获取经纬度所在网格号，根据左上原则
	 * 
	 * @param x
	 *            经度
	 * @param y
	 *            纬度
	 * @return 网格号
	 */
	public static String location2Grid(double x, double y) {
		String meshId = MeshUtils.location2Mesh(MeshUtils.decimal2Second(x),
				MeshUtils.decimal2Second(y));

		double[] data = MeshUtils.mesh2LocationLatLon(meshId);

		double cX = data[2];

		double cY = data[3];

		if (x <= cX) {
			if (y >= cY) {
				meshId += "01";
			} else {
				meshId += "03";
			}
		} else {
			if (y >= cY) {
				meshId += "02";
			} else {
				meshId += "04";
			}
		}

		return meshId;
	}

	/**
	 * 求出一系列grids外包矩形的geohash
	 * 
	 * @param grids
	 * @return
	 */
	public static String[] getEnclosingRectangle(JSONArray grids) {

		/**
		 * 求出外包矩形，最小经度、最小纬度、最大经度、最大纬度
		 */
		double minLon = 180;

		double minLat = 90;

		double maxLon = -180;

		double maxLat = -90;

		for (int i = 0; i < grids.size(); i++) {
			String grid = grids.getString(i);

			double[] loc = GridUtils.grid2Location(grid);

			if (loc[0] < minLon) {
				minLon = loc[0];
			}

			if (loc[1] < minLat) {
				minLat = loc[1];
			}

			if (loc[2] > maxLon) {
				maxLon = loc[2];
			}

			if (loc[3] > maxLat) {
				maxLat = loc[3];
			}
		}

		String startRowkey = GeoHash.geoHashStringWithCharacterPrecision(
				minLat, minLon, 12);

		String stopRowkey = GeoHash.geoHashStringWithCharacterPrecision(maxLat,
				maxLon, 12);

		return new String[] { startRowkey, stopRowkey };
	}

	/**
	 * 判断特定点是否在网格数组中
	 * 
	 * @param lon
	 * @param lat
	 * @param grids
	 * @return
	 */
	public static boolean isInGrids(double lon, double lat, JSONArray grids) {
		boolean flag = false;

		String grid = location2Grid(lon, lat);

		for (int i = 0; i < grids.size(); i++) {
			String g = grids.getString(i);
			if (grid.equals(g)) {
				flag = true;

				break;
			}
		}

		return flag;
	}

	/**
	 * geohash转换成经纬度
	 * 
	 * @param geohash
	 * @return
	 */
	public static double[] geohash2Lonlat(String geohash) {

		double lonlat[] = new double[2];

		WGS84Point p = GeoHash.fromGeohashString(geohash).getPoint();

		double lon = p.getLongitude();

		double lat = p.getLatitude();

		lonlat[0] = lon;

		lonlat[1] = lat;

		return lonlat;
	}

	public static void main(String[] args) {

		double[] data = grid2Location("59567201");

		System.out.println(data[0] + "," + data[1]);
		System.out.println(data[2] + "," + data[3]);

		String[] str = getEnclosingRectangle(JSONArray
				.fromObject(new String[] { "59567201" }));

		for (String s : str) {
			System.out.println(s);
		}

		System.out.println(GeoHash.geoHashStringWithCharacterPrecision(39, 116,
				12));

		System.out.println(GeoHash.geoHashStringWithCharacterPrecision(39, 117,
				12));

		System.out.println(GeoHash.geoHashStringWithCharacterPrecision(40, 117,
				12));

		System.out.println(GeoHash.geoHashStringWithCharacterPrecision(40, 116,
				12));
		for (int i = 0; i < 1000; i++) {

			System.out.println(UuidUtils.genUuid());
		}

		WGS84Point p = GeoHash.fromGeohashString("tuzhpe291z9x").getPoint();

		double lon = p.getLongitude();

		double lat = p.getLatitude();

		System.out.println(lon + "," + lat);

		/*
		 * data = grid2Location("59567202");
		 * System.out.println(location2Grid(data[0], data[1]));
		 * System.out.println(location2Grid(data[2], data[3]));
		 * 
		 * data = grid2Location("59567203");
		 * System.out.println(location2Grid(data[0], data[1]));
		 * System.out.println(location2Grid(data[2], data[3]));
		 * 
		 * data = grid2Location("59567204");
		 * System.out.println(location2Grid(data[0], data[1]));
		 * System.out.println(location2Grid(data[2], data[3]));
		 */

	}

}
