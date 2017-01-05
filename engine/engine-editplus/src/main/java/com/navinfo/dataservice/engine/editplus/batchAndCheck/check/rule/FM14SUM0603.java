package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

public class FM14SUM0603 extends BasicCheckRule {
	private Map<Long,Long> pidAdminId;
	private Map<String, Map<String, String>> d6Map;

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();	
		List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
		if (addresses.size()==0) {
			return;
		}
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		d6Map = metaApi.scPointNameckTypeD6();
		List<String> errMsgList = new ArrayList<String>();
		for (IxPoiAddress addr:addresses) {
			if (!addr.getLangCode().equals("CHI") && !addr.getLangCode().equals("CHT")) {
				continue;
			}
			ckAddress(addr.getProvince(),poi,errMsgList);
			ckAddress(addr.getCity(),poi,errMsgList);
			ckAddress(addr.getCounty(),poi,errMsgList);
			ckAddress(addr.getTown(),poi,errMsgList);
			ckAddress(addr.getPlace(),poi,errMsgList);
			ckAddress(addr.getStreet(),poi,errMsgList);
			ckAddress(addr.getLandmark(),poi,errMsgList);
			ckAddress(addr.getPrefix(),poi,errMsgList);
			ckAddress(addr.getHousenum(),poi,errMsgList);
			ckAddress(addr.getType(),poi,errMsgList);
			ckAddress(addr.getSubnum(),poi,errMsgList);
			ckAddress(addr.getSurfix(),poi,errMsgList);
			ckAddress(addr.getEstab(),poi,errMsgList);
			ckAddress(addr.getBuilding(),poi,errMsgList);
			ckAddress(addr.getUnit(),poi,errMsgList);
			ckAddress(addr.getFloor(),poi,errMsgList);
			ckAddress(addr.getRoom(),poi,errMsgList);
			ckAddress(addr.getAddons(),poi,errMsgList);
		}
		if (errMsgList.size()>0) {
			String error = StringUtils.join(errMsgList, ";");
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),error);
			return;
		}
		
	}
	
	private void ckAddress(String word,IxPoi poi,List<String> errMsgList) throws Exception {
		if (d6Map.containsKey(word)) {
			Map<String, String> resultAdmin = d6Map.get(word);
			if (resultAdmin.get("adminArea") != null && !resultAdmin.get("adminArea").isEmpty()) {
				String adminId = resultAdmin.get("adminArea");
				if (adminId.startsWith("11") || adminId.startsWith("12") || adminId.startsWith("31") || adminId.startsWith("50")) {
					if (adminId.substring(0, 2).equals(pidAdminId.get(poi.getPid()).toString().substring(0, 2))) {
						errMsgList.add("“" + word + "”是错别字，确认是否修改为“" + resultAdmin.get("resultKey") + "”");
					}
				} else {
					if (adminId.substring(0, 4).equals(pidAdminId.get(poi.getPid()).toString().substring(0, 4))) {
						errMsgList.add("“" + word + "”是错别字，确认是否修改为“" + resultAdmin.get("resultKey") + "”");
					}
				}
			} else {
				errMsgList.add("“" + word + "”是错别字，确认是否修改为“" + resultAdmin.get("resultKey") + "”");
			}
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
