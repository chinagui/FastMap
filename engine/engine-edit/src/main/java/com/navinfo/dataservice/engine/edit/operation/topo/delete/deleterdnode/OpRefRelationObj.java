package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

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

//	// CRF道路
//	public String handleRdroad(Result result, Command command) throws Exception {
//
//		com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation(
//				conn);
//
//		List<Integer> linkPids = new ArrayList<Integer>();
//
//		for (RdLink link : command.getLinks()) {
//
//			linkPids.add(link.getPid());
//		}
//
//		operation.deleteByLinks(linkPids, result);
//
//		return null;
//	}
	
	//CRF要素 (RdInter、RdRoad、RdObject)
	public String handleCRF(Result result, Command command) throws Exception {
		
		com.navinfo.dataservice.engine.edit.utils.RdCRFOperateUtils rdCRFOperateUtils=new com.navinfo.dataservice.engine.edit.utils.RdCRFOperateUtils(this.conn);
		
		rdCRFOperateUtils.delNodeLink(result,command.getLinkPids(),command.getNodePids());
	
		return null;
	}

	// 警示信息
	public String handleWarninginfo(Result result, Command command)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation warninginfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation(
				conn);
		for (int pid : command.getLinkPids()) {

			warninginfoOperation.deleteByLink(pid, result);
		}

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