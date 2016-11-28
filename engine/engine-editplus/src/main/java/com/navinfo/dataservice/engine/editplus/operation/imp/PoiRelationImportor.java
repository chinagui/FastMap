package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationResultException;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjChildrenIncreSelector;

/** 
 * @ClassName: PoiRelationImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: PoiRelationImportor.java
 */
public class PoiRelationImportor extends AbstractOperation{
	
	public PoiRelationImportor(Connection conn, String name, OperationResult preResult) {
		super(conn, name, preResult);
	}

	public static void doImport(Connection conn,OperationResult or,List<PoiRelation> relations) 
			throws Exception{
		Set<PoiRelation> fatherAndSonSet = new HashSet<PoiRelation>();
//		Set<PoiRelation> samePoiSet = new HashSet<PoiRelation>();
		Set<Long> fatherPidSet = new HashSet<Long>();
		Set<String> fatherFidSet = new HashSet<String>();
		Map<Long,BasicObj> fatherIncreSelMap = new HashMap<Long,BasicObj>();
		
		for(PoiRelation poiRelation:relations){
			if(poiRelation.getPoiRelationType().equals(PoiRelationType.FATHER_AND_SON)){
				fatherAndSonSet.add(poiRelation);
				long fatherPid = poiRelation.getFatherPid();
				if(fatherPid!=0&&!or.isObjExist(ObjectType.IX_POI,fatherPid)){
					//加载不存在父子关系子表的obj
					if(or.isObjExist(ObjectType.IX_POI,fatherPid)){
						if(or.getObjsMapByType(ObjectType.IX_POI).get(fatherPid).getRowsByName("IX_POI_PARENT")==null){
							fatherIncreSelMap.put(fatherPid, or.getObjsMapByType(ObjectType.IX_POI).get(fatherPid));
						}

	@Override
	public void operate() throws Exception {
		
<<<<<<< .mine
					}
=======
	}
>>>>>>> .theirs
					fatherPidSet.add(fatherPid);
				}else{
					fatherFidSet.add(poiRelation.getFatherFid());
				}
			}
		}
		
		//维护父子关系
		importFatherAndSon(conn,or,fatherAndSonSet,fatherPidSet,fatherFidSet,fatherIncreSelMap);
		
		//维护统一关系
	}

	/**
	 * @param or
	 * @param fatherAndSonSet
	 * @param fatherPidSet
	 * @param fatherFidSet 
	 * @param fatherIncreSelMap 
	 * @throws Exception 
	 */
	private static void importFatherAndSon(Connection conn,OperationResult or, Set<PoiRelation> fatherAndSonSet
			, Set<Long> fatherPidSet, Set<String> fatherFidSet, Map<Long, BasicObj> fatherIncreSelMap) throws Exception {
		//加载所有对象
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_PARENT");
		tabNames.add("IX_POI_CHILDREN");

		or.getObjsMapByType(ObjectType.IX_POI);
		
		if(!fatherPidSet.isEmpty()){
			List<BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectType.IX_POI, tabNames, fatherPidSet, true, true);
			for(BasicObj obj:objs){
				or.putObj(obj);
			}
		}
		if(!fatherFidSet.isEmpty()){
			List<BasicObj> objs = ObjBatchSelector.selectBySpecColumn(conn, ObjectType.IX_POI, tabNames, "POI_NUM", fatherFidSet, true, true);
			for(BasicObj obj:objs){
				if(!or.isObjExist(obj)){
					or.putObj(obj);
				}
			}
		}
		
		//不存在父子关系子表的对象加载父子关系
		if(!fatherIncreSelMap.isEmpty()){
			ObjChildrenIncreSelector.increSelect(fatherIncreSelMap, tabNames);
		}
		
		//遍历fatherAndSonSet,维护关系
		for(PoiRelation poiRelation:fatherAndSonSet){
			long fatherPid = poiRelation.getFatherPid();
			long pid = poiRelation.getPid();
			BasicObj fatherObj = or.getObjsMapByType(ObjectType.IX_POI).get(fatherPid);
			//非父节点，需要创建其为父节点
			if(fatherObj.getRowsByName("IX_POI_PARENT").isEmpty()){
				IxPoiParent ixPoiParent = (IxPoiParent) ObjFactory.getInstance().createRow("IX_POI_PARENT", fatherPid);
				ixPoiParent.setAttrByCol("PARENT_POI_PID", fatherPid);
				
				List<BasicRow> parentList = new ArrayList<BasicRow>();
				parentList.add(ixPoiParent);
				IxPoiChildren ixPoiChildren = (IxPoiChildren) ObjFactory.getInstance().createRow("IX_POI_CHIDREN", fatherPid);
				ixPoiChildren.setAttrByCol("GROUP_ID", ixPoiParent.getGroupId());
				ixPoiChildren.setAttrByCol("CHILD_POI_PID", pid);
				List<BasicRow> childrenList = new ArrayList<BasicRow>();
				childrenList.add(ixPoiChildren);
				fatherObj.setSubrows("IX_POI_PARENT", parentList);
				fatherObj.setSubrows("IX_POI_CHIDREN", childrenList);
				
			}
			
		}
		
		
		
	}
	
}
