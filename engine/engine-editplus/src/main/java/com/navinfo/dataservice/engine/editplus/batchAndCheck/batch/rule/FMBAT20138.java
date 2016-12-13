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
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * 查询条件：本次日编存在IX_POI_NAME新增或者修改履历，或者存在主表改分类的修改履历，且分类为150101（Chain为6003、6045、6002、6001、6000、6028、6025、602C、6047、6027、600A）
 * 或分类为150101（chain为6020）且官方标准化简体中文名称中不包含{"平安银行"}中的银行类POI，则执行如下批处理：
 * 
 * 批处理：将官方标准化中文名称（包括简体中文和繁体中文）的开头部分中包含有“SC_POINT_NAMECK”配置表中的“TYPE”=10的“PRE_KEY”字段内容（不区分大小写），
 * 则将官方标准化中文名称对应的简称(NAME_CLASS=5)修改为“RESULT_KEY”字段内容+官方标准化中文名称中未被替换的其余部分
 * （如果没有简称，则新增一条简称记录（NAME_GROUPID=max(POI名称组号）+1，NAME_TYPE=1，NAME_ID程序申请PID赋值，LANG_CODE赋值“CHI或者CHT”），
 * 内容为“RESULT_KEY”字段内容+官方标准化中文名称中的剩余部分），同时需要维护“简称名称”字段对应的“拼音”NAME_PHONETIC；
 * 如果数据中存在简称，当“RESULT_KEY”字段内容+官方标准化中文名称中未被替换的其余部分在简称名称能找到时，则不处理；
 * 如果数据中存在简称，当“RESULT_KEY”字段内容+官方标准化中文名称中未被替换的其余部分在简称名称找不到时，则标识删除所有简称记录，
 * 并新增一条简称（NAME_GROUPID=max(POI名称组号）+1，NAME_TYPE=1，NAME_ID程序申请PID赋值，LANG_CODE赋值“CHI或者CHT”），
 * 内容为“RESULT_KEY”字段内容+官方标准化中文名称中的剩余部分），内容为“RESULT_KEY”字段内容+官方标准化中文名称中的剩余部分；生成批处理履历;
 * @author zhangxiaoyi
 */
public class FMBAT20138 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isBatch(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String newKindCode=poi.getKindCode();
			String newChain=poi.getChain();
			IxPoiName br=poiObj.getOfficeStandardCHName();
			if(br==null){return;}
			String name=br.getName();
			if(newKindCode.equals("150101")){
				if (newChain.equals("6003")||newChain.equals("6045")||newChain.equals("6002")
					||newChain.equals("6001")||newChain.equals("6000")||newChain.equals("6028")
					||newChain.equals("6025")||newChain.equals("602C")||newChain.equals("6047")
					||newChain.equals("6027")||newChain.equals("600A")||(newChain.equals("6020")
					&&!name.contains("平安银行"))){
					
					MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					Map<String, String> typeD10 = metadataApi.scPointNameckTypeD10();
					Map<String, String> keyResult=ScPointNameckUtil.matchTypeD10(name, typeD10);
					String newShortName=name;
					if (keyResult.size()==0){return;}
					for(String preKey:keyResult.keySet()){
						newShortName=newShortName.replace(preKey, keyResult.get(preKey));
						}
					
					List<IxPoiName> sn=poiObj.getShortStandardCHName();
					//如果没有简称，则新增一条简称记录
					if(sn.size()==0){
						IxPoiName newShortSubrow= poiObj.createIxPoiName();
						newShortSubrow.setLangCode("CHI");
						newShortSubrow.setNameClass(5);
						newShortSubrow.setNameType(1);
						newShortSubrow.setName(newShortName);
						newShortSubrow.setNameGroupid(poiObj.getMaxGroupIdFromNames()+1);
						//批拼音
						newShortSubrow.setNamePhonetic(metadataApi.pyConvert(newShortName)[0]);	
						return;
					}
					//如果数据中存在简称，当“RESULT_KEY”字段内容+官方标准化中文名称中未被替换的其余部分在简称名称能找到时，则不处理；
					for (IxPoiName sName:sn){
						if (newShortName.equals(sName.getName())){return;}
					}
					//如果数据中存在简称，当“RESULT_KEY”字段内容+官方标准化中文名称中未被替换的其余部分在简称名称找不到时，则标识删除所有简称记录，并新增一条简称
					for (IxPoiName sName:sn){
						poiObj.deleteSubrow(sName);
					}
					//新增一条
					IxPoiName newShortSubrow= poiObj.createIxPoiName();
					newShortSubrow.setLangCode("CHI");
					newShortSubrow.setNameClass(5);
					newShortSubrow.setNameType(1);
					newShortSubrow.setName(newShortName);
					newShortSubrow.setNameGroupid(poiObj.getMaxGroupIdFromNames()+1);
					//批拼音
					newShortSubrow.setNamePhonetic(metadataApi.pyConvert(newShortName)[0]);				
				}
			}	
		}		
	}
	/**
	 * 本次日编存在IX_POI_NAME新增或者修改履历，或者存在主表改分类的修改履历
	 * @param poiObj
	 * @return
	 */
	private boolean isBatch(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			String newKindCode=poi.getKindCode();
			if(!newKindCode.equals(oldKindCode)){
				return true;
			}
		}
		//存在IX_POI_NAME新增或者修改履历
		IxPoiName br=poiObj.getOfficeStandardCHName();
		if(br!=null){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}
			}
		}
		return false;
	}

}
