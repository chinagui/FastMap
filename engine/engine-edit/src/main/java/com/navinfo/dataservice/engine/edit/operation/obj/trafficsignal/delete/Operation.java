package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
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
		
		for(RdTrafficsignal trafficsignal : command.getRdTrafficsignalList())
		{
			result.insertObject(trafficsignal, ObjStatus.DELETE, trafficsignal.getPid());
		}
		
		// 维护路口signal属性(如果此路口的“信号灯”原为“有路口红绿灯”，则将其修改为“无红绿灯”)
		RdCross cross = this.command.getRdCross();

		if (cross != null) {
			cross.changedFields().put("signal", 0);

			result.insertObject(cross, ObjStatus.UPDATE, cross.getPid());
		}

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
	public void deleteByNode(Result result, List<IRow> crossNodes) throws Exception {

		if (conn == null || CollectionUtils.isEmpty(crossNodes)) {
			return;
		}
		int[] crossNodePids = new int[crossNodes.size()];

		for (int i = 0; i < crossNodes.size(); i++) {
			crossNodePids[i] = ((RdCrossNode) crossNodes.get(i)).getNodePid();
		}

		RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

		List<RdTrafficsignal> trafficsignals = selector.loadByNodeId(true, crossNodePids);

		for (RdTrafficsignal trafficsignal : trafficsignals) {

			result.insertObject(trafficsignal, ObjStatus.DELETE, trafficsignal.getPid());
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
	public void deleteByLink(Result result, Integer... linkPids) throws Exception {

		if (conn == null || linkPids.length == 0) {
			return;
		}

		RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

		List<RdTrafficsignal> trafficsignals = selector.loadByLinkPid(true, linkPids);
		
//		//信号灯map（key：路口点pid，路口点信号灯的进入linkPid集合）
//		Map<Integer,List<Integer>> trafficMap = new HashMap<>();
//		
//		for(RdTrafficsignal signal : trafficsignals)
//		{
//			int nodePid = signal.getNodePid();
//			
//			int linkPid = signal.getLinkPid();
//			
//			if(trafficMap.get(nodePid) != null)
//			{
//				List<Integer> signalListPids = trafficMap.get(nodePid);
//				
//				signalListPids.add(linkPid);
//			}
//			else
//			{
//				List<Integer> signalListPids = new ArrayList<>();
//				
//				signalListPids.add(linkPid);
//				
//				trafficMap.put(nodePid, signalListPids);
//			}
//		}
//		
//		for(Map.Entry<Integer, List<Integer>> entry : trafficMap.entrySet())
//		{
//			int nodePid = entry.getKey();
//			
//			List<Integer> linkPidList = entry.getValue();
//			
//			List<Integer> paramLinkList = Arrays.asList(linkPids);
//			
//			//如果删除的link包含了路口点位所有进入link的红绿灯，则维护路口信号灯属性为无
//			if(paramLinkList.containsAll(linkPidList))
//			{
//				//TODO是否维护路口
//			}
//		}
		
		for (RdTrafficsignal trafficsignal : trafficsignals) {

			result.insertObject(trafficsignal, ObjStatus.DELETE, trafficsignal.getPid());
		}
	}

	/**
	 * 删除link对限速的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdTrafficInfectData(int linkPid, Connection conn) throws Exception {

		RdTrafficsignalSelector trafficsignalSelector = new RdTrafficsignalSelector(conn);

		List<RdTrafficsignal> trafficsignals = trafficsignalSelector.loadByLinkPid(true, linkPid);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdTrafficsignal trafficsiginal : trafficsignals) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(trafficsiginal.objType());

			alertObj.setPid(trafficsiginal.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除路口对红绿灯的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteCrossTrafficInfectData(List<IRow> crossNodes, Connection conn) throws Exception {

		List<AlertObject> alertList = new ArrayList<>();

		int[] crossNodePids = new int[crossNodes.size()];

		for (int i = 0; i < crossNodes.size(); i++) {
			crossNodePids[i] = ((RdCrossNode) crossNodes.get(i)).getNodePid();
		}

		RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

		List<RdTrafficsignal> trafficsignals = selector.loadByNodeId(true, crossNodePids);

		for (RdTrafficsignal trafficsignal : trafficsignals) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(trafficsignal.objType());

			alertObj.setPid(trafficsignal.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
