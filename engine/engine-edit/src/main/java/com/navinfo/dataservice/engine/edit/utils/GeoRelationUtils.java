package com.navinfo.dataservice.engine.edit.utils;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;

/**
 * @Title: GeoRelationUtils.java
 * @Description: 判断线与面的关系
 * @author zhangyt
 * @date: 2016年8月16日 下午5:26:57
 * @version: v1.0
 */
public abstract class GeoRelationUtils {

	/**
	 * link完全包含在ring内，不存在交点</br>
	 * 对应业务规则A
	 * 
	 * @param link
	 * @param ring
	 * @return
	 */
	public static boolean Interior(Geometry link, Geometry ring) {
		return link.relate(ring, "1FF0FF212");
	}

	public static boolean Interior(IntersectionMatrix matrix) {
		return matrix.matches("1FF0FF212");
	}

	/**
	 * link包含在ring内，两个端点在ring组成线上</br>
	 * 对应业务规则B
	 * 
	 * @param link
	 * @param ring
	 * @return
	 */
	public static boolean InteriorAnd2Intersection(Geometry link, Geometry ring) {
		return link.relate(ring, "1FFF0F212");
	}

	public static boolean InteriorAnd2Intersection(IntersectionMatrix matrix) {
		return matrix.matches("1FFF0F212");
	}

	/**
	 * link包含在ring内，一个端点在ring组成线上，壹个端点在ring内</br>
	 * 对应业务规则C
	 * 
	 * @param link
	 * @param ring
	 * @return
	 */
	public static boolean InteriorAnd1Intersection(Geometry link, Geometry ring) {
		return link.relate(ring, "1FF00F212");
	}

	public static boolean InteriorAnd1Intersection(IntersectionMatrix matrix) {
		return matrix.matches("1FF00F212");
	}

	/**
	 * link完全在ring的组成线上</br>
	 * 对应业务规则D
	 * 
	 * @param link
	 * @param ring
	 * @return
	 */
	public static boolean Boundary(Geometry link, Geometry ring) {
		return link.relate(ring, "F1FF0F212");
	}

	public static boolean Boundary(IntersectionMatrix matrix) {
		return matrix.matches("F1FF0F212");
	}

	/**
	 * link在ring外，两个端点在ring的组成线上</br>
	 * 对应业务规则E
	 * 
	 * @param link
	 * @param ring
	 * @return
	 */
	public static boolean ExteriorAnd2Intersection(Geometry link, Geometry ring) {
		return link.relate(ring, "FF1F0F212");
	}

	public static boolean ExteriorAnd2Intersection(IntersectionMatrix matrix) {
		return matrix.matches("FF1F0F212");
	}

	/**
	 * link在ring外，有一部分link与ring的组成线重叠，两个端点在ring外</br>
	 * 对应业务规则F
	 * 
	 * @param link
	 * @param ring
	 * @return
	 */
	public static boolean ExteriorAndLineOverlap(Geometry link, Geometry ring) {
		return link.relate(ring, "F11FF0212");
	}

	public static boolean ExteriorAndLineOverlap(IntersectionMatrix matrix) {
		return matrix.matches("F11FF0212");
	}

	/**
	 * link穿过ring的组成线，两个端点处在ring外部</br>
	 * 对应业务规则G
	 * 
	 * @param link
	 * @param ring
	 * @return
	 */
	public static boolean CrossAnd2IntersectExterior(Geometry link, Geometry ring) {
		return link.relate(ring, "101FF0212");
	}

	public static boolean CrossAnd2IntersectExterior(IntersectionMatrix matrix) {
		return matrix.matches("101FF0212");
	}

	/**
	 * link穿过ring的组成线，壹个端点处于ring外部，壹个端点处于ring内部</br>
	 * 对应业务规则H
	 * 
	 * @param link
	 * @param ring
	 * @return
	 */
	public static boolean CrossAnd1IntersectExterior(Geometry link, Geometry ring) {
		return link.relate(ring, "1010F0212");
	}

	public static boolean CrossAnd1IntersectExterior(IntersectionMatrix matrix) {
		return matrix.matches("1010F0212");
	}

	public static boolean IsLinkOnLeftOfRing(Geometry link, Geometry ring) {
		List<Coordinate> pts = new ArrayList<Coordinate>();
		// 首先将Link的坐标从起点至终点依次填入
		Coordinate[] linkCoords = link.getCoordinates();
		for (int i = 0; i < linkCoords.length; i++) {
			pts.add(linkCoords[i]);
		}
		// 求取其余部分
		Geometry diffGeo = ring.getBoundary().difference(link);
		if (diffGeo instanceof LineString) {
			// 将其余部分坐标填入
			Coordinate[] diffCoords = diffGeo.getCoordinates();
			if (pts.get(pts.size() - 1).equals(diffCoords[0])) {// 顺序连接
				for (int i = 1; i < diffCoords.length; i++) {
					pts.add(diffCoords[i]);
				}
			} else {
				for (int i = diffCoords.length - 1; i > 0; i--) {
					pts.add(diffCoords[i]);
				}
			}
		}
		// 若所构成的环为逆时针则证明多变形在Link行进方向的左侧
		boolean isCCW = false;
		try {
			isCCW = GeometryUtils.IsCCW(pts.toArray(new Coordinate[pts.size()]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isCCW;
	}
}