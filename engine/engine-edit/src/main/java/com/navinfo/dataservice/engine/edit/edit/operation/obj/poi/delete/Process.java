package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.delete;

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
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private IxPoi ixPoi;

	private IxPoiParent ixPoiParent;

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

		List<IRow> childrens = ixPoiChildrenSelector.loadRowsByPoiId(this.getCommand().getPid(), true);

		if (CollectionUtils.isNotEmpty(childrens) && childrens.size() == 1) {
			ixPoiChildren = (IxPoiChildren) childrens.get(0);

			List<IRow> allChilds = ixPoiChildrenSelector.loadRowsByParentId(ixPoiChildren.getGroupId(), true);

			if (allChilds.size() == 1
					&& ((IxPoiChildren) allChilds.get(0)).getChildPoiPid() == this.getCommand().getPid()) {
				// 需要删除ix_poi_parent表中的记录
				IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(this.getConn());

				ixPoiParent = (IxPoiParent) ixPoiParentSelector.loadById(ixPoiChildren.getGroupId(), true);

				ixPoiParent.setPoiChildrens(childrens);
			}
		} else {
			throw new Exception("poi:" + this.getCommand().getPid() + "的父不唯一");
		}
	}

	@Override
	public String exeOperation() throws Exception {
		// 删除poi操作
		IOperation op = new Operation(this.getCommand(), this.ixPoi);
		String msg = op.run(this.getResult());
		// 维护poi父子关系
		IOperation opParent = new OpRefParent(this.getCommand(), ixPoiParent, ixPoiChildren);
		opParent.run(this.getResult());
		return msg;
	}
}
