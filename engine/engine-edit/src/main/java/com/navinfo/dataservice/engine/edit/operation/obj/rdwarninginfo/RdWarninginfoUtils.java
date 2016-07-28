package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;

public class RdWarninginfoUtils {

	private Connection conn;

	public RdWarninginfoUtils(Connection conn) {
		this.conn = conn;

	}

	/**
	 * 根据linkPid获取警示信息
	 * 
	 * @param linkPid
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public List<RdWarninginfo> getWarninginfoByLink(int linkPid)
			throws Exception {

		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

		List<RdWarninginfo> warninginfos = selector.loadByLink(linkPid, true);

		return warninginfos;
	}

	/**
	 * 根据nodePid获取警示信息
	 * 
	 * @param nodePid
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public List<RdWarninginfo> getWarninginfoByNode(int nodePid)
			throws Exception {

		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

		List<RdWarninginfo> warninginfos = selector.loadByNode(nodePid, true);

		return warninginfos;
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
	public void breakRdLink(int oldLinkPid, List<RdLink> newLinks, Result result)
			throws Exception {

		List<RdWarninginfo> warninginfos = getWarninginfoByLink(oldLinkPid);

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
	 * 删除node维护警示信息
	 * 
	 * @param nodePid
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void deleteByNode(int nodePid, Result result) throws Exception {

		List<RdWarninginfo> warninginfos = getWarninginfoByNode(nodePid);

		for (RdWarninginfo warninginfo : warninginfos) {

			result.insertObject(warninginfo, ObjStatus.DELETE,
					warninginfo.getPid());
		}
	}

	/**
	 * 删除link维护警示信息
	 * 
	 * @param linkPid
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void deleteByLink(int linkPid, Result result) throws Exception {

		List<RdWarninginfo> warninginfos = getWarninginfoByLink(linkPid);

		for (RdWarninginfo warninginfo : warninginfos) {

			result.insertObject(warninginfo, ObjStatus.DELETE,
					warninginfo.getPid());
		}
	}
	
	
	/**
	 * 批量删除link维护警示信息
	 * 
	 * @param linkPid
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void batchDeleteByLink(List<RdLink> links, Result result) throws Exception {
		
		if(links.isEmpty())
		{
			return;
		}
		
		List<Integer> linkPids=new ArrayList<Integer>();
		
		for(RdLink link :links)
		{
			linkPids.add(link.getPid());
		}
		
		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

		List<RdWarninginfo> warninginfos = selector.loadByLinks(linkPids, true);

		for (RdWarninginfo warninginfo : warninginfos) {

			result.insertObject(warninginfo, ObjStatus.DELETE,
					warninginfo.getPid());
		}
	}
}
