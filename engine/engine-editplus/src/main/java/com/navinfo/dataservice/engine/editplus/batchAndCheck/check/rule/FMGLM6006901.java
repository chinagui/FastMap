package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-GLM60069-01	
 * 检查条件：Lifecycle！=1（删除）
 * 检查原则：
 * kindCode为机场（230126）的POI，其名称（name）应以“机场”/“機場”、“航站楼”/“航站樓”、“候机楼”/“候機樓”结尾，
 * 否则报log：机场（230126）分类POI应以“机场”/“機場”、“航站楼”/“航站樓”、“候机楼”/“候機樓”结尾。
 * @author zhangxiaoyi
 */
public class FMGLM6006901 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();			
			String kind=poi.getKindCode();
			if(!kind.equals("230126")){return;}
			
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			
			if(!nameStr.endsWith("机场")&&!nameStr.endsWith("機場")&&!nameStr.endsWith("航站楼")&&!nameStr.endsWith("航站樓")
					&&!nameStr.endsWith("候机楼")&&!nameStr.endsWith("候機樓")){
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
