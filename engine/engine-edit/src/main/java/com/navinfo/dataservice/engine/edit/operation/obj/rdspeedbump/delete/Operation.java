package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.selector.rd.speedbump.RdSpeedbumpSelector;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 上午11:16:12
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
		result.insertObject(command.getRdSpeedbump(), ObjStatus.DELETE, command.getPid());
		return null;
	}

	/**
	 * 删除RdLink是维护减速带信息
	 * 
	 * @param result
	 *            结果集
	 * @param linkPids
	 *            待删除RdLinkPids
	 * @return
	 * @throws Exception
	 */
	public String deleteSpeedbump(Result result, List<Integer> linkPids) throws Exception {
		RdSpeedbumpSelector selector = new RdSpeedbumpSelector(this.conn);
		for (int linkPid : linkPids) {
			// 根据RdLinkPid查找出关联的减速带并将之删除
			List<RdSpeedbump> speedbumps = selector.loadByLinkPid(linkPid, true);
			for (RdSpeedbump speedbump : speedbumps) {
				result.insertObject(speedbump, ObjStatus.DELETE, speedbump.pid());
			}
		}
		return null;
	}
	
	/**
	 * 删除link对减速带的删除影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteRdSpeedbumpInfectData(int linkPid,Connection conn) throws Exception {
		
		RdSpeedbumpSelector rdSpeedbumpSelector = new RdSpeedbumpSelector(conn);
		
		List<RdSpeedbump> rdSpeedbumps = rdSpeedbumpSelector.loadByLinkPid(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdSpeedbump speedbump : rdSpeedbumps) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(speedbump.objType());

			alertObj.setPid(speedbump.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
