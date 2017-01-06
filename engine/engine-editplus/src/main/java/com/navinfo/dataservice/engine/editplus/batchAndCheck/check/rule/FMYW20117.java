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

/** 
* @ClassName: FMYW20117 
* @author: zhangpengpeng 
* @date: 2017年1月6日
* @Desc: FMYW20117.java
* 检查条件：
  该POI发生变更(新增或修改主子表、删除子表)；
     检查原则：
英文地址全称中存在“字母+0（数字零）+字母”或“字母+0（数字零）+空格”或“空格+0（数字零）+字母”时，
报log:英文地址全称单词中含有数字零
*/
public class FMYW20117 extends BasicCheckRule{
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
			for (IxPoiAddress address: addresses){
				if (address.isEng()){
					String fullName = address.getFullname();
					if (StringUtils.isEmpty(fullName)){
						continue;
					}
					// 字母+0+字母
					Pattern p1 = Pattern.compile(".*([a-zA-Z]0[a-zA-Z])+.*");
					Matcher m1 = p1.matcher(fullName);
					// 空格+0+字母
					Pattern p2 = Pattern.compile(".*( 0[a-zA-Z])+.*");
					Matcher m2 = p2.matcher(fullName);
					// 字母+0+空格
					Pattern p3 = Pattern.compile(".*([a-zA-Z]0 )+.*");
					Matcher m3 = p3.matcher(fullName);
					
					if (m1.find() || m2.find() || m3.find()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		String fullName = "eo0r a 0a bb0 a";
		// 字母+0+字母
		Pattern p1 = Pattern.compile(".*([a-zA-Z]0[a-zA-Z])+.*");
		Matcher m1 = p1.matcher(fullName);
		// 空格+0+字母
		Pattern p2 = Pattern.compile(".*( 0[a-zA-Z])+.*");
		Matcher m2 = p2.matcher(fullName);
		// 字母+0+空格
		Pattern p3 = Pattern.compile(".*([a-zA-Z]0 )+.*");
		Matcher m3 = p3.matcher(fullName);
		
		if (m1.find() ){
			System.out.println("m1");
		}
		if (m2.find() ){
			System.out.println("m2");
		}
		if (m3.find()){
			System.out.println("m3");
		}
	}
}
