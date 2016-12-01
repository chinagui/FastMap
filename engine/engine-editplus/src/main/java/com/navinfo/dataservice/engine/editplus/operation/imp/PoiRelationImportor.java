package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.exception.ServiceException;

/** 
 * @ClassName: PoiRelationImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: PoiRelationImportor.java
 */
public class PoiRelationImportor extends AbstractOperation{
	protected Logger log = Logger.getLogger(this.getClass());
	protected Map<String,String> errLog=new HashMap<String,String>();
	
	protected PoiRelationImportorCommand poiRelationImporterCommand;
	
	public PoiRelationImportor(Connection conn,  OperationResult preResult) {
		super(conn,  preResult);
		
	}


	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.plus.operation.AbstractOperation#operate()
	 */
	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		poiRelationImporterCommand = (PoiRelationImportorCommand) cmd;
		//<childPid,parentPid>，用以处理对象父子关系关联。parentPid为空=解除父子关系
		Map<Long,Long> childPidParentPid = new HashMap<Long,Long>();
		//<parentFid,childPid>，用于根据父对象fid加载对象之后更新childPidParentPid
		Map<String,Long> parentFidChildPid = new HashMap<String,Long>();
		//<parentPid,childPid>，用于根据父对象Pid加载对象之后更新childPidParentPid
		Map<Long,Long> parentPidChildPid = new HashMap<Long,Long>();
		
		for(PoiRelation poiRelation:poiRelationImporterCommand.getPoiRels()){
			//处理父子关系导入
			if(poiRelation.getPoiRelationType().equals(PoiRelationType.FATHER_AND_SON)){
				long pid = poiRelation.getPid();
				long fatherPid = poiRelation.getFatherPid();
				childPidParentPid.put(pid, fatherPid);
				//需要加载的父对象存在pid,优先用pid加载;pid不存在用fid加载;pid/fid均为空则不加载
				if(fatherPid!=0&&!result.isObjExist(ObjectName.IX_POI,fatherPid)){
					parentPidChildPid.put(fatherPid, pid);
				}else{
					String fatherFid = poiRelation.getFatherFid();
					if(fatherFid!=null&&!fatherFid.equals(""))
					{
						//result中是否含此父对象。如果包含则不加载
						boolean isFatherExist = false;
						for(Map.Entry<Long, BasicObj> entry:result.getObjsMapByType(ObjectName.IX_POI).entrySet()){
							BasicRow fatherObj = entry.getValue().getMainrow();
							if(fatherObj.getAttrByColName("POI_NUM").equals(fatherFid)){
								childPidParentPid.put(pid, fatherObj.getObjPid());
								isFatherExist = true;
								break;
							}
						}
						if(!isFatherExist){
							parentFidChildPid.put(fatherFid,pid);
						}
					}			
				}
			}
		}
		
