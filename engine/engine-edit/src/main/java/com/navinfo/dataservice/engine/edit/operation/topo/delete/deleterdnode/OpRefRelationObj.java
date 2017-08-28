package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

import java.sql.Connection;

public class OpRefRelationObj {

	private Connection conn;

	public OpRefRelationObj(Connection conn) {
		this.conn = conn;
	}

	// 同一线
	public String handleSameLink(Result result, Command command)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(
				conn);

		for (RdLink link : command.getLinks()) {
			operation.deleteByLink(link, result);
		}

		return null;
	}

	
	//CRF要素 (RdInter、RdRoad、RdObject)
	public String handleCRF(Result result, Command command) throws Exception {
		
		com.navinfo.dataservice.engine.edit.utils.RdCRFOperateUtils rdCRFOperateUtils=new com.navinfo.dataservice.engine.edit.utils.RdCRFOperateUtils(this.conn);
		
		rdCRFOperateUtils.delNodeLink(result,command.getLinkPids(),command.getNodePids());
	
		return null;
	}


	// 警示信息
	public String handleLinkWarning(Result result, Command command)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update.Operation(
				conn);

		operation.updateByLinks(command.getLinkPids(), result);
		return null;
	}

	// 语音引导
	public String handleVoiceguide(Result result, Command command)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation(
				conn);

		for (RdLink link : command.getLinks()) {

			operation.deleteByLink(link.getPid(), result);
		}

		return null;
	}

	// 顺行
	public String handleDirectroute(Result result, Command command)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(
				conn);

		for (RdLink link : command.getLinks()) {
			operation.deleteByLink(link, result);
		}

		return null;
	}

}