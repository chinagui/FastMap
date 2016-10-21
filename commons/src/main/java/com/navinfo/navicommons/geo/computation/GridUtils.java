package com.navinfo.navicommons.geo.computation;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * 网格的帮助类
 */
public class GridUtils {
	
	public static void main(String[] args) {
		try{
//			JSONArray grids = JSONArray.fromObject(new Integer[]{60560303,60560302});
//			String wkt = GridUtils.grids2Wkt(grids);
			String[] grids = get9NeighborGrids("59567100");
			System.out.println(StringUtils.join(grids, ","));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

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
	
	public static Geometry grid2Geometry(String gridId) throws ParseException{
		
		String wkt = grid2Wkt(gridId);
		
		WKTReader reader = new WKTReader();
		
		return reader.read(wkt);
	}
	
	/**
	 * grid转wkt
	 * @param gridId
	 * @return
	 * @throws ParseException 
	 */
	public static String grids2Wkt(JSONArray grids) throws ParseException {

		Geometry geometry = null;
		
		for (int i = 0; i < grids.size(); i++) {
			String gridId = grids.getString(i);

			Geometry geo = grid2Geometry(gridId);
			
			if(geometry == null){
				geometry = geo;
			}
			else{
				geometry = geometry.union(geo);
			}
		}
		

		WKTWriter w = new WKTWriter();
		
		return w.write(geometry);
	}
	
	public static String grids2Wkt(Set<String> grids) throws ParseException {

		Geometry geometry = null;
		
		for (String gridId : grids) {
			
			Geometry geo = grid2Geometry(gridId);
			
			if(geometry == null){
				geometry = geo;
			}
			else{
				geometry = geometry.union(geo);
			}
		}
		

		WKTWriter w = new WKTWriter();
		
		return w.write(geometry);
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
	
	public static String getNeighborGrid(String gridId,TopoLocation loc){
		gridId = StringUtils.leftPad(gridId, 8, '0');
		int m12 = Integer.valueOf(gridId.substring(0, 2));
		int m34 = Integer.valueOf(gridId.substring(2, 4));
		int m5 = Integer.valueOf(gridId.substring(4, 5));
		int m6 = Integer.valueOf(gridId.substring(5, 6));
		int m7 = Integer.valueOf(gridId.substring(6, 7));
		int m8 = Integer.valueOf(gridId.substring(7, 8));
		switch(loc){
		case Top:
			if((++m7)>3){
				m7=0;
				if((++m5)>7){m12++;m5=0;}
			}
			break;
		case Bottom:
			if((--m7)<0){
				m7=3;
				if((--m5)<0){m12--;m5=7;}
			}
			break;
		case Left:
			if((--m8)<0){
				m8=3;
				if((--m6)<0){m34--;m6=7;}
			}
			break;
		case Right:
			if((++m8)>3){
				m8=0;
				if((++m6)>7){m34++;m6=0;}
			}
			break;
		case LeftTop:
			if((++m7)>3){
				m7=0;
				if((++m5)>7){m12++;m5=0;}
			}
			if((--m8)<0){
				m8=3;
				if((--m6)<0){m34--;m6=7;}
			}
			break;
		case LeftBottom:
			if((--m7)<0){
				m7=3;
				if((--m5)<0){m12--;m5=7;}
			}
			if((--m8)<0){
				m8=3;
				if((--m6)<0){m34--;m6=7;}
			}
			break;
		case RightTop:
			if((++m7)>3){
				m7=0;
				if((++m5)>7){m12++;m5=0;}
			}
			if((++m8)>3){
				m8=0;
				if((++m6)>7){m34++;m6=0;}
			}
			break;
		case RightBottom:
			if((--m7)<0){
				m7=3;
				if((--m5)<0){m12--;m5=7;}
			}
			if((++m8)>3){
				m8=0;
				if((++m6)>7){m34++;m6=0;}
			}
			break;
		default:
			break;
		}
		return String.format("%02d%02d%d%d%d%d", m12, m34, m5, m6,m7,m8);
	}

	/**
	 * 计算grid周边的9个grid
	 * [0]为本身，从1-8的顺序为左下开始逆时针方向
	 * @param meshId
	 * @return
	 */
	public static String[] get9NeighborGrids(String gridId) {
		String allGrids[] = new String[9];
		allGrids[0] = gridId;
		allGrids[1] = getNeighborGrid(gridId,TopoLocation.LeftBottom);
		allGrids[2] = getNeighborGrid(gridId,TopoLocation.Bottom);
		allGrids[3] = getNeighborGrid(gridId,TopoLocation.RightBottom);
		allGrids[4] = getNeighborGrid(gridId,TopoLocation.Right);
		allGrids[5] = getNeighborGrid(gridId,TopoLocation.RightTop);
		allGrids[6] = getNeighborGrid(gridId,TopoLocation.Top);
		allGrids[7] = getNeighborGrid(gridId,TopoLocation.LeftTop);
		allGrids[8] = getNeighborGrid(gridId,TopoLocation.Left);
		return allGrids;

	}

}
