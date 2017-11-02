package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 查询条件：存在IX_POI_NAME新增或者修改，且IX_POI_NAME.U_RECORD！=2(删除)
 * 批处理：查询该POI是否存在官方标准名称(NAME_CLASS=1,NAME_TYPE=1，LANG_CODE=CHI或CHT，U_RECORD!=2)，
 * (1)如果存在，若官方原始名称与官方标准名称一致(名称判断时不区分全半角)，则不处理，否则将官方原始名称(NAME_CLASS=1,NAME_TYPE=2
 * ，LANG_CODE=CHI或CHT)对应的名称(NAME)值赋给官方标准名称(NAME)，NAME_PHONETIC根据官方标准名称转拼音；
 * (2)如果不存在官方标准名称，则增加一条IX_POI_NAME记录,NAME_ID申请赋值，POI_PID赋值该POI的PID，NAME_GROUPID=
 * max(NAME_GROUPID)+1,LANG_CODE赋值CHI或者CHT，NAME_CLASS=1，NAME_TYPE=1，
 * NAME赋值官方原始名称对应的名称，NAME_PHONETIC根据官方标准名称转拼音。 
 * (3)将官方原始名称和官方标准名称(NAME)转全角；
 * (4)为官方原始(NAME_CLASS=1,NAME_TYPE=2)拼音NAME_PHONETIC赋值；
 * (4)以上批处理生成批处理履历；
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20104 extends BasicBatchRule {
	private Map<Long,Long> pidAdminId;
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		pidAdminId = IxPoiSelector.getAdminIdByPids(getBatchRuleCommand().getConn(), pidList);
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		List<IxPoiName> poiNames = poiObj.getIxPoiNames();
		boolean changeFlag = false;
		for (IxPoiName poiName:poiNames) {
			// 存在IX_POI_NAME新增或者修改
			if (poiName.getHisOpType().equals(OperationType.INSERT) || poiName.getHisOpType().equals(OperationType.UPDATE)) {
				changeFlag = true;
			}
		}
		if (!changeFlag) {
			return;
		}
		IxPoiName standardPoiName = poiObj.getOfficeStandardCHName();
		IxPoiName originalPoiName = poiObj.getOfficeOriginCHName();
		MetadataApi apiService=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		String adminCode=null;
		if(pidAdminId!=null&&pidAdminId.containsKey(poi.getPid())){
			adminCode=pidAdminId.get(poi.getPid()).toString();
		}
		
		if (standardPoiName != null) {
			// (1)如果存在
			String standardName = "";
			if (standardPoiName.getName() != null) {
				standardName = standardPoiName.getName();
			}
			String originalName = "";
			if (originalPoiName.getName() != null) {
				originalName = originalPoiName.getName();
			}
			standardName = ExcelReader.h2f(standardName);
			originalName = ExcelReader.h2f(originalName);
			if (originalName.equals(standardName)) {
				// 若官方原始名称与官方标准名称一致(名称判断时不区分全半角)，则不处理
				return;
			}
			// 否则将官方原始名称(NAME_CLASS=1,NAME_TYPE=2，LANG_CODE=CHI或CHT)对应的名称(NAME)值赋给官方标准名称(NAME)，
			// NAME_PHONETIC根据官方标准名称转拼音；
			// (3)将官方原始名称和官方标准名称(NAME)转全角；
			// (4)为官方原始(NAME_CLASS=1,NAME_TYPE=2)拼音NAME_PHONETIC赋值；
			String namePy = apiService.pyConvert(originalName,adminCode,null);
			//生产过程中，出现了名称不为空但是转出拼音为空的数据，特加此判断进行捕获调查
			if(StringUtils.isNotEmpty(originalName)&&StringUtils.isEmpty(namePy)){
				throw new Exception("名称为:“"+originalName+"”的数据，对应转出的拼音为“"+namePy+"”，请调查转拼音是否正确");
			}
			standardPoiName.setName(originalName);
			standardPoiName.setNamePhonetic(namePy);
			originalPoiName.setName(originalName);
			originalPoiName.setNamePhonetic(namePy);
		} else {
			//(2)如果不存在官方标准名称，则增加一条IX_POI_NAME记录,NAME_ID申请赋值，POI_PID赋值该POI的PID，NAME_GROUPID=
			// max(NAME_GROUPID)+1,LANG_CODE赋值CHI或者CHT，NAME_CLASS=1，NAME_TYPE=1，
			// NAME赋值官方原始名称对应的名称，NAME_PHONETIC根据官方标准名称转拼音。
			// (3)将官方原始名称和官方标准名称(NAME)转全角；
			// (4)为官方原始(NAME_CLASS=1,NAME_TYPE=2)拼音NAME_PHONETIC赋值；
			String originalName = "";
			if (originalPoiName.getName() != null) {
				originalName = originalPoiName.getName();
			}
			originalName = ExcelReader.h2f(originalName);
			String namePy = apiService.pyConvert(originalName,adminCode,null);
			//生产过程中，出现了名称不为空但是转出拼音为空的数据，特加此判断进行捕获调查
			if(StringUtils.isNotEmpty(originalName)&&StringUtils.isEmpty(namePy)){
				throw new Exception("名称为:“"+originalName+"”的数据，对应转出的拼音为“"+namePy+"”，请调查转拼音是否正确");
			}
			
			standardPoiName = poiObj.createIxPoiName();
			standardPoiName.setNameClass(1);
			standardPoiName.setNameType(1);
			standardPoiName.setNameGroupid(poiObj.getMaxGroupIdFromNames()+1);
			standardPoiName.setLangCode(originalPoiName.getLangCode());
			standardPoiName.setName(originalName);
			standardPoiName.setNamePhonetic(namePy);
			
			originalPoiName.setName(originalName);
			originalPoiName.setNamePhonetic(namePy);
			
		}

	}

}
