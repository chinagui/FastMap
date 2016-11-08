package com.navinfo.dataservice.engine.edit.operation.obj.rdnode.depart;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;

public class Operation {

	private Connection conn;

	public Operation(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 获取目标link之间的挂接nodePid
	 * 
	 * @param sNodePid
	 * @param eNodePid
	 * @param targetLinks
	 * @return
	 */
	private List<Integer> getConnectNodePid(int sNodePid, int eNodePid,
			List<RdLink> targetLinks) {

		List<Integer> nodePids = new ArrayList<Integer>();

		for (RdLink link : targetLinks) {

			if (!nodePids.contains(link.getsNodePid())) {

				nodePids.add(link.getsNodePid());
			}

			if (!nodePids.contains(link.geteNodePid())) {

				nodePids.add(link.geteNodePid());
			}

			// 过滤端点
			if (nodePids.contains(sNodePid)) {

				nodePids.remove((Integer) sNodePid);
			}

			if (nodePids.contains(eNodePid)) {

				nodePids.remove((Integer) eNodePid);
			}
		}

		return nodePids;
	}

	/**
	 * 获取目标link之间的挂接node
	 * 
	 * @param sNodePid
	 * @param eNodePid
	 * @param targetLinks
	 * @return
	 */
	private List<RdNode> getConnectNode(List<Integer> nodePids, Result result)
			throws Exception {

		List<RdNode> nodes = new ArrayList<RdNode>();

		for (IRow row : result.getUpdateObjects()) {

			if (!(row instanceof RdNode)) {

				continue;
			}

			RdNode node = (RdNode) row;

			if (nodePids.contains(node.getPid())) {

				nodes.add(node);

				nodePids.remove((Integer) node.getPid());
			}
		}

		if (nodePids.size() > 0) {

			RdNodeSelector nodeSelector = new RdNodeSelector(this.conn);

			List<IRow> nodeRows = nodeSelector.loadByIds(nodePids, true, true);

			for (IRow nodeRow : nodeRows) {

				RdNode node = (RdNode) nodeRow;

				nodes.add(node);
			}
		}

		return nodes;
	}

	/**
	 * 处理图幅号
	 * 
	 * @return 是否为图廓点
	 */
	private boolean handleMesh(RdNode node, Result result) throws Exception {

		Geometry oldGeo = GeoTranslator.transform(node.getGeometry(), 0.00001,
				5);

		Set<String> oldMeshes = CompGeometryUtil
				.geoToMeshesWithoutBreak(oldGeo);

		if (!node.changedFields().containsKey("geometry")) {

			if (oldMeshes.size() > 1) {
				return true;
			} else {
				return false;
			}
		}

		JSONObject jsonGeo = (JSONObject) node.changedFields().get("geometry");

		Geometry newGeo = GeoTranslator.geojson2Jts(jsonGeo, 0.00001, 5);

		Set<String> newMeshes = CompGeometryUtil
				.geoToMeshesWithoutBreak(newGeo);

		boolean isMeshNode = false;

		if (newMeshes.size() > 1) {

			isMeshNode = true;
		}

		// new、old幅图并集
		Set<String> meshes = new HashSet<String>();

		meshes.addAll(oldMeshes);
		meshes.addAll(newMeshes);

		// new、old图幅一致清空new图幅
		if (newMeshes.size() == oldMeshes.size()
				&& meshes.size() == newMeshes.size()) {

			newMeshes.clear();
		}

		// 图幅处理
		if (newMeshes.size() > 0) {
			// 删除旧图幅
			for (IRow meshRow : node.getMeshes()) {

				RdNodeMesh mesh = (RdNodeMesh) meshRow;

				result.insertObject(mesh, ObjStatus.DELETE, mesh.getNodePid());
			}

			// 增加新图幅
			for (String meshId : newMeshes) {

				RdNodeMesh newMesh = new RdNodeMesh();

				newMesh.setNodePid(node.getPid());

				newMesh.setMeshId(Integer.parseInt(meshId));

				result.insertObject(newMesh, ObjStatus.INSERT,
						newMesh.getNodePid());
			}
		}

		return isMeshNode;
	}

	/**
	 * NODE形态：原线组中的NODE，上下线分离后新生成两个node，新生成的node形态都修改为“无属性”（
	 * 继承原本PID和新生成的PIDnode形态值都维护为“无属性”），如果在图廓线上则形态为“图廓点” 图幅号：维护图幅变更信息
	 * 
	 * @param sNodePid
	 * @param eNodePid
	 * @param targetLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String updownDepart(int sNodePid, int eNodePid,
			List<RdLink> targetLinks, Result result) throws Exception {

		String msg = "";

		// 目标link之间的挂接nodePid
		List<Integer> nodePids = getConnectNodePid(sNodePid, eNodePid,
				targetLinks);

		if (nodePids.size() == 0) {
			return msg;
		}

		// 目标link之间的挂接node
		List<RdNode> nodes = getConnectNode(nodePids, result);

		for (RdNode node : nodes) {

			// 图幅处理
			boolean isMeshNode = handleMesh(node, result);

			// 形态处理
			for (IRow formRow : node.getForms()) {

				RdNodeForm form = (RdNodeForm) formRow;

				result.insertObject(form, ObjStatus.DELETE, form.getNodePid());
			}

			RdNodeForm newForm = new RdNodeForm();

			newForm.setNodePid(node.getPid());

			// 在图廓线上，形态为“图廓点”
			if (isMeshNode) {

				newForm.setFormOfWay(2);
			}

			result.insertObject(newForm, ObjStatus.INSERT, newForm.getNodePid());
		}

		return msg;
	}

}
