package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName: GLM55061
 * @author: zhangpengpeng
 * @date: 2017年10月11日
 * @Desc: GLM55061.java 检查条件： 非删除点门牌对象 检查原则：
 *        1.点门牌的引导坐标X、Y不能为空，且不能为0，否则Log：引导坐标为空或为0！
 *        2.点门牌的显示坐标X、Y不能为空，且不能为0，否则Log：显示坐标为空或为0！
 */
public class GLM55061 extends BasicCheckRule {

	private String log1 = "引导坐标为空或为0！";
	private String log2 = "显示坐标为空或为0！";

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		if (obj.objName().equals(ObjectName.IX_POINTADDRESS) && !obj.opType().equals(OperationType.PRE_DELETED)) {
			IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) obj;
			IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
			Geometry geo = ixPonitaddress.getGeometry();
			if (ixPonitaddress.getXGuide() == 0 || ixPonitaddress.getYGuide() == 0) {
				setCheckResult(geo, ixPointaddressObj, ixPonitaddress.getMeshId(), log1);
			}
			if(geo == null || geo.getCoordinate().x == 0 || geo.getCoordinate().y == 0){
				setCheckResult(geo, ixPointaddressObj, ixPonitaddress.getMeshId(), log2);
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
