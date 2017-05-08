package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 	GLM60069		官方标准化中文与分类匹配性检查		DHM
	 检查条件：
	非删除POI对象
	检查原则：
	分类为机场（230126）的POI记录，其标准化官方中文名称应以“机场”/“機場”、“航站楼”/“航站樓”、“候机楼”/“候機樓”结尾，否则报log：分类为机场（230126）的POI应以“机场”/“機場”、“航站楼”/“航站樓”、“候机楼”/“候機樓”结尾。
	Name_class=1，name_type=1，lang_code=CHI或CHT
 * @author sunjiawei
 */
public class GLM60069 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();			
			String kind=poi.getKindCode();
			if(!kind.equals("230126")){return;}
			
			IxPoiName nameObj = poiObj.getOfficeStandardCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			
			if(!nameStr.endsWith("机场")&&!nameStr.endsWith("機場")&&!nameStr.endsWith("航站楼")&&!nameStr.endsWith("航站樓")
					&&!nameStr.endsWith("候机楼")&&!nameStr.endsWith("候機樓")){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
