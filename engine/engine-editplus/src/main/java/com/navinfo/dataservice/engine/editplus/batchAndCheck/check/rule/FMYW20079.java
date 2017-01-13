package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

import net.sf.json.JSONObject;

/**
 * 检查条件：
 * 以下条件其中之一满足时，需要进行检查：
 * (1)存在IX_POI_ADDRESS新增且FULLNAME不为空； 
 * (2)存在IX_POI_ADDRESS修改且FULLNAME不为空；
 * 检查原则：
 * 中文街巷名STREET不为空，且不是以“道、路、街、巷、线、段、条、弄、胡同、街道”中的一个结尾时，且此点行政区划前两位与配置表（RD_NAME）
 * (lang_code='CHI')表中的“admin_id”（admin_id<>214）字段前两位一致，但街巷名中的内容在RD_NAME表中“name”字段中不存在时，
 * 报log：街巷名疑似错误，请确认
 * 注：行政区划判断：poi.region_id关联ad_admin.region_id.admin_id
 * @author gaopengrong
 *
 */
public class FMYW20079 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
			if(addrs.size()==0){return;}
			long regionId = poi.getRegionId();
			int region=(int)regionId;
			List<String> wordList=Arrays.asList("道","路","街","巷","线","段","条","弄","胡同","街道");
			for(IxPoiAddress addr:addrs){
				if(addr.getLangCode().equals("CHI")){
					String fullname = addr.getFullname();
					if(fullname==null||fullname.isEmpty()){continue;}
					String street = addr.getStreet();
					if(street==null||street.isEmpty()){continue;}
					int flag=0;
					for(String word:wordList){
						if(street.endsWith(word)){
							flag=1;
						}
					}
					Connection conn= (Connection) getCheckRuleCommand().getConn();
					if(flag==0&&CheckUtil.matchStreet(street,region,conn)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "街巷名疑似错误，请确认");
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
