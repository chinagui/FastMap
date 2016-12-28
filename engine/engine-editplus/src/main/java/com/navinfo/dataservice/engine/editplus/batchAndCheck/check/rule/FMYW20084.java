package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.sun.mail.handlers.message_rfc822;
/**
 * FM-YW-20-084	A-7英文名括号前后空格检查	DHM	
 * 检查条件：
 *     以下条件其中之一满足时，需要进行检查：
 *     (1)存在IX_POI_NAME新增；
 *     (2)存在IX_POI_NAME修改或KIND_CODE字段修改；
 *     检查原则：
 *     1）官方原始英文名或官方标准化英文名中括号里面存在空格
 *     2）官方原始英文名或官方标准化英文名中括号外面不存在空格
 *     报log：英文名括号前后空格错误
 * @author zhangxiaoyi
 */
public class FMYW20084 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()
						&&(nameTmp.isOriginName()||nameTmp.isStandardName())){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					if(name.contains("( ")||name.contains(" )")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						return;
					}
					Pattern p = Pattern.compile("[^ ]+\\(.+");
					Matcher m = p.matcher(name);
					Pattern p1 = Pattern.compile(".+\\)[^ ]+");
					Matcher m1 = p1.matcher(name);
					if(m.matches()||m1.matches()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
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
