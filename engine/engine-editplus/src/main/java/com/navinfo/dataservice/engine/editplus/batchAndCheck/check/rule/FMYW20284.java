package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FMYW20284
 * @author Han Shaoming
 * @date 2017年2月28日 下午1:30:03
 * @Description TODO
 * 检查条件：  非删除POI对象
 * 检查原则：
 * 机场(分类为230105、230103)、火车站(分类为230126、230127)POI关联link为10级路(rd_link.kind=10),
 * 则报log：重要POI关联到10级道路，请确认！
 */
public class FMYW20284 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
			long linkPid = poi.getLinkPid();
			if("230105".equals(kindCode)||"230103".equals(kindCode)
					||"230126".equals(kindCode)||"230127".equals(kindCode)){
				Map<Long, Integer> rdLink = CheckUtil.searchRdLink(linkPid, this.getCheckRuleCommand().getConn());
				if(rdLink.get(linkPid) == 10){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
