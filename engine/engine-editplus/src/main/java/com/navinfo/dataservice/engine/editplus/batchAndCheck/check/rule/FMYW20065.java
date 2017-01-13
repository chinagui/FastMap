package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/** 
* @ClassName: FMYW20065 
* @author: zhangpengpeng 
* @date: 2017年1月10日
* @Desc: FMYW20065.java
*   检查条件：该POI发生变更(新增或修改主子表、删除子表)；
*	检查原则：
*	将拆分后的18个字段按“省名、市名、区县名、乡镇街道办、地名小区名、街巷名、标志物名、前缀、门牌号、类型名、子号、后缀、附属设施名、楼栋号、楼门号、楼层、房间号、附加信息”合并后，如果不存在汉字时，
*	报log：拆分后的地址不存在汉字，请确认是否清空拆分地址。
*/
public class FMYW20065 extends BasicCheckRule{
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
			if (addresses == null || addresses.size() == 0) {
				return;
			}
			for (IxPoiAddress addrTmp : addresses) {
				if (addrTmp.isCH()){
					// 获取中文地址拆分的18个字段合并
					String chiAddr = CheckUtil.getMergerAddr(addrTmp);
					if (StringUtils.isEmpty(chiAddr)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						continue;
					}
					boolean errorFlag = true;
					for (int i=0;i<chiAddr.length();i++){
						String check = chiAddr.substring(i, i+1);
						if (CheckUtil.isChinese(check)){
							errorFlag = false;
							break;
						}
					}
					if (errorFlag){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						continue;
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String addr = "a*dc 中";
		for (int i=0;i<addr.length();i++){
			//System.out.println(addr.substring(i, i+1));
			System.out.println(CheckUtil.isChinese(addr.substring(i, i+1)));
		}

	}
	

}
