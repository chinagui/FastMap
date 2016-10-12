package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;

public class Operation implements IOperation {

	private Command command;
	
	private Connection conn=null;

	private RdWarninginfo  rdWarninginfo;

	public Operation(Command command) {
		this.command = command;

		this.rdWarninginfo = command.getRdWarninginfo();
	}
	
	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = delete(result);

		return msg;
	}
	
	private String delete(Result result)
	{
		result.insertObject(rdWarninginfo, ObjStatus.DELETE, rdWarninginfo.getPid());

		return null;
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
		
		if (conn == null) {
			return;
		}
		
		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

		List<RdWarninginfo> warninginfos = selector.loadByNode(nodePid, true);

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

		if (conn == null) {
			return;
		}
		
		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

		List<RdWarninginfo> warninginfos = selector.loadByLink(linkPid, true);

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

		if (conn == null) {
			return;
		}
		
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
	
	/**
	 * 删除link对警示信息的删除影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteRdWarningInfectData(int linkPid,Connection conn) throws Exception {
		
		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

		List<RdWarninginfo> warninginfos = selector.loadByLink(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdWarninginfo warninginfo : warninginfos) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(warninginfo.objType());

			alertObj.setPid(warninginfo.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
