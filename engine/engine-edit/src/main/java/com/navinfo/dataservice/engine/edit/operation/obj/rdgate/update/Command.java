package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGateCondition;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	
	private String requester;
	private RdGate rdGate;

	public RdGate getRdGate() {
		return rdGate;
	}

	public void setRdGate(RdGate rdGate) {
		this.rdGate = rdGate;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDGATE;
	}
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");
		this.rdGate.setPid(data.getInt("pid"));
		this.rdGate.setInLinkPid(data.getInt("inLinkPid"));
		this.rdGate.setOutLinkPid(data.getInt("outLinkPid"));
		this.rdGate.setNodePid(data.getInt("nodePid"));
		this.rdGate.setType(data.getInt("type"));
		this.rdGate.setDir(data.getInt("dir"));
		this.rdGate.setFee(data.getInt("fee"));
		JSONArray conditionArray = data.getJSONArray("condition");
		List<IRow> conditionList = new ArrayList<IRow>();
		for (int i=0;i<conditionArray.size();i++){
			RdGateCondition condition = new RdGateCondition();
			JSONObject conditionObj = conditionArray.getJSONObject(i);
			condition.setPid(conditionObj.getInt("pid"));
			condition.setValidObj(conditionObj.getInt("validObj"));
			condition.setTimeDomain(conditionObj.getString("timeDomain"));
			conditionList.add(condition);
		}
		this.rdGate.setCondition(conditionList);
	}

}
