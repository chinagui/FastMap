package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * （1）IX_POI_ADDRESS表的“HOUSENUM（门牌号）”字段如果含有全角的“阿拉伯数字、字母、~、/”以外的字符时报log：“门牌号”字段中含有“数字、字母、~、/”以外的字符xxx；！
 * （2）字母出现在阿拉伯数字后面的时候，报出log：字母不能出现在阿拉伯数字后面！
 * （3）字母为小写字母时，程序报log：门牌号中存在小写字母！
 * （4）门牌号长度＞10时，程序报log：门牌号长度＞10！
 *
 */
public class GLM60115 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		//存在IxPoiAddress新增或者修改履历
		IxPoiAddress address=poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		if (address.getFullname() == null || address.getFullname().isEmpty()) {
			return;
		}
		String houseNum = address.getHousenum();
		if (houseNum == null || houseNum.isEmpty()) {
			return;
		}
		Matcher matcher = null;
		Pattern pattern = Pattern.compile(".*[ａ-ｚＡ-Ｚ０-９～／]+.*");
		String log1 = "";
		for (int i=0;i<houseNum.length();i++) {
			matcher = pattern.matcher(String.valueOf(houseNum.charAt(i)));
			boolean find1 = matcher.find();
			if (!find1) {
				if (log1.length()>0) {
					log1 += ",";
				}
				log1 += houseNum.charAt(i);
			}
		}
		if (log1.length()>0) {
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"“门牌号”字段中含有“数字、字母、~、/”以外的字符" +log1 );
			return;
		}
		
		pattern = Pattern.compile(".*[０-９]+[ａ-ｚＡ-Ｚ]+.*");
		matcher = pattern.matcher(houseNum);
		boolean find2 = matcher.find();
		if (find2) {
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"字母不能出现在阿拉伯数字后面");
			return;
		}
		
		pattern = Pattern.compile(".*[ａ-ｚ]+.*");
		matcher = pattern.matcher(houseNum);
		boolean find3 = matcher.find();
		if (find3) {
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"字母不能是小写字母");
			return;
		}
		
		if (houseNum.length()>10) {
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"门牌号长度＞10");
			return;
		}
		
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
