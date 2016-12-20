package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminPart;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminTree;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminPartSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminTreeSelector;
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
		
		int regionId = adAdmin.getPid();
		
		//删除维护行政区划面
		updateAdFace(regionId, result);
		
		//删除维护zone面
		updateZoneFace(regionId, result);
		
		//删除维护行政区划层级
		updateAdminGroup(regionId,result);
				
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
	
	/**
	 * 删除行政区划代表点维护行政区划层级关系
	 * @param regionId 行政区划代表点主键pid
	 * @param result 结果集
	 * @throws Exception 
	 */
	public void updateAdminGroup(int regionId,Result result) throws Exception
	{
		AdAdminPartSelector partSelector = new AdAdminPartSelector(conn);
		
		AdAdminPart part = partSelector.loadByRegionId(regionId, true);
		
		if(part == null)
		{
			return;
		}
		
		AdAdminTreeSelector treeSelector = new AdAdminTreeSelector(conn);
		
		AdAdminTree tree = treeSelector.loadRowsByRegionId(regionId, true, part.getGroupId());
		
		if(tree == null)
		{
			return;
		}
		
		result.insertObject(part, ObjStatus.DELETE, part.getGroupId());
		
		if(tree.getGroup() != null)
		{
			result.insertObject(tree.getGroup(), ObjStatus.DELETE, tree.getGroup().getGroupId());
		}
		
		//判断该regionId的父下是否还有其他子，没有的删除regionId的父记录，有的不删除
		AdAdminGroup group = (AdAdminGroup) new AbstractSelector(AdAdminGroup.class,conn).loadById(part.getGroupId(), true);
		
		if(group.getParts().size() == 1)
		{
			result.insertObject(group, ObjStatus.DELETE, group.getGroupId());
		}
		
		List<AdAdminTree> treeList = tree.getChildren();
		
		//递归删除子节点
		deleteChild(treeList,result);
	}
	
	/**
	 * 递归删除行政区划层级
	 * @param treeList 行政区划层级树
	 * @param result
	 */
	private void deleteChild(List<AdAdminTree> treeList,Result result)
	{
		if(CollectionUtils.isNotEmpty(treeList))
		{
			for(AdAdminTree tree : treeList)
			{
				AdAdminGroup group = tree.getGroup();
				
				if(group != null)
				{
					result.insertObject(group, ObjStatus.DELETE, group.getPid());
				}
				
				AdAdminPart part = tree.getPart();
				
				if(part != null)
				{
					result.insertObject(part, ObjStatus.DELETE, part.getGroupId());
				}
				
				//递归调用
				deleteChild(tree.getChildren(), result);
			}
		}
	}
}
