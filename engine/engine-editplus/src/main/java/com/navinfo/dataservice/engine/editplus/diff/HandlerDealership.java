package com.navinfo.dataservice.engine.editplus.diff;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;

/**
 * 代理店与Poi属性比较类
 * 
 * @author jch
 *
 */
public class HandlerDealership {

	/**
	 * 一览表与库相同判断条件：通过RESULT.cfm_poi_num关联的非删除POI官方标准中文名称、中文别名、分类、品牌、中文地址、邮编、电话（
	 * 多个电话｜分割合并对比）
	 * 与RESULT表中“厂商提供名称”、“厂商提供简称”、“代理店分类”、“代理店品牌”、“厂商提供地址”、“厂商提供邮编”、“厂商提供电话（销售、
	 * 维修、其它）”（多个电话｜分割合并对比，跟顺序无关）均相同，则“一览表与库相同”；
	 * 
	 * @param dealershipMR
	 * @param p
	 * @param poiObj
	 * @return
	 * @throws Exception
	 */
	public static boolean isSameTableAndDb(IxDealershipResult dealershipMR, BasicObj obj) throws Exception {

		if (dealershipMR == null || obj == null) {
			return false;
		}

		IxPoi p = (IxPoi) obj.getMainrow();
		IxPoiObj poiObj = (IxPoiObj) obj;

		boolean isEqual = false;
		boolean nameFlag = true;
		boolean nameShortFlag = true;
		boolean kindFlag = true;
		boolean chainFlag = true;
		boolean addressFlag = true;
		boolean postCodeFlag = true;
		boolean phoneFlag = true;

		StringBuffer str = new StringBuffer();

		IxPoiName poiName = poiObj.getOfficeStandardCHName();
		IxPoiName poiAliasName = poiObj.getAliasCHITypeName();

		IxPoiAddress poiAddr = poiObj.getChiAddress();
		System.out.println(dealershipMR.getName());
		System.out.println(poiName.getName());
		// 判断官方标准名称是否相等
		if (dealershipMR.getName() != null && poiName.getName() != null
				&& (!dealershipMR.getName().equals(poiName.getName()))) {
			str.append("官方标准名称不同；");
			nameFlag = false;
		}
		// 判断别名是否相等
		if (dealershipMR.getNameShort() != null && poiAliasName.getName() != null
				&& (!dealershipMR.getNameShort().equals(poiAliasName.getName()))) {
			str.append("别名不同；");
			nameShortFlag = false;
		}
		// 判断分类是否相等
		if (!dealershipMR.getKindCode().equals(p.getKindCode())) {
			str.append("分类不同；");
			kindFlag = false;
		}
		// 判断品牌是否相等
		if (!dealershipMR.getChain().equals(p.getChain())) {
			str.append("品牌不同；");
			chainFlag = false;
		}
		// 判断地址是否相等
		if (dealershipMR.getAddress() != null && poiAddr.getAddrname() != null
				&& (!dealershipMR.getAddress().equals(poiAddr.getAddrname()))) {
			str.append("地址不同；");
			kindFlag = false;
		}
		// 判断邮编是否相等
		if (dealershipMR.getPostCode()!=null && p.getPostCode()!=null &&(!dealershipMR.getPostCode().equals(p.getPostCode()))) {
			str.append("邮编不同；");
			postCodeFlag = false;
		}

		// 判断联系方式是否相同
		StringBuffer sb = new StringBuffer();
		String telephone = "";
		for (IxPoiContact c : poiObj.getIxPoiContacts()) {
			sb.append(c.getContact()).append(";");
		}
		if (sb.length() > 0)
			telephone = sb.toString().substring(0, sb.toString().length() - 1);
		telephone = StringUtil.sortPhone(telephone);
		if (!dealershipMR.getTelephone().equals(telephone)) {
			str.append("电话不同；");
			phoneFlag = false;
		}

		if (nameFlag && nameShortFlag && kindFlag && chainFlag && addressFlag && phoneFlag && postCodeFlag) {
			isEqual = true;
		}

		return isEqual;
	}

