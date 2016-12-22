package com.navinfo.dataservice.engine.edit.operation.obj.lclink.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private LcLink updateLink;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.updateLink).run(this.getResult());

	}

	@Override
	public boolean prepareData() throws Exception {

		LcLinkSelector linkSelector = new LcLinkSelector(this.getConn());
		// 加载对应LC_LINK信息
		this.updateLink = (LcLink) linkSelector.loadById(this.getCommand().getLinkPid(), true);

		return false;
	}

	public String innerRun() throws Exception {
		try {
			this.prepareData();
			IOperation operation = new Operation(this.getCommand(), updateLink);
			operation.run(this.getResult());
			String preCheckMsg = this.preCheck();
			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
		} catch (Exception e) {

			getConn().rollback();

			throw e;
		}

		return null;
	}

}
