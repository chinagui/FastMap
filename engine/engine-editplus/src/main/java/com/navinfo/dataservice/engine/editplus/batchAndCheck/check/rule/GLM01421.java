package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.utils.AdFaceSelector;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: GLM01421
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description:检查条件：
					非删除点门牌对象
				检查原则：
					点门牌的行政区划号码（REGION_ID）应与其所在的非删除行政区划面的行政区划号码相同，否则报LOG：点门牌与行政区划面的行政区划号码不同！
				说明：如果点门牌落在行政区划面的公共边界上，则点门牌的行政区划号与任意一个相邻面的行政区划号码相同即可
 * @Author: LittleDog
 * @Date: 2017年10月9日
 * @Version: V1.0
 */
public class GLM01421 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POINTADDRESS) && !obj.opType().equals(OperationType.PRE_DELETED)) {
			IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) obj;
			IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();

			Geometry geo = ixPonitaddress.getGeometry();
			if (geo != null) {
				Connection conn = getCheckRuleCommand().getConn();
				AdFaceSelector adFaceSelector = new AdFaceSelector(conn);
				Map<Long, Geometry> regionIdAndGeometryMap = adFaceSelector.loadRelateFaceByGeometry(geo);

				long regionId = ixPonitaddress.getRegionId();
				if (!regionIdAndGeometryMap.containsKey(regionId)) {
					setCheckResult(geo, String.format("[IX_POINTADDRESS,%s]", ixPonitaddress.getPid()),
							ixPonitaddress.getMeshId());
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
	}

}
