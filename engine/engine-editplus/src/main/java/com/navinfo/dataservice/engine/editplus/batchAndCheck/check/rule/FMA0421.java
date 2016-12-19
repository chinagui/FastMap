package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 *  以下条件中（1）、（2）满足其中之一，（3）必须满足时，进行检查：
 *	(1)存在IX_POI_NAME新增；
 *	(2)存在IX_POI_NAME修改或修改分类存在；
 *	(3) kindCode为“210302”
 *	检查原则：
 *	官方标准化中文名称包含全角阿拉伯数字（0，1，……9）或者中文数字（特指“零、一、二……十”）时，程序包log
 *	提示：POI火车票代售点名称统一
 *
 */
public class FMA0421 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		List<IxPoiName> names = poiObj.getIxPoiNames();
		boolean isChanged = false;
		IxPoiName standardName = null;
		for (IxPoiName name:names) {
			if (name.getHisOpType().equals(OperationType.INSERT) || name.getHisOpType().equals(OperationType.UPDATE) || poi.hisOldValueContains(IxPoi.KIND_CODE)) {
				isChanged = true;
			}
			if (name.getNameClass()==1 && name.getNameType()==1 && name.getLangCode().equals("CHI") || name.getLangCode().equals("CHT")) {
				standardName = name;
			}
		}
		
		if (isChanged && standardName!=null && poi.getKindCode().equals("210302")) {
			// 官方标准化中文名称包含全角阿拉伯数字（0，1，……9）或者中文数字（特指“零、一、二……十”）
			Pattern p = Pattern.compile(".*[零一二三四五六七八九十０-９]+.*");
			Matcher m = p.matcher(standardName.getName());

			if (m.matches()) {
				setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
