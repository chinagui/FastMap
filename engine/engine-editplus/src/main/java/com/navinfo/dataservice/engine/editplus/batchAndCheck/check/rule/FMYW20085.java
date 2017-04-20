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
 * FM-YW-20-085	B-11合法字符集前后空格检查	DHM
 * 检查条件：
 *     以下条件其中之一满足时，需要进行检查：
 *     (1)存在IX_POI_NAME新增；
 *     (2)存在IX_POI_NAME修改或KIND_CODE字段修改；
 *     检查原则：
 *     官方原始英文名或官方标准化英文名中合法字符（不查括号，不查No.中的点）前存在空格，后不存在空格或前不存在空格，
 *     后存在空格时，报log：英文名合法字符前后空格错误
 *     备注：符号-_/:;'""~^.,?!*<>$%&#@+
 *     备注：
 *     Bang&Bang    不用报log；
 *     Bang &Bang   要报log；
 *     Bang& Bang   要报log；
 *     Bang & Bang  不报log
 * @author zhangxiaoyi
 */
public class FMYW20085 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			Pattern p = Pattern.compile(".*[^ ]+[\\-_/:;'\"~^.,?!*<>$%&#@+]+ .*");
			Pattern p1 = Pattern.compile(".* +[\\-_/:;'\"~^.,?!*<>$%&#@+]+[^ ]+.*");
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()
						&&(nameTmp.isOriginName()||nameTmp.isStandardName())){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					String nameLow=name.toLowerCase();
					String[] nameList = nameLow.split("no.");
					for(String subname:nameList){
						if(p.matcher(subname).matches()||p1.matcher(subname).matches()){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
						}
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
