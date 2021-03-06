package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 
 * 检查条件： 以下条件其中之一满足时，需要进行检查： (1)存在IX_POI_ADDRESS新增且FULLNAME不为空；
 * (2)存在IX_POI_ADDRESS修改且FULLNAME不为空； 检查原则：
 * 地址名称发音（只是针对“LANG_CODEe”为“CHI（中国大陆）或CHT（港澳）”下的地址名称对应的地址名称发音）不属于自动转拼音中的任何一个则报出。
 * 提示：检查地址与地址拼音不匹配检查：地址与地址拼音不匹配（提示不匹配字段）
 *
 */
public class FMA0916 extends BasicCheckRule {
	private Map<Long,Long> pidAdminId;
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		String adminId=null;
		if(pidAdminId!=null&&pidAdminId.containsKey(poi.getPid())){
			adminId=pidAdminId.get(poi.getPid()).toString();
		}
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		List<String> errList = new ArrayList<String>();
		checkPhonetic(address.getProvince(),address.getProvPhonetic(),"省名",errList,adminId);
		checkPhonetic(address.getCity(),address.getCityPhonetic(),"市名",errList,adminId);
		checkPhonetic(address.getCounty(),address.getCountyPhonetic(),"区县名",errList,adminId);
		checkPhonetic(address.getTown(),address.getTownPhonetic(),"乡镇街道办",errList,adminId);
		checkPhonetic(address.getPlace(),address.getPlacePhonetic(),"地名小区名",errList,adminId);
		checkPhonetic(address.getStreet(),address.getStreetPhonetic(),"街巷名",errList,adminId);
		checkPhonetic(address.getLandmark(),address.getLandmarkPhonetic(),"标志物名",errList,adminId);
		checkPhonetic(address.getPrefix(),address.getPrefixPhonetic(),"前缀",errList,adminId);
		checkPhonetic(address.getHousenum(),address.getHousenumPhonetic(),"门牌号",errList,adminId);
		checkPhonetic(address.getType(),address.getTypePhonetic(),"类型名",errList,adminId);
		checkPhonetic(address.getSubnum(),address.getSubnumPhonetic(),"子号",errList,adminId);
		checkPhonetic(address.getSurfix(),address.getSurfixPhonetic(),"后缀",errList,adminId);
		checkPhonetic(address.getEstab(),address.getEstabPhonetic(),"附属设施名",errList,adminId);
		checkPhonetic(address.getBuilding(),address.getBuildingPhonetic(),"楼栋号",errList,adminId);
		checkPhonetic(address.getFloor(),address.getFloorPhonetic(),"楼层",errList,adminId);
		checkPhonetic(address.getUnit(),address.getUnitPhonetic(),"楼门号",errList,adminId);
		checkPhonetic(address.getRoom(),address.getRoomPhonetic(),"房间号",errList,adminId);
		checkPhonetic(address.getAddons(),address.getAddonsPhonetic(),"附加信息",errList,adminId);
		if (errList.size()>0) {
			String errStr = org.apache.commons.lang.StringUtils.join(errList, ";");
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),errStr);
		}
	}
	
	private void checkPhonetic(String addrStr,String phonetic,String colName,List<String> errList,String adminId) throws Exception {
		if (StringUtils.isEmpty(addrStr)) {return;}
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		String phonetics = metadataApi.pyConvert(addrStr,adminId,null);
		
		if (!phonetics.equals(phonetic)) {
			errList.add("检查地址与地址拼音不匹配检查："+colName+"与"+phonetic+"不匹配");
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		pidAdminId = IxPoiSelector.getAdminIdByPids(getCheckRuleCommand().getConn(), pidList);
	}

}
