package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	
	private String requester;
	
	private RdElectroniceye entryEleceye;
	
	private RdElectroniceye exitEleceye;
	
	private int eleceyePid1;
	
	private int eleceyePid2;

	public RdElectroniceye getEntryEleceye() {
		return entryEleceye;
	}

	public void setEntryEleceye(RdElectroniceye entryEleceye) {
		this.entryEleceye = entryEleceye;
	}

	public RdElectroniceye getExitEleceye() {
		return exitEleceye;
	}

	public void setExitEleceye(RdElectroniceye exitEleceye) {
		this.exitEleceye = exitEleceye;
	}

	public int getEleceyePid1() {
		return eleceyePid1;
	}

	public void setEleceyePid1(int eleceyePid1) {
		this.eleceyePid1 = eleceyePid1;
	}

	public int getEleceyePid2() {
		return eleceyePid2;
	}

	public void setEleceyePid2(int eleceyePid2) {
		this.eleceyePid2 = eleceyePid2;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDELECEYEPAIR;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");
		this.eleceyePid1 = data.getInt("pid1");
		this.eleceyePid2 = data.getInt("pid2");
	}

}
