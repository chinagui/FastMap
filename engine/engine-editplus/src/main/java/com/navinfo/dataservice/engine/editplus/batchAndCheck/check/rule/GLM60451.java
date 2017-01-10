package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import oracle.net.aso.l;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 检查条件：
 * IX_POI表中"STATE（状态）"非"1（删除）"
 * 检查对象：种别为：230210的POI
 * 检查原则：
 * POI的LABEL字段以“|”为分界拆分，拆分后内容都不等于“室内”“室外”“占道”“室内地上”“地下”的，报LOG。
 * @author zhangxiaoyi
 */
public class GLM60451 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(!kind.equals("230210")){return;}
			String label = poi.getLabel();
			if(label==null||label.isEmpty()){return;}
			String[] labelList = label.split("\\|");
			for(String tmp:labelList){
				if(tmp.equals("室内")||tmp.equals("室外")||tmp.equals("占道")
						||tmp.equals("室内地上")||tmp.equals("地下")){
					return;
				}
			}
			setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
