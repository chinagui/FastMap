package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/** 
* @ClassName: FMYW20115 
* @author: zhangpengpeng 
* @date: 2017年1月5日
* @Desc: FMYW20115.java
* 检查条件：
  	该POI发生变更(新增或修改主子表、删除子表)；
检查原则：
	英文地址中存在" a"," b"," c"," d"," e"," f"," g"," h"," i"," j"," k"," l"," m"," n"," o"," p"," q"," r"," s"," t"," u"," v"," w"," x"," y"," z"时，报log:英文地址中***（按实际单词报）首字母小写
	备注：1、如果单词本身为如下单词则不报错
			1）介词：in，on，into，to，of，at，from，with，by，for，as，than，after，since，until
			2）连接词：and，or
			3）a，an，the
		2、对于首个单词的首个字母也需要检查是否为小写
*/
public class FMYW20115 extends BasicCheckRule{
	
	private List<String> words = Arrays.asList("in","on","into","to","of","at","from","with","by","for","as","than","after","since","until","and","or","a","an","the");
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addresses= poiObj.getIxPoiAddresses();
			if (addresses == null || addresses.size() == 0){return;}
			String allErrorNameStr = "";
			for (IxPoiAddress address: addresses){
				String errorNameStr = "";
				if (address.isEng()){
					String fullName = address.getFullname();
					if (StringUtils.isEmpty(fullName)){
						continue;
					}
					String[] addrFullSplits = fullName.split(" ");
					for (String tmp: addrFullSplits){
						boolean error = false;
						char c = tmp.charAt(0);
						if (isLittleLetter(c)){
							error = true;
							if (words.contains(tmp)){
								error = false;
							}
						}
						if (error){
							errorNameStr += tmp + ",";
						}
					}
				}
				if (StringUtils.isNotEmpty(errorNameStr)){
					allErrorNameStr += "英文地址中" + errorNameStr + "首字母小写 ";
				}
			}
			
			if (StringUtils.isNotEmpty(allErrorNameStr)){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), allErrorNameStr);
				return;
			}
		}
	}
	
	/**
	 * 判断是小写字母
	 * @param c
	 * @return
	 */
	private static boolean isLittleLetter(char c){
		if (c >= 'a' && c <= 'z'){
			return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		String word = "I am A boy";
		String[] addrFullSplits = word.split(" ");
		for(String tmp: addrFullSplits){
			char c = tmp.charAt(0);
			if (isLittleLetter(c)){
				System.out.println(tmp);
			}
		}
	}

}
