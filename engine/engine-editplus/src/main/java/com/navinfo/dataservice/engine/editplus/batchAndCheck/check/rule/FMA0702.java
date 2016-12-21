package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

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
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * FM-A07-02	银行类POI简称作业	DHM	
 * 检查条件：检查条件（1）、（3）同时满足，或者（2）、（3）同时满足才执行
 * (1)存在IX_POI_NAME新增；
 * (2)存在IX_POI_NAME修改或修改分类存在；
 * (3) kindCode为150101（Chain为6003、6045、6002、6001、6000、6028、6025、602C、6047、6027、600A）150101（chain为6020）
 * 且官方标准化简体中文名称中不包含{""平安银行""}中的银行类POI
 * 检查原则：
 * ① 无对应的简称名称时，程序报log
 * ② 对应简称名称不是1个时，程序报log
 * ③ 检查官方标准化简体或繁体中文名称，如果开头部分在“SC_POINT_NAMECK”表中TYPE=“10”的记录中，与“PRE_KEY”字段内容相同，
 * 将官方标准化简体或繁体中文名称的开头部分替换成“SC_POINT_NAMECK”表中“RESULT_KEY”字段内容，
 * 将替换结果与POI的简称简体中文或简称繁体中文进行对比，如果不同，程序报Log
 * 提示：
 * ① 银行类POI简称作业
 * ② 银行类POI简称个数错误
 * ③ 银行类POI简称制作错误
 * @author zhangxiaoyi
 *
 */
public class FMA0702 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> shortCHNames = poiObj.getShortCHNames();
			if(shortCHNames==null||shortCHNames.size()==0){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),"银行类POI简称作业");
				return;
			}
			if(shortCHNames.size()>1){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),"银行类POI简称个数错误");
				return;
			}
			IxPoiName shortCHName=shortCHNames.get(0);
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, String> typeD10 =metadataApi.scPointNameckTypeD10();
			IxPoiName br=poiObj.getOfficeStandardCHName();
			if(br==null){return;}
			String name=br.getName();
			if(name==null){return;}
			Map<String, String> keyResult=ScPointNameckUtil.matchTypeD10(name, typeD10);		
			if (keyResult.size()==0){return;}
			for(String preKey:keyResult.keySet()){
				name=name.replace(preKey, keyResult.get(preKey));
				if(!name.equals(shortCHName.getName())){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),"银行类POI简称制作错误");
					return;
				}				
			}
		}
	}
	
	/**
	 * 检查条件（1）、（3）同时满足，或者（2）、（3）同时满足才执行
	 * (1)存在IX_POI_NAME新增；
	 * (2)存在IX_POI_NAME修改或修改分类存在；
	 * (3) kindCode为150101（Chain为6003、6045、6002、6001、6000、6028、6025、602C、6047、6027、600A）150101（chain为6020）
	 * 且官方标准化简体中文名称中不包含{""平安银行""}中的银行类POI
	 * @param poiObj
	 * @return true满足检查条件，false不满足检查条件
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		String newKindCode=poi.getKindCode();
		if(!newKindCode.equals("150101")){return false;}
		String chain=poi.getChain();
		if(chain==null){return false;}
		if(!(chain.equals("6003") || chain.equals("6045")|| chain.equals("6002")|| chain.equals("6001")
				|| chain.equals("6000")|| chain.equals("6028")|| chain.equals("6025")|| chain.equals("602C")
				|| chain.equals("6047")|| chain.equals("6027")|| chain.equals("600A")|| chain.equals("6020"))){
			return false;
		}
		if(chain.equals("6020")){
			IxPoiName officeName = poiObj.getOfficeStandardCHIName();
			if(officeName!=null){
				String office=officeName.getName();
				if(office!=null&&office.contains("平安银行")){
					return false;
				}
			}
		}
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			if(!newKindCode.equals(oldKindCode)){
				return true;
			}
		}
		//(1)存在IX_POI_NAME的新增；(2)存在IX_POI_NAME的修改；
		List<IxPoiName> names = poiObj.getIxPoiNames();
		for (IxPoiName br:names){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}}
		}
		return false;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
