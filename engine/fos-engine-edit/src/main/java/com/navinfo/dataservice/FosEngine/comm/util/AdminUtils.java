package com.navinfo.dataservice.FosEngine.comm.util;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.FosEngine.edit.model.bean.ad.AdFace;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.selector.ad.AdFaceSelector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.GeometryRelationUtils;
import com.vividsolutions.jts.geom.Geometry;

public class AdminUtils {

	public static void SetAdminInfo4Link(RdLink link, Connection conn)
			throws Exception {

		Geometry linkGeo = link.getGeometry();
		
		String linkWkt = GeoTranslator.jts2Wkt(linkGeo, 0.00001, 5);
		
		AdFaceSelector selector = new AdFaceSelector(conn);
		
		List<AdFace> faces = selector.loadAdFaceByLinkGeometry(linkWkt, false);

		List<AdFace> allContainFaces = new ArrayList<AdFace>();
		List<AdFace> allLeftsideFaces = new ArrayList<AdFace>();
		List<AdFace> allRightsideFaces = new ArrayList<AdFace>();
 
		for (AdFace face : faces) {

			Geometry faceGeo = face.getGeometry();

			if (GeometryRelationUtils.RingContainLink(faceGeo, linkGeo)) {
				allContainFaces.add(face);
				continue;
			}

			if (GeometryRelationUtils.LinkOnRingSide(faceGeo, linkGeo)) {
				if (GeometryRelationUtils.IsLinkOnLeftOfRing(faceGeo, linkGeo)) {
					allLeftsideFaces.add(face);
				} else {
					allRightsideFaces.add(face);
				}
			}
		}

		int adminFlag = 0x00; // 左右行政区划号赋值标志

		for (AdFace face : allContainFaces) {
			if (0x00 == adminFlag) {

				link.setLeftRegionId(face.getRegionId());

				link.setRightRegionId(face.getRegionId());

				adminFlag = 0x11;
			}
		}

		for (AdFace face : allLeftsideFaces) {

			if (0x00 == adminFlag) {
				link.setLeftRegionId(face.getRegionId());

				adminFlag |= 0x10;
			}
		}

		for (AdFace face : allRightsideFaces) {
			if (0x00 == adminFlag || 0x10 == adminFlag) {
				link.setRightRegionId(face.getRegionId());
				
				adminFlag |= 0x01;
			}
		}
	}
}
