package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * POI英文地址中含有配置表“POI名称相关检查配置表（SC_POINT_NAMECK）”中的“TYPE=9”的“PRE_KEY”的POI英文地址报log：英文地址格式错误：英文地址中含有：xxxx！
 * 
 */
public class GLM60335 extends BasicCheckRule {
	private MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	private List<String> scPointNameckType9 = new ArrayList<>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
		if (addresses == null) {return;}
		for(IxPoiAddress address:addresses){
			if(address.isEng()){
				String fullname= address.getFullname();
				if(fullname==null||fullname.length()==0){return;}
				for(String str:scPointNameckType9){
					if(str==null||str.length()==0){continue;}
					if(fullname.contains(str)){
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"英文地址格式错误：英文地址中含有："+str);
					}
				}
			}
		}
	}
	

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
		scPointNameckType9 = metadataApi.scPointNameckType9();
	}

}
