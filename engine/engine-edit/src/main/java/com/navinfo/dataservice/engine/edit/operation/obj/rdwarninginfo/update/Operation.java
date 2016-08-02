package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update;

import java.sql.Connection;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;

public class Operation implements IOperation {

	private Command command;

	private Connection conn = null;

	private RdWarninginfo rdWarninginfo;

	public Operation(Command command) {
		this.command = command;

		this.rdWarninginfo = this.command.getRdWarninginfo();

	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = update(result);

		return msg;
	}

	private String update(Result result) throws Exception {
		JSONObject content = command.getContent();

		if (!content.containsKey("objStatus")) {

			return null;
		}

		if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
			result.insertObject(rdWarninginfo, ObjStatus.DELETE,
					rdWarninginfo.pid());
			return null;
		}

		boolean isChanged = rdWarninginfo.fillChangeFields(content);

		if (isChanged) {
			result.insertObject(rdWarninginfo, ObjStatus.UPDATE,
					rdWarninginfo.pid());
		}

		return null;
	}

	/**
	 * 打断link维护警示信息
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void breakRdLink(int linkPid, List<RdLink> newLinks, Result result)
			throws Exception {
		if (conn == null) {
			return;
		}

		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

		List<RdWarninginfo> warninginfos = selector.loadByLink(linkPid, true);

		for (RdWarninginfo warninginfo : warninginfos) {

			for (RdLink link : newLinks) {

				if (link.getsNodePid() == warninginfo.getNodePid()
						|| link.geteNodePid() == warninginfo.getNodePid()) {

					warninginfo.changedFields().put("linkPid", link.getPid());

					result.insertObject(warninginfo, ObjStatus.UPDATE,
							warninginfo.pid());
				}
			}
		}
	}

}
