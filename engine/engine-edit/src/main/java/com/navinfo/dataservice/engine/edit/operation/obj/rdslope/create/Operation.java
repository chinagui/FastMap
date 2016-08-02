package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.iface.Result;

import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.pidservice.PidService;

/**
 * 新增坡度信息
 * 
 * @author 赵凯凯
 * 
 */
public class Operation implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		this.createSlope(result);
		return null;
	}
	/**
	 * 新增坡度信息
	 * @param result
	 * @throws Exception
	 */
	private void createSlope(Result result) throws Exception {
		RdSlope slope = new RdSlope();
		slope.setPid(PidService.getInstance().applyRdSlopePid());
		slope.setNodePid(this.command.getInNodePid());
		slope.setLinkPid(this.command.getOutLinkPid());
		if (this.command.getSeriesLinkPids() != null) {
			if (this.command.getSeriesLinkPids().size() > 0) {
				List<IRow> rdSlopeVias = new ArrayList<IRow>();
				for (int i = 0; i < this.command.getSeriesLinkPids().size(); i++) {
					RdSlopeVia slopeVia = new RdSlopeVia();
					slopeVia.setSlopePid(slope.getPid());
					slopeVia.setLinkPid(this.command.getSeriesLinkPids().get(i));
					slopeVia.setSeqNum(i+1);
					rdSlopeVias.add(slopeVia);
				}
				slope.setSlopeVias(rdSlopeVias);

			}
		}
		result.insertObject(slope, ObjStatus.INSERT, slope.pid());

	}
}