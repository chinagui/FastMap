package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * 
 * 检查条件：以下条件其中之一满足时，需要进行检查： (1)存在IX_POI_ADDRESS新增且FULLNAME不为空；
 * (2)存在IX_POI_ADDRESS修改且FULLNAME不为空； 检查原则：
 * 将拆分后的18个字段按“省名、市名、区县名、乡镇街道办、地名小区名、街巷名、标志物名、前缀、门牌号、类型名、子号、后缀、附属设施名、楼栋号、楼门号、楼层、
 * 房间号、附加信息”顺序合并（各字段“LANG_CODE”为“CHI（中国大陆）或CHT（港澳）”），将合并后的中文字段与FULLNAME对比，
 * 不一致的报出来，不区分全半角和大小写。 提示： 地址拆分后对比检查：拆分合并后地址xxxx（xxxx为18个字段合并后地址）与地址全称不一致。
 *
 */
public class FMA0911 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		if (!address.getHisOpType().equals(OperationType.INSERT)&& !address.getHisOpType().equals(OperationType.UPDATE)) {
			return;
		}
		if (address.getFullname() == null || address.getFullname().isEmpty()) {
			return;
		}
		String mergeAdd = CheckUtil.getMergerAddr(address);
		if (!mergeAdd.equals(address.getFullname())) {
			String errStr = "地址拆分后对比检查：拆分合并后地址" + mergeAdd + "（" + mergeAdd + "为18个字段合并后地址）与地址全称不一致。";
			setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), errStr);
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
