package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * @ClassName FMYW20051
 * @author Han Shaoming
 * @date 2017年2月20日 下午5:12:59
 * @Description TODO
 * 检查对象：非删除POI对象
 * 检查原则：
 * 当父POI的分类是220100时
 * 1、父POI.vip_flag=2或POI_NUM在元数据库SC_POINT_FOCUS.POI_NUM列表且SC_POINT_FOCUS.TYPE=2中不存在时
 * 报LOG： POI与公司制作了父子关系（当确认父POI是车厂时，点忽略；当确认父POI不是车厂时，请解除父子关系）；
 */
public class FMYW20051 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//是否有父
			if(!parentMap.containsKey(poi.getPid())){return;}
			Long parentId=parentMap.get(poi.getPid());
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
			IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
			IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
			String kindCodeP = parentPoi.getKindCode();
			if(kindCodeP == null || !"220100".equals(kindCodeP)){return;}
			String vipFlag = parentPoi.getVipFlag();
			//父POI.vip_flag=2
			if(vipFlag != null){
				String str = vipFlag.replace("\\|", ",");
				List<Integer> vipFlags = StringUtils.getIntegerListByStr(str);
				if(vipFlags.contains(2)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
			}
			//POI_NUM在元数据库SC_POINT_FOCUS.POI_NUM列表且SC_POINT_FOCUS.TYPE=2中不存在
			String poiNumP = parentPoi.getPoiNum();
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, Integer> searchScPointFocus = metadataApi.searchScPointFocus(poiNumP);
			if(searchScPointFocus==null || !searchScPointFocus.containsKey(poiNumP)){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		//referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
