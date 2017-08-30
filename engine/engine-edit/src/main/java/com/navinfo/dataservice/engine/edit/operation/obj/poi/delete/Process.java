package com.navinfo.dataservice.engine.edit.operation.obj.poi.delete;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiChildrenSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxSamepoiPartSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxSamepoiSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private IxPoi ixPoi;

	private List<IRow> ixPoiParents = new ArrayList<>();

	private IxPoiChildren ixPoiChildren;

	private List<IRow> ixPoiSames = new ArrayList<IRow>();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public boolean prepareData() throws Exception {

		// 获取该link对象
		lockParent();
		lockSamePoi();

		IxPoiSelector selector = new IxPoiSelector(this.getConn());

		this.ixPoi = (IxPoi) selector
				.loadById(this.getCommand().getPid(), true);

		return true;
	}

	private void lockSamePoi() throws Exception {
		IxSamepoiPartSelector samepoiSelector = new IxSamepoiPartSelector(
				this.getConn());
		ixPoiSames = samepoiSelector.loadSameByPid(this.getCommand().getPid(),
				true);
	}

	private void lockParent() throws Exception {

		IxPoiChildrenSelector ixPoiChildrenSelector = new IxPoiChildrenSelector(
				this.getConn());

		IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(
				this.getConn());

		// poi作为父的时候的数据
		List<IRow> parents = ixPoiParentSelector.loadRowsByParentId(this
				.getCommand().getPid(), true);

		if (CollectionUtils.isNotEmpty(parents)) {
			ixPoiParents.add((IxPoiParent) parents.get(0));
		}

		// poi作为子的时候数据
		List<IRow> child2parents = ixPoiParentSelector
				.loadParentRowsByChildrenId(this.getCommand().getPid(), true);

		if (CollectionUtils.isNotEmpty(child2parents)) {
			IxPoiParent parent = (IxPoiParent) child2parents.get(0);

			List<IRow> childs = parent.getPoiChildrens();

			if (childs.size() == 1) {
				ixPoiParents.add(parent);
			} else {
				List<IRow> childList = ixPoiChildrenSelector.loadRowsByPoiId(
						this.getCommand().getPid(), true);

				if (CollectionUtils.isNotEmpty(childList)) {
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
		IOperation opParent = new OpRefParent(this.getCommand(), ixPoiParents,
				ixPoiChildren);
		opParent.run(this.getResult());
		// 情况poi的父\子表数据，避免删除poi递归调用错误删除数据
		this.ixPoi.getParents().clear();
		this.ixPoi.children().clear();
		// 维护poi父子关系
		IOperation opSamePoi = new OpRefSamePoi(this.getCommand(), ixPoiSames);
		opSamePoi.run(this.getResult());

		return msg;
	}
}
