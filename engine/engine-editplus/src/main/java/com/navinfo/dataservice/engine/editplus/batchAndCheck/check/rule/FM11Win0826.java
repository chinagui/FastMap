package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM11Win0826
 * @author zhangxiaoyi
 * @date 2017年2月8日 下午3:50:52
 * @Description TODO
 * 检查条件：    Lifecycle！=1（删除）
 * 检查原则：
 * 父名称修改，子设施地址未修改
 * 子设施为内部POI且其地址（address）不包含父POI的名称（name），
 * 报Log：父名称修改，子设施地址未修改
 */
public class FM11Win0826 extends BasicCheckRule {

	private Map<Long, BasicObj> referObjs=new HashMap<Long, BasicObj>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		log.info("start FM11Win0826");
			IxPoiObj poiObj=(IxPoiObj) obj;
			//IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj == null){return;}
			//是否有子
			List<IxPoiChildren> childs = poiObj.getIxPoiChildrens();
			if(childs==null||childs.size()==0){return;}
			//父名称未修改
			if(!nameObj.getHisOpType().equals(OperationType.UPDATE)
					||!nameObj.hisOldValueContains(IxPoiName.NAME)){
				return;
				//log.info("");
			}
			for(IxPoiChildren c:childs){
				//子是否内部poi
				BasicObj basicObj=referObjs.get(c.getChildPoiPid());
				if(basicObj.isDeleted()){continue;}
				IxPoiObj cpoiObj=(IxPoiObj) basicObj;
				IxPoi cpoi=(IxPoi) cpoiObj.getMainrow();
				int indoor = cpoi.getIndoor();
				if(indoor!= 1){continue;}
				IxPoiAddress address = cpoiObj.getCHAddress();
				if(address==null){continue;}
				String fullname = address.getFullname();
				String name = nameObj.getName();
				if(fullname == null || name == null){return;}
				if(!fullname.contains(name)){
					String target="[IX_POI,"+obj.objPid()+"];[IX_POI,"+basicObj.objPid()+"]";
					setCheckResult(cpoi.getGeometry(), target,cpoi.getMeshId());
					return;
				}
			}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		log.info("start loadReferDatas");
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			IxPoiObj poiObj=(IxPoiObj) obj;
			List<IxPoiChildren> childs = poiObj.getIxPoiChildrens();
			if(childs==null||childs.size()==0){continue;}
			for(IxPoiChildren c:childs){pidList.add(c.getChildPoiPid());}
		}
		//parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_ADDRESS");
		referObjs = getCheckRuleCommand().loadReferObjsByLog(pidList, ObjectName.IX_POI, referSubrow, false);
		log.info("end loadReferDatas");
	}

}
