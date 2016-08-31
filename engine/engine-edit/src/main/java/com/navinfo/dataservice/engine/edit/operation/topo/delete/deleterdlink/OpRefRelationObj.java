package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRelationObj {
	private Connection conn = null;

	public OpRefRelationObj(Connection conn) {

		this.conn = conn;

	}

	public String handleRoad(Result result, Command command) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation(
				conn);

		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.add(command.getLinkPid());

		operation.deleteByLinks(linkPids, result);

		return null;
	}

	public String handleSameLink(Result result, Command command) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(
				conn);

		operation.deleteByLink(command.getLink(), result);

		return null;
	}

	public String handleWarninginfo(Result result, int linkPid) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation warninginfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation(
				conn);

		warninginfoOperation.deleteByLink(linkPid, result);

		return null;
	}

	public String handleVoiceguide(Result result, RdLink oldLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation(
				conn);

		operation.deleteByLink(oldLink.getPid(), result);

		return null;
	}

	public String handleDirectroute(Result result, RdLink oldLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(
				conn);

		operation.deleteByLink(oldLink, result);

		return null;
	}

	public String handlePoiGuideLink(Result result, RdLink link) throws Exception {

		// poi被动维护（引导link）
		com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Operation deletePoiOperation = new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Operation(
				this.conn);
		deletePoiOperation.deleteGuideLink(link.getPid(), result);

		return null;
	}
}