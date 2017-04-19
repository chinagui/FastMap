package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.sql.Connection;
import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.utils.AdFaceSelector;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 查询条件：
 * 新增POI或修改显示坐标的POI对象
 * 批处理：
 * 针对满足条件的POI，根据显示坐标位置找到对应的ADFACE的region_id赋值，并生成履历；
 * 如果找不到对应的ADFACE，则POI的region_id赋值0(理论上不存在此情况)；
 * 
 * @author gaopengrong
 *
 */
public class FMBATD20002 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();

			if (poi.getHisOpType().equals(OperationType.INSERT) || (poi.getHisOpType().equals(OperationType.UPDATE)&&poi.hisOldValueContains(IxPoi.GEOMETRY))) {
				Geometry poiGeo=poi.getGeometry();
				long oldRegion = poi.getRegionId();
				Connection conn=this.getBatchRuleCommand().getConn();
				long regionId = new AdFaceSelector(conn).getAdFaceRegionId(poiGeo);
				if(oldRegion!=regionId&&regionId!=0){
					poi.setRegionId(regionId);
				}
				
			}
		}
	}

}
