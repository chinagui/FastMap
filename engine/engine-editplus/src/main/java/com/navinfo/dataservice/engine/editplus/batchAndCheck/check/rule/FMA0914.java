package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * 
 * 检查条件：
该POI发生变更(新增或修改主子表、删除子表)；
检查原则：
中文（LANG_CODE=CHI或CHT）地址拆分的18个字段：省名、市名、区县名、乡镇名、街巷名、地名小区名、标志物名、前缀、门牌号、类型名、子号、后缀、附属设施名、楼栋号、楼门号、楼层、房间号、附加信息：
1）字段内容中存在空格，且空格前后若为以下组合，将Err的情况，程序报出；
见《备注》中空格规则表
2）回车符检查：包含回车符的记录报出；
3） Tab符检查：包含Tab符号的记录报出；
4） 多个空格检查：两个及两个以上空格的记录报出；
5） 前后空格检查：名称开始前或者结尾处包含空格的记录报出；
一个POI满足多个报出条件，只报一次；
提示：地址格式检查 ：地址中不能存在“xx”（提示错误字段，错误类型，提示信息中的符号全部用中文名称）
 *
 */
public class FMA0914 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();	
		List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
		if (addresses.size()==0) {
			return;
		}
		
		List<String> errMsgList = new ArrayList<String>();
		
		for (IxPoiAddress addr:addresses) {
			if (!addr.getLangCode().equals("CHI") && !addr.getLangCode().equals("CHT")) {
				continue;
			}
			
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getProvince()),"省名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getCity()),"市名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getCounty()),"区县名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getTown()),"乡镇名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getPlace()),"地名小区名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getStreet()),"街巷名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getLandmark()),"标志物名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getPrefix()),"前缀",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getHousenum()),"门牌号",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getType()),"类型名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getSubnum()),"子号",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getSurfix()),"后缀",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getEstab()),"附属设施名",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getBuilding()),"楼栋号",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getUnit()),"楼门号",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getFloor()),"楼层",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getRoom()),"房间号",errMsgList);
			mergerErrList(CheckUtil.blankRuleErrStr(addr.getAddons()),"附加信息",errMsgList);
			
			if (errMsgList.size()>0) {
				String error = "地址格式检查 ：" + StringUtils.join(errMsgList, ";");
				setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),error);
				return;
			}
			
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getProvince()),"省名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getCity()),"市名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getCounty()),"区县名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getTown()),"乡镇名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getPlace()),"地名小区名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getStreet()),"街巷名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getLandmark()),"标志物名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getPrefix()),"前缀",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getHousenum()),"门牌号",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getType()),"类型名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getSubnum()),"子号",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getSurfix()),"后缀",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getEstab()),"附属设施名",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getBuilding()),"楼栋号",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getUnit()),"楼门号",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getFloor()),"楼层",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getRoom()),"房间号",poi)) {return;}
			if (setCheckRet(CheckUtil.checkIllegalBlank(addr.getAddons()),"附加信息",poi)) {return;}
			
		}

	}
	
	private void mergerErrList(String errMsg,String colName,List<String> errMsgList) throws Exception {
		if (errMsg == null) {
			return;
		}
		errMsgList.add(colName+":"+errMsg);
	}
	
	private boolean setCheckRet(List<String> errList,String colName,IxPoi poi) throws Exception {
		if (errList == null || errList.size() == 0) {
			return false;
		}
		String error = "地址格式检查 ：" + colName + "中不能存在" + errList.get(0);
		setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),error);
		return true;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
