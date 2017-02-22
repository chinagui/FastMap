package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * 检查条件：
	以下条件其中之一满足时，需要进行检查：
	(1)存在官方原始中文名称新增；
	(2)存在官方原始中文名称修改
	(3)存在分类修改；
	检查原则：
	如果种别代码为190100、190101、190102、190103、190104、190105、190106、190107、190108、190109、190110、190111、
	190112、190500、190501、190502、190301、190200、190201、190202、190203、190204、230103、230111、230114、230105、
	230126、230127、150101（chain值为：6003、6045、6002、6001、6000、6028、6025、602C、6047、6020、6027、600A）、
	230208、230128、230210（有父子关系并且父分类是230103或230126的记录），
	但官方标准化简体(CHI)中文名称与官方原始简体(CHI)中文名称一致的POI全部报出。
	提示：无简化的POI名称统一
 * @author zhangli5174
 *
 */
public class FMA0404 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			
			//判断poi 分类是否修改
			String newKCStr=poi.getKindCode();
			String newCStr=poi.getChain();
			boolean poiUpdateFlag = false;
			if(poi.getHisOpType().equals(OperationType.UPDATE) && poi.hisOldValueContains(IxPoi.KIND_CODE)){
				String oldKCStr=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
				if(!newKCStr.equals(oldKCStr)){
					poiUpdateFlag = true;
				}
			}
			
			String[] kindCodeArr = new String[]{"190100","190101","190102","190103","190104","190105","190106","190107","190108","190109","190110","190111","190112","190500","190501","190502","190301","190200","190201","190202","190203","190204","230103","230111","230114","230105","230126","230127","150101","230208","230128","230210"};
			List<String> kindCodeList  = Arrays.asList(kindCodeArr);
			
			String[] chainArr = new String[]{"6003","6045","6002","6001","6000","6028","6025","602C","6047","6020","6027","600A"};
			List<String> chainList  = Arrays.asList(chainArr);
			
			//获取父
			boolean poiParentFlag = false;
			if(newKCStr.equals("230210")&&(parentMap.containsKey(poi.getPid()))){
				Long parentPid=parentMap.get(poi.getPid());
				Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
				IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
				IxPoi parentPoi=(IxPoi) parentObj.getMainrow();
				if(parentPoi.getKindCode().equals("230103")||parentPoi.getKindCode().equals("230126")){poiParentFlag = true;}
			}

			

			//获取
			IxPoiName standardNameObj=poiObj.getOfficeStandardCHName();
			IxPoiName originalNameObj=poiObj.getOfficeOriginCHIName();
			if(standardNameObj != null && originalNameObj != null ){
				String standardName = "";
				String originalName = "";
			
				//存在IX_POI_NAME新增或者修改履历
				if((poiUpdateFlag ||  originalNameObj.getHisOpType().equals(OperationType.INSERT)|| originalNameObj.getHisOpType().equals(OperationType.UPDATE)) 
						&& kindCodeList.contains(newKCStr)){
					if(newKCStr.equals("150101")){
						if(!chainList.contains(newCStr)){return;}
					}
					if(newKCStr.equals("230210")){
						if(!poiParentFlag){return;}
					}
					
					//标准化
					standardName = standardNameObj.getName();
					//原始
					originalName = originalNameObj.getName();
				}
				if(StringUtils.isNotEmpty(standardName) && StringUtils.isNotEmpty(originalName) && standardName.equals(originalName)){
					String log="无简化的POI名称统一";
					System.out.println("poi"+poi.getPid());
					setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
				}
		}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentMap=IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, null, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}
	
}
