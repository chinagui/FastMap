package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20057
 * @author Han Shaoming
 * @date 2017年2月8日 下午4:57:47
 * @Description TODO
 * 检查条件：Lifecycle！=1（删除）
 * 检查原则：
 * 地址(address)中存在全、半角字符“|”，报log：地址中含有非法字符“|”
 * 充电桩（分类为230227）不参与检查。
 * 
 */
public class FMYW20057 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//充电桩（230227）不参与检查
			String kindCode = poi.getKindCode();
			if(kindCode == null || "230227".equals(kindCode)){return;}
			//存在IxPoiAddress修改
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			//错误数据
			if(ixPoiAddress==null){return;}
			String fullname = ixPoiAddress.getFullname();
			//地址(address)中存在全、半角字符“|”
			if(fullname != null){
				if(fullname.contains("|")||fullname.contains("｜")){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
