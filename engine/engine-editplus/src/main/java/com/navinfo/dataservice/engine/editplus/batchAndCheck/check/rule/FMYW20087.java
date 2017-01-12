package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-YW-20-087	C-4单词中含有零的检查	DHM
 * 检查条件：
 *     以下条件其中之一满足时，需要进行检查：
 *     (1)存在IX_POI_NAME新增；
 *     (2)存在IX_POI_NAME修改或KIND_CODE字段修改；
 *     检查原则：
 *     1）官方原始英文名中存在“字母+0（数字零）+字母”或“字母+0（数字零）+空格”或“空格+0（数字零）+字母”时，
 *     报log1:官方原始英文名单词中含有数字零
 *     2）官方标准化英文名中存在“字母+0（数字零）+字母”或“字母+0（数字零）+空格”或“空格+0（数字零）+字母”时，
 *     报log2：官方标准化英文名单词中含有数字零
 * @author zhangxiaoyi
 */
public class FMYW20087 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			Pattern p1 = Pattern.compile(".* +0+[a-zA-z]+.*");
			Pattern p2 = Pattern.compile(".*[a-zA-z]+0+[a-zA-z]+.*");
			Pattern p3 = Pattern.compile(".*[a-zA-z]+0+ +.*");
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					if(nameTmp.isOriginName()&&(p1.matcher(name).matches()||p2.matcher(name).matches()
							||p3.matcher(name).matches())){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方原始英文名单词中含有数字零");
						return;
					}
					if(nameTmp.isStandardName()&&(p1.matcher(name).matches()||p2.matcher(name).matches()
							||p3.matcher(name).matches())){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方标准化英文名单词中含有数字零");
						return;
					}
				}
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
		// TODO Auto-generated method stub
		
	}

}
