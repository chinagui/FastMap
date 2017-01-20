package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * FM-14Sum-11-17-01
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查；
 * 检查原则：
 * 只针对改名称、新增且有父子关系的POI
 * 名称（name）中不以父设施名称开头的子设施停车场（kindCode=230210\230211\230212\230213\230214）。
 * @author zhangxiaoyi
 */
public class FM14Sum111701 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind = poi.getKindCode();
			if(!kind.equals("230210")&&!kind.equals("230211")&&!kind.equals("230212")
					&&!kind.equals("230213")&&!kind.equals("230214")){
				return;
			}
			if(!isCheck(poiObj)){return;}
			//是否有父
			if(!parentMap.containsKey(poi.getPid())){return;}
			Long parentId=parentMap.get(poi.getPid());
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
			//名称（name）中不以父设施名称开头的子设施停车场
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			
			IxPoiName parentNameObj = ((IxPoiObj) parentObj).getOfficeOriginCHName();
			if(parentNameObj==null){return;}
			String parentNameStr = parentNameObj.getName();
			if(parentNameStr==null||parentNameStr.isEmpty()){return;}
			
			if(!nameStr.startsWith(parentNameStr)){
				setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}
	/**
	 * 只针对改名称、新增的POI true
	 * @param poiObj
	 * @return
	 */
	private boolean isCheck(IxPoiObj poiObj){
		//存在IX_POI_NAME新增或者修改履历
		IxPoiName br=poiObj.getOfficeOriginCHName();
		if(br!=null){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}
			}
		}
		return false;
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
