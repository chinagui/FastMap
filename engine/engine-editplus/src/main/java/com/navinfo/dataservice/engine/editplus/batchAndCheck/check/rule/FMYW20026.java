package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * 检查条件：
 * 以下条件其中之一满足时，需要进行检查：
 * (1)存在IX_POI_ADDRESS新增 ；
 * (2)存在IX_POI_ADDRESS修改且IX_POI_ADDRESS存在记录；
 * 检查原则： 
 * 检查SC_POINT_NAMECK表中TYPE=5且HM_FLAG<>’HM’，ROADNAME+ADDRNAME包含关键字且拼音与配置表中拼音不相同的报出
 * 检查SC_POINT_NAMECK表中TYPE=7且HM_FLAG<>’HM’，只要ROADNAME+ADDRNAME中包含关键字的报出。
 * 提示：地址常见多音字检查：地址中含有多音字“xx”
 * @author gaopengrong
 */
public class FMYW20026 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//存在IxPoiAddress新增或者修改履历
			IxPoiAddress address=poiObj.getCHIAddress();
			if(address.getHisOpType().equals(OperationType.INSERT)||(address.getHisOpType().equals(OperationType.UPDATE))){
				String oldRoadNme=(String) address.getHisOldValue(IxPoiAddress.ROADNAME);
				String newRoadNme=(String) address.getRoadname();
				String oldAddrName=(String) address.getHisOldValue(IxPoiAddress.ADDRNAME);
				String newAddrName=(String) address.getAddrname();
				if (newRoadNme.equals(oldRoadNme)&&newAddrName.equals(oldAddrName)){return;}
				String roadNmePhonetic=(String) address.getRoadnamePhonetic();
				String addrNamePhonetic=(String) address.getAddrnamePhonetic();
				String RAName=newRoadNme+newAddrName;
				String RANamePhonetic=roadNmePhonetic+addrNamePhonetic;
				
				String[] RANameStr= RAName.split("|");
				//String[] RANamePhoneticStr= RANamePhonetic.split("|");
				
				for (String nameStr:RANameStr){
					//检查SC_POINT_NAMECK表中TYPE=5且HM_FLAG<>’HM’名称包含关键字且拼音与配置表中拼音不相同的报出
					MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					Map<String, String> typeD5 = metadataApi.scPointNameckTypeD5();
					Map<String, String> keyResult5=ScPointNameckUtil.matchType(nameStr, typeD5);
					for(String preKey:keyResult5.keySet()){
						if (!RANamePhonetic.contains(keyResult5.get(preKey))){
							String log="ROADNAME+ADDRNAME包含多音字“"+preKey+"”,且拼音与配置表中拼音不相同";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						}
					}
					//检查SC_POINT_NAMECK表中TYPE=7且HM_FLAG<>’HM’只要名称中包含关键字的报出
					Map<String, String> typeD7 = metadataApi.scPointNameckTypeD7();
					Map<String, String> keyResult7=ScPointNameckUtil.matchType(nameStr, typeD7);
					if (keyResult7.size()!=0){
						String log="ROADNAME+ADDRNAME中包含关键字“"+(keyResult7.keySet()).toString()+"”";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
				}
			}
		 }
	}
	
	
}
