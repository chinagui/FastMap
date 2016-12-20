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
 * FM-A04-09	名称完整性作业	DHM	
 * 检查条件： 
 * 以下条件其中之一满足时，需要进行检查：
 * (1)存在IX_POI_NAME新增；
 * (2)存在IX_POI_NAME修改或修改分类存在；
 * 检查原则：
 * 官方标准化中文（langCode=CHI或CHT）名称内容满足以下任意一种的，需要报出：
 * (1)名称是“图书馆”、“索道”、“圖書館”的记录，需要报出。
 * 2)分类为“170104”或“170105”，并且名称中不包含“医院”、“保健院”、“醫院”字样的记录，需要报出。
 * 提示：POI名称完整性作业：POI名称不完整
 * @author zhangxiaoyi
 *
 */
public class FMA0409 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName officNameObj = poiObj.getOfficeStandardCHName();
			if(officNameObj==null){return;}
			String name = officNameObj.getName();
			if(name ==null||name.isEmpty()){return;}
			if(name.contains("图书馆")||name.contains("索道")||name.contains("圖書館")){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),null);
			}			
			String kindCode=poi.getKindCode();
			if((kindCode.equals("170104")||kindCode.equals("170105"))
					&&(name.contains("医院")||name.contains("保健院")||name.contains("醫院"))){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),null);
			}
		}
	}
	
	/**
	 * 以下条件其中之一满足时，需要进行检查：
	 *  (1)存在IX_POI_NAME新增；
	 *  (2)存在IX_POI_NAME修改或修改分类存在；
	 * @param poiObj
	 * @return true满足检查条件，false不满足检查条件
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String newKindCode=poi.getKindCode();
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			if(!newKindCode.equals(oldKindCode)){return true;}
		}
		//(1)存在IX_POI_NAME的新增；(2)存在IX_POI_NAME的修改；
		List<IxPoiName> names = poiObj.getIxPoiNames();
		for (IxPoiName br:names){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}}
		}
		return false;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
