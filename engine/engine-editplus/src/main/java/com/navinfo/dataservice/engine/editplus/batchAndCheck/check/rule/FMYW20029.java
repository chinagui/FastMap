package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName: FMYW20029
 * @author: zhangpengpeng
 * @date: 2017年1月3日
 * @Desc: FMYW20029.java 检查条件： 该POI发生变更(新增或修改主子表、删除子表)； 检查原则：
 *        如果英文地址中存在数字，字母，符号（-_/:;'"~^.,?!*()<>$%&#@+半角空格）以外的POI，全部报出。
 *        提示：英文地址中含有非法字符“xx”
 */
public class FMYW20029 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			if (addresses == null || addresses.size() == 0) {
				return;
			}
			for (IxPoiAddress addrTmp : addresses) {
				if (addrTmp.isEng()) {
					String addrStr = addrTmp.getFullname();
					if (StringUtils.isEmpty(addrStr)) {
						continue;
					}
					List<String> errorList = new ArrayList<String>();
					for (char c : addrStr.toCharArray()) {
						String cStr = String.valueOf(c);
						if (!CheckUtil.isValidEngChar(cStr)) {
							errorList.add(cStr);
						}
					}
					if (errorList != null && errorList.size() > 0) {
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),
								"英文地址中含有非法字符“" + errorList.toString().replace("[", "").replace("]", "") + "”");
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}
}
