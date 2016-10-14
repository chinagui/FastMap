package com.navinfo.dataservice.engine.edit.operation.obj.poi.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiChildrenSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private IxPoi ixPoi;

	private List<IxPoiParent> ixPoiParents = new ArrayList<>();

	private IxPoiChildren ixPoiChildren;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public boolean prepareData() throws Exception {

		// 获取该link对象
		lockParent();

		IxPoiSelector selector = new IxPoiSelector(this.getConn());

		this.ixPoi = (IxPoi) selector.loadById(this.getCommand().getPid(), true);

		return true;
	}

	private void lockParent() throws Exception {

		IxPoiChildrenSelector ixPoiChildrenSelector = new IxPoiChildrenSelector(this.getConn());
		
		IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(this.getConn());
		
		//poi作为父的时候的数据
		List<IRow> parents = ixPoiParentSelector.loadRowsByParentId(this.getCommand().getPid(), true);
		
		if(CollectionUtils.isNotEmpty(parents))
		{
			ixPoiParents.add((IxPoiParent) parents.get(0));
		}
		
		//poi作为子的时候数据
		List<IRow> child2parents = ixPoiParentSelector.loadParentRowsByChildrenId(this.getCommand().getPid(), true);

		if (CollectionUtils.isNotEmpty(child2parents) ) {
			IxPoiParent parent = (IxPoiParent) child2parents.get(0);
			
			List<IRow> childs = parent.getPoiChildrens();
			
			if(childs.size() == 1)
			{
				ixPoiParents.add(parent);
			}
			else
			{
				List<IRow> childList = ixPoiChildrenSelector.loadRowsByPoiId(this.getCommand().getPid(), true);
				
				if(CollectionUtils.isNotEmpty(childList))
				{
					ixPoiChildren = (IxPoiChildren) childList.get(0);
				}
			}
		} 
	}

	@Override
	public String exeOperation() throws Exception {
		// 删除poi操作
		IOperation op = new Operation(this.getCommand(), this.ixPoi);
		String msg = op.run(this.getResult());
		// 维护poi父子关系
		IOperation opParent = new OpRefParent(this.getCommand(), ixPoiParents, ixPoiChildren);
		opParent.run(this.getResult());
		return msg;
	}
}
