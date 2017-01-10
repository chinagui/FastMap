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
* @ClassName: FMYW20034 
* @author: zhangpengpeng 
* @date: 2017年1月3日
* @Desc: FMYW20034.java
* 检查条件：
	该POI发生变更(新增或修改主子表、删除子表)；
     检查原则：
	1）介词：in，on，into，to，of，at，from，with，by，for，as，than，after，since，until
	2）连接词：and，or
	3）a，an，the
    虚词出现在英文地址开头，则首字母大写，其他情况应小写，否则报
	log1：英文地址中虚词“**”首字母应小写
	log2：英文地址中虚词“**”首字母应大写
*/
public class FMYW20034 extends BasicCheckRule{
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			if(addresses == null || addresses.size() == 0){
				return;
			}
			List<String> wordList = Arrays.asList("in","on","into","to","of","at","from","with","by","for","as","than",
					"after","since","until","and","or","a","an","the");
			for (IxPoiAddress addressTmp: addresses){
				if (addressTmp.isEng()){
					String engAddress = addressTmp.getFullname();
					if (StringUtils.isEmpty(engAddress)){
						continue;
					}
					String[] addrList = engAddress.split(" ");
					String firstWord = addrList[0];
					if(wordList.contains(firstWord.toLowerCase())){
						//首字母大写
						String rightWord=String.valueOf(firstWord.toCharArray()[0]).toUpperCase()+firstWord.substring(1);
						if(!rightWord.equals(firstWord)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "英文地址中虚词“"+firstWord+"”首字母应大写");
						}
					}
					for(int i=0;i<addrList.length;i++){
						if( i == 0){
							continue;
						}
						String subaddr = addrList[i];
						if(wordList.contains(subaddr.toLowerCase()) && !subaddr.toLowerCase().equals(subaddr)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "英文地址中虚词“"+subaddr+"”首字母应小写");
						}
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
}
