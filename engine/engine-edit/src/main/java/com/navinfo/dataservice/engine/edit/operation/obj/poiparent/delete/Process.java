package com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiChildrenSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

	private IxPoiParent ixPoiParent;

	private IxPoiChildren ixPoiChildren;

	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		IxPoiChildrenSelector ixPoiChildrenSelector = new IxPoiChildrenSelector(this.getConn());

		List<IRow> childrens = ixPoiChildrenSelector.loadRowsByPoiId(this.getCommand().getObjId(), true);

		if (CollectionUtils.isNotEmpty(childrens) && childrens.size() == 1) {
			ixPoiChildren = (IxPoiChildren) childrens.get(0);

			List<IRow> allChilds = ixPoiChildrenSelector.loadRowsByParentId(ixPoiChildren.getGroupId(), true);

			if (allChilds.size() == 1
					&& ((IxPoiChildren) allChilds.get(0)).getChildPoiPid() == this.getCommand().getObjId()) {
				// 需要删除ix_poi_parent表中的记录
				IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(this.getConn());

				ixPoiParent = (IxPoiParent) ixPoiParentSelector.loadById(ixPoiChildren.getGroupId(), true);

				ixPoiParent.setPoiChildrens(childrens);
			}
		} else {
			throw new Exception("poi:" + this.getCommand().getObjId() + "的父数据不正确");
		}
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.ixPoiParent, this.ixPoiChildren,this.getConn()).run(this.getResult());
	}

}
