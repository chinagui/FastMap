package com.navinfo.dataservice.engine.edit.operation.obj.poi.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

public class Operation implements IOperation {

	private Command command;

	private IxPoi ixPoi;

	private Connection conn;

	public Operation(Connection conn) {
		this.conn = conn;
	}

	public Operation(Command command, IxPoi ixPoi) {
		this.command = command;

		this.ixPoi = ixPoi;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(ixPoi, ObjStatus.DELETE, command.getPid());

		return null;
	}

	/**
	 * 删除link维护poi的引导link，引导坐标和side不维护
	 * @param linkPid 引导link
	 * @param result 结果集
	 * @throws Exception
	 */
	public void deleteGuideLink(int linkPid, Result result) throws Exception {
		IxPoiSelector selector = new IxPoiSelector(conn);

		List<IxPoi> poiList = selector.loadIxPoiByLinkPid(linkPid, true);
		
		for(IxPoi poi : poiList)
		{
			poi.changedFields().put("linkPid", 0);
			
			result.insertObject(poi, ObjStatus.UPDATE, poi.getPid());
		}
	}
	
	/**
	 * 删除link对POI的维护影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getUpdatePoiInfectData(int linkPid,Connection conn) throws Exception {
		
		IxPoiSelector selector = new IxPoiSelector(conn);

		List<IxPoi> poiList = selector.loadIxPoiByLinkPid(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (IxPoi ixPoi : poiList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(ixPoi.objType());

			alertObj.setPid(ixPoi.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
