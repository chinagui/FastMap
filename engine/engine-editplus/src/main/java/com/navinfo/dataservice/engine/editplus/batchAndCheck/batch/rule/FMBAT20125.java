package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 查询条件：该POI发生变更(新增或修改主子表)且KIND_CODE在重要分类表中 批处理：
 * (1)当FULLNAME字段修改且不为空，且存在对应的英文地址时，更新英文地址（LANG_CODE=""ENG""）生成履历；
 * (2)当FULLNAME字段修改且不为空，但没有英文地址时，增加英文地址（LANG_CODE=""ENG""）生成履历，；
 * (3)当FULLNAME字段不为空且没有修改，但没有英文地址（LANG_CODE=""ENG""）时，增加英文地址（LANG_CODE=""ENG""）
 * 生成履历，
 * (4)需要翻译的中文地址：将中文地址拆分后的15个字段按照“附加信息、房间号、楼层、楼门号、楼栋号、附属设施名、后缀、子号、类型名、门牌号、前缀、标志物名
 * 、街巷名、地名小区名、乡镇街道办”进行合并; (5)合并后的中文地址，进行分词时，应将人工拆分的中文地址作为一级分词结果;
 * (6)在(5)的基础上对一级分词的结果里的房间号（ROOM）、楼层（FLOOR）、楼门号（UNIT）、楼栋号（BUILDING）等四个词，
 * 进行二级分词后采用再次倒序翻译，即“5层”翻译为“Floor 5”而不是“5 Floor”；
 * 房间号关键字对应SC_POINT_ADDRCK.type=7对应的pre_key；
 * 楼层关键字对应SC_POINT_ADDRCK.type=7对应的pre_key；
 * 楼门号关键字对应SC_POINT_ADDRCK.type=7对应的pre_key；
 * 楼栋号关键字对应SC_POINT_ADDRCK.type=7对应的pre_key；
 * (7)当子号为“－”开头时，按“门牌号+子号”的顺序翻译。如果“门牌号+子号”组合后有关键字，需要将“门牌号+子号”组合后的关键字(
 * SC_POINT_ADDRCK.type=1)提到门牌号前面，按照“类型名+门牌号+子号”顺序翻译。如果翻译后，出现“No.No.”时，去掉多余的
 * “No.”；如果翻译后，出现“No.no.”时，去掉多余的“no.”;
 * (8)当子号不是“－”开头时，按照“子号+类型名+门牌号”的顺序翻译。如果子号有关键字(SC_POINT_ADDRCK.type=1)，需要将“子号”
 * 的关键字提到子号前面; (9)首字母大写的原则，可避免关键词库中大小问题，如“DAZHONG ELECTRONICS”、”town”、“village”
 * (10)“No.”中点后如果有空格，应去掉空格,当没有对应词库时，翻译的多个拼音之间没有空格;
 *
 * @author wangdongbin
 */
