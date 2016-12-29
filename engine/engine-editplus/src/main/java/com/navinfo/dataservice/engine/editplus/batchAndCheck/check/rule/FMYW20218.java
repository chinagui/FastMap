package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 * 非删除（根据履历判断删除）
 * 检查原则：（网址：IX_POI_DETAIL.WEB_SITE）（只针对有值得检查）
 * 1.网址信息以http://开头
 * 2.网址信息不能存在空格，tab符，回车符
 * 3.网址信息不能为http://x/x格式
 * 4.网址信息不以“\”结尾
 * log：
 * 1.网址信息格式错误，网址不是以”http://”开头
 * 2.网址信息格式错误，网址中存在Tab符、回车符或者空格
 * 3.网址信息格式错误，网址中存在多余的“/”
 * 4.网址信息格式错误，网址以“\”结尾
 */
public class FMYW20218 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		List<IxPoiDetail> poiDetails = poiObj.getIxPoiDetails();
		for (IxPoiDetail poiDetail : poiDetails) {
			String webSite = poiDetail.getWebSite();
			if (!webSite.startsWith("http://")) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						"网址信息格式错误，网址不是以”http://”开头");
			}
			if (webSite.indexOf(" ") >= 0 || webSite.indexOf("	") >= 0 || webSite.indexOf("\n") >= 0) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						"网址信息格式错误，网址中存在Tab符、回车符或者空格");
			}
			if (webSite.substring(7).indexOf("/") >= 0) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						"网址信息格式错误，网址中存在多余的“/”");
			}
			if (webSite.endsWith("\\")) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						"网址信息格式错误，网址以“\\”结尾");
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
