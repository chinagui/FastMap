package com.navinfo.dataservice.FosEngine.edit.operation.topo.smoothrepirelink;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdLink updateLink;

	public Operation(Command command, RdLink updateLink) {
		this.command = command;

		this.updateLink = updateLink;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getUpdateContent();

		boolean isChanged = updateLink.fillChangeFields(content);

		if (isChanged) {
			result.getUpdateObjects().add(updateLink);
		}

		return null;
	}

}