	/**
	 * 日库POI属性无变化判断条件：
	 * 通过RESULT.cfm_poi_num关联的非删除POI官方标准中文名称、分类、品牌、中文地址、邮编、电话（多个电话｜分割合并对比）
	 * 与RESULT表中已采纳POI名称、
	 * 已采纳POI分类、已采纳POI品牌、已采纳POI地址、已采纳POI邮编、已采纳POI电话（电话对比顺序无关）均相同，则“日库POI属性无变化”；
	 * 
	 * @param dealershipMR
	 * @param p
	 * @param poiObj
	 * @return
	 * @throws Exception
	 */
	public static boolean isNoChangePoiNature(IxDealershipResult dealershipMR, BasicObj obj) throws Exception {
		IxPoi p = (IxPoi) obj.getMainrow();
		IxPoiObj poiObj = (IxPoiObj) obj;

		if (dealershipMR == null || p == null) {
			return false;
		}

		boolean isEqual = false;
		boolean nameFlag = true;
		boolean kindFlag = true;
		boolean chainFlag = true;
		boolean addressFlag = true;
		boolean postCodeFlag = true;
		boolean phoneFlag = true;

		StringBuffer str = new StringBuffer();

		IxPoiName poiName = poiObj.getOfficeStandardCHName();

		IxPoiAddress poiAddr = poiObj.getChiAddress();
		// 判断官方标准名称是否相等
		if (!dealershipMR.getPoiName().equals(poiName.getName())) {
			str.append("官方标准名称不同；");
			nameFlag = false;
		}
		// 判断分类是否相等
		if (!dealershipMR.getPoiKindCode().equals(p.getKindCode())) {
			str.append("分类不同；");
			kindFlag = false;
		}
		// 判断品牌是否相等
		if (!dealershipMR.getPoiChain().equals(p.getChain())) {
			str.append("品牌不同；");
			chainFlag = false;
		}
		// 判断地址是否相等
		if (!dealershipMR.getPoiAddress().equals(poiAddr.getAddrname())) {
			str.append("地址不同；");
			kindFlag = false;
		}
		// 判断邮编是否相等
		if (!dealershipMR.getPoiPostCode().equals(p.getPostCode())) {
			str.append("邮编不同；");
			postCodeFlag = false;
		}

		// 判断联系方式是否相同
		StringBuffer sb = new StringBuffer();
		String telephone = "";
		for (IxPoiContact c : poiObj.getIxPoiContacts()) {
			sb.append(c.getContact()).append(";");
		}
		if (sb.length() > 0)
			telephone = sb.toString().substring(0, sb.toString().length() - 1);
		telephone = StringUtil.sortPhone(telephone);
		String adoptPoiTel = StringUtil.sortPhone(StringUtil.contactFormat(dealershipMR.getPoiTel()));
		if (!adoptPoiTel.equals(telephone)) {
			str.append("电话不同；");
			phoneFlag = false;
		}

		if (nameFlag && kindFlag && chainFlag && addressFlag && phoneFlag && postCodeFlag) {
			isEqual = true;
		}

		return isEqual;
	}

	// 是否是一栏表品牌
	// 通过RESULT.cfm_poi_num关联的非删除POI的品牌与元数据库表SC_POINT_SPEC_KINDCODE_NEW表中type＝15的chain是否相同，若相同则为“一览表品牌”，否则为“非一览表品牌”；
	public static boolean isDealershipChain(BasicObj obj) throws Exception {
		IxPoi p = (IxPoi) obj.getMainrow();
		MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, String> mapKindChain = metadataApi.scPointSpecKindCodeType15();
		if (p.getChain().equals(mapKindChain.get(p.getKindCode()))) {
			return true;
		}
		return false;
	}

	// 分类与品牌是否一致
	// 通过RESULT.cfm_poi_num关联的非删除POI的分类和品牌与RESULT表中POI分类和POI品牌一致，则“分类与品牌一致”，否则“分类与品牌不一致”；
	public static boolean isSameKindChain(IxDealershipResult dealershipMR, BasicObj obj) {
		IxPoi p = (IxPoi) obj.getMainrow();
		if (p.getChain().equals(dealershipMR.getPoiChain()) && p.getKindCode().equals(dealershipMR.getPoiKindCode())) {
			return true;
		}
		return false;
	}

}