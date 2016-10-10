package com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:43:54
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		result.insertObject(command.getRdSe(), ObjStatus.DELETE, command.getPid());
		return null;
	}

	/**
	 * 根据删除线的PID查询出与之相关的分叉口提示并且删除
	 * 
	 * @param result
	 *            存放待处理的结果集
	 * @param linkPids
	 *            将删除线的PID
	 * @return
	 * @throws Exception
	 */
	public String deleteRdSe(Result result, List<Integer> linkPids) throws Exception {
		RdSeSelector selector = new RdSeSelector(this.conn);
		for (Integer linkPid : linkPids) {
			List<RdSe> rdSes = selector.loadRdSesWithLinkPid(linkPid, true);
			for (RdSe rdSe : rdSes) {
				result.insertObject(rdSe, ObjStatus.DELETE, rdSe.pid());
			}
		}
		return null;
	}
	
	/**
	 * 删除link对分叉口提示的删除影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteRdSeInfectData(int linkPid,Connection conn) throws Exception {
		
		RdSeSelector selector = new RdSeSelector(conn);

		List<RdSe> seList = selector.loadRdSesWithLinkPid(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdSe se : seList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(se.objType());

			alertObj.setPid(se.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
