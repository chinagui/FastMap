package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-YW-20-092	英文名称	英文作业	可以忽略	F-3出入口翻译检查
 * 检查条件：
 *  以下条件其中之一满足时，需要进行检查：
 * (1)存在官方原始中文名称IX_POI_NAME新增；
 * (2)存在官方原始中文名称IX_POI_NAME修改或KIND_CODE字段修改；
 * 检查原则：
 * 1）官方标准化中文名中包含“口”且不包含“入”和“出”，官方原始英文名称中包含“exit”或“entrance”且不包含“exit/entrance”时
 * 2）官方标准化中文名中包含“出入口”，官方原始英文名中不包含“exit/entrance”时
 * 报log：出入口翻译错误
 * @author zhangxiaoyi
 */
public class FMYW20092 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if (!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName name=poiObj.getOfficeStandardCHIName();
			if(name==null){return;}
			String nameStr=name.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			IxPoiName engName=poiObj.getOfficeOriginEngName();
			if(engName==null){return;}
			String engNameStr=engName.getName();
			if(engNameStr==null||engNameStr.isEmpty()){return;}
			//System.out.println(nameStr);
			//System.out.println(engNameStr);
			if(nameStr.contains("口")&&!nameStr.contains("入")&&!nameStr.contains("出")
					&&(engNameStr.contains("exit")||engNameStr.contains("entrance"))
					&&!engNameStr.contains("exit/entrance")){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
			if(nameStr.contains("出入口")&&!engNameStr.contains("exit/entrance")){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}
	
	private boolean isCheck(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			String newKindCode=poi.getKindCode();
			if(!newKindCode.equals(oldKindCode)){
				return true;
			}
		}
		//存在IX_POI_NAME新增或者修改履历
		IxPoiName br=poiObj.getOfficeOriginCHIName();
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
