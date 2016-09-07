package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGateCondition;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gate.RdGateSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		try {
			updateRdGate(result);
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	public void updateRdGate(Result result) throws Exception {
		JSONObject content = command.getContent();

		RdGate rdGate = command.getRdGate();

		boolean isChanged = rdGate.fillChangeFields(content);

		if (isChanged) {
			result.insertObject(rdGate, ObjStatus.UPDATE, rdGate.pid());
			result.setPrimaryPid(rdGate.pid());
		}

		if (content.containsKey("condition")) {
			updateCondition(result, content.getJSONArray("condition"));
		}
	}

	@SuppressWarnings("unchecked")
	private void updateCondition(Result result, JSONArray array) throws Exception {
		try {
			Iterator<JSONObject> iterator = array.iterator();
			RdGateCondition condition = null;
			JSONObject conditionObj = null;
			while (iterator.hasNext()) {
				conditionObj = iterator.next();
				if (conditionObj.containsKey("objStatus")) {
					String objStatus = conditionObj.getString("objStatus");
					if (ObjStatus.INSERT.toString().equals(objStatus)) {
						condition = new RdGateCondition();
						condition.setPid(this.command.getPid());
						result.insertObject(condition, ObjStatus.INSERT, condition.getPid());
					} else {
						condition = this.command.getRdGate().rdGateConditionMap.get(conditionObj.getString("rowId"));
						if (ObjStatus.UPDATE.toString().equals(objStatus)) {
							boolean isChange = condition.fillChangeFields(conditionObj);
							if (isChange) {
								result.insertObject(condition, ObjStatus.UPDATE, condition.getPid());
							}
						} else if (ObjStatus.DELETE.toString().equals(objStatus)) {
							result.insertObject(condition, ObjStatus.DELETE, condition.getPid());
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * 
	 * @param linkPid
	 * @param newLinks
	 * @param result
	 * @throws Exception
	 */
	public void breakRdLink(int linkPid, List<RdLink> newLinks, Result result) throws Exception {
		if (conn == null) {
			return;
		}

		RdGateSelector gateSelector = new RdGateSelector(conn);
		List<RdGate> rdGateList = gateSelector.loadByLink(linkPid, true);
		for (RdGate gate : rdGateList) {
			if (gate.getInLinkPid() == linkPid) {
				// 打断进入线
				int newInLink = 0;
				for (RdLink newLink : newLinks) {
					if (newLink.getsNodePid() == gate.getNodePid() || newLink.geteNodePid() == gate.getNodePid()) {
						newInLink = newLink.getPid();
						break;
					}
				}
				if (newInLink != 0) {
					JSONObject content = new JSONObject();
					content.put("inLinkPid", newInLink);
					boolean isChanged = gate.fillChangeFields(content);
					if (isChanged) {
						result.insertObject(gate, ObjStatus.UPDATE, gate.pid());
						result.setPrimaryPid(gate.pid());
					}
				} else {
					throw new Exception("错误的大门数据:" + gate.getPid());
				}

			} else {
				// 打断退出线
				int newInLink = 0;
				for (RdLink newLink : newLinks) {
					if (newLink.getsNodePid() == gate.getNodePid() || newLink.geteNodePid() == gate.getNodePid()) {
						newInLink = newLink.getPid();
						break;
					}
				}
				if (newInLink != 0) {
					JSONObject content = new JSONObject();
					content.put("outLinkPid", newInLink);
					boolean isChanged = gate.fillChangeFields(content);
					if (isChanged) {
						result.insertObject(gate, ObjStatus.UPDATE, gate.pid());
						result.setPrimaryPid(gate.pid());
					}
				} else {
					throw new Exception("错误的大门数据:" + gate.getPid());
				}
			}
		}
	}

}
