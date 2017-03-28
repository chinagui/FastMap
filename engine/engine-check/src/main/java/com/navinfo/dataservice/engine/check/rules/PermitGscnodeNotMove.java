package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/*
 * @ClassName：PermitGscnodeNotMove
 * @author:Feng Haixia
 * @data:2017/03/27
 * @Description:创建或修改link，节点不能到已有的立交点处，请先删除立交关系
 */
public class PermitGscnodeNotMove extends baseRule {

	Map<Integer, List<Integer>> nodePidSet = new HashMap<Integer, List<Integer>>();
	Set<String> geometrySet = new HashSet<>();

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			prepareDataForLink(row);
			prepareDataForNode(row);
		}

		for (String geometryStr : geometrySet) {

			String sql = String.format(
					"SELECT COUNT(*) FROM RD_GSC p WHERE p.U_RECORD <> 2 AND sdo_Geom.relate(p.GEOMETRY,'EQUAL',sdo_geometry('%s', 8307), 0.5) = 'EQUAL'",
					geometryStr);

			List<Integer> resultSet = ExecuteSQL(sql);

			if (resultSet.get(0) == 0) {
				continue;
			}

			this.setCheckResult("", "", 0);
		} // 遍历linkPid
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
	}

	/*
	 * Function:新增link，分离节点记录起终点几何
	 */
	private void prepareDataForLink(IRow row) throws Exception {

		if (!(row instanceof RdLink) || row.status() == ObjStatus.DELETE) {
			return;
		}

		RdLink rdLink = (RdLink) row;
		Geometry geo = GeoTranslator.transform(rdLink.getGeometry(), 0.00001, 5);
		
		if (rdLink.changedFields().containsKey("geometry")) {
			JSONObject geojts = (JSONObject) rdLink.changedFields().get("geometry");
			geo = GeoTranslator.geojson2Jts(geojts);
		}
		
		Coordinate[] coords = geo.getCoordinates();

		if (coords.length < 2) {
			return;
		}

		String snodeGeo = GeoTranslator.jts2Wkt(GeoTranslator.createPoint(coords[0]));
		String enodeGeo = GeoTranslator.jts2Wkt(GeoTranslator.createPoint(coords[coords.length - 1]));

		this.geometrySet.add(snodeGeo);
		this.geometrySet.add(enodeGeo);
	}

	/*
	 * Function：修改端点几何，记录端点几何
	 */
	private void prepareDataForNode(IRow row) throws Exception {

		if (!(row instanceof RdNode) || row.status() != ObjStatus.UPDATE) {
			return;
		}

		RdNode rdNode = (RdNode) row;

		if (!rdNode.changedFields().containsKey("geometry")) {
			return;
		}

		JSONObject geojts = (JSONObject) rdNode.changedFields().get("geometry");
		Geometry geometry = GeoTranslator.geojson2Jts(geojts);
		String geoStr = GeoTranslator.jts2Wkt(geometry);

		this.geometrySet.add(geoStr);
	}

	/*
	 * 执行输入sql
	 */
	private List<Integer> ExecuteSQL(String sql) throws Exception {
		PreparedStatement pstmt = this.getConn().prepareStatement(sql.toString());
		ResultSet resultSet = pstmt.executeQuery();
		List<Integer> result = new ArrayList<Integer>();

		if (resultSet.next()) {
			result.add(resultSet.getInt(1));
		}

		resultSet.close();
		pstmt.close();

		return result;
	}
}
