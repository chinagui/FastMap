package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdEleceyePairSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdEleceyePartSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process() {
		super();
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		// 根据pairId加载RdEleceyePair和RdEleceyePart
		Command command = this.getCommand();
		command.setPair((RdEleceyePair) new RdEleceyePairSelector(this.getConn()).loadById(command.getGroupId(), true));

		RdEleceyePartSelector selector = new RdEleceyePartSelector(this.getConn());
		List<IRow> rows = selector.loadRowsByGroupId(command.getGroupId(), true);
		List<RdEleceyePart> parts = new ArrayList<RdEleceyePart>();
		for (IRow row : rows) {
			parts.add((RdEleceyePart) row);
		}
		command.setParts(parts);

		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
