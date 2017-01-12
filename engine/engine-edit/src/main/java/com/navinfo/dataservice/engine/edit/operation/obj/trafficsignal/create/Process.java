package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private Check check = new Check(this.getConn());

	@Override
	public boolean prepareData() throws Exception {

		int nodePid = this.getCommand().getNodePid();

		RdCrossNodeSelector selector = new RdCrossNodeSelector(this.getConn());

		IRow crossNode = selector.loadByNodeId(nodePid, true);
		
		check.checkHasCross(crossNode);

		RdCrossSelector crossSelector = new RdCrossSelector(this.getConn());
		
		RdCross cross = (RdCross) crossSelector.loadById(crossNode.parentPKValue(), true);

		check.checkHasTrafficSignal(cross,nodePid);

		this.getCommand().setCross(cross);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		Operation operation = new Operation(this.getCommand(),this.getConn());
		String msg = operation.run(this.getResult());
		return msg;
	}

}
