package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.engine.editplus.utils.AdFaceSelector;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName: FMBATPA20002
 * @author: zhangpengpeng
 * @date: 2017年10月9日
 * @Desc: FMBATPA20002.java查询条件： 新增点门牌或修改显示坐标的点门牌对象 批处理：
 *        针对满足条件的点门牌，根据显示坐标位置找到对应的ADFACE的region_id赋值，并生成履历；
 *        如果找不到对应的ADFACE，则点门牌的region_id赋值0(理论上不存在此情况)；
 */
public class FMBATPA20002 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) obj;
		IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
		if (ixPonitaddress.getHisOpType().equals(OperationType.INSERT)
				|| (ixPonitaddress.getHisOpType().equals(OperationType.UPDATE)
						&& ixPonitaddress.hisOldValueContains(IxPointaddress.GEOMETRY))) {
			Geometry geo = ixPonitaddress.getGeometry();
			long oldRegion = ixPonitaddress.getRegionId();
			long regionId = new AdFaceSelector(this.getBatchRuleCommand().getConn()).getAdFaceRegionId(geo);
			if (oldRegion != regionId) {
				ixPonitaddress.setRegionId(regionId);
			}
		}
	}

}
