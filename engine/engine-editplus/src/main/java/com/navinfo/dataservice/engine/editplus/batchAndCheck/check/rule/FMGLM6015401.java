package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * FM-GLM60154-01	POI中文名称检查	DHM	
 * 检查条件：
 *   Lifecycle！=1（删除）
 *   检查原则：
 *     1、检查POI名称（name）中“(”与“)”、中括号“［”和“］”、大括号“｛”、“｝”、书名号“《”、“》”应成对出现；
 *     2、括号“（”和“）”、中括号“［”和“］”、大括号“｛”、“｝”、书名号“《”、“》”中间必须有内容；
 *     3、不允许括号嵌套；
 *     否则报出：POI中文名称中“(”与“)”应成对出现
 *     备注：都是半角或都是全角的括号，不能一边是半角一边是全角的
 * @author zhangxiaoyi
 */
public class FMGLM6015401 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String name = nameObj.getName();
			if(name==null||name.isEmpty()){return;}
			
			if((name.contains("(")&&name.contains("）"))||(name.contains("（")&&name.contains(")"))
					||(name.contains("[")&&name.contains("］"))||(name.contains("［")&&name.contains("]"))
					||(name.contains("{")&&name.contains("｝"))||(name.contains("｛")&&name.contains("}"))){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI中文名称中存在半角括号");
				return;
			}
			String errorMsg=CheckUtil.isRightKuohao(name,"(",")");
			if(errorMsg!=null){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), errorMsg);
				return;
			}
			errorMsg=CheckUtil.isRightKuohao(name,"[","]");
			if(errorMsg!=null){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), errorMsg);
				return;
			}
			errorMsg=CheckUtil.isRightKuohao(name,"{","}");
			if(errorMsg!=null){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), errorMsg);
				return;
			}
			errorMsg=CheckUtil.isRightKuohao(name,"《","》");
			if(errorMsg!=null){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), errorMsg);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
