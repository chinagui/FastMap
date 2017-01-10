package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * FM-GLM60335	英文名错误检查	DHM	
 * 检查条件：该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：POI英文名称中含有配置表“POI名称相关检查配置表（SC_POINT_NAMECK）”中的“TYPE=9”的“PRE_KEY”的POI英文名称报出：英文名中含有：xxxx
 * 检查名称：官方英文名称（NAME_TYPE={1,2}，NAME_CLASS=1，LANG_CODE=ENG）
 * @author zhangxiaoyi
 */
public class FMGLM60335 extends BasicCheckRule {
	private MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();	
			List<String> type9=metadataApi.scPointNameckType9();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if((nameTmp.getNameType()==1||nameTmp.getNameType()==2)
						&&nameTmp.getNameClass()==1&&nameTmp.isEng()){
					String nameStr=nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					List<String> error = ScPointNameckUtil.matchType(nameStr, type9);
					if(error!=null&&error.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
								"英文名中含有："+error.toString().replace("[", "").replace("]", ""));
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
