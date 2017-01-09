package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 当标准化简体中文名称（names-langCode="CHI",names-type=1）更新NAME时，将标准化简体中文NAME字段转拼音并赋值到对应的NAME_PHONETIC中
 * @author gaopengrong
 */
public class FMBAT20141 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//存在IX_POI_NAME标准化中文名称，新增或者修改履历
			List<IxPoiName> br=poiObj.getIxPoiNames();
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			for(IxPoiName name:br){
				if(name.getNameType()==1&&(name.getHisOpType().equals(OperationType.INSERT)
						||(name.getHisOpType().equals(OperationType.UPDATE)&&name.hisOldValueContains(IxPoiName.NAME)))){
					String oldName=(String) name.getHisOldValue(IxPoiName.NAME);
					String newName=name.getName();
					if(!newName.equals(oldName)){
						//批拼音
						name.setNamePhonetic(metadataApi.pyConvert(newName)[1]);
					}
					
				}
			}
		}		
	}

}
