package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * @ClassName FMGLM60271
 * @author Han Shaoming
 * @date 2017年2月20日 上午11:35:10
 * @Description TODO
 * 检查对象：非删除POI对象
 * 检查原则：当父POI的分类是公司、企业（220100）时，不参与检查
 * 1、常规POI（除内部POI（ix_poi.indoor=1)外的POI），不能与附表中内部POI的父分类制作父子关系，否则程序报LOG:正常POI与内部POI的父制作父子关系
 * （parent=2的POI只能是内部POI的父，当kindCode对应的parent=2且子POI是常规POI时，报log）
 * 2、父POI分类（通过fid关联kindCode）不在附表中时，程序报LOG:父POI的分类不在可以作为父分类的范围内
 * 附表见附件中的POI父分类(SC_FM_CONTROL表，parent！=0 或者parent=0 且fid在SC_POINT_FOCUS.POI_NUM列表中且SC_POINT_FOCUS.TYPE=2可为父)
 */
public class FMGLM60271 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
			//是否有父
			if(!parentMap.containsKey(poi.getPid())){return;}
			Long parentId=parentMap.get(poi.getPid());
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
			IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
			IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
			String kindCodeP = parentPoi.getKindCode();
			if(kindCodeP == null ||"220100".equals(kindCodeP)){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, Integer> searchScFmControl = metadataApi.searchScFmControl(kindCodeP);
			if(searchScFmControl == null || searchScFmControl.isEmpty()){return;}
			//子poi内部标识
			int indoor = poi.getIndoor();
			if(searchScFmControl.containsKey(kindCodeP)){
				int parent = searchScFmControl.get(kindCodeP);
				if(parent == 2 && indoor==0){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "正常POI与内部POI的父制作父子关系");
					return;
				}
				else if(parent == 0){
					String poiNumP = parentPoi.getPoiNum();
					Map<String, Integer> searchScPointFocus = metadataApi.searchScPointFocus(poiNumP);
					if(searchScPointFocus==null || !searchScPointFocus.containsKey(poiNumP)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "父POI的分类不在可以作为父分类的范围内");
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
			pidList.add(obj.objPid());
		}
		parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		//referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
