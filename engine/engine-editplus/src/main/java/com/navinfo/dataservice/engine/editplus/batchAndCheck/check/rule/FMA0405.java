package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 *检查条件：
 *以下条件其中之一满足时，需要进行检查：
(1)存在官方原始中文名称新增；
(2)存在官方原始中文名称修改
(3)存在分类修改；
 *检查原则：
 *如果种别代码为190100、190101、190102、190103、190104、190105、190106、190107、190108、190109、190110、190111、190112、190500、
 *190501、190502、1190301、190200、190201、190202、190203、190204、230103、230111、230114、230105、230126、230127、150101、
 *（chain值为：6003、6045、6002、6001、6000、6028、6025、602C、6047、6020、6027、600A）、230208、230128、230210（有父子关系并且父分类是230103或230126的记录），官方标准化简体(CHI)中文名称与官方原始简体(CHI)中文不一致的POI全部报出。
 *提示：有简化的POI名称统一：“xxxx” （PRE_KEY）应简化为“xxxx” （RESULT_KEY）
 *备注：如果原始名称在SC_POINT_NAMECK中TYPE=“1”的“PRE_KEY”中能查到，则对应的简化名称在“RESULT_KEY”中；
 *如果原始名称在在SC_POINT_NAMECK中TYPE=“1”的“PRE_KEY”中查不到，则直接报有化简的POI名称统一;
 * @author gaopengrong
 */
public class FMA0405 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
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

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if (!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String newKindCode=poi.getKindCode();
			String newCStr=poi.getChain();
			
			String[] chainArr = new String[]{"6003","6045","6002","6001","6000","6028","6025","602C","6047","6020","6027","600A"};
			List<String> chainList  = Arrays.asList(chainArr);
			
			//获取父
			boolean poiParentFlag = false;
			if(newKindCode.equals("230210")&&(parentMap.containsKey(poi.getPid()))){
				Long parentPid=parentMap.get(poi.getPid());
				Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
				IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
				IxPoi parentPoi=(IxPoi) parentObj.getMainrow();
				if(parentPoi.getKindCode().equals("230103")||parentPoi.getKindCode().equals("230126")){poiParentFlag = true;}
			}
			
			if(newKindCode.equals("190100") ||newKindCode.equals("190100")
					||newKindCode.equals("190101")||newKindCode.equals("190102")||newKindCode.equals("190103")
					||newKindCode.equals("190104")||newKindCode.equals("190105")||newKindCode.equals("190106")
					||newKindCode.equals("190107")||newKindCode.equals("190108")||newKindCode.equals("190109")
					||newKindCode.equals("190110")||newKindCode.equals("190111")||newKindCode.equals("190112")
					||newKindCode.equals("190500")||newKindCode.equals("190501")||newKindCode.equals("190301")
					||newKindCode.equals("190200")||newKindCode.equals("190201")||newKindCode.equals("190202")
					||newKindCode.equals("190203")||newKindCode.equals("190204")||newKindCode.equals("230103")
					||newKindCode.equals("230111")||newKindCode.equals("230114")||newKindCode.equals("230105")
					||newKindCode.equals("230126")||newKindCode.equals("230127")||newKindCode.equals("230208")
					||newKindCode.equals("230128")||newKindCode.equals("190501")||newKindCode.equals("230210")
					||newKindCode.equals("190502")||newKindCode.equals("150101")){
				
				if(newKindCode.equals("150101")){
					if(!chainList.contains(newCStr)){return;}
				}
				if(newKindCode.equals("230210")){
					if(!poiParentFlag){return;}
				}
				
				//取官方原始英中文
				IxPoiName originName=poiObj.getOfficeOriginCHIName();
				String originNameStr=originName.getName();
				//取官方标准化中文
				IxPoiName standardName=poiObj.getOfficeStandardCHIName();
				String standardNameStr=standardName.getName();
				
				if(originNameStr.isEmpty()&&standardNameStr.isEmpty()){return;}
				if(originNameStr.equals(standardNameStr)){return;}
				
				MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				
				List<ScPointNameckObj> typeD1 = metadataApi.scPointNameckTypeD1();
				List<ScPointNameckObj> keyResult=ScPointNameckUtil.matchTypeD1(originNameStr, typeD1);
				if (keyResult.size()==0){
					String log="有简化的POI名称统一";
					setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					return;
				}
				for(ScPointNameckObj nameckObj:keyResult){
					if(originNameStr.contains(nameckObj.getPreKey())&&!standardNameStr.contains(nameckObj.getResultKey())){
						String log="有简化的POI名称统一：“"+nameckObj.getPreKey()+"”,应简化为“"+nameckObj.getResultKey()+"”";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
				}

			}
		}
	}
	private boolean isCheck(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			String newKindCode=poi.getKindCode();
			if(!newKindCode.equals(oldKindCode)){
				return true;
			}
		}
		//存在IX_POI_NAME新增或者修改履历
		IxPoiName br=poiObj.getOfficeOriginCHIName();
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
	
}
