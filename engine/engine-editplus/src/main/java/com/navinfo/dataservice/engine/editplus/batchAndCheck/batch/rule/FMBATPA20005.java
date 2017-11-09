package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName: FMBATPA20005
 * @author: zhangpengpeng
 * @date: 2017年10月28日
 * @Desc: 批处理对象：针对外业点门牌新增，或修改对象且修改内容为改外业LABEL 批处理原则：（该批处理在FM-BAT-PA20-004之后）
 *        针对lang_code＝“CHI”或“CHT”记录赋值，赋值如下：
 *        若MEMOIRE中包含“不分奇偶”，则IX_POINTADDRESS_NAME.sum_char赋值3；
 *        若MEMOIRE中不包含“不分奇偶”也不包含“偶”，但仅包含“奇”，则IX_POINTADDRESS_NAME.sum_char赋值1；
 *        若MEMOIRE中不包含“不分奇偶”也不包含“奇”，但仅包含“偶”，则IX_POINTADDRESS_NAME.sum_char赋值2；
 *        若以上都不满足，则赋值0； 备注：若IX_POINTADDRESS_NAME记录不存在，则不执行该批处理；
 */
public class FMBATPA20005 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		if (!obj.objName().equals(ObjectName.IX_POINTADDRESS)) {
			return;
		}

		IxPointAddressObj ixPointObj = (IxPointAddressObj) obj;
		IxPointaddress ixPoint = (IxPointaddress) ixPointObj.getMainrow();
		if (ixPoint.getHisOpType().equals(OperationType.INSERT) || (ixPoint.getHisOpType().equals(OperationType.UPDATE)
				&& ixPoint.hisOldValueContains(IxPointaddress.MEMOIRE))) {
			List<IxPointaddressName> ixPointNames = ixPointObj.getIxPointaddressNames();
			if (ixPointNames == null || ixPointNames.isEmpty()) {
				return;
			}
			// 大陆数据赋值"CHI",港澳数据赋值"CHT"
			IxPointaddressName chiName = ixPointObj.getCHIName();
			if (chiName == null) {
				return;
			}
			String memoire = ixPoint.getMemoire() == null ? "" : ixPoint.getMemoire();

			if (memoire.contains("不分奇偶")) {
				chiName.setSumChar(3);
			} else if (memoire.contains("奇") && !memoire.contains("偶")) {
				chiName.setSumChar(1);
			} else if (memoire.contains("偶") && !memoire.contains("奇")) {
				chiName.setSumChar(2);
			} else {
				chiName.setSumChar(0);
			}
		}
	}

}
