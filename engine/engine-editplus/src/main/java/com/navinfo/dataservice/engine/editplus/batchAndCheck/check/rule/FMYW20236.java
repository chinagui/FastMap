package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20236
 * @author Han Shaoming
 * @date 2017年2月28日 下午12:55:25
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：
 * 2)官方原始中文名称中不包含“太平洋”且chain=6203，则报log：品牌错误，请确认！
 */
public class FMYW20236 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String chain = poi.getChain(); 
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			boolean check = false;
			if(ixPoiName == null&&StringUtils.equals(chain, "6203")){check = true;}
			String name = ixPoiName.getName();
			if(name == null&&StringUtils.equals(chain, "6203")){check = true;}
			if(!name.contains("太平洋") &&StringUtils.equals(chain, "6203")){check = true;}
			if(check){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
