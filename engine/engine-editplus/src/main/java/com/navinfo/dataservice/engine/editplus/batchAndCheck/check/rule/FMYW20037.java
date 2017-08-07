package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;
/**
 * FM-YW-20-037	
 * 检查条件：
 *     Lifecycle！=1（删除）
 *     检查原则：
 *     主名称（name）字段不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值：
 *     1、如果是1表示不转化，不用报log；
 *     2、如果是0表示需要确认后转化，报log：**是繁体字，对应的简体字是**，需确认是否转化；
 * @author zhangxiaoyi *
 */
public class FMYW20037 extends BasicCheckRule {
	
	
	public void run() throws Exception {
		log.info("CopyOfFMYW20037");
		Map<Long, BasicObj> rows=getRowList();
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, JSONObject> ft = metadataApi.tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){continue;}
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			
			for(char item:nameStr.toCharArray()){
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
		}}
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
