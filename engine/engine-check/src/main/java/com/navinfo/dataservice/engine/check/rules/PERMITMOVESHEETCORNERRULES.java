package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNodeMesh;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnodeMesh;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lc.LcNodeMesh;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNodeMesh;
import com.navinfo.dataservice.dao.glm.search.RdNodeSearch;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * 后检查：图廓点，只能在图廓线上
 * 
 * @author Feng Haixia
 * @since 2017/4/12
 * @change 2017/5/9
 */
public class PERMITMOVESHEETCORNERRULES extends baseRule {

	Set<Coordinate> nodeSet = new HashSet<>();

	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			prepareNodeData(row);
		}
	}

	/**
	 * 修改的node点如果是图廓点，记录图廓点几何
	 * 
	 * @param row
	 * @return
	 * @throws Exception
	 */
	private void prepareNodeData(IRow row) throws Exception {
		ObjType objType = row.objType();

		switch (objType) {
		case RDNODEFORM:
			RdNodeForm rdform = (RdNodeForm) row;
			int rdformkind = rdform.getFormOfWay();
			if (rdform.changedFields().containsKey("formOfWay")) {
				rdformkind = (int) rdform.changedFields().get("formOfWay");
			}
			if (rdformkind == 2) {
				RdNodeSearch search = new RdNodeSearch(this.getConn());
				RdNode rdNode = (RdNode) search.searchDataByPid(rdform.getNodePid());
				executeCheck(rdNode.getGeometry(), rdNode.getPid(),
						((RdNodeMesh) rdNode.getMeshes().get(0)).getMeshId(), "RD_NODE");
			}
			break;
		case RWNODE:
			RwNode rwNode = (RwNode) row;
			if (IsSheet(rwNode)) {
				executeCheck(rwNode.getGeometry(), rwNode.getPid(),
						((RwNodeMesh) rwNode.getMeshes().get(0)).getMeshId(), "RW_NODE");
			}
			break;
		case ZONENODE:
			ZoneNode zoneNode = (ZoneNode) row;
			if (IsSheet(zoneNode)) {
				executeCheck(zoneNode.getGeometry(), zoneNode.getPid(),
						((ZoneNodeMesh) zoneNode.getMeshes().get(0)).getMeshId(), "ZONE_NODE");
			}
			break;
		case ADNODE:
			AdNode adNode = (AdNode) row;
			if (IsSheet(adNode)) {
				executeCheck(adNode.getGeometry(), adNode.getPid(),
						((AdNodeMesh) adNode.getMeshes().get(0)).getMeshId(), "AD_NODE");
			}
			break;
		case LCNODE:
			LcNode lcNode = (LcNode) row;
			if (IsSheet(lcNode)) {
				executeCheck(lcNode.getGeometry(), lcNode.getPid(),
						((LcNodeMesh) lcNode.getMeshes().get(0)).getMeshId(), "LC_NODE");
			}
			break;
		case LUNODE:
			LuNode luNode = (LuNode) row;
			if (IsSheet(luNode)) {
				executeCheck(luNode.getGeometry(), luNode.getPid(),
						((LuNodeMesh) luNode.getMeshes().get(0)).getMeshId(), "LU_NODE");
			}
			break;
		case CMGBUILDNODE:
			CmgBuildnode cmgNode = (CmgBuildnode) row;
			if (IsSheet(cmgNode)) {
				executeCheck(cmgNode.getGeometry(), cmgNode.getPid(),
						((CmgBuildnodeMesh) cmgNode.getMeshes().get(0)).getMeshId(), "CMG_BUILDNODE");
			}
			break;
		default:
			break;
		}
		return;
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
	 * 执行检查的具体逻辑：判断图廓点的图幅是否为2
	 * 
	 * @param geo
	 *            node点几何
	 * @param nodePid
	 *            node点的pid
	 * @param meshID
	 *            对应的图幅号（一个）
	 * @param type
	 *            类型
	 * @throws Exception
	 */
	private void executeCheck(Geometry geo, int nodePid, int meshID, String type) throws Exception {
		Geometry geoNew = GeoTranslator.transform(geo, 0.00001, 5);
		Coordinate[] coords = geoNew.getCoordinates();
		Coordinate coord = coords[0];

		if (coord == null) {
			return;
		}

		if (MeshUtils.isPointAtMeshBorder(coord.x, coord.y) == false) {
			this.setCheckResult(geo, "[" + type + "," + nodePid + "]", meshID);
		}
	}
}
