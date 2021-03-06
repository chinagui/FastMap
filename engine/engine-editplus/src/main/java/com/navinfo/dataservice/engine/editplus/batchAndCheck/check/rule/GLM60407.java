package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * GLM60407		POI简称错误	DHM
	检查对象：
	非删除POI对象且分类kind_code为150101的记录；
	检查原则：
	1、官方标准化中文名以元数据库sc_point_nameck中type为10、PRE_KET开头的数据，都需要有简称,否则报log：银行检查错误！
	2、制作了银行类简称的POI，标准化名称后缀与简称后缀应相同，否则报log：银行检查错误！
	说明：后缀是标准化名称去掉“PRE_KEY”字段值之后的内容
	例如：标准化名称“中国银行北京分行”与简称“中行北京分行”的后缀“北京分行”，应该相同；
	3、银行类POI简称只能存在一条记录，否则报log：银行检查错误！
 * @author sunjiawei
 *
 */
public class GLM60407 extends BasicCheckRule {
	
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
			IxPoiName br=poiObj.getOfficeStandardCHName();
			if(br==null){return;}
			String name=br.getName();
			String kindCode= poi.getKindCode();
			if(kindCode.equals("150101")){
				//银行类POI简称只能存在一条记录;
				List<IxPoiName> shortNames=poiObj.getShortCHNames();
				if(shortNames.size()>1){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),null);
					return;
				}
				
				MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				Map<String, String> typeD10 = metadataApi.scPointNameckTypeD10();
				Map<String, String> keyResult=ScPointNameckUtil.matchTypeD10(name, typeD10);
				
				//1、官方标准化中文名以元数据库sc_point_nameck中type为10、PRE_KET开头的数据，都需要有简称；
				if (keyResult.size()>0&&shortNames.size()==0){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),null);
					return;
				}
				//制作了银行类简称的POI，标准化名称后缀与简称后缀应相同
				if(shortNames.size()==1&&keyResult.size()>0){
					IxPoiName shortName=shortNames.get(0);
					if(shortName==null){return;}
					String shortNameStr=shortName.getName();
					for(String preKey:keyResult.keySet()){
						name=name.replace(preKey, keyResult.get(preKey));
					}
					if (!name.equals(shortNameStr)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),null);
						return;
					}
				}
					
			}
		}
	}
	

}
