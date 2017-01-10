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
 *检查条件：
 *以下条件其中之一满足时，需要进行检查：
 *(1) 存在IX_POI_NAME新增；
 *(2)存在IX_POI_NAME修改；
 *检查原则：
 *官方标准化中文名称包含“Ｎｏ．”、“Ｎ０．”（这个是全角的零）、“ｎｏ．”、“ｎＯ．”，报log：POI分店名称错误，正确写法是“ＮＯ．”
 * @author gaopengrong
 */
public class FMYW20060 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow(); 
			IxPoiName name=poiObj.getOfficeStandardCHName();
			String nameStr= name.getName();
			if(nameStr.contains("Ｎｏ．")||nameStr.contains("Ｎ０．")||nameStr.contains("ｎｏ．")||nameStr.contains("ｎＯ．")){
				setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());
			}
		}
	}
	/**
	 * 存在IX_POI_NAME新增或者修改履历
	 * @param poiObj
	 * @return
	 */
	private boolean isCheck(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		IxPoiName br=poiObj.getOfficeStandardCHName();
		if(br!=null){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}
			}
		}
		return false;
	}
	
}
