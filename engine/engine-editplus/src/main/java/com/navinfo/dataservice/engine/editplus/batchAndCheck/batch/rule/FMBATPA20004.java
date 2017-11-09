package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName: FMBATPA20004
 * @author: zhangpengpeng
 * @date: 2017年10月28日
 * @Desc: 批处理对象：针对外业点门牌新增，或修改对象且修改内容为改DPR_NAME或修改内容为DP_NAME 批处理原则：
 *        将DPR_NAME和DP_NAME组合后赋值给FULLNAME，若IX_POIINTADDRESS_NAME记录不存在，
 *        则新增一条IX_POINTADDRESS_NAME，NAME_ID申请赋值，PID赋值点门牌PID，LANG_CODE赋值“CHI”或“
 *        CHT”，其它字读赋默认值，且生成履历；
 */
public class FMBATPA20004 extends BasicBatchRule {

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
				&& (ixPoint.hisOldValueContains(IxPointaddress.DPR_NAME)
						|| ixPoint.hisOldValueContains(IxPointaddress.DP_NAME)))) {
			String dprName = ixPoint.getDprName() == null ? "" : ixPoint.getDprName();
			String dpName = ixPoint.getDpName() == null ? "" : ixPoint.getDpName();
			String fullName = dprName + dpName;
			// langCode先赋值为"CHI"大陆数据,等港澳后,在做判断赋值"CHT"
			String langCode = "CHI";
			List<IxPointaddressName> ixPointNames = ixPointObj.getIxPointaddressNames();
			if (ixPointNames == null || ixPointNames.isEmpty()) {
				IxPointaddressName ixPointName = ixPointObj.createIxPointaddressName();
				ixPointName.setFullname(fullName);
				ixPointName.setLangCode(langCode);
			} else {
				// 大陆数据给CHI的NAME子表赋值,后续给港澳的NAME子表赋值
				IxPointaddressName chiName = ixPointObj.getCHIName();
				if(chiName != null){
					chiName.setFullname(fullName);
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
