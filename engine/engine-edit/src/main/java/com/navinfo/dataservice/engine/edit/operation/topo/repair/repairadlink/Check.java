package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Check {

	// 形状点和形状点不能重合
	public void checkPointCoincide(double[][] ps) throws Exception {

		Set<String> set = new HashSet<String>();

		for (double[] p : ps) {
			set.add(p[0] + "," + ps[1]);
		}

		if (ps.length != set.size()) {
			throwException("形状点和形状点不能重合");
		}
	}

	// 对组成路口的node挂接的link线进行编辑操作时，不能分离组成路口的node点
	public void checkIsCrossNode(Connection conn, int nodePid) throws Exception {

		String sql = "select node_pid from rd_cross_node where node_pid = :1 and rownum =1";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, nodePid);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}

		resultSet.close();

		pstmt.close();

		if (flag) {

			throwException("对组成路口的node挂接的link线进行编辑操作时，不能分离组成路口的node点");
		}
	}

	// 该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续
	public void checkIsVia(Connection conn, int linkPid) throws Exception {
		String sql = "select link_pid from rd_lane_via where link_pid =:1 and rownum=1 union all select link_pid from rd_restriction_via where link_pid =:2 and rownum=1 union all select link_pid from rd_branch_via where link_pid =:3 and rownum=1 ";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, linkPid);

		pstmt.setInt(2, linkPid);

		pstmt.setInt(3, linkPid);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}

		resultSet.close();

		pstmt.close();

		if (flag) {

			throwException("该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续");
		}
	}

	// 相邻形状点不可过近，不能小于2m
	public void checkShapePointDistance(JSONObject geom) throws Exception {

		Geometry g = GeoTranslator.geojson2Jts(geom);

		Coordinate[] coords = g.getCoordinates();

		for (int i = 0; i < coords.length - 1; i++) {

			double distance = GeometryUtils.getDistance(coords[i].y,
					coords[i].x, coords[i + 1].y, coords[i + 1].x);

			if (distance <= 2) {
				throwException("相邻形状点不可过近，不能小于2m");
			}
		}
	}

	//背景：前检查“不允许对构成面的Link的端点处形状点，进行修形操作”
	public void PERMIT_MODIFICATE_POLYGON_ENDPOINT(Command command, Connection conn) throws Exception {
		int linkPid = command.getLinkPid();
		AdFaceSelector selector = new AdFaceSelector(conn);
		List<AdFace> faces = selector.loadAdFaceByLinkId(linkPid, false);

		if (command.getCatchInfos() == null || faces.size() == 0) {
			return;
		}
		
		for (int i = 0; i < command.getCatchInfos().size(); i++) {
			JSONObject obj = command.getCatchInfos().getJSONObject(i);
			int nodePid = obj.getInt("nodePid");
			if (faces.size() > 0 && nodePid != 0) {
				throwException("不允许对构成面的Link的端点处形状点，进行修形操作");
			}
		}
	}
	
	/**
	 * AD_LINK修形，背景面不能自相交
	 * 
	 * @param command
	 * @param conn
	 * @throws Exception
	 */
	public void checkIntersectFace(Command command, Connection conn) throws Exception {
		int linkpid = command.getLinkPid();
		Geometry g = command.getLinkGeom();
		
		List<AdFace> faces = command.getFaces();

		if (faces.size() == 0) {
			return;
		}

		AdLinkSelector linkselector = new AdLinkSelector(conn);

		for (AdFace face : faces) {
			List<IRow> topos = face.getFaceTopos();
			for (IRow row : topos) {
				AdFaceTopo topo = (AdFaceTopo) row;

				if (topo.getLinkPid() == linkpid) {
					continue;
				}

				AdLink link = (AdLink) linkselector.loadById(topo.getLinkPid(), true, false);
				if (g.crosses(GeoTranslator.transform(link.getGeometry(),0.00001,5))) {
					throwException("背景面不能自相交");
				}
			} // for
		} // for
	}
	
	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}

}
