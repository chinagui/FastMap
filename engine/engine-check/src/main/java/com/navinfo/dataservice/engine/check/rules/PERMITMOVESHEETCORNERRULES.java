package com.navinfo.dataservice.engine.check.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.search.RdNodeSearch;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * 前检查：图廓点，只能在图廓线上
 * 
 * @author Feng Haixia
 * @since 2017/4/12
 */
public class PERMITMOVESHEETCORNERRULES extends baseRule {

	Set<Coordinate> nodeSet = new HashSet<>();

	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			Coordinate coord = prepareNodeData(row);
			if (coord == null) {
				continue;
			}

			if (MeshUtils.isPointAtMeshBorder(coord.x, coord.y) == false) {
				this.setCheckResult("", "", 0);
			}
		}
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
	}

	/**
	 * 修改的node点如果是图廓点，记录图廓点几何
	 * 
	 * @param row
	 * @return
	 * @throws Exception
	 */
	private Coordinate prepareNodeData(IRow row) throws Exception {
		ObjType objType = row.objType();
		Coordinate coord = null;

		switch (objType) {
		case RDNODEFORM:
			RdNodeForm rdform = (RdNodeForm) row;
			int rdformkind = rdform.getFormOfWay();
			if (rdform.changedFields().containsKey("formOfWay")) {
				rdformkind = (int) rdform.changedFields().get("formOfWay");
			}
			if (rdformkind == 2) {
				RdNodeSearch search = new RdNodeSearch(this.getConn());
				RdNode rdNode1 = (RdNode) search.searchDataByPid(rdform.getNodePid());
				coord = changeNodeGeo(rdNode1, rdNode1.getGeometry());
			}
			break;
		case RWNODE:
			RwNode rwNode = (RwNode) row;
			if (IsSheet(rwNode)) {
				coord = changeNodeGeo(rwNode, rwNode.getGeometry());
			}
			break;
		case ZONENODE:
			ZoneNode zoneNode = (ZoneNode) row;
			if (IsSheet(zoneNode)) {
				coord = changeNodeGeo(zoneNode, zoneNode.getGeometry());
			}
			break;
		case ADNODE:
			AdNode adNode = (AdNode) row;
			if (IsSheet(adNode)) {
				coord = changeNodeGeo(adNode, adNode.getGeometry());
			}
			break;
		case LCNODE:
			LcNode lcNode = (LcNode) row;
			if (IsSheet(lcNode)) {
				coord = changeNodeGeo(lcNode, lcNode.getGeometry());
			}
			break;
		case LUNODE:
			LuNode luNode = (LuNode) row;
			if (IsSheet(luNode)) {
				coord = changeNodeGeo(luNode, luNode.getGeometry());
			}
			break;
		case CMGBUILDNODE:
			CmgBuildnode cmgNode = (CmgBuildnode) row;
			if (IsSheet(cmgNode)) {
				coord = changeNodeGeo(cmgNode, cmgNode.getGeometry());
			}
			break;
		default:
			break;
		}
		return coord;
	}

	// 铁路点是否为图廓点
	private boolean IsSheet(RwNode rwNode) {
		int nodeForm = changeNodeForm(rwNode, rwNode.getForm());
		if (nodeForm == 4) {
			return true;
		}
		return false;
	}

	// 区域点是否为图廓点
	private boolean IsSheet(ZoneNode zoneNode) {
		int nodeForm = changeNodeForm(zoneNode, zoneNode.getForm());
		if (nodeForm == 1) {
			return true;
		}
		return false;
	}

	// 行政区划点是否为图廓点
	private boolean IsSheet(AdNode adNode) {
		int nodeForm = changeNodeForm(adNode, adNode.getForm());
		if (nodeForm == 1) {
			return true;
		}
		return false;
	}

	// 土地覆盖点是否为图廓点
	private boolean IsSheet(LcNode lcNode) {
		int nodeForm = changeNodeForm(lcNode, lcNode.getForm());
		if (nodeForm == 1) {
			return true;
		}
		return false;
	}

	// 土地覆盖点是否为图廓点
	private boolean IsSheet(LuNode luNode) {
		int nodeForm = changeNodeForm(luNode, luNode.getForm());
		if (nodeForm == 1) {
			return true;
		}
		return false;
	}

	// CMG_BUILD_NODE是否为图廓点
	private boolean IsSheet(CmgBuildnode cmgNode) {
		int nodeForm = changeNodeForm(cmgNode, cmgNode.getForm());
		if (nodeForm == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 获取node的最新node形态
	 * 
	 * @param row
	 *            操作对象
	 * @param nodeForm
	 *            原node形态
	 * @return
	 */
	private int changeNodeForm(IRow row, int nodeForm) {
		int newNodeForm = nodeForm;
		if (row.changedFields().containsKey("form")) {
			newNodeForm = (int) row.changedFields().get("form");
		}
		return newNodeForm;
	}

	/**
	 * 记录当前修改的几何
	 * 
	 * @param row
	 * @param geo
	 * @return 几何点位
	 * @throws Exception
	 */
	private Coordinate changeNodeGeo(IRow row, Geometry geo) throws Exception {
		Geometry newGeo = geo;
		if (row.changedFields().containsKey("geometry")) {
			JSONObject geojson = (JSONObject) row.changedFields().get("geometry");
			newGeo = GeoTranslator.geojson2Jts(geojson);
		}
        Geometry newGeo2=GeoTranslator.transform(newGeo, 0.00001, 5);
		Coordinate[] coords = newGeo2.getCoordinates();
		Coordinate coord = coords[0];
		return coord;
	}
}
