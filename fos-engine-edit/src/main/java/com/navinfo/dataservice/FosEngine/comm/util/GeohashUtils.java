package com.navinfo.dataservice.FosEngine.comm.util;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

public class GeohashUtils {

	public static boolean isWithin(String rowkey,double minLon,double maxLon,double minLat,double maxLat){
		
		boolean flag = true;

		GeoHash hash = GeoHash.fromGeohashString(rowkey.substring(0, 12));
		
		WGS84Point point = hash.getPoint();
		
		double lon = point.getLongitude();
		
		double lat = point.getLatitude();
		
		if (lon < minLon || lon >maxLon || lat < minLat || lat > maxLat){
			return false;
		}
		
		return flag;
	}
	
	public static void main(String[] args) {
		
		System.out.println(isWithin("wx4em7bv1754", 116.25, 116.3125, 39.958333, 40.0));
	}
}
