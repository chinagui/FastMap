package com.navinfo.dataservice.engine.edit.edit.operation.obj.poiparent.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

	/**
	 * 被选父poi，已经作为poiParent的父子关系列表
	 */
	List<IRow> parents = new ArrayList<IRow>();

	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareData() throws Exception {
		IxPoiParentSelector selector = new IxPoiParentSelector(this.getConn());

		this.parents = selector.loadRowsByParentId(this.getCommand()
				.getParentPid(), true);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#
	 * createOperation()
	 */
	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), parents, this.getConn()).run(this
				.getResult());
	}

}
