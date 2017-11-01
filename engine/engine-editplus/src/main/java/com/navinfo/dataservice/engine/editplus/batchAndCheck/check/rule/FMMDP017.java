package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiBusinesstime;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 * 非删除（根据履历判断删除）
 * 检查原则：
 * 1.营业时长IX_POI_BUSINESSTIME.TIME_DUR不能小于4小时
 * log1：营业时长小于4小时
 */
public class FMMDP017 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		String kindcode=poi.getKindCode();
		if (!(kindcode.equals("170100")&&kindcode.equals("170101")&&kindcode.equals("170102"))) {
			return;
		}
		List<IxPoiDetail> details = poiObj.getIxPoiDetails();
		for (IxPoiDetail detail : details) {
			int hospitaClass=detail.getHospitalClass();
			if (hospitaClass>9) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "医院等级字段值域错误；");
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
