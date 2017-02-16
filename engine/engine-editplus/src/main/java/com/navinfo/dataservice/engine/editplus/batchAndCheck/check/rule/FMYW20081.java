package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName: FMYW20081
 * @author: zhangpengpeng
 * @date: 2017年1月10日
 * @Desc: FMYW20081.java 检查条件： 以下条件其中之一满足时，需要进行检查：
 *        (1)存在官方中文地址IX_POI_ADDRESS新增且FULLNAME不为空；
 *        (2)存在官方中文地址IX_POI_ADDRESS修改且FULLNAME不为空； 检查原则：
 *        地址FULLNAME中存在“*路*条*、*路*巷*、*路*弄*、*路*段*、*街*条*、*街*巷*、*街*弄*、*街*段*、*道*条*、
 *        道*巷*、*道*弄*、*道*段*、*巷*条*、*巷*弄*、*巷*段*”时，门楼址类型不存在“*条*、*巷*、*弄*”或街巷名不存在“*段*”
 *        时， 报log：道路名加条巷弄拆分错误
 */
public class FMYW20081 extends BasicCheckRule {
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 存在IxPoiAddress新增或者修改履历
			IxPoiAddress addr = poiObj.getCHAddress();
			if (addr == null) {
				return;
			}
			if (!addr.getHisOpType().equals(OperationType.INSERT)
					&& (!addr.getHisOpType().equals(OperationType.UPDATE))) {
				return;
			}
			if (addr.getFullname() == null || addr.getFullname().isEmpty()) {
				return;
			}
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			if (addresses == null || addresses.size() == 0) {
				return;
			}
			for (IxPoiAddress address : addresses) {
				if (address.isCH()) {
					String fullName = address.getFullname();
					if (StringUtils.isNotEmpty(fullName)) {
						Pattern p1 = Pattern.compile("(.*路{1}.*条{1}.*)|(.*街{1}.*条{1}.*)|(.*道{1}.*条{1}.*)");
						Pattern p2 = Pattern.compile("(.*路{1}.*巷{1}.*)|(.*街{1}.*巷{1}.*)|(.*道{1}.*巷{1}.*)");
						Pattern p3 = Pattern.compile("(.*路{1}.*弄{1}.*)|(.*街{1}.*弄{1}.*)|(.*道{1}.*弄{1}.*)");
						Pattern p4 = Pattern.compile("(.*道{1}.*段{1}.*)|(.*街{1}.*段{1}.*)|(.*路{1}.*段{1}.*)");
						Pattern p5 = Pattern.compile("(.*巷{1}.*条{1}.*)");
						Pattern p6 = Pattern.compile("(.*巷{1}.*弄{1}.*)");
						Pattern p7 = Pattern.compile("(.*巷{1}.*段{1}.*)");
						Pattern p8 = Pattern.compile("(.*条{1}.*)");
						Pattern p9 = Pattern.compile("(.*巷{1}.*)");
						Pattern p10 = Pattern.compile("(.*弄{1}.*)");
						Pattern p11 = Pattern.compile("(.*段{1}.*)");
						
						String type = address.getType();
						String street = address.getStreet();
						
		                if (p1.matcher(fullName).find() && (StringUtils.isEmpty(type) || !p8.matcher(type).find())){
		                	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
		                }
		                if (p2.matcher(fullName).find() && (StringUtils.isEmpty(type) || !p9.matcher(type).find())){
		                	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
		                }
		                if (p3.matcher(fullName).find() && (StringUtils.isEmpty(type) || !p10.matcher(type).find())){
		                	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
		                }
		                if (p4.matcher(fullName).find() && (StringUtils.isEmpty(street) || !p11.matcher(street).find())){
		                	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
		                }

		                if(p5.matcher(fullName).find() && (StringUtils.isEmpty(type) || (!p8.matcher(type).find() && !p9.matcher(type).find()))){
		                	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
		                }
		                if(p6.matcher(fullName).find() && (StringUtils.isEmpty(type) || (!p9.matcher(type).find() && !p10.matcher(type).find()))){
		                	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
		                }
		                if(p7.matcher(fullName).find() && ((StringUtils.isEmpty(type) || !p9.matcher(type).find()) && (StringUtils.isEmpty(street) || !p11.matcher(street).find()))){
		                	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
		                }
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fullName = "西安市锦业路5号";
		Pattern p1 = Pattern.compile("(.*道{1}.*段{1}.*)|(.*街{1}.*段{1}.*)|(.*路{1}.*段{1}.*)");
		//Matcher m1 = p1.matcher(fullName);
		System.out.println(p1.matcher(fullName).find());
	}

}
