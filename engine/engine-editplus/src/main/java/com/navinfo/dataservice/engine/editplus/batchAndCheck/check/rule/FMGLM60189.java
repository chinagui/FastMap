package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-GLM60189	英文地址长度检查	D
 * 检查条件：该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则：英文地址（语言代码是英文的“地址全称”）长度超过50个字符且为非重要分类的POI，报出：非重要分类的英文地址长度不成超过50
 * @author zhangxiaoyi
 *
 */
public class FMGLM60189 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
			if (addrs == null || addrs.size() == 0){return;}
			for(IxPoiAddress addTmp:addrs){
				if(addTmp.isEng()){
					MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					String addFullname = addTmp.getFullname();
					if(addFullname.length()>50&&!metadataApi.judgeScPointKind(poi.getKindCode(), poi.getChain())){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
