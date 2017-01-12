package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * 检查条件：
 * 以下条件其中之一满足时，需要进行检查：
 * (1)存在官方中文地址IX_POI_ADDRESS新增且FULLNAME不为空； 
 * (2)存在官方中文地址IX_POI_ADDRESS修改且FULLNAME不为空；
 * 检查原则：
 * 中文地址全称（FULLNAME）中含有“国道、省道、县道、乡道、G、S、X、Y”中的一个时，报log：地址中含有国省道，请确认
 * @author gaopengrong
 *
 */
public class FMYW20078 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
			if(addrs.size()==0){return;}
			List<String> wordList=Arrays.asList("国道","省道","县道","乡道","G","S","X","Y");
			for(IxPoiAddress addr:addrs){
				if(addr.getLangCode().equals("CHI")){
					String fullname = addr.getFullname();
					String error = "";
					if(fullname==null||fullname.isEmpty()){continue;}
					for(String word:wordList){
						if(fullname.contains(word)){
							error=error+word;
						}
					}
					if(error.length()>0){setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "地址中含有国省道，请确认");}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
