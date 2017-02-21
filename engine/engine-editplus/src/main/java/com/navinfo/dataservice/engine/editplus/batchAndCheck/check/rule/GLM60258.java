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
 * @date 2017年2月20日 下午4:55:51
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：
 * POI父子关系父表（IX_POI_PARANT）中，有两条及以上记录的“父POI号码”相同，
 * 报出LOG：具有相同父的子POI没有放在同一组父子关系中
 */
public class GLM60258 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			long pid = poi.getPid();
			List<Long> parentGroupIds = CheckUtil.getParentGroupIds(pid, this.getCheckRuleCommand().getConn());
			if(parentGroupIds.size() >1){
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
