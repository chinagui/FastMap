package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-YW-20-053	非重要分类英文名超长作业	DHM	
 * 检查条件：
 * 非删除POI
 * 检查原则：
 * 官方原始英文名长度大于35，报LOG：官方标准化英文名作业
 * @author gaopengrong
 *
 */
public class FMYW20053 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName name = poiObj.getOfficeOriginEngName();
			String nameStr= name.getName();
			if(nameStr.length()>35){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
