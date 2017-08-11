package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * @category 行政区划（道路关联）
 * @rule_desc 行政区划代表点点位不能落入土地利用类型为地上停车场的功能面上，否则报log
 * @author fhx
 * @since 2017/5/15
 */
public class GLM50200 extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			if (row.objType() == ObjType.ADADMIN) {
				checkByAdAdmin((AdAdmin) row);
			}

			if (row.objType() == ObjType.LUFACE) {
				CheckByLuFace((LuFace) row);
			}
		}
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
	}

	private void checkByAdAdmin(AdAdmin admin) throws Exception {
		// admin
		Geometry geo = admin.getGeometry();
		if (admin.changedFields().containsKey("geometry")) {
			geo = GeoTranslator.geojson2Jts((JSONObject) admin.changedFields().get("geometry"));
		}
		Geometry geoNew = GeoTranslator.transform(geo, 0.00001, 5);

		// luface
		int mesh = admin.getMeshId();
		StringBuilder str = new StringBuilder();

		str.append("SELECT geometry FROM LU_FACE WHERE MESH_ID = " + mesh);
		str.append(" AND KIND = " + 6);
		str.append(" AND U_RECORD <> 2");

		List<Object> resultList = executeQuery(str);

		for (Object obj : resultList) {
			Geometry luGeo = GeoTranslator.struct2Jts((STRUCT) obj);
			if (luGeo.intersects(geoNew) == false) {
				continue;
			}
			this.setCheckResult("", "", 0);
			return;
		}
	}

	private void CheckByLuFace(LuFace luFace) throws Exception {
		// luFace
		int kind = luFace.getKind();
		Geometry geo = luFace.getGeometry();

		if (luFace.changedFields().containsKey("kind")) {
			kind = (int) luFace.changedFields().get("kind");
		}

		// luFace非“地上停车场功能面”，不执行判断
		if (kind != 6)
			return;

		if (luFace.changedFields().containsKey("geometry")) {
			geo = GeoTranslator.geojson2Jts((JSONObject) luFace.changedFields().get("geometry"));
		}
		Geometry geoNew = GeoTranslator.transform(geo, 0.00001, 5);

		// AD_ADMIN
		int mesh = luFace.getMeshId();
		StringBuilder str = new StringBuilder();

		str.append("SELECT GEOMETRY FROM AD_ADMIN WHERE MESH_ID = " + mesh);
		str.append(" AND U_RECORD <> 2");

		List<Object> resultList = executeQuery(str);

		for (Object obj : resultList) {
			Geometry adminGeo = GeoTranslator.struct2Jts((STRUCT) obj);
			if (adminGeo.intersects(geoNew) == false) {
				continue;
			}
			this.setCheckResult("", "", 0);
			return;
		}
	}

	private List<Object> executeQuery(StringBuilder str) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		List<Object> resultList = new ArrayList<Object>();
		try {
			pstmt = this.getConn().prepareStatement(str.toString());
			resultSet = pstmt.executeQuery();
	
			while (resultSet.next()) {
				resultList.add(resultSet.getObject(1));
			}
		}catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return resultList;
	}
}
