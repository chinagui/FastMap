package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	/**
	 * 分离节点
	 * @param link 
	 * @param nodePid
	 * @param rdlinks 
	 * @param result
	 * @throws Exception
	 */
	public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks,
			Result result) throws Exception {

		int linkPid = link.getPid();
	
		// 跨图幅处理的RdWarninginfo
		Map<Integer, RdWarninginfo> warninginfoMesh =null;	

		if (rdlinks != null && rdlinks.size() > 1) {

			warninginfoMesh = new HashMap<Integer, RdWarninginfo>();
		}		
		
		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

		List<RdWarninginfo> warninginfos = selector.loadByLink(linkPid, true);
		
		for (RdWarninginfo warninginfo : warninginfos) {
			
			if (warninginfo.getNodePid() == nodePid) {

				result.insertObject(warninginfo, ObjStatus.DELETE,
						warninginfo.getPid());

			} else if (warninginfoMesh != null) {

				warninginfoMesh.put(warninginfo.getPid(), warninginfo);
			}
		}
	
		if (warninginfoMesh == null ) {
			
			return;
		}
		
		int connectNode = link.getsNodePid() == nodePid ? link.geteNodePid()
				: link.getsNodePid();

		for (RdLink rdlink : rdlinks) {

			if (rdlink.getsNodePid() != connectNode
					&& rdlink.geteNodePid() != connectNode) {

				continue;
			}
			for (RdWarninginfo warninginfo : warninginfoMesh.values()) {

				warninginfo.changedFields().put("linkPid", rdlink.getPid());

				result.insertObject(warninginfo, ObjStatus.UPDATE, warninginfo.pid());
			}
		}
	}
}
