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
* @ClassName: FMYW20100 
* @author: zhangpengpeng 
* @date: 2017年1月4日
* @Desc: FMYW20100.java
* 检查条件：
  	该POI发生变更(新增或修改主子表、删除子表)；
      检查原则：
	英文地址（FULLNAME）中合法字符（不查No.中的点）前存在空格，后不存在空格或前不存在空格，后存在空格时，
	报log：英文地址中合法字符前后空格错误
	备注：符号-_/:;'"~^.,?!*()<>$%&#@+
	备注：
	Bang&Bang    不用报log；
	Bang &Bang   要报log；
	Bang& Bang   要报log；
	Bang & Bang  不报log
* 
*/
public class FMYW20100 extends BasicCheckRule{
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			if (addresses == null || addresses.size() == 0){return;}
			for (IxPoiAddress address: addresses){
				if (address.isEng()){
					String EngAddr = address.getFullname();
					if (StringUtils.isNotEmpty(EngAddr)){
						// 不查No.中的点
						Pattern pattern = Pattern.compile("[Nn]{1}[Oo]{1}.{1}");
						Matcher matcher = pattern.matcher(EngAddr);
						if (matcher.find()){
							String tmp = matcher.group();
							EngAddr = EngAddr.replace(tmp, "");
						}
	                    
	                    // 合法字符（不查No.中的点）前存在空格，后不存在空格
	                    Pattern patternPreSpe = Pattern.compile(".* {1}[-_/:;'\"~^.,?!*()<>$%&#@+]{1}.*");
	                    Matcher matcherPreSpe = patternPreSpe.matcher(EngAddr);
	                    
	                    // 合法字符（不查No.中的点）前不存在空格，后存在空格
	                    Pattern patternPostSpe = Pattern.compile(".*[-_/:;'\"~^.,?!*()<>$%&#@+]{1} {1}.*");
	                    Matcher matcherPostSpe = patternPostSpe.matcher(EngAddr);
	                    
	                    // 合法字符（不查No.中的点）前存在空格，后存在空格
	                    Pattern patternPreAndPostSpe = Pattern.compile(".* {1}[-_/:;'\"~^.,?!*()<>$%&#@+]{1} {1}.*");
	                    Matcher matcherPreAndPostSpe = patternPreAndPostSpe.matcher(EngAddr);
	                    
	                    if ((matcherPreSpe.find() || matcherPostSpe.find()) && (!matcherPreAndPostSpe.find())){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
	                    }
					}
				}
			}
		}
	}
}
