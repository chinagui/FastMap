package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 检查条件： 
 * 别名原始英文新增或别名原始英文修改
 * 检查原则：
 * 存在别名标准英文名称(name_class=3，name_type=1,lang_code=ENG)，则报log：别名标准英文名称需作业！
 * @author gaopengrong
 */
public class FMM0104 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names=poiObj.getOriginAliasENGNameList();
			for (IxPoiName name:names){
				//存在IX_POI_NAME新增或者修改履历
				if(name.getHisOpType().equals(OperationType.INSERT)||(name.getHisOpType().equals(OperationType.UPDATE) && name.hisOldValueContains(IxPoiName.NAME))){
					String oldNameStr=(String) name.getHisOldValue(IxPoiName.NAME);
					String newNameStr=name.getName();
					if(!newNameStr.equals(oldNameStr)){
						long nameGroupId= name.getNameGroupid();
						IxPoiName standardAliasENGName=poiObj.getStandardAliasENGName(nameGroupId);
						if(standardAliasENGName!=null){
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());
						}
					}
				}
			}
		}
	}
}
