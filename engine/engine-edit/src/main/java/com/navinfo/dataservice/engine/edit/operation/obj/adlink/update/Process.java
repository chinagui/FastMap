package com.navinfo.dataservice.engine.edit.operation.obj.adlink.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @author zhaokk 修改行政区划线参数基础类
 */
public class Process extends AbstractProcess<Command> {

	private AdLink updateLink;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public String exeOperation() throws Exception {

		return new Operation(this.getCommand(), this.updateLink).run(this
				.getResult());

	}

	@Override
	public boolean prepareData() throws Exception {

		AdLinkSelector linkSelector = new AdLinkSelector(this.getConn());
		// 加载对应AD_LINK信息
		this.updateLink = (AdLink) linkSelector.loadById(this.getCommand()
				.getLinkPid(), true);

		return false;
	}

	public String innerRun() throws Exception {
		try {
			this.prepareData();
			String preCheckMsg = this.preCheck();
			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			IOperation operation = new Operation(this.getCommand(), updateLink);
			operation.run(this.getResult());
			this.postCheck();

		} catch (Exception e) {

			getConn().rollback();

			throw e;
		}

		return null;
	}

}