public class FMBAT20125 extends BasicBatchRule {
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
		String kindCode = poi.getKindCode();
		String chain = poi.getChain();
		String adminId=null;
		if(pidAdminId!=null&&pidAdminId.containsKey(poi.getPid())){
			adminId=pidAdminId.get(poi.getPid()).toString();
		}
		MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		if (!metadata.judgeScPointKind(kindCode, chain)) {
			return;
		}
		List<IxPoiAddress> poiAddresses = poiObj.getIxPoiAddresses();
		if (!isBatch(poiAddresses)) {
			return;
		}
		IxPoiAddress chiAddress = poiObj.getChiAddress();
		IxPoiAddress engAddress = poiObj.getENGAddress(chiAddress.getNameGroupid());
		String fullEng = transEng(chiAddress,metadata,adminId);
		if (engAddress == null) {
			engAddress = poiObj.createIxPoiAddress();
			engAddress.setNameGroupid(chiAddress.getNameGroupid());
			engAddress.setPoiPid(poi.getPid());
			engAddress.setLangCode("ENG");
			engAddress.setFullname(fullEng);
		} else {
			engAddress.setFullname(fullEng);
		}
		
	}
	
	private boolean isBatch(List<IxPoiAddress> poiAddresses) {
		IxPoiAddress chiAddress = null;
		IxPoiAddress engAddress = null;
		for (IxPoiAddress temp:poiAddresses) {
			if (temp.getLangCode()!=null && temp.getLangCode().equals("CHI")) {
				chiAddress = temp;
			} else if (temp.getLangCode()!=null && temp.getLangCode().equals("ENG")) {
				engAddress = temp;
			}
		}
		if (chiAddress == null) {
			return false;
		}
		
		if (StringUtils.isNotEmpty(chiAddress.getFullname())) {
			if (chiAddress.hisOldValueContains(IxPoiAddress.FULLNAME)) {
				// (1)当FULLNAME字段修改且不为空，且存在对应的英文地址时
				// (2)当FULLNAME字段修改且不为空，但没有英文地址时
				return true;
			} else {
				// (3)当FULLNAME字段不为空且没有修改，但没有英文地址时
				if (engAddress == null) {
					return true;
				}
			}
		}
		
		return false;
	}

	private String transEng(IxPoiAddress chiAddress,MetadataApi metadata,String adminId) throws Exception{
		String addOns = metadata.convertEng(chiAddress.getAddons());// 附加信息
		String roomNum = metadata.convertEng(keyAhead(chiAddress.getRoom(),metadata.queryAdRack(7)));// 房间号
		String floor = metadata.convertEng(keyAhead(chiAddress.getFloor(),metadata.queryAdRack(7)));// 楼层
		String unit = metadata.convertEng(keyAhead(chiAddress.getUnit(),metadata.queryAdRack(7)));// 楼门号
		String building = metadata.convertEng(keyAhead(chiAddress.getBuilding(),metadata.queryAdRack(7)));// 楼栋号
		String estab = metadata.convertEng(chiAddress.getEstab());// 附属设施名
		String surfix = metadata.convertEng(chiAddress.getSurfix());// 后缀
		String houseNumTypeSubNum = "";
		String houseNum = "";
		if (StringUtils.isNotEmpty(chiAddress.getHousenum())) {
			houseNum = chiAddress.getHousenum();
		}
		String subnum = "";
		if (StringUtils.isNotEmpty(chiAddress.getSubnum())) {
			subnum = chiAddress.getSubnum();
		}
		String type = "";
		if (StringUtils.isNotEmpty(chiAddress.getType())) {
			type = chiAddress.getType();
		}
		if (StringUtils.isNotEmpty(subnum)&&(subnum.startsWith("-")||subnum.startsWith("－"))) {
			houseNumTypeSubNum = metadata.convertEng(type+keyAhead(houseNum+subnum,metadata.queryAdRack(1)));
		} else {
			houseNumTypeSubNum = metadata.convertEng(keyAhead(subnum,metadata.queryAdRack(1))+type+houseNum);
		}
		if (houseNumTypeSubNum.indexOf("No. No.")>=0) {
			houseNumTypeSubNum = houseNumTypeSubNum.replace("No. No.", "No.");
		} else if (houseNumTypeSubNum.indexOf("No. no.")>=0) {
			houseNumTypeSubNum = houseNumTypeSubNum.replace("No. no.", "No.");
		}
		String prefix =  metadata.convertEng(chiAddress.getPrefix());
		String landMark = metadata.convertEng(chiAddress.getLandmark());
		String street = metadata.convertEng(chiAddress.getStreet());
		String place = metadata.convertEng(chiAddress.getPlace());
		String town = metadata.convertEng(chiAddress.getTown());
		if (addOns==null){
			addOns="";
		}
		String fullName = addOns;
		if (roomNum!=null&&!roomNum.isEmpty()) {
			fullName +=  " " + roomNum;
		}
		if (floor!=null&&!floor.isEmpty()) {
			fullName +=  " " + floor;
		}
		if (unit!=null&&!unit.isEmpty()) {
			fullName +=  " " + unit;
		}
		if (building!=null&&!building.isEmpty()) {
			fullName +=  " " + building;
		}
		if (estab!=null&&!estab.isEmpty()) {
			fullName +=  " " + estab;
		}
		if (surfix!=null&&!surfix.isEmpty()) {
			fullName +=  " " + surfix;
		}
		if (houseNumTypeSubNum!=null&&!houseNumTypeSubNum.isEmpty()) {
			fullName +=  " " + houseNumTypeSubNum;
		}
		if (prefix!=null&&!prefix.isEmpty()) {
			fullName +=  " " + prefix;
		}
		if (landMark!=null&&!landMark.isEmpty()) {
			fullName +=  " " + landMark;
		}
		if (street!=null&&!street.isEmpty()) {
			fullName +=  " " + street;
		}
		if (place!=null&&!place.isEmpty()) {
			fullName +=  " " + place;
		}
		if (town!=null&&!town.isEmpty()) {
			fullName +=  " " + town;
		}
		if(fullName!=null){
			return fullName.trim();
		}
		return fullName;
	}
	
	private String keyAhead(String word,List<String> keyArr) {
		if (StringUtils.isEmpty(word)) {
			return "";
		}
		// 循环拿出最后一个关键词及其位置
		// 将最后一个关键词提前
		int keyIndex = 0;
		String indexWord = "";
		for (String keyWord:keyArr) {
			int temp = word.indexOf(keyWord);
			if (temp>keyIndex) {
				keyIndex = temp;
				indexWord = keyWord;
			}
		}
		if (StringUtils.isNotEmpty(indexWord)) {
			word = indexWord + word.substring(0, keyIndex);
		}
		return word;
	}
	
}
