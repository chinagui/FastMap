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
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjChildrenIncreSelector;

/** 
 * @ClassName: PoiRelationImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: PoiRelationImportor.java
 */
public class PoiRelationImportor extends AbstractOperation{
	protected PoiRelationImporterCommand poiRelationImporterCommand;
	
	public PoiRelationImportor(Connection conn,  OperationResult preResult
			,PoiRelationImporterCommand poiRelationImporterCommand) {
		super(conn,  preResult);
		this.poiRelationImporterCommand = poiRelationImporterCommand;
		
	}

	private void importFatherAndSon(Set<PoiRelation> fatherAndSonSet
			, Set<Long> fatherPidSet, Set<String> fatherFidSet) throws Exception {
		//加载所有对象
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_PARENT");
		tabNames.add("IX_POI_CHILDREN");

		result.getObjsMapByType(ObjectType.IX_POI);
		
		if(!fatherPidSet.isEmpty()){
			List<BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectType.IX_POI, tabNames, fatherPidSet, true, true);
			for(BasicObj obj:objs){
				result.putObj(obj);
			}
		}
		if(!fatherFidSet.isEmpty()){
			List<BasicObj> objs = ObjBatchSelector.selectBySpecColumn(conn, ObjectType.IX_POI, tabNames, "POI_NUM", fatherFidSet, true, true);
			for(BasicObj obj:objs){
				if(!result.isObjExist(obj)){
					result.putObj(obj);
				}
			}
		}
		
//		//不存在父子关系子表的对象加载父子关系
//		if(!fatherIncreSelMap.isEmpty()){
//			ObjChildrenIncreSelector.increSelect(fatherIncreSelMap, tabNames);
//		}
		
		//遍历fatherAndSonSet,维护关系
		for(PoiRelation poiRelation:fatherAndSonSet){
			long fatherPid = poiRelation.getFatherPid();
			long pid = poiRelation.getPid();
			BasicObj fatherObj = result.getObjsMapByType(ObjectType.IX_POI).get(fatherPid);
			List<BasicRow> parentList = new ArrayList<BasicRow>();
			
			if(fatherObj.getRowsByName("IX_POI_PARENT").isEmpty()){
				//非父节点，需要创建其为父节点
				IxPoiParent ixPoiParent = (IxPoiParent) ObjFactory.getInstance().createRow("IX_POI_PARENT", fatherPid);
				ixPoiParent.setAttrByCol("PARENT_POI_PID", fatherPid);
				parentList = new ArrayList<BasicRow>();
				parentList.add(ixPoiParent);
				fatherObj.setSubrows("IX_POI_PARENT", parentList);
			}else{
				parentList = fatherObj.getRowsByName("IX_POI_PARENT");
			}
			IxPoiParent ixPoiParent = (IxPoiParent) parentList.get(0);
			
			List<BasicRow> childrenList = fatherObj.getRowsByName("IX_POI_CHIDREN");
			
			IxPoiChildren ixPoiChildren = (IxPoiChildren) ObjFactory.getInstance().createRow("IX_POI_CHIDREN", fatherPid);
			ixPoiChildren.setAttrByCol("GROUP_ID", ixPoiParent.getGroupId());
			ixPoiChildren.setAttrByCol("CHILD_POI_PID", pid);
			childrenList.add(ixPoiChildren);
			fatherObj.setSubrows("IX_POI_CHIDREN", childrenList);
		}
		
		
		
	}


	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.plus.operation.AbstractOperation#operate()
	 */
	@Override
	public void operate() throws Exception {
		Set<PoiRelation> fatherAndSonSet = new HashSet<PoiRelation>();
		Set<Long> fatherPidSet = new HashSet<Long>();
		Set<String> fatherFidSet = new HashSet<String>();
		
		for(PoiRelation poiRelation:poiRelationImporterCommand.getPoiRels()){
			if(poiRelation.getPoiRelationType().equals(PoiRelationType.FATHER_AND_SON)){
				fatherAndSonSet.add(poiRelation);
				long fatherPid = poiRelation.getFatherPid();
				if(fatherPid!=0&&!result.isObjExist(ObjectType.IX_POI,fatherPid)){
					fatherPidSet.add(fatherPid);
				}else{
					fatherFidSet.add(poiRelation.getFatherFid());
				}
			}
		}
		
		//维护父子关系
		importFatherAndSon(fatherAndSonSet,fatherPidSet,fatherFidSet);
		
		//维护统一关系
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "PoiRelationImportor";
}
}
