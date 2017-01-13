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
 * @ClassName: FMYW20110
 * @author: zhangpengpeng
 * @date: 2017年1月5日
 * @Desc: FMYW20110.java 检查条件： 该POI发生变更(新增或修改主子表、删除子表)； 检查原则：
 *        中文地址合并包含“胡同”，但是英文地址不存在“Hu Tong”时， 报log：英文地址中“胡同”的翻译错误（几个“胡同”对应几个“Hu
 *        Tong”）
 */
public class FMYW20110 extends BasicCheckRule {

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
			String chiAddr = "";
			String engAddr = "";
			for (IxPoiAddress address: addresses){
				if (address.isCH()){
					chiAddr = CheckUtil.getMergerAddr(address);
				}
				if (address.isEng()){
					engAddr = address.getFullname();
				}
			}
			if (StringUtils.isEmpty(chiAddr)){return;}
			// 计算中文地址合并中“胡同”个数
			int chiCount = 0;
			String tmpChiAddr = chiAddr;
			while (tmpChiAddr.contains("胡同")){
				chiCount += 1;
				tmpChiAddr = tmpChiAddr.replaceFirst("胡同", "");
			}
			// 计算英文地址中“Hu Tong”个数
			int engCount = 0;
			String tmpEngAddr = engAddr;
			if (StringUtils.isNotEmpty(tmpEngAddr)){
				while (tmpEngAddr.contains("Hu Tong")){
					engCount += 1;
					tmpEngAddr = tmpEngAddr.replaceFirst("Hu Tong", "");
				}
			}
			// 中文地址合并包含“胡同”，但是英文地址不存在“Hu Tong”时，
			// log：英文地址中“胡同”的翻译错误（几个“胡同”对应几个“Hu Tong”）
			if (chiAddr.contains("胡同") && (StringUtils.isEmpty(engAddr) || !engAddr.contains("Hu Tong") || chiCount != engCount)){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String chiAddr = "胡同胡同胡同";
		int chiCount = 0;
		while (chiAddr.contains("胡同")) {
			chiCount += 1;
			chiAddr = chiAddr.replaceFirst("胡同", "");
		}
		String engAddr = "Hu Tong Hu Tong Hu Tong";
		System.out.println(chiCount);
		int engCount = 0;
		while (engAddr.contains("Hu Tong")) {
			engCount += 1;
			engAddr = engAddr.replaceFirst("Hu Tong", "");
		}
		System.out.println(engCount);
	}

}
