package com.navinfo.dataservice.engine.edit.operation.obj.poiparent.update;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

	/**
	 * 被选父poi，已经作为poiParent的父子关系列表
	 */
	List<IRow> parentsByParent = new ArrayList<IRow>();
	/**
	 * 被选子poi，已经作为PoiChildren的父子关系列表
	 */
	List<IRow> parentsByChildren = new ArrayList<IRow>();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		IxPoiParentSelector selector = new IxPoiParentSelector(this.getConn());

		this.parentsByParent = selector.loadRowsByParentId(this.getCommand()
				.getParentPid(), true);

		this.parentsByChildren = selector.loadParentRowsByChildrenId(this
				.getCommand().getObjId(), true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.parentsByParent,
				this.parentsByChildren,this.getConn()).run(this.getResult());

	}

}
