package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRelationObj {

	private Connection conn = null;

	public OpRefRelationObj(Connection conn) {

		this.conn = conn;
	}

	/**
	 * 同一线
	 * 
	 * @param breakLink
	 * @param command
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleSameLink(RdLink breakLink, Command command,
			Result result) throws Exception {

		List<IObj> newLinks = new ArrayList<IObj>();

		newLinks.addAll(command.getNewLinks());

		// 打断link维护同一线
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
				this.conn);
		operation.breakLink(breakLink, newLinks, command.getBreakNode(),
				command.getRequester(), result);

		return null;
	}

	/**
	 * 交限
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdRestriction(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

	/**
	 * 车信
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdLaneconnexity(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

	/**
	 * 语音引导
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdVoiceguide(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

	/**
	 * 分歧
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdBranch(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

	/**
	 * 顺行
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdDirectroute(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

}