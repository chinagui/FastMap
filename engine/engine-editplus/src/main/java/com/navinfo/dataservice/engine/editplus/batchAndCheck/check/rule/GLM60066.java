package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName GLM60258
 * @author Han Shaoming
 * @date 2017年2月18日 下午7:23:15
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：服务区/停车区（230206或者230207）不能为子，否则报log：服务区/停车区不能为子！
 */
public class GLM60066 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null || (!"230206".equals(kindCode)&&!"230207".equals(kindCode))){return;}
			List<Long> childGroupIds = CheckUtil.getChildGroupIds(poi.getPid(), this.getCheckRuleCommand().getConn());
			//是否为子
			if(!childGroupIds.isEmpty()){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
	}

}
