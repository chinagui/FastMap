package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * 检查条件：该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则：如果POI的标准化官方中文名称拼音、标准化简称拼音为空时，程序报出：POI中文名称官方标准化中文（标准化简称中文）未制作拼音
 * @author gaopengrong
 *
 */
public class FMGLM60254 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName br=poiObj.getOfficeStandardCHIName();
			String py=br.getNamePhonetic();
			if (py==null){setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());}
			List<IxPoiName> shorNames= poiObj.getShortStandardCHIName();
			for(IxPoiName shorName:shorNames){
				String spy=shorName.getNamePhonetic();
				if (spy==null){setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());return;}
			}
		}
	}
}
