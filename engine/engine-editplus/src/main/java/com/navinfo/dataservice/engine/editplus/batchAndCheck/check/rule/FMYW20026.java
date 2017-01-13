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
 * (1)存在官方中文地址IX_POI_ADDRESS新增 ；
 * (2)存在官方中文地址IX_POI_ADDRESS修改且官方中文地址IX_POI_ADDRESS存在记录；
 * 检查原则： 
 * 检查SC_POINT_NAMECK表中TYPE=5且HM_FLAG<>’HM’，18个字段组合后(省名|市名|区县名|街道名|小区名|街巷名|标志物名|前缀|门牌号|类型名|子号|后缀|附属设施名|楼栋号|楼层|楼门号|房间号|附加信息)包含关键字且拼音与配置表中拼音不相同的报出
 * 检查SC_POINT_NAMECK表中TYPE=7且HM_FLAG<>’HM’，只要18个字段中包含关键字的报出。
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
			IxPoiAddress address=poiObj.getCHAddress();
			//错误数据
			if(address==null){return;}
			if(address.getHisOpType().equals(OperationType.INSERT)||(address.getHisOpType().equals(OperationType.UPDATE))){
				
				MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				
				String allStr = address.getProvince()+"|"+address.getCity()+"|"+address.getCounty()+"|"+address.getTown()+"|"
						+address.getPlace()+"|"+address.getStreet()+"|"+address.getLandmark()+"|"+address.getPrefix()+"|"+address.getHousenum()+"|"
						+address.getType()+"|"+address.getSubnum()+"|"+address.getSurfix()+"|"+address.getEstab()+"|"+address.getBuilding()+"|"
						+address.getUnit()+"|"+address.getFloor()+"|"+address.getRoom()+"|"+address.getAddons();
				String allStrPhonetic = address.getProvPhonetic()+"|"+address.getCityPhonetic()+"|"+address.getCountyPhonetic()+"|"+address.getTownPhonetic()+"|"
						+address.getPlacePhonetic()+"|"+address.getStreetPhonetic()+"|"+address.getLandmarkPhonetic()+"|"+address.getPrefixPhonetic()+"|"+address.getHousenumPhonetic()+"|"
						+address.getTypePhonetic()+"|"+address.getSubnumPhonetic()+"|"+address.getSurfixPhonetic()+"|"+address.getEstabPhonetic()+"|"+address.getBuildingPhonetic()+"|"
						+address.getUnitPhonetic()+"|"+address.getFloorPhonetic()+"|"+address.getRoomPhonetic()+"|"+address.getAddonsPhonetic();
				String[] allStrSplit= allStr.split("\\|");
				
				Map<String, String> typeD5 = metadataApi.scPointNameckTypeD5();
				Map<String, String> typeD7 = metadataApi.scPointNameckTypeD7();
				for (String strSplit:allStrSplit){	
					//检查SC_POINT_NAMECK表中TYPE=5且HM_FLAG<>’HM’名称包含关键字且拼音与配置表中拼音不相同的报出
					Map<String, String> keyResult5=ScPointNameckUtil.matchType(strSplit, typeD5);
					for(String preKey:keyResult5.keySet()){
						if (!allStrPhonetic.contains(keyResult5.get(preKey))){
							String log="18个字段包含多音字“"+preKey+"”,且拼音与配置表中拼音不相同";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						}
					}

					//检查SC_POINT_NAMECK表中TYPE=7且HM_FLAG<>’HM’只要名称中包含关键字的报出
					Map<String, String> keyResult7=ScPointNameckUtil.matchType(strSplit, typeD7);
					if (keyResult7.size()!=0){
						String log="18个字段中包含关键字“"+(keyResult7.keySet()).toString()+"”";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
				}
			}
		}
	}
}
