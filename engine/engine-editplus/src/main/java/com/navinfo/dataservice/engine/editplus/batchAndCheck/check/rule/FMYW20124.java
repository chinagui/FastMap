package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FMYW20124
 * @author Han Shaoming
 * @date 2017年2月8日 下午5:45:01
 * @Description TODO
 * 检查条件：   Lifecycle=3（新增）且address不为空或Lifecycle=2（更新）且address修改后不为空
 * 检查原则：
 * 1）主地址（address）只含有阿拉伯数字时，报log1；
 * 2）主地址（address）为长度为1时，报log2；
 * 充电桩（分类为230227）不参与检查。
 */
public class FMYW20124 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//存在IxPoiAddress修改
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			//错误数据
			if(ixPoiAddress==null){return;}
			if(ixPoiAddress.getHisOpType().equals(OperationType.INSERT)
					||(ixPoiAddress.getHisOpType().equals(OperationType.UPDATE)
					&&ixPoiAddress.hisOldValueContains(IxPoiAddress.FULLNAME))){
				String fullname = ixPoiAddress.getFullname();
				//1）主地址（address）只含有阿拉伯数字时，报log1；
				if(CheckUtil.isDigit(fullname)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "地址只含有数字，请确认");
				}
				//2）主地址（address）为长度为1时，报log2；
				if(fullname.length() == 1){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "地址只有1个字，请确认");
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
