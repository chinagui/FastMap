package com.navinfo.dataservice.commons.util;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class GeometryRelationUtils {

	public static boolean LinkCrossRing(Geometry ring, Geometry link) {
		return ring.relate(link, "**2T*1102");
	}

	public static boolean RingContainLink(Geometry ring, Geometry link) {
		return ring.relate(link, "1*2F*1FF2");
	}

	public static boolean LinkOnRingSide(Geometry ring, Geometry link) {
		return ring.relate(link, "FF210TFF2");
	}

	public static boolean IsLinkOnLeftOfRing(Geometry ring, Geometry link) throws Exception {

		if (GeometryRelationUtils.LinkOnRingSide(ring, link)) {
			return false;
		}

		List<Coordinate> pts = new ArrayList<Coordinate>();

		// 首先将Link的坐标从起点至终点依次填入

		Coordinate[] linkCoords = link.getCoordinates();

		for (int i = 0; i < linkCoords.length; i++) {
			pts.add(linkCoords[i]);
		}

		// 求取其余部分

		Geometry diffGeo = ring.difference(link);

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

			pts.add(linkCoords[0]); // 闭合环
		}

		else if (diffGeo instanceof MultiLineString) {

			AddMultiDiffPaths(pts, (MultiLineString) diffGeo);
		}
		
		RemoveDumiPoints(pts);
		
		//若所构成的环为逆时针则证明多变形在Link行进方向的左侧
		boolean isCCW = GeometryUtils.IsCCW((Coordinate[]) pts.toArray());

        return isCCW;
	}

	private static void AddMultiDiffPaths(List<Coordinate> pts,
			MultiLineString multidiff) {
		Geometry firstPath = null;

		Geometry secondPath = null;

		Coordinate lastPoint = pts.get(pts.size() - 1);

		Geometry firstLine = multidiff.getGeometryN(0);

		Coordinate[] coords = firstLine.getCoordinates();

		if (lastPoint.equals(coords[0])
				|| lastPoint.equals(coords[coords.length - 1])) {
			firstPath = multidiff.getGeometryN(0);
			secondPath = multidiff.getGeometryN(1);
		} else {
			firstPath = multidiff.getGeometryN(1);
			secondPath = multidiff.getGeometryN(0);
		}

		Coordinate[] firstCoords = firstPath.getCoordinates();

		Coordinate[] secondCoords = secondPath.getCoordinates();

		if (firstCoords[0].equals(pts.get(pts.size() - 1))) {// 第一条线顺序连接

			for (int i = 1; i < firstCoords.length; i++) {
				pts.add(firstCoords[i]);
			}

			if (pts.get(0).equals(secondCoords[secondCoords.length - 1])) { // 第二条线顺序连接
				for (int i = 1; i < secondCoords.length - 1; i++) {
					pts.add(secondCoords[i]);
				}
			} else {
				for (int i = secondCoords.length - 2; i > 0; i--) {
					pts.add(secondCoords[i]);
				}
			}
		} else if(firstCoords[firstCoords.length-1].equals(pts.get(pts.size()-1))){//第一条线逆序连接

			for(int i=firstCoords.length-1;i>0;i--){
				pts.add(firstCoords[i]);
			}
			
			if (pts.get(0).equals(secondCoords[secondCoords.length - 1])) { // 第二条线顺序连接
				for (int i = 1; i < secondCoords.length - 1; i++) {
					pts.add(secondCoords[i]);
				}
			} else {
				for (int i = secondCoords.length - 2; i > 0; i--) {
					pts.add(secondCoords[i]);
				}
			}
		}
		
		//封闭ring
        pts.add(pts.get(0));
	}
	
	private static void RemoveDumiPoints(List<Coordinate> pts)
    {
        Coordinate preNode = null;
        for (int i = 0; i < pts.size(); )
        {
            if (null == preNode || preNode != pts.get(i))
            {
                preNode = pts.get(i);
                i++;
                continue;
            }

            pts.remove(i);
        }
    }
}
