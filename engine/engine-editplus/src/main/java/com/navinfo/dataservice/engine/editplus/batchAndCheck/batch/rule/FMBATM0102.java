package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 查询条件： 
 * (1)别名原始英文(name_class=3,name_type=2,lang_code='ENG')小于等于35且存在别名标准化英文(name_class=3,name_type=1,lang_code='ENG')；
 *  
 * 批处理： 
 * 满足条件(1)时，删除别名标准英文记录；
 * 
 * @author jch
 */
public class FMBATM0102 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			// 查询别名原始英文列表
			List<IxPoiName> brList = poiObj.getOriginAliasENGNameList();
			for (IxPoiName br : brList) {
				if (br.getOpType().equals(OperationType.DELETE)) {
					continue;
				}
				IxPoiName standardAliasEngName = poiObj.getStandardAliasENGName(br.getNameGroupid());
				if ((br.getName()).length() <= 35 && standardAliasEngName != null) {
					poiObj.deleteSubrow(standardAliasEngName);
				}
			}
		}
	}

}
