package com.navinfo.dataservice.engine.edit.edit.operation.topo.updowndepartlink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.edit.operation.topo.updowndepartlink.Operation;

/**
 * @author zhaokk
 * 制作上下线分离
 */

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}
	
	public String preCheck() throws Exception {
		return null;

	}
	
	/*
	 * 加载上下线分离线
	 */
	public void lockRdLinks() throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());
		this.getCommand().setLinks(linkSelector.loadByPids(this.getCommand().getLinkPids(), true));
	}
	@Override
	public void postCheck() {

		// 对数据进行检查、检查结果存储在数据库，并存储在临时变量postCheckMsg中
	}
	@Override
	public boolean prepareData() throws Exception {
		lockRdLinks();
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}
