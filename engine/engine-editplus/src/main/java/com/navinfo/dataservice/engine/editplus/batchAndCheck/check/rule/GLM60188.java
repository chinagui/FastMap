package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * IX_POI_ADDRESS表中：
    1） 地名小区名有值，街巷名字段为空，则地名小区名长度应小于等于25，否则报log：地名/小区名有值，街巷名字段为空，则地名/小区名长度小于等于25；
    2） 街巷名字段有值，街巷名字段应小于等于25，否则报log：街巷名字段有值，街巷名字段小于等于25；
    3） 街巷名字段不为空或者地址类型名不为“胡同”、“巷”、“条”、“弄”，则“前缀+门牌+类型+子号+后缀”应小于等于25，否则报log：街巷名字段不为空或者类型不为胡同、巷、条、弄，则前缀+门牌+类型+子号+后缀小于等于25；
    4） 除附加信息外其他字段都为空，则附加信息字段长度应小于等于50，否则报log：除其他附加信息外其他字段都为空，则其他附加信息字段长度小于等于50。
 * 
 *
 */
public class GLM60188 extends BasicCheckRule {

	private List<String> streetList = new ArrayList<String>();

	public GLM60188() {
		streetList.add("胡同");
		streetList.add("巷");
		streetList.add("条");
		streetList.add("弄");
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		List<String> errList = new ArrayList<String>();
		if (StringUtils.isNotEmpty(address.getPlace())&&StringUtils.isEmpty(address.getStreet())) {
			if (address.getPlace().length()>25) {
				errList.add("地名/小区名有值，街巷名字段为空，则地名/小区名长度小于等于25；");
			}
		}
		if (StringUtils.isNotEmpty(address.getStreet())) {
			if (address.getStreet().length()>25) {
				errList.add("街巷名字段有值，街巷名字段小于等于25；");
			}
		}
		if (StringUtils.isNotEmpty(address.getStreet())||streetList.contains(address.getType())) {
			if (getLength(address.getPrefix())+getLength(address.getHousenum())+getLength(address.getType())+getLength(address.getSubnum())+getLength(address.getSurfix())>25) {
				errList.add("街巷名字段不为空或者类型不为胡同、巷、条、弄，则前缀+门牌+类型+子号+后缀小于等于25；");
			}
		}
		String mergeAdd = CheckUtil.getMergerAddr(address);
		 if (mergeAdd.equals(address.getAddons())) {
			 if (address.getAddons().length()>50) {
				 errList.add("除其他附加信息外其他字段都为空，则其他附加信息字段长度小于等于50。");
			 }
		 }
		if (errList.size()>0) {
			String errStr = org.apache.commons.lang.StringUtils.join(errList, ";");
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),errStr);
		}

	}
	
	private int getLength(String str) {
		if (str == null) {
			return 0;
		} else {
			return str.length();
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
