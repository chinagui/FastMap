package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FM11Win0201
 * @author Han Shaoming
 * @date 2017年2月28日 下午8:24:49
 * @Description TODO
 * 检查条件：    非删除POI对象
 * 检查原则：
 * 1）分类为230210或230213；
 * 2）官方原始中文名称中含有“-”；
 * 3）官方原始中文名称中含有“门”或“門”；
 * 满足上面三个条件中的任意一条的子设施，其父设施修改了官方原始中文名称，子设施官方原始中文名称没有修改，
 * 则报Log：父设施名称修改，子设施名称未修改！
 * 备注：连接符“-”不区分全半角
 */
public class FM11Win0201 extends BasicCheckRule {

	private Map<Long, BasicObj> referObjs=new HashMap<Long, BasicObj>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			//IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			//是否有子
			List<IxPoiChildren> childs = poiObj.getIxPoiChildrens();
			if(childs==null||childs.size()==0){return;}
			//父名称未修改
			if(!ixPoiName.getHisOpType().equals(OperationType.UPDATE)
					||!ixPoiName.hisOldValueContains(IxPoiName.NAME)){
				//log.info("");
				return;
			}
			for(IxPoiChildren c:childs){
				boolean check = false;
				//子是否满足以上3个条件之一
				BasicObj basicObj=referObjs.get(c.getChildPoiPid());
				if(basicObj.isDeleted()){continue;}
				IxPoiObj cpoiObj=(IxPoiObj) basicObj;
				IxPoi cpoi=(IxPoi) cpoiObj.getMainrow();
				String kindCodeC = cpoi.getKindCode();
				if(kindCodeC == null){continue;}
				IxPoiName ixPoiNameC = cpoiObj.getOfficeOriginCHName();
				if(ixPoiNameC == null){continue;}
				if("230210".equals(kindCodeC)||"230213".equals(kindCodeC)){
					check = true;
				}
				String nameC = ixPoiNameC.getName();
				if(nameC != null){
					String nameCStr = CheckUtil.strQ2B(nameC);
					if(nameCStr.contains("-")||nameCStr.contains("门")||nameCStr.contains("門")){
						check = true;
					}
				}
				if(check){
					if(!ixPoiNameC.getHisOpType().equals(OperationType.UPDATE)
							||!ixPoiNameC.hisOldValueContains(IxPoiName.NAME)){
						String target="[IX_POI,"+obj.objPid()+"];[IX_POI,"+basicObj.objPid()+"]";
						setCheckResult(cpoi.getGeometry(), target,cpoi.getMeshId());
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			IxPoiObj poiObj=(IxPoiObj) obj;
			List<IxPoiChildren> childs = poiObj.getIxPoiChildrens();
			if(childs==null||childs.size()==0){continue;}
			for(IxPoiChildren c:childs){pidList.add(c.getChildPoiPid());}
		}
		//parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		referObjs = getCheckRuleCommand().loadReferObjsByLog(pidList, ObjectName.IX_POI, referSubrow, false);
	}

}
