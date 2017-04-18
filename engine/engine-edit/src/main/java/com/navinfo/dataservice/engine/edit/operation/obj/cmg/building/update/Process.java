package com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildingSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	public Process() {
	}

	public Process(AbstractCommand command, Result result, Connection conn)
			throws Exception {

		super(command, result, conn);
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		CmgBuildingSelector selector = new CmgBuildingSelector(this.getConn());

		CmgBuilding building = (CmgBuilding) selector.loadById(this
				.getCommand().getPid(), true);

		this.getCommand().setBuilding(building);

		return super.prepareData();
	}

	@Override
	public String exeOperation() throws Exception {

		return new Operation(getCommand()).run(getResult());
	}
}