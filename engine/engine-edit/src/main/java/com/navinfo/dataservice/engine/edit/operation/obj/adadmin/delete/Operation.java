package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;

public class Operation implements IOperation {

	private Command command;

	private AdAdmin adAdmin;
	
	private Connection conn;
	
	public Operation(Connection conn)
	{
		this.conn = conn;
	}

	public Operation(Command command, AdAdmin adAdmin,Connection conn) {
		this.command = command;

		this.adAdmin = adAdmin;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(adAdmin, ObjStatus.DELETE, adAdmin.pid());
		
		//维护行政区划面，ZONE面
		int regionId = adAdmin.getPid();
		
		updateAdFace(regionId, result);
		
		updateZoneFace(regionId, result);
				
		return null;
	}
	
	/**
	 * 删除行政区划代表点维护行政区划面:regionId更新为0
	 * @param regionId 代表点id
	 * @param result
	 * @throws Exception
	 */
	public void updateAdFace(int regionId,Result result) throws Exception
	{
		AdFaceSelector adFaceSelector = new AdFaceSelector(conn);
		
		List<AdFace> faceList = adFaceSelector.loadAdFaceByRegionId(regionId, true);
		
		for(AdFace face : faceList)
		{
			face.changedFields().put("regionId", 0);
			
			result.insertObject(face, ObjStatus.UPDATE, face.getPid());
		}
	}
	
	/**
	 * 删除行政区划代表点维护ZONEFACE:regionId更新为0
	 * @param regionId 代表点id
	 * @param result
	 * @throws Exception
	 */
	public void updateZoneFace(int regionId,Result result) throws Exception
	{
		ZoneFaceSelector zoneFaceSelector = new ZoneFaceSelector(conn);
		
		List<ZoneFace> faceList = zoneFaceSelector.loadZoneFaceByRegionId(regionId, true);
		
		for(ZoneFace face : faceList)
		{
			face.changedFields().put("regionId", 0);
			
			result.insertObject(face, ObjStatus.UPDATE, face.getPid());
		}
	}
}
