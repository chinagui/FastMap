package com.navinfo.dataservice.engine.edit.operation.obj.lulink.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.obj.lulink.create.Check;

public class Process extends AbstractProcess<Command> {
	
	private LuLink updateLink;
	
	private Check check;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.updateLink).run(this.getResult());
	}
	
	@Override
	public boolean prepareData() throws Exception {
		
		LuLinkSelector linkSelector = new LuLinkSelector(this.getConn());
		//加载对应LuLink信息
		this.updateLink = (LuLink)linkSelector.loadById(this.getCommand().getLinkPid(),true);

		return false;
	}
}
