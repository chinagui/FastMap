package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossName;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;

public class Operation implements IOperation {

	private Command command;

	private RdCross cross;

	private Connection conn;

	public Operation(Command command, RdCross cross, Connection conn) {
		this.command = command;

		this.cross = cross;

		this.conn = conn;

	}
	
	public Operation(Connection conn) {

		this.conn = conn;
	}

	private String updateProperty(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(cross, ObjStatus.DELETE, cross.pid());

				return null;
			} else {

				boolean isChanged = cross.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(cross, ObjStatus.UPDATE, cross.pid());
				}
			}
		}

//		if (content.containsKey("nodes")) {
//			JSONArray nodes = content.getJSONArray("nodes");
//
//			for (int i = 0; i < nodes.size(); i++) {
//
//				JSONObject nodeJson = nodes.getJSONObject(i);
//
//				if (nodeJson.containsKey("objStatus")) {
//
//					if (!ObjStatus.INSERT.toString().equals(nodeJson.getString("objStatus"))) {
//
//						RdCrossNode node = cross.nodeMap.get(nodeJson.getString("rowId"));
//
//						if (node == null) {
//							throw new Exception("rowId=" + nodeJson.getString("rowId") + "的rd_cross_node不存在");
//						}
//
//						if (ObjStatus.DELETE.toString().equals(nodeJson.getString("objStatus"))) {
//							result.insertObject(node, ObjStatus.DELETE, cross.pid());
//
//							continue;
//						} else if (ObjStatus.UPDATE.toString().equals(nodeJson.getString("objStatus"))) {
//
//							boolean isChanged = node.fillChangeFields(nodeJson);
//
//							if (isChanged) {
//								result.insertObject(node, ObjStatus.UPDATE, cross.pid());
//							}
//						}
//					} else {
//						RdCrossNode node = new RdCrossNode();
//
//						node.Unserialize(nodeJson);
//
//						node.setPid(cross.getPid());
//
//						node.setMesh(cross.mesh());
//
//						result.insertObject(node, ObjStatus.INSERT, cross.pid());
//
//						continue;
//					}
//				}
//
//			}
//		}
//
//		if (content.containsKey("links")) {
//			JSONArray links = content.getJSONArray("links");
//
//			for (int i = 0; i < links.size(); i++) {
//
//				JSONObject json = links.getJSONObject(i);
//
//				if (json.containsKey("objStatus")) {
//
//					if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
//
//						RdCrossLink node = cross.linkMap.get(json.getString("rowId"));
//
//						if (node == null) {
//							throw new Exception("rowId=" + json.getString("rowId") + "的rd_cross_link不存在");
//						}
//
//						if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
//							result.insertObject(node, ObjStatus.DELETE, cross.pid());
//
//							continue;
//						} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
//
//							boolean isChanged = node.fillChangeFields(json);
//
//							if (isChanged) {
//								result.insertObject(node, ObjStatus.UPDATE, cross.pid());
//							}
//						}
//					} else {
//						RdCrossLink link = new RdCrossLink();
//
//						link.Unserialize(json);
//
//						link.setPid(cross.getPid());
//
//						link.setMesh(cross.mesh());
//
//						result.insertObject(link, ObjStatus.INSERT, cross.pid());
//
//						continue;
//					}
//				}
//
//			}
//		}

		if (content.containsKey("names")) {
			JSONArray array = content.getJSONArray("names");

			for (int i = 0; i < array.size(); i++) {

				JSONObject json = array.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

						RdCrossName name = cross.nameMap.get(json.getString("rowId"));

						if (name == null) {
							throw new Exception("rowId=" + json.getString("rowId") + "的rd_cross_name不存在");
						}

						if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
							result.insertObject(name, ObjStatus.DELETE, cross.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

							boolean isChanged = name.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(name, ObjStatus.UPDATE, cross.pid());
							}
						}
					} else {
						RdCrossName name = new RdCrossName();

						name.Unserialize(json);

						name.setNameId(PidUtil.getInstance().applyRdCrossNameId());

						name.setPid(cross.getPid());

						name.setMesh(cross.mesh());

						result.insertObject(name, ObjStatus.INSERT, cross.pid());

						continue;
					}
				}

			}
		}

		return null;
	}

	private String updateNodeLink(Result result) throws Exception {

		JSONObject content = command.getContent();

		JSONArray nodePidArray = content.getJSONArray("nodePids");

		JSONArray linkPidArray = content.getJSONArray("linkPids");

		if (nodePidArray.size() == 1 && cross.getType() == 1) {
			cross.changedFields().put("type", 0);

			result.insertObject(cross, ObjStatus.UPDATE, cross.pid());
		} else if (nodePidArray.size() > 1 && cross.getType() == 0) {
			cross.changedFields().put("type", 1);

			result.insertObject(cross, ObjStatus.UPDATE, cross.pid());
		}

		for (IRow row : cross.getNodes()) {
			RdCrossNode node = (RdCrossNode) row;

			int nodePid = node.getNodePid();

			int index = nodePidArray.indexOf(nodePid);
			if (index == -1) {
				result.insertObject(node, ObjStatus.DELETE, cross.pid());
			} else {
				nodePidArray.remove(index);
			}
		}

		for (int i = 0; i < nodePidArray.size(); i++) {
			int nodePid = nodePidArray.getInt(i);

			RdCrossNode node = new RdCrossNode();

			node.setPid(cross.getPid());

			node.setNodePid(nodePid);

			node.setMesh(cross.mesh());

			result.insertObject(node, ObjStatus.INSERT, cross.pid());
		}

		ISelector selector = new AbstractSelector(RdLinkForm.class, conn);

		for (IRow row : cross.getLinks()) {
			RdCrossLink crosslink = (RdCrossLink) row;

			int linkPid = crosslink.getLinkPid();

			int index = linkPidArray.indexOf(linkPid);
			if (index == -1) {
				result.insertObject(crosslink, ObjStatus.DELETE, cross.pid());

				// 维护道路形态
				List<IRow> forms = selector.loadRowsByParentId(linkPid, true);

				boolean needDelete = true;
				IRow deleteRow = null;

				for (IRow formrow : forms) {

					RdLinkForm form = (RdLinkForm) formrow;

					if (form.getFormOfWay() == 33) {// 环岛
						needDelete = false;
					} else if (form.getFormOfWay() == 50) { // 交叉点内道路
						form.changedFields().put("formOfWay", 1);
						deleteRow = form;
					}
				}

				if (needDelete && deleteRow != null) {

					if (forms.size() == 1) {
						result.insertObject(deleteRow, ObjStatus.UPDATE, cross.pid());
					} else {
						result.insertObject(deleteRow, ObjStatus.DELETE, cross.pid());
					}
				}
			} else {
				linkPidArray.remove(index);
			}
		}

		for (int i = 0; i < linkPidArray.size(); i++) {
			int linkPid = linkPidArray.getInt(i);

			RdCrossLink link = new RdCrossLink();

			link.setPid(cross.getPid());

			link.setLinkPid(linkPid);

			link.setMesh(cross.mesh());

			result.insertObject(link, ObjStatus.INSERT, cross.pid());

			// 维护道路形态
			List<IRow> forms = selector.loadRowsByParentId(linkPid, true);

			boolean needAdd = true;

			IRow editRow = null;

			for (IRow formrow : forms) {

				RdLinkForm form = (RdLinkForm) formrow;

				if (form.getFormOfWay() == 33) {// 环岛
					needAdd = false;
				} else if (form.getFormOfWay() == 1) {
					form.changedFields().put("formOfWay", 50);

					editRow = form;
				}
			}

			if (needAdd) {
				if (editRow != null) {
					result.insertObject(editRow, ObjStatus.UPDATE, linkPid);
				} else {
					RdLinkForm form = new RdLinkForm();

					form.setLinkPid(linkPid);

					form.setMesh(cross.mesh());

					form.setFormOfWay(50);

					result.insertObject(form, ObjStatus.INSERT, cross.pid());
				}
			}

		}

		return null;
	}
	
	public String breakRdLink(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

		boolean isCrossLink = false;

		RdLink breakLink = oldLink;

		for (IRow row : breakLink.getForms()) {
			
			RdLinkForm form = (RdLinkForm) row;

			if (form.getFormOfWay() == 50) {
				
				isCrossLink = true;
				
				break;
			}
		}

		if (isCrossLink) {
			
			RdCrossSelector crossSelector = new RdCrossSelector(conn);

			List<Integer> linkPid = new ArrayList<>();

			linkPid.add(breakLink.getPid());

			List<RdCross> crossList = crossSelector.loadRdCrossByNodeOrLink(null, linkPid, true);
			
			if(CollectionUtils.isEmpty(crossList))
			{
				return null;
			}
			
			RdCross cross = crossList.get(0);

			// 是路口内link的，需要新增路口点和路口组成link，删除原路口组成link
			Set<Integer> nodePids = new HashSet<Integer>();

			for (RdLink link : newLinks) {

				nodePids.add(link.getsNodePid());

				nodePids.add(link.geteNodePid());
			}

			nodePids.remove(oldLink.getsNodePid());

			nodePids.remove(oldLink.geteNodePid());
			
			
			for (int nodePid : nodePids) {
				// 新增路口点 rd_cross_node

				RdCrossNode crossNode = new RdCrossNode();

				crossNode.setPid(cross.getPid());

				crossNode.setNodePid(nodePid);

				result.insertObject(crossNode, ObjStatus.INSERT,
						crossNode.getPid());
			}

			for (RdLink link : newLinks) {
				// 新增路口组成link rd_cross_link
				RdCrossLink crossLink = new RdCrossLink();

				crossLink.setPid(cross.getPid());

				crossLink.setLinkPid(link.getPid());

				result.insertObject(crossLink, ObjStatus.INSERT,
						crossLink.getPid());
			}

			// 删除原路口组成link
			for (IRow row : cross.getLinks()) {

				RdCrossLink crosLink = (RdCrossLink) row;

				if (crosLink.getLinkPid() == breakLink.getPid()) {

					result.insertObject(crosLink, ObjStatus.DELETE,
							crosLink.getPid());
					// 打断前只有一条，找到后跳出循环，提高维护效率
					break;
				}
			}
		}


		return null;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

//		if (content.containsKey("nodePids") || content.containsKey("linkPids")) {
//			return updateNodeLink(result);
//		} else {
			return updateProperty(result);
//		}

	}
}
