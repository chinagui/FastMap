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
import org.apache.solr.common.util.Hash;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoiPart;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxSamePoiObj;
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
 * POI关系维护：父子关系，同一关系，目前只实现了父子关系
 * 父子关系传参：PoiRelation
 * 			若删除父子关系，则fatherPid=0&&fatherFid=null/""
 * 			若对象删除，父子关系维护同删除父子关系。
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
		
		//*********************
		//<pid,otherPid>，用以处理对象同一关系关联。samePid为空=解除父子关系
		Map<Long,Long> pidSamePoiPid = new HashMap<Long,Long>();
		//<sameFid,pid>，用于根据samefid加载对象之后更新pidSamePoiPid
		Map<String,Long> sameFidPid = new HashMap<String,Long>();
		
		//用于存储fid 为空的 poi ,这样的poi 有可能没有同一关系;有可能解除同一关系
		List<Long> emptyFidPids = new ArrayList<Long>();
		
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
			//处理同一关系导入
			else if(poiRelation.getPoiRelationType().equals(PoiRelationType.SAME_POI)){
				long pid = poiRelation.getPid();
				long otherPid = poiRelation.getSamePid();
				
				pidSamePoiPid.put(pid, otherPid);//全量的 poi
				//需要加载的samepoi存在pid,优先用pid加载;pid不存在用fid加载;pid/fid均为空则不加载
				
				String sameFid = poiRelation.getSameFid();
				if(sameFid != null && !sameFid.equals(""))
				{//当samefid存在时 存入 sameFidPid
					sameFidPid.put(sameFid,pid);
					/*for(Map.Entry<Long, BasicObj> entry:result.getObjsMapByType(ObjectName.IX_POI).entrySet()){
						BasicRow samepoiObj = entry.getValue().getMainrow();
						if(samepoiObj.getAttrByColName("POI_NUM").equals(sameFid)){
							//判断samefid 对应的 ix_poi对象是否存在
							pidSamePoiPid.put(pid, samepoiObj.getObjPid());
							
							break;
						}
					}*/
				}else{//当sameFid为空时
					emptyFidPids.add(pid);
				}
				}
				
			}
	
		
		//维护父子关系
		log.info("开始维护父子关系");
		importFatherAndSon(childPidParentPid,parentPidChildPid,parentFidChildPid);
		log.info("结束维护父子关系");
		//维护统一关系
		log.info("开始维护同一关系");
		importSamePoi(pidSamePoiPid,sameFidPid,emptyFidPids);
		log.info("结束维护同一关系");
	}

	private void importSamePoi(Map<Long, Long> pidSamePoiPid,
			Map<String, Long> sameFidPid,List<Long> emptyFidPids) throws ServiceException {
		//维护同一关系对象必须加载IX_SAMEPOI，IX_SAMEPOI_PART两张子表
		Set<String> tabNames = new HashSet<String>();
		//tabNames.add("IX_SAMEPOI");
		tabNames.add("IX_SAMEPOI_PART");

		//根据fid加载父对象
		log.info("根据fid加载ix_samepoi父对象");
		loadSameObjByFids(conn,sameFidPid,tabNames,pidSamePoiPid,emptyFidPids);
		//加载子对象原始父对象
		log.info("加载子对象原始父对象");
		//<childPid,parentPid>,用于解除原始父子关系
		Map<Long,Long> pidOriginSamePoiPid = new HashMap<Long,Long>();
		pidOriginSamePoiPid = loadOriginSamePoiObjs(conn,pidSamePoiPid,tabNames,pidOriginSamePoiPid);
		//处理父子关系
		log.info("遍历pidSamePoiPid,维护关系");
		handleSamepoiRelation(conn,pidSamePoiPid,emptyFidPids);
	}

	private void handleSamepoiRelation(Connection conn, Map<Long, Long> pidSamePoiPid,List<Long> emptyFidPids) throws ServiceException {
		//遍历childPidParentPid,维护关系
				try{
					for(Map.Entry<Long,Long> entry:pidSamePoiPid.entrySet()){
						long thisPid = entry.getKey();
						long thisSamePid = entry.getValue();
						
						long thisGroupId = 0;
						long thisSameGroupId = 0;
						//获取 当前pid及 samepoipid对应的groupid 
						Set<Long> pidSet = new HashSet<Long>();
						pidSet.add(thisPid);
						pidSet.add(thisSamePid);
						List<Map<String,Long>> pidGroupIds = IxPoiSelector.getIxSamePoiGroupIdsByPids(conn, pidSet);
						for(Map<String,Long> map : pidGroupIds){
							if(map.get("poi_pid") == thisPid){
								thisGroupId = map.get("group_id");
							}
							else if(map.get("poi_pid") == thisSamePid){
								thisSameGroupId = map.get("group_id");
							}
						}
						BasicObj thisPidObj = null;
						BasicObj thisSamePidObj = null;
						if(thisGroupId != 0){
							thisPidObj = result.getObjsMapByType(ObjectName.IX_SAMEPOI).get(thisGroupId);
						}
						if(thisSameGroupId != 0){
							thisSamePidObj = result.getObjsMapByType(ObjectName.IX_SAMEPOI).get(thisSameGroupId);
						}	
							
						
						if(thisSamePid != 0){//上传数据存在 samefid 
							if(thisGroupId != 0 && thisPidObj != null ){//当poi 存在 原始 同一关系
								if(thisGroupId != thisSameGroupId){//原始的同组poi 不是 上传的samepoi
									log.info("解除 当前poi 的同一关系 :thisPid :" + thisPid );
									thisPidObj.deleteObj();//解除 当前poi 的同一关系
									if(thisSameGroupId != 0 && thisSamePidObj != null ){
										log.info("解除上传的same poi 的同一关系 :thisSamePid :" + thisSamePid );
										thisSamePidObj.deleteObj();//如果上传的poi 存在原始 同组poi ,解除上传的poi 的同一关系
									}
									log.info("创建新的同一关系，thisPid:" + thisPid + ";thisSamePid: " + thisSamePid);
									IxSamePoiObj obj = (IxSamePoiObj) ObjFactory.getInstance().create(ObjectName.IX_SAMEPOI);
									 long groupId = obj.objPid();
									 IxSamepoiPart ixSamepoiPart = obj.createIxSamepoiPart();
									 	ixSamepoiPart.setGroupId(groupId);
									 	ixSamepoiPart.setPoiPid(thisPid);
									 IxSamepoiPart ixSamepoiPartOther = obj.createIxSamepoiPart();
									 	ixSamepoiPartOther.setGroupId(groupId);
									 	ixSamepoiPartOther.setPoiPid(pidSamePoiPid.get(thisSamePid));
									//**将当前 poi的新增的 ix_samepoi 存入 result
									 if(!result.isObjExist(obj)){
											result.putObj(obj);
									}
								}
								
							}else{//不存在 原始同一关系 ,需要新增
								if(thisSameGroupId != 0 && thisSamePidObj != null ){
									log.info("解除上传的same poi 的同一关系 :thisSamePid :" + thisSamePid );
									thisSamePidObj.deleteObj();//如果上传的poi 存在原始 同组poi ,解除上传的poi 的同一关系
								}
								log.info("创建新的同一关系，thisPid:" + thisPid + ";thisSamePid: " + thisSamePid);
								IxSamePoiObj obj = (IxSamePoiObj) ObjFactory.getInstance().create(ObjectName.IX_SAMEPOI);
								 long groupId = obj.objPid();
								 IxSamepoiPart ixSamepoiPart = obj.createIxSamepoiPart();
								 	ixSamepoiPart.setGroupId(groupId);
								 	ixSamepoiPart.setPoiPid(thisPid);
								 IxSamepoiPart ixSamepoiPartOther = obj.createIxSamepoiPart();
								 	ixSamepoiPartOther.setGroupId(groupId);
								 	ixSamepoiPartOther.setPoiPid(pidSamePoiPid.get(thisSamePid));
								//**将当前 poi的新增的 ix_samepoi 存入 result
								 if(!result.isObjExist(obj)){
										result.putObj(obj);
								}
							}
							
						}else{//上传数据 不存在 samefid 
							if(thisGroupId != 0 && thisPidObj != null ){//当poi 存在 原始 同一关系 ,需解除 原始同一关系
								log.info("解除 当前poi 的同一关系 :thisPid :" + thisPid );
								thisPidObj.deleteObj();//解除 当前poi 的同一关系
							}
						}
					}	
				}catch(Exception e){
					DbUtils.rollbackAndCloseQuietly(conn);
					log.error(e.getMessage(), e);
					throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
				}
	}


	private Map<Long, Long> loadOriginSamePoiObjs(Connection conn, Map<Long, Long> pidSamePoiPid, Set<String> tabNames,
			Map<Long, Long> pidOriginSamePoiPid) throws ServiceException {
		//加载子对象原始samepoi对象
		try{
			//获取当前poi对象原始同组对象pid   map<thispid,samepoiPid>
			pidOriginSamePoiPid = IxPoiSelector.getSamePoiPidsByThisPids(conn, pidSamePoiPid.keySet());
			//pidOriginSamePoiPid  是 map<thisPid,samePoiPid>
			//需要加载的原始父对象Pid
			Map<Long,Long> pidOriginSamePoiPidNeedToBeLoad = new HashMap<Long,Long>();
			for(Map.Entry<Long, Long> entry:pidOriginSamePoiPid.entrySet()){
				if(!result.isObjExist(ObjectName.IX_POI, entry.getValue())){
					pidOriginSamePoiPidNeedToBeLoad.put(entry.getKey(), entry.getValue());
				}
			}
			//************需要  ixsamepoi Obj****************
			//<thisPid,groupid>
			//Map<Long,Long> pidOriginGroupIdNeedToBeLoad = IxPoiSelector.getGroupIdByPids(conn,pidOriginSamePoiPidNeedToBeLoad);
			//加载子对象原始父对象
			Map<Long,BasicObj> originSamepoiMap = new HashMap<Long,BasicObj>();
			if(!pidOriginSamePoiPidNeedToBeLoad.isEmpty()){
				originSamepoiMap = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, false,pidOriginSamePoiPidNeedToBeLoad.values(), true, true);
			}

			log.info("将缺失的samepoi对象加载到OperationResult");
			for(Map.Entry<Long, BasicObj> entry:originSamepoiMap.entrySet()){
				if(!result.isObjExist(entry.getValue())){
					result.putObj(entry.getValue());
				}
		}
			log.info("OperationResult所有POI对象加载父子关系子表");
			return pidOriginSamePoiPid;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}


	private void loadSameObjByFids(Connection conn, Map<String, Long> sameFidPid, Set<String> tabNames,
			Map<Long, Long> pidSamePoiPid,List<Long> emptyFidPids) throws ServiceException {
		//根据Pid加载samepoi对象
		try{
			List<Long> otherPids = new ArrayList<Long>();
			if(!sameFidPid.keySet().isEmpty()){
				Map<Long,BasicObj> objs = ObjBatchSelector.selectBySpecColumn(conn, ObjectName.IX_POI, tabNames,false, "POI_NUM", sameFidPid.keySet(), true, true);
				for(BasicObj obj:objs.values()){
					pidSamePoiPid.put(sameFidPid.get((String) obj.getMainrow().getAttrByColName("POI_NUM")), obj.getMainrow().getObjPid());
					otherPids.add(obj.getMainrow().getObjPid());
				}
				//判断哪些父对象没有被加载到
				if(objs.size()<sameFidPid.keySet().size()){
					for(String samepoiFid:sameFidPid.keySet()){
						for(BasicObj obj:objs.values()){
							if(obj.getMainrow().getAttrByColName("POI_NUM").equals(samepoiFid)){
								continue;
							};
						}
						//没有加载到父对象，则不维护该父子关系
						errLog.put(sameFidPid.get(samepoiFid).toString(), "无法根据samepoiFid:"+samepoiFid +" 找到同组poi对象");
						pidSamePoiPid.remove(sameFidPid.get(samepoiFid));
					}
				}
				//加载 ix_samepoi 
				//去 数据库 ix_samepoi_part表查询当前poi 已经再数据库存在的group_id
				List<Map<String,Long>> thisList =IxPoiSelector.getIxSamePoiGroupIdsByPids(conn, pidSamePoiPid.keySet());
				List<Long> thisGroupIdList = new ArrayList<Long>();
				List<Long> thisPidList = new ArrayList<Long>();//存在原始 samepoi 的 pid
				List<Long> thisPidListToNew = new ArrayList<Long>();//需要新建 Ixsamepoi obj 的 pid的集合
				if(thisList != null && thisList.size() > 0){
					for(Map<String,Long> map : thisList){
						thisGroupIdList.add(map.get("group_id"));
						thisPidList.add(map.get("poi_pid"));
					}
					
					if(thisGroupIdList != null && thisGroupIdList.size() > 0){
						//如果thispoi 存在原始 同一关系  ,直接查询加入到 result
						Map<Long,BasicObj> samePoiObjs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_SAMEPOI, tabNames,false,thisGroupIdList, true, true); 
						//**将当前 poi的原始的 ix_samepoi 存入 result
						for(BasicObj samePoiObj : samePoiObjs.values()){
							if(!result.isObjExist(samePoiObj)){
								result.putObj(samePoiObj);
							}
						}
					}
				}
				//处理 同组的 other poi 
				List<Long> otherGroups = IxPoiSelector.getIxSamePoiGroupIdsByPids(conn, otherPids);
				if(otherGroups != null && otherGroups.size() > 0){
					//如果thispoi 存在原始 同一关系  ,直接查询加入到 result
					Map<Long,BasicObj>	otherSamePoiObjs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_SAMEPOI, tabNames,false,thisGroupIdList, true, true); 
					//**将当前 other poi的原始的 ix_samepoi 存入 result
					for(BasicObj otherSamePoiObj : otherSamePoiObjs.values()){
						if(!result.isObjExist(otherSamePoiObj)){
							result.putObj(otherSamePoiObj);
						}
					}
				}
				
				for(Long thispid : pidSamePoiPid.keySet()){
					//排除存在原始 samepoi 的 pid  及 samefid 为空的pid ,不需要新建 Ix_samepoi obj
					if(!thisPidList.contains(thispid) && !emptyFidPids.contains(thispid)){
						thisPidListToNew.add(thispid);
					}
				}
				/*if(thisPidListToNew != null && thisPidListToNew.size() >0 ){
					//如果thispoi 不存在原始同一关系,则先创建再加入到 result 
					for(Long thispid : thisPidListToNew){
						IxSamePoiObj obj = (IxSamePoiObj) ObjFactory.getInstance().create(ObjectName.IX_SAMEPOI);
						 long groupId = obj.objPid();
						 IxSamepoiPart ixSamepoiPart = obj.createIxSamepoiPart();
						 ixSamepoiPart.setGroupId(groupId);
						 ixSamepoiPart.setPoiPid(thispid);
						 IxSamepoiPart ixSamepoiPartOther = obj.createIxSamepoiPart();
						 ixSamepoiPartOther.setGroupId(groupId);
						 ixSamepoiPartOther.setPoiPid(pidSamePoiPid.get(thispid));
						//**将当前 poi的新增的 ix_samepoi 存入 result
						 if(!result.isObjExist(obj)){
								result.putObj(obj);
						}
					}
				}*/
				
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
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
		childPidOriginParentPid = loadOriginParentObjs(conn,childPidParentPid,tabNames,childPidOriginParentPid);
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
	private Map<Long, Long> loadOriginParentObjs(Connection conn, Map<Long, Long> childPidParentPid, Set<String> tabNames, Map<Long, Long> childPidOriginParentPid) throws ServiceException {
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
			Map<Long,BasicObj> originParentMap = new HashMap<Long,BasicObj>();
			if(!childPidOriginParentPidNeedToBeLoad.isEmpty()){
				originParentMap = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, false,childPidOriginParentPidNeedToBeLoad.values(), true, true);
			}

			log.info("将缺失的父对象加载到OperationResult");
			for(Map.Entry<Long, BasicObj> entry:originParentMap.entrySet()){
				if(!result.isObjExist(entry.getValue())){
					result.putObj(entry.getValue());
				}
			}
			log.info("OperationResult所有POI对象加载父子关系子表");
			return childPidOriginParentPid;
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
				Map<Long,BasicObj> objs = ObjBatchSelector.selectBySpecColumn(conn, ObjectName.IX_POI, tabNames,false, "POI_NUM", parentFidChildPid.keySet(), true, true);
				for(BasicObj obj:objs.values()){
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
						for(BasicObj obj:objs.values()){
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
				Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames,false, parentPidChildPid.keySet(), true, true);
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
	
	
	
	public static void main(String[] args) {
		 Map<String, Long> sameFidPid = new HashMap<>();
		 sameFidPid.put("111", (long) 121);
		 sameFidPid.put("222", (long) 221);
		 sameFidPid.put("333", (long) 321);
		 sameFidPid.put(null, (long) 421);
		 sameFidPid.put("333", (long) 521);
		 System.out.println(sameFidPid.size());
		 
		 for(String key : sameFidPid.keySet()){
			 System.out.println("key: "+key+"   "+sameFidPid.get(key));
		 }
		// System.out.println();
		 
	}
}
