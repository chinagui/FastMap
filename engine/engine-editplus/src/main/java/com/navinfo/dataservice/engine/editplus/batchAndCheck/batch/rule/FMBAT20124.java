package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 查询条件：查询条件：本次日编非删除POI且分类不在("230111","230114")中且IX_POI_NAME中存在NAME_CLASS=8(中英文)的记录
 * 批处理：标识删除IX_POI_NAME表中NAME_CLASS=8的记录；生成批处理履历，且IX_POI_NAME.U_RECORD=2（删除）
 * @author Gao Pengrong
 */
public class FMBAT20124 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	//月编无删除数据
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode= poi.getKindCode();
			if (kindCode.equals("230111")||kindCode.equals("230114")){return;}
			List<IxPoiName> Names= poiObj.getIxPoiNames();
			for (IxPoiName Name : Names) {
				if (Name.getNameClass()==8){
					poiObj.deleteSubrow(Name);
				}
			}
		}		
	}

}
