package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/** 
* @ClassName: FMCHR71040 
* @author: zhangpengpeng 
* @date: 2017年1月3日
* @Desc: 
* 检查条件：
   	该POI发生变更(新增或修改主子表、删除子表)；
     检查原则：英文地址字段中若存在以下，则报出：
	1） 回车符检查：包含回车符的记录；
	2） Tab符检查：包含Tab符号的记录；
	3） 多个空格检查：两个及两个以上空格的记录；
	4） 前后空格检查：名称开始前或者结尾处包含空格的记录；
	提示：英文地址（LANG_CODE="ENG"）格式：英文地址中不能名存在“xx” （提示信息中的符号全部用中文名称）
*/
public class FMCHR71040 extends BasicCheckRule{
	
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
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			//List<IxPoiName> names = poiObj.getIxPoiNames();
			if(addresses == null||addresses.size() == 0){
				return;
			}
			for (IxPoiAddress address: addresses){
				String langCode = address.getLangCode();
				if ("ENG".equals(langCode)){
					String engAddress = address.getFullname();
					if (StringUtils.isNotEmpty(engAddress)){
						List<String> error = CheckUtil.checkIllegalBlank(engAddress);
						if (error != null && error.size() > 0){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
									"英文地址中不能存在“"+error.toString().replace("[", "").replace("]", "")+"”");
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		String word = " Lin 69   Juhe West\r\n	 Rd ";
		Pattern pattern = Pattern.compile("\r|\n");
		Matcher matcher = pattern.matcher(word);
		if (matcher.find()){
			System.out.println("回车符");
		}
	}
}
