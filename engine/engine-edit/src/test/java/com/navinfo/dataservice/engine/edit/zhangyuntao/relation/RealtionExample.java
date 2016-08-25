package com.navinfo.dataservice.engine.edit.zhangyuntao.relation;

import java.sql.Connection;
import java.util.List;

import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * @Title: RealtionExample.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月16日 上午9:55:57
 * @version: v1.0
 */
public class RealtionExample extends InitApplication {

	public RealtionExample() {
	}

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void relate() {
		Connection conn;
		try {
			int linkPid = 100008708;
			Geometry geometry = this.loadLinkGeometry(linkPid);
			conn = DBConnector.getInstance().getConnectionById(42);
			LuFaceSelector selector = new LuFaceSelector(conn);
			List<LuFace> faces = selector.loadRelateFaceByGeometry(geometry);
			for (LuFace face : faces) {
				Geometry faceGeometry = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(face.getGeometry()),
						0.00001, 5);
				this.geoRelate(geometry, faceGeometry);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void geoRelate(Geometry link, Geometry face) {
		System.out.println("完全包含：" + GeoRelationUtils.Interior(link, face));
		System.out.println("内部两个交点：" + GeoRelationUtils.InteriorAnd2Intersection(link, face));
		System.out.println("内部壹个交点：" + GeoRelationUtils.InteriorAnd1Intersection(link, face));
		System.out.println("组成线上：" + GeoRelationUtils.Boundary(link, face));
		System.out.println("外部两个交点：" + GeoRelationUtils.ExteriorAnd2Intersection(link, face));
		System.out.println("外部部分重叠：" + GeoRelationUtils.ExteriorAndLineOverlap(link, face));
		System.out.println("穿过端点都在外：" + GeoRelationUtils.CrossAnd2IntersectExterior(link, face));
		System.out.println("穿过断点壹个在内：" + GeoRelationUtils.CrossAnd1IntersectExterior(link, face));
		System.out.println("在线的" + (GeoRelationUtils.IsLinkOnLeftOfRing(link, face) ? "行进方向左侧" : "行进方向右侧"));
	}

	private Geometry loadLinkGeometry(int linkPid) throws Exception {
		Connection conn = DBConnector.getInstance().getConnectionById(42);
		RdLinkSelector rdSelector = new RdLinkSelector(conn);
		JSONObject json = GeoTranslator.jts2Geojson(((RdLink) rdSelector.loadById(linkPid, false)).getGeometry());
		Geometry geometry = GeoTranslator.geojson2Jts(json, 0.00001, 5);
		return geometry;
	}
}
