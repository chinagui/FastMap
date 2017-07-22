package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *  GLM60003 		机场名称检查		DHM
	检查条件：
	非删除POI对象
	检查原则：
	IMPORTANCE为1且KIND_CODE为230126的POI，
	则POI的标准化官方中文名称(name_class=1，name_type=1，lang_code=CHI或CHT)应以“机场”或“機場”结尾，
	否则log：IMPORTANCE为1的，分类为机场的POI标准化官方中文名称应以“机场”或“機場”结尾！
 * @author sunjiawei
 */
public class GLM60003 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			if(poi.getImportance()==1&&poi.getKindCode().equals("230126")){
				List<IxPoiName> names = poiObj.getIxPoiNames();
				if(names==null||names.size()==0){return;}
				for(IxPoiName nameTmp:names){
					if(nameTmp.isCH()&&nameTmp.isOfficeName()){
						String name=nameTmp.getName();
						if(name==null||name.isEmpty()){continue;}
						if(nameTmp.isStandardName()&&(!name.endsWith("机场")&&!name.endsWith("機場"))){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
