package com.navinfo.dataservice.engine.edit.operation.obj.poi.delete;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;

public class OpRefSamePoi implements IOperation {
	private Command command;

	private List<IRow> ixPoiSames;

	public OpRefSamePoi(Command command, List<IRow> ixPoiSames) {
		this.command = command;

		this.ixPoiSames = ixPoiSames;

	}

	@Override
	public String run(Result result) throws Exception {

		if (CollectionUtils.isNotEmpty(ixPoiSames)) {
			for (IRow same : ixPoiSames) {
				IxSamepoi samepoi = (IxSamepoi) same;
				result.insertObject(same, ObjStatus.DELETE, samepoi.getPid());
			}

		}
		return null;
	}
}