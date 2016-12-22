package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink;

import java.sql.Connection;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * 修行ZONE线参数操作类
 * 
 * @author zhaokk
 * 
 */
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
				(ZoneLink) new ZoneLinkSelector(this.getConn()).loadById(this
						.getCommand().getLinkPid(), true));

		this.getCommand().setFaces(
				new ZoneFaceSelector(this.getConn()).loadZoneFaceByLinkId(this
						.getCommand().getLinkPid(), true));
		return false;
	}

	@Override
	public String preCheck() throws Exception {

		check.checkShapePointDistance(GeoTranslator.jts2Geojson(this
				.getCommand().getLinkGeom()));
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {

		return new Operation(this.getConn(), this.getCommand()).run(this
				.getResult());
	}

	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			IOperation operation = new Operation(this.getConn(),
					this.getCommand());

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

}
