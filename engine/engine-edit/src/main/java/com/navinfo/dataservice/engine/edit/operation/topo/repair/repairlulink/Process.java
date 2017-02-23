package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.parameterCheck.DepartCheck;

public class Process extends AbstractProcess<Command> {

	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public Process(Command command, Result result, Connection conn)
			throws Exception {
		super();
		this.setCommand(command);
		this.setResult(result);
		this.setConn(conn);
		this.initCheckCommand();
	}

	@Override
	public boolean prepareData() throws Exception {

		this.getCommand().setUpdateLink(
				(LuLink) new LuLinkSelector(this.getConn()).loadById(this
						.getCommand().getLinkPid(), true));

		this.getCommand().setFaces(
				new LuFaceSelector(this.getConn()).loadLuFaceByLinkId(this
						.getCommand().getLinkPid(), true));
		return false;
	}

	@Override
	public String preCheck() throws Exception {

		// check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());

		check.checkShapePointDistance(GeoTranslator.jts2Geojson(this
				.getCommand().getLinkGeom()));
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		
		parameterCheck();
		return new Operation(this.getCommand(), this.getConn()).run(this
				.getResult());
	}

	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			IOperation operation = new Operation(this.getCommand(),
					this.getConn());

			msg = operation.run(this.getResult());

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}
	
	private void parameterCheck() throws Exception {

		DepartCheck departCheck = new DepartCheck(this.getConn());
		
		LuLinkSelector linkSelector=new LuLinkSelector(this.getConn());

		if (this.getCommand().getCatchInfos() != null
				&& this.getCommand().getCatchInfos().size() > 0) {

			List<Integer> nodePids = new ArrayList<Integer>();

			for (int i = 0; i < this.getCommand().getCatchInfos().size(); i++) {
				JSONObject obj = this.getCommand().getCatchInfos()
						.getJSONObject(i);
				// 分离移动的node
				nodePids.add(obj.getInt("nodePid"));
			}

			for (int nodePid : nodePids) {

				List<Integer> linkPids = linkSelector.loadLinkPidByNodePid(
						nodePid, false);

				if (linkPids.size() > 1) {
					departCheck.checkIsSameNode(nodePid, "LU_NODE");
				}
			}
		}
	}

}
