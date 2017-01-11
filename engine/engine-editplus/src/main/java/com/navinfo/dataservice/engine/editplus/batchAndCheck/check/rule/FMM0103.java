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
 * 别名标准中文新增或别名标准中文修改
 * 检查原则：
 * 存在别名原始英文名称(name_class=3，name_type=2,lang_code=ENG)，则报log：别名原始名称需作业！
 * @author gaopengrong
 */
public class FMM0103 extends BasicCheckRule {
	
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
			List<IxPoiName> names=poiObj.getIxPoiNames();
			for (IxPoiName name:names){
				//存在IX_POI_NAME新增或者修改履历
				if(name.getNameClass()==3&&name.getNameType()==1&&name.getLangCode().equals("ENG")){
					if(name.getHisOpType().equals(OperationType.INSERT)||(name.getHisOpType().equals(OperationType.UPDATE) && name.hisOldValueContains(IxPoiName.NAME))){
						String oldNameStr=(String) name.getHisOldValue(IxPoiName.NAME);
						String newNameStr=name.getName();
						if(!newNameStr.equals(oldNameStr)){
							long nameGroupId= name.getNameGroupid();
							IxPoiName originAliasENGName=poiObj.getOriginAliasENGName(nameGroupId);
							if(originAliasENGName!=null){
								setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());
							}
						}
					}
				}
			}
		}
	}
	
	
}
