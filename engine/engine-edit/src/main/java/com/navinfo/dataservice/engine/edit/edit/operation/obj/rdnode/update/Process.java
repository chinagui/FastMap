package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdnode.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private RdNode rdnode;

	@Override
	public boolean prepareData() throws Exception {

		RdNodeSelector selector = new RdNodeSelector(this.getConn());

		this.rdnode = (RdNode) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.rdnode);
	}

	
	

}
