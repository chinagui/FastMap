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
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 1.检查POI中文（LANG_CODE=CHI或CHT）拆分的18个地址中“(”与“)”应成对出现；
 * 2、括号“（”和“）”中间必须有内容；
 * 3、不允许括号嵌套；
 * 否则报出：POI中文地址中“(”与“)”应成对出现
 * 备注：此处只能是全角的括号
 * @author gaopengrong
 */
public class FMYW20061 extends BasicCheckRule {
	
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
			if(address.getHisOpType().equals(OperationType.INSERT)||(address.getHisOpType().equals(OperationType.UPDATE))){
				
				String allStr = address.getProvince()+"|"+address.getCity()+"|"+address.getCounty()+"|"+address.getTown()+"|"
						+address.getPlace()+"|"+address.getStreet()+"|"+address.getLandmark()+"|"+address.getPrefix()+"|"+address.getHousenum()+"|"
						+address.getType()+"|"+address.getSubnum()+"|"+address.getSurfix()+"|"+address.getEstab()+"|"+address.getBuilding()+"|"
						+address.getUnit()+"|"+address.getFloor()+"|"+address.getRoom()+"|"+address.getAddons();

				String[] allStrSplit= allStr.split("|");
				for (String strSplit:allStrSplit){
					//“(”与“)”应成对出现；
					int i = strSplit.length()-strSplit.replace("（", "").length();
					int j = strSplit.length()-strSplit.replace("）", "").length();
					if(i!=j){
						String log="“(”与“)”应成对出现";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
				}
			}
		 }
	}
	
	
}
