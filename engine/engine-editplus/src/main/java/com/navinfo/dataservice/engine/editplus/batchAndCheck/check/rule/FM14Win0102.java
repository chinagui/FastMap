package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM14Win0102
 * @author Han Shaoming
 * @date 2017年2月28日 下午7:47:54
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：将官方原始中文名称只包含分类关键字(SC_POINT_POICODE_NEW.kind_name)的POI显示出来，
 * 报log：重新确认成果中的设施名称是否正确
 */
public class FM14Win0102 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			String name = ixPoiName.getName();
			if(name == null){return;}
			MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, String> kindNameMap = metadataApi.getKindNameByKindCode();
			if(kindNameMap.containsKey(kindCode)){
				String kindName = kindNameMap.get(kindCode);
				if(StringUtils.equals(name, kindName)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
