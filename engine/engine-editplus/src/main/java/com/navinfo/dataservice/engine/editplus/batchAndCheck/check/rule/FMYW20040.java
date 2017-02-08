package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20040
 * @author Han Shaoming
 * @date 2017年2月8日 下午3:27:46
 * @Description TODO
 * 检查条件：    Lifecycle！=1（删除）
 * 检查原则：
 * 1）新增的POI地址（address）不为空，且以“路、街、道、弄、巷、市、县”结尾时；
 * 2）POI地址更新后不为空，且以“路、街、道、弄、巷、市、县”结尾时。
 * 要报log：地址为道路名，请确认
 * 充电桩（分类为230227）不参与检查。
 */
public class FMYW20040 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//存在IxPoiAddress新增或者修改
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			//错误数据
			if(ixPoiAddress==null){return;}
			if(ixPoiAddress.getHisOpType().equals(OperationType.INSERT)
					||(ixPoiAddress.getHisOpType().equals(OperationType.UPDATE))){
				String fullname = ixPoiAddress.getFullname();
				if(fullname != null){
					if(fullname.endsWith("路")||fullname.endsWith("街")||fullname.endsWith("道")||fullname.endsWith("弄")
							||fullname.endsWith("巷")||fullname.endsWith("市")||fullname.endsWith("县")){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
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
