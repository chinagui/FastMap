package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.delete;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private RdCross cross;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		RdCrossSelector selector = new RdCrossSelector(this.getConn());

		this.cross = (RdCross) selector.loadById(this.getCommand().getPid(), true);

		List<IRow> rdCrossNodeList = cross.getNodes();
		
		List<IRow> trafficsignals = new ArrayList<>();

		RdTrafficsignalSelector rdTrafficsignalSelector = new RdTrafficsignalSelector(this.getConn());
		
		for (IRow row : rdCrossNodeList) {
			RdCrossNode crossNode = (RdCrossNode) row;

			int nodePid = crossNode.getNodePid();
			
			IRow rdTrafficSignal = rdTrafficsignalSelector.loadByNodeId(nodePid, true);
			
			trafficsignals.add(rdTrafficSignal);

		}
		
		this.getCommand().setTrafficsignals(trafficsignals);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.cross).run(this.getResult());
	}
}
