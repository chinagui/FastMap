package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

/**
 * 
* @ClassName: Operation 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:02 
* @Description: TODO
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

		result.insertObject(command.getRdInter(), ObjStatus.DELETE, command.getPid());
		
		//维护CRFO:如果删除的CRFI属于某个CRFO，要从CRFO组成信息中去掉
		//TODO
		return null;
	}
	
	/**
	 * 删除link维护信息
	 * 
	 * @param linkPid
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void deleteByNode(Result result,List<IRow> crossNodes) throws Exception {		

		if (conn == null || CollectionUtils.isEmpty(crossNodes)) {
			return;
		}
		int [] crossNodePids = new int[crossNodes.size()];
		
		for(int i=0;i<crossNodes.size();i++)
		{
			crossNodePids[i] = ((RdCrossNode)crossNodes.get(i)).getNodePid();
		}
		
		RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

		List<RdTrafficsignal> trafficsignals = selector.loadByNodeId(true,crossNodePids);

		for (RdTrafficsignal trafficsignal : trafficsignals) {

			result.insertObject(trafficsignal, ObjStatus.DELETE,
					trafficsignal.getPid());
		}
	}
	
	/**
	 * 删除link维护信息
	 * 
	 * @param linkPid
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void deleteByLink(Result result,int ... linkPids) throws Exception {		

		if (conn == null || linkPids.length == 0) {
			return;
		}
	}

}
