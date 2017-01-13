package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-YW-20-154
 * 检查条件： 
 *  Lifecycle！=1（删除）
 *  检查原则：
 *  品牌为特斯拉（348D)时，报log：请确认名称是否包含“特斯拉”或“TESLA”。
 * @author zhangxiaoyi
 */
public class FMYW20154 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String chain=poi.getChain();
			if(chain==null||chain.isEmpty()){return;}
			if(!chain.equals("348D")){return;}
			IxPoiName nameobj = poiObj.getOfficeOriginCHName();
			if(nameobj==null){return;}
			String nameStr = nameobj.getName();
			if(nameStr==null||nameStr.isEmpty()||(!nameStr.contains("特斯拉")&&!nameStr.contains("TESLA"))){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
