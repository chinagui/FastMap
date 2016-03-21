package com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link;

import java.util.List;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;

public class Relate {

	private ObjType type;

	private List<Integer> pids;

	public ObjType getType() {
		return type;
	}

	public void setType(ObjType type) {
		this.type = type;
	}

	public List<Integer> getPids() {
		return pids;
	}

	public void setPids(List<Integer> pids) {
		this.pids = pids;
	}

}
