package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

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
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 18个中文字段(LANG_CODE="CHI")不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值：
 * 1、如果是1表示不转化，不用报log；
 * 2、如果是0表示需要确认后转化，报log：“XX”为“**”是繁体字，对应的简体字是**，需确认是否转化；
 * @author gaopengrong
 *
 */
public class FMYW20038 extends BasicCheckRule {
	MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
			if(addrs.size()==0){return;}
			for(IxPoiAddress addr:addrs){
				if(addr.getLangCode().equals("CHI")){
					Map<String, JSONObject> ft = metadataApi.tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap();
					String mergeAddr = CheckUtil.getMergerAddr(addr);
					if(mergeAddr==null||mergeAddr.isEmpty()){continue;}
					for(char item:mergeAddr.toCharArray()){
						String str=String.valueOf(item);
						if(ft.containsKey(str)){
							JSONObject data= ft.get(str);
							Object convert= data.get("convert");
							if(convert.equals(0)){
								String jt=(String) data.get("jt");
								String log="“"+str+"”是繁体字，对应的简体字是“"+jt+"”，需确认是否转化";
								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),log);
							}
						}
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
