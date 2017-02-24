package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 *	以下条件其中之一满足时，需要进行检查：
 *	(1)存在IX_POI_NAME新增；
 *	(2)存在IX_POI_NAME修改或修改分类存在；
 *	检查原则：
 *	【数字】指的是“零~十，０~９，或者这些数字与字母共存（不包括“壹、贰。。。百、千”等等） ；
 *	官方标准化中文（langCode=CHI或CHT）名称内容满足以下任意一种组合方式的，需要报出：
 *	① 名称以“第+【数字】+分店|店|家|号店（號店）|连锁店（連鎖店）|店”结尾。
 *	    举例：第１０１店、北京第十五分店、第１０１连锁店
 *	② 名称以“【数字】+分店|店|连锁店（連鎖店）”结尾；
 *	③ 名称以“ＮＯ．【数字（可有汉字或全角符号）】+店|分店|号店（號店）|门店（門店）|店”结尾；
 *	    举例：ＮＯ．３３１店
 *	④ 名称包含“Ｎｏ．”、“Ｎ０．”、“ｎｏ．”、“ｎＯ．”、“ＮＯ：”
 *	提示：POI分店名称统一
 *
 */
public class FMA0410 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		List<IxPoiName> names = poiObj.getIxPoiNames();
		boolean isChanged = false;
		IxPoiName standardName = null;
		for (IxPoiName name:names) {
			if (name.getHisOpType().equals(OperationType.INSERT) || name.getHisOpType().equals(OperationType.UPDATE)) {
				isChanged = true;
			}
			if (name.getNameClass()==1 && name.getNameType()==1 && name.getLangCode().equals("CHI") || name.getLangCode().equals("CHT")) {
				standardName = name;
			}
		}
		
		if (isChanged && standardName!=null) {
			// ① 名称以“第+【数字】+分店|店|家|号店（號店）|连锁店（連鎖店）|店”结尾。
	        // 举例：第１０１店、北京第十五分店、第１０１连锁店
			Pattern p1 = Pattern.compile(".*第[a-zA-Zａ-ｚＡ-Ｚ]+(分店|店|家|号店|號店|连锁店|連鎖店)$");
			Matcher m1 = p1.matcher(standardName.getName());
			
			Pattern p2 = Pattern.compile(".*第[零一二三四五六七八九十0-9０-９a-zA-Zａ-ｚＡ-Ｚ]+(分店|店|家|号店|號店|连锁店|連鎖店)$");
			Matcher m2 = p2.matcher(standardName.getName());
			if (!m1.matches() && m2.matches()) {
				setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
			}
			
			// ② 名称以“【数字】+分店|店|连锁店（連鎖店）”结尾；
			Pattern p3 = Pattern.compile("[^零一二三四五六七八九十0-9０-９a-zA-Zａ-ｚＡ-Ｚ]*[a-zA-Zａ-ｚＡ-Ｚ]+(分店|店|连锁店|連鎖店)$");
			Matcher m3 = p3.matcher(standardName.getName());
			
			Pattern p4 = Pattern.compile(".*[零一二三四五六七八九十0-9０-９a-zA-Zａ-ｚＡ-Ｚ]+(分店|店|连锁店|連鎖店)$");
			Matcher m4 = p4.matcher(standardName.getName());
			if (!m3.matches() && m4.matches()) {
				setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
			}
			
			// ③ 名称以“ＮＯ．【数字（可有汉字或全角符号）】+店|分店|号店（號店）|门店（門店）|店”结尾；
	        // 举例：ＮＯ．３３１店
			Pattern p5 = Pattern.compile(".*(ＮＯ．)+.*[零一二三四五六七八九十0-9０-９a-zA-Zａ-ｚＡ-Ｚ]+.*(店|分店|号店|號店|门店|門店)$");
			Matcher m5 = p5.matcher(standardName.getName());

			if (m5.matches()) {
				setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
			}
			
			// ④ 名称包含“Ｎｏ．”、“Ｎ０．”、“ｎｏ．”、“ｎＯ．”
			Pattern p6 = Pattern.compile(".*(Ｎｏ|Ｎ０|ｎｏ|ｎＯ|ＮＯ：)+．+.*");
			Matcher m6 = p6.matcher(standardName.getName());

			if (m6.matches()) {
				setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