		//维护父子关系
		log.info("开始维护父子关系");
		importFatherAndSon(childPidParentPid,parentPidChildPid,parentFidChildPid);
		log.info("结束维护父子关系");
		//维护统一关系
		
	}

	/**
	 * @param childPidParentPid <childPid,parentPid>用以维护父子关系
	 * @param parentPidChildPid <parentPid,childPid>用以根据父Pid加载父对象
	 * @param parentFidChildPid <parentFid,childPid>用以根据父fid加载父对象
	 * @throws Exception 
	 */
	private void importFatherAndSon(Map<Long, Long> childPidParentPid, Map<Long, Long> parentPidChildPid,
			Map<String, Long> parentFidChildPid) throws Exception {
		//维护父子关系对象必须加载IX_POI_PARENT，IX_POI_CHILDREN两张子表
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_PARENT");
		tabNames.add("IX_POI_CHILDREN");

		//根据pid加载父对象
		log.info("根据pid加载父对象");
		loadParentObjByPids(conn,parentPidChildPid,tabNames,childPidParentPid);
		//根据fid加载父对象
		log.info("根据fid加载父对象");
		loadParentObjByFids(conn,parentFidChildPid,tabNames,childPidParentPid);
		//加载子对象原始父对象
		log.info("加载子对象原始父对象");
		//<childPid,parentPid>,用于解除原始父子关系
		Map<Long,Long> childPidOriginParentPid = new HashMap<Long,Long>();
		loadOriginParentObjs(conn,childPidParentPid,tabNames,childPidOriginParentPid);
		//处理父子关系
		log.info("遍历childPidParentPid,维护关系");
		handleParentChildrenRelation(conn,childPidParentPid,childPidOriginParentPid);
	}


	/**
	 * 维护父子关系
	 * @param conn
	 * @param childPidParentPid <childPid,parentPid>
	 * @param childPidOriginParentPid <childPid,originParentPid>
	 * @throws ServiceException 
	 */
	private void handleParentChildrenRelation(Connection conn, Map<Long, Long> childPidParentPid,
			Map<Long, Long> childPidOriginParentPid) throws ServiceException {
		//遍历childPidParentPid,维护关系
		try{
			for(Map.Entry<Long,Long> entry:childPidParentPid.entrySet()){
				long childPid = entry.getKey();
				long parentPid = entry.getValue();
				/**
				 * 存在原父，原始父与新父一致，不做任何操作
				 */
				if(childPidOriginParentPid.containsKey(childPid)&&childPidOriginParentPid.get(childPid)==parentPid){
					log.info("原始父与新父一致，不做任何操作，childPid:" + childPid + ";OrigionParentPid:" + childPidOriginParentPid.get(childPid)
					 + ";parentPid:" + parentPid);
					continue;
				}
				/**
				 * 存在原父，原始父与新父不一致，解除原始父子关系
				 * 原始父只有一个子，且为该子，则删除父下ix_poi_parent子表记录
				 */
				if(childPidOriginParentPid.containsKey(childPid)&&childPidOriginParentPid.get(childPid)!=parentPid){
					log.info("解除原始父子关系，childPid:" + childPid + ";parentPid:" + childPidOriginParentPid.get(childPid));
					BasicObj origionParentObj = result.getObjsMapByType(ObjectName.IX_POI).get(childPidOriginParentPid.get(childPid));
					List<BasicRow> ixPoiChildrenList = origionParentObj.getRowsByName("IX_POI_CHILDREN");
					List<BasicRow> ixPoiParentList = origionParentObj.getRowsByName("IX_POI_PARENT");
					if(ixPoiChildrenList.size()==1
							&&(long)ixPoiChildrenList.get(0).getAttrByColName("CHILD_POI_PID")==childPid){
						origionParentObj.deleteSubrow(ixPoiParentList.get(0));
					}
					for(BasicRow ixPoiChild:ixPoiChildrenList){
						if((long)ixPoiChild.getAttrByColName("CHILD_POI_PID")==childPid){
							origionParentObj.deleteSubrow(ixPoiChild);
							break;
						}
					}
				}
				/**
				 * 维护新的父子关系
				 */
				if(parentPid!=0){
					log.info("创建父子关系，childPid:" + childPid + ";parentPid:" + parentPid);
					BasicObj parentObj = result.getObjsMapByType(ObjectName.IX_POI).get(parentPid);
					List<BasicRow> parentList = new ArrayList<BasicRow>();
					if(parentObj.getRowsByName("IX_POI_PARENT")==null||parentObj.getRowsByName("IX_POI_PARENT").isEmpty()){
						//非父节点，需要创建其为父节点
						IxPoiParent ixPoiParent = (IxPoiParent) ObjFactory.getInstance().createRow("IX_POI_PARENT", parentPid);
						ixPoiParent.setAttrByCol("PARENT_POI_PID", parentPid);
						parentList = new ArrayList<BasicRow>();
						parentList.add(ixPoiParent);
						parentObj.setSubrows("IX_POI_PARENT", parentList);
					}else{
						parentList = parentObj.getRowsByName("IX_POI_PARENT");
					}
					IxPoiParent ixPoiParent = (IxPoiParent) parentList.get(0);
					
					List<BasicRow> childrenList = parentObj.getRowsByName("IX_POI_CHILDREN");
					if(childrenList==null){
						childrenList = new ArrayList<BasicRow>();
					}
					
					IxPoiChildren ixPoiChildren = (IxPoiChildren) ObjFactory.getInstance().createRow("IX_POI_CHILDREN", parentPid);
					ixPoiChildren.setAttrByCol("GROUP_ID", ixPoiParent.getGroupId());
					ixPoiChildren.setAttrByCol("CHILD_POI_PID", childPid);
					childrenList.add(ixPoiChildren);
					parentObj.setSubrows("IX_POI_CHILDREN", childrenList);
				}
			}	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param childPidParentPid
	 * @param tabNames 
	 * @param childPidOriginParentPid
	 * @throws ServiceException 
	 */
	private void loadOriginParentObjs(Connection conn, Map<Long, Long> childPidParentPid, Set<String> tabNames, Map<Long, Long> childPidOriginParentPid) throws ServiceException {
		//加载子对象原始父对象
		try{
			//获取子对象父对象pid
			childPidOriginParentPid = IxPoiSelector.getParentPidsByChildrenPids(conn, childPidParentPid.keySet());
			//需要加载的原始父对象Pid
			Map<Long,Long> childPidOriginParentPidNeedToBeLoad = new HashMap<Long,Long>();
			for(Map.Entry<Long, Long> entry:childPidOriginParentPid.entrySet()){
				if(!result.isObjExist(ObjectName.IX_POI, entry.getValue())){
					childPidOriginParentPidNeedToBeLoad.put(entry.getKey(), entry.getValue());
				}
			}
			//加载子对象原始父对象
			Map<Long,BasicObj> originParentMap = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, childPidOriginParentPidNeedToBeLoad.values(), true, true);

			log.info("将缺失的父对象加载到OperationResult");
			for(Map.Entry<Long, BasicObj> entry:originParentMap.entrySet()){
				if(!result.isObjExist(entry.getValue())){
					result.putObj(entry.getValue());
				}
			}
			log.info("OperationResult所有POI对象加载父子关系子表");
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
		
	}


	/**
	 * @param conn
	 * @param parentFidChildPid
	 * @param tabNames
	 * @param childPidParentPid 
	 * @throws ServiceException 
	 */
	private void loadParentObjByFids(Connection conn, Map<String, Long> parentFidChildPid, Set<String> tabNames, Map<Long, Long> childPidParentPid) throws ServiceException {
		//根据fid加载父对象
		try{
			if(!parentFidChildPid.keySet().isEmpty()){
				List<BasicObj> objs = ObjBatchSelector.selectBySpecColumn(conn, ObjectName.IX_POI, tabNames, "POI_NUM", parentFidChildPid.keySet(), true, true);
				for(BasicObj obj:objs){
					if(!result.isObjExist(obj)){
						result.putObj(obj);
					}
					long childPid = parentFidChildPid.get(obj.getMainrow().getAttrByColName("POI_NUM"));
					//更新childPidParentPid，将父对象pid补上
					childPidParentPid.put(childPid, obj.objPid());
				}
				//判断哪些父对象没有加载到
				if(objs.size()<parentFidChildPid.keySet().size()){
					for(String parentFid:parentFidChildPid.keySet()){
						for(BasicObj obj:objs){
							if(obj.getMainrow().getAttrByColName("POI_NUM").equals(parentFid)){
								continue;
							};
						}
						//没有加载到父对象，则不维护该父子关系
						errLog.put(parentFidChildPid.get(parentFid).toString(), "无法根据父FID:"+parentFid +"加载到父对象");
						childPidParentPid.remove(parentFidChildPid.get(parentFid));
					}
				}
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
		
	}


	/**
	 * @param conn
	 * @param parentPidChildPid
	 * @param tabNames 
	 * @param childPidParentPid 
	 * @throws ServiceException 
	 */
	private void loadParentObjByPids(Connection conn, Map<Long, Long> parentPidChildPid, Set<String> tabNames, Map<Long, Long> childPidParentPid) throws ServiceException {
		//根据Pid加载父对象
		try{
			if(!parentPidChildPid.keySet().isEmpty()){
				Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, parentPidChildPid.keySet(), true, true);
				//父对象加入OperationResult
				for(BasicObj obj:objs.values()){
					if(!result.isObjExist(obj)){
						result.putObj(obj);
					}
				}
				//判断哪些父对象没有被加载到
				if(objs.size()<parentPidChildPid.keySet().size()){
					for(long parentPid:parentPidChildPid.keySet()){
						for(BasicObj obj:objs.values()){
							if(obj.objPid()==parentPid){
								continue;
							};
						}
						//没有加载到父对象，则不维护该父子关系
						errLog.put(parentPidChildPid.get(parentPid).toString(), "无法根据父PID:"+parentPid +"加载到父对象");
						childPidParentPid.remove(parentPidChildPid.get(parentPid));
					}
				}
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
		
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "PoiRelationImportor";
	}
}
