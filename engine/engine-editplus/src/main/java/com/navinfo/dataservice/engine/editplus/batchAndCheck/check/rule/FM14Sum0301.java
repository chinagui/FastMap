package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM14Sum0301
 * @author Han Shaoming
 * @date 2017年2月8日 下午3:57:11
 * @Description TODO
 * 检查条件： *     Lifecycle！=1（删除）
 * 检查原则：
 * （1） 地址（address）修改前存在“号”或“號”，修改后没有“号”或“號”，且修改前地址以“号”或“號”结尾的，报Log1：请确认地址中是否存在“号”；
 * （2） 修改前存在地址但修改后地址为空时，报Log2：请确认地址是否删除正确；
 * 充电桩（分类为230227）不参与检查。
 * 备注：地址不区分全半角
 */
public class FM14Sum0301 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//存在IxPoiAddress修改
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			//错误数据
			if(ixPoiAddress==null){return;}
			if(ixPoiAddress.getHisOpType().equals(OperationType.UPDATE)
					&&ixPoiAddress.hisOldValueContains(IxPoiAddress.FULLNAME)){
				String oldFullname = (String) ixPoiAddress.getHisOldValue(IxPoiAddress.FULLNAME);
				String fullname = ixPoiAddress.getFullname();
				//（1） 地址（address）修改前存在“号”或“號”，修改后没有“号”或“號”，且修改前地址以“号”或“號”结尾的
				if(fullname != null && oldFullname != null){
					if(fullname.contains("号")||fullname.contains("號")){return;}
					if((oldFullname.contains("号")&&!fullname.contains("号"))
							||(oldFullname.contains("號")&&!fullname.contains("號"))){
						if(oldFullname.endsWith("号")){
							setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "请确认地址中是否存在“号”");
						}
						if(oldFullname.endsWith("號")){
							setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "请确认地址中是否存在“號”");
						}
					}
				}
				//（2） 修改前存在地址但修改后地址为空时
				if(fullname == null && oldFullname != null){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "请确认地址是否删除正确");
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
