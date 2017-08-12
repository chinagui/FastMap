package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *  GLM60437 		中文数字制作别名检查		DHM
	检查对象：
	非删除POI对象
	检查原则：
	官方标准化名称中包含三个及三个以上连续的中文数字零到九（包含中文数字〇）的POI，
	如果没有制作包含阿拉伯数字０~９的别名(name_type=1,name_class=3,lang_code=CHI或CHT)，
	则报log:名称中包含中文数字组合，但是未制作别名!
	此处曾用名不检查；
 * @author sunjiawei
 */
public class GLM60437 extends BasicCheckRule {

//	@Override
//	public void runCheck(BasicObj obj) throws Exception {
//		if(obj.objName().equals(ObjectName.IX_POI)){
//			IxPoiObj poiObj=(IxPoiObj) obj;
//			IxPoi poi=(IxPoi) poiObj.getMainrow();
//			List<IxPoiName> names = poiObj.getIxPoiNames();
//			if(names==null||names.size()==0){return;}
//			for(IxPoiName nameTmp:names){
//				if(nameTmp.isCH()){
//					String name=nameTmp.getName();
//					IxPoiName officeStandardCHName = poiObj.getOfficeStandardCHName();
//					if(name==null||name.isEmpty()){continue;}
//					if(officeStandardCHName==null){return;}
//					if(nameTmp.isUsedName()){continue;}
//					Pattern p = Pattern.compile(".*[〇一二三四五六七八九十]{3,}.*"); //三个及三个以上连续的中文数字零到九（包含中文数字〇）
//					String oscName = officeStandardCHName.getName();
//					boolean flag = false;
//					if(p.matcher(oscName).matches()){
//						flag = true;
//					}
//					IxPoiName aliasCHIName = poiObj.getAliasCHIName(0);
//					if(aliasCHIName==null){//没有别名
//						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
//						return;
//					}
//					if(nameTmp.isAliasName()){
//						if(flag){
//							Pattern p1 = Pattern.compile(".*[\uFF10-\uFF19]{3,}.*"); //三个及三个以上连续的阿拉伯数字“０到９”（全角）
//							String aliasName = nameTmp.getName();
//							if(!p1.matcher(aliasName).matches()){
//								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
//								return;
//							}
//							
//						}
//					}
//				}
//			}
//		}
//	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.isOfficeName()&&nameTmp.isStandardName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					
					Pattern p = Pattern.compile(".*[〇零一二三四五六七八九]{3,}.*"); //三个及三个以上连续的中文数字零到九（包含中文数字〇）
					boolean flag = false;
					if(p.matcher(name).matches()){
						flag = true;
					}
					
					if(flag){
						List<IxPoiName> aliasCHINames = poiObj.getAliasCHIName();
						if(aliasCHINames==null||aliasCHINames.isEmpty()){	//没有别名
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
						}
						boolean noMatch = true;
						for (IxPoiName aliasCHIName : aliasCHINames) {
							Pattern p1 = Pattern.compile(".*[\uFF10-\uFF19]{3,}.*"); //三个及三个以上连续的阿拉伯数字“０到９”（全角）
							String aliasName = aliasCHIName.getName();
							if(p1.matcher(aliasName).matches()){
								noMatch=false;
							}
						}
						if(noMatch){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
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

	/**
	 * 全角转中文
	 * @param str
	 * @return
	 */
	public String SBC2Chinese(String str){
		char[] array = str.toCharArray();
		String convertStr = "";
		for (char c : array) {
			switch(c)
			{
				case '１':convertStr+="一";break;
				case '２':convertStr+="二";break;
				case '３':convertStr+="三";break;
				case '４':convertStr+="四";break;
				case '５':convertStr+="五";break;
				case '６':convertStr+="六";break;
				case '７':convertStr+="七";break;
				case '８':convertStr+="八";break;
				case '９':convertStr+="九";break;
				case '０':convertStr+="〇";break;
			}
		}
		return convertStr;
	}
	
	
}
