package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjChildrenIncreSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/** 
 * @ClassName: PoiRelationImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: PoiRelationImportor.java
 */
public class PoiRelationImportor extends AbstractOperation{
	protected Logger log = Logger.getLogger(this.getClass());
	
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
		//需要加载的父对象pid
		Set<Long> fatherPidSet = new HashSet<Long>();
		//需要加载的父对象fid
		Set<String> fatherFidSet = new HashSet<String>();
		
		for(PoiRelation poiRelation:poiRelationImporterCommand.getPoiRels()){
			//处理父子关系导入
			if(poiRelation.getPoiRelationType().equals(PoiRelationType.FATHER_AND_SON)){
				long pid = poiRelation.getPid();
				long fatherPid = poiRelation.getFatherPid();
				childPidParentPid.put(pid, fatherPid);
				//需要加载的父对象存在pid,优先用pid加载
				if(fatherPid!=0&&!result.isObjExist(ObjectType.IX_POI,fatherPid)){
					fatherPidSet.add(fatherPid);	
				}else{
					String fatherFid = poiRelation.getFatherFid();
					if(fatherFid!=null&&!fatherFid.equals(""))
					{
						//result中是否含此父对象。如果包含则不加载
						boolean isFatherExist = false;
						for(Map.Entry<Long, BasicObj> entry:result.getObjsMapByType(ObjectType.IX_POI).entrySet()){
							BasicRow fatherObj = entry.getValue().getMainrow();
							if(fatherObj.getAttrByColName("POI_NUM").equals(fatherFid)){
								childPidParentPid.put(pid, fatherObj.getObjPid());
								isFatherExist = true;
								break;
							}
						}
						if(!isFatherExist){
							fatherFidSet.add(poiRelation.getFatherFid());
							parentFidChildPid.put(fatherFid,pid);
						}
					}			
				}
			}
		}
		
		//维护父子关系
		log.info("开始维护父子关系");
		importFatherAndSon(childPidParentPid,parentFidChildPid,fatherPidSet,fatherFidSet);
		log.info("结束维护父子关系");
		//维护统一关系
		
	}

	/**
	 * @param childPidParentPid
	 * @param parentFidChildPid
	 * @param fatherPidSet
	 * @param fatherFidSet
	 * @throws Exception 
	 */
	private void importFatherAndSon(Map<Long, Long> childPidParentPid, Map<String, Long> parentFidChildPid,
			Set<Long> fatherPidSet, Set<String> fatherFidSet) throws Exception {
		//维护父子关系对象必须加载IX_POI_PARENT，IX_POI_CHILDREN两张子表
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_PARENT");
		tabNames.add("IX_POI_CHILDREN");
		
		log.info("根据pid加载父对象");
		//根据fid加载父对象
		if(!fatherPidSet.isEmpty()){
			Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectType.IX_POI, tabNames, fatherPidSet, true, true);
			for(BasicObj obj:objs.values()){
				result.putObj(obj);
			}
		}
		log.info("根据fid加载父对象");
		//根据fid加载父对象
		if(!fatherFidSet.isEmpty()){
			List<BasicObj> objs = ObjBatchSelector.selectBySpecColumn(conn, ObjectType.IX_POI, tabNames, "POI_NUM", fatherFidSet, true, true);
			for(BasicObj obj:objs){
				if(!result.isObjExist(obj)){
					result.putObj(obj);
				}
				long childPid = parentFidChildPid.get(obj.getMainrow().getAttrByColName("POI_NUM"));
				//更新childPidParentPid，将父对象pid补上
				childPidParentPid.put(childPid, obj.objPid());
			}
		}
		log.info("加载子对象原始父对象");
		//加载子对象原始父对象
		Map<Long,BasicObj> originParentMap = IxPoiSelector.getIxPoiParentMapByChildrenPidList(conn, childPidParentPid.keySet());
		Map<Long,Long> childPidOrigionParentPid = new HashMap<Long,Long>();
		log.info("将缺失的父对象加载到OperationResult");
		for(Map.Entry<Long, BasicObj> entry:originParentMap.entrySet()){
			childPidOrigionParentPid.put(entry.getKey(), entry.getValue().objPid());
			if(!result.isObjExist(entry.getValue())){
				result.putObj(entry.getValue());
			}
		}
		log.info("OperationResult所有POI对象加载父子关系子表");
		//加载父子关系子表
		ObjChildrenIncreSelector.increSelect(conn, result.getObjsMapByType(ObjectType.IX_POI), tabNames);
	
		//处理父子关系
		//遍历childPidParentPid,维护关系
		log.info("遍历childPidParentPid,维护关系");
		for(Map.Entry<Long,Long> entry:childPidParentPid.entrySet()){
			long childPid = entry.getKey();
			long parentPid = entry.getValue();
			//解除原始父子关系
			if(childPidOrigionParentPid.containsKey(childPid)&&childPidOrigionParentPid.get(childPid)!=entry.getValue()){
				log.info("解除原始父子关系，childPid:" + childPid + ";parentPid:" + childPidOrigionParentPid.get(childPid));
				BasicObj origionParentObj = result.getObjsMapByType(ObjectType.IX_POI).get(childPidOrigionParentPid.get(childPid));
				List<BasicRow> ixPoiChildrenList = origionParentObj.getRowsByName("IX_POI_CHILDREN");
				List<BasicRow> ixPoiParentList = origionParentObj.getRowsByName("IX_POI_PARENT");
				if(ixPoiChildrenList.size()==1){
					origionParentObj.deleteSubrow(ixPoiParentList.get(0));
				}
				for(BasicRow ixPoiChild:ixPoiChildrenList){
					if((long)ixPoiChild.getAttrByColName("CHILD_POI_PID")==childPid){
						origionParentObj.deleteSubrow(ixPoiChild);
						break;
					}
				}
				
			}
			//维护新的父子关系
			if(parentPid!=0){
				log.info("创建父子关系，childPid:" + childPid + ";parentPid:" + parentPid);
				BasicObj parentObj = result.getObjsMapByType(ObjectType.IX_POI).get(parentPid);
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
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "PoiRelationImportor";
	}
}
