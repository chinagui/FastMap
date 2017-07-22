package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 		FM-D01-94	F-1官方原始英文称存在首字母小写	DHM
 *      检查条件：
		非删除POI对象
		检查原则：
		官方原始英文名中存在" a"," b"," c"," d"," e"," f"," g"," h"," i"," j"," k"," l"," m"," n"," o"," p"," q"," r"," s"," t"," u"," v"," w"," x"," y"," z"开头的单词时，报log：英文名中***（按实际单词报）首字母小写！
		备注：
		1、如果单词本身为如下单词则不报错
		1）介词：in，on，into，to，of，at，from，with，by，for，as，than，after，since，until
		2）连接词：and，or
		3）a，an，the
		2、对于首个单词的首个字母也需要检查是否为小写
		检查名称：官方原始英文名称（name_type=2，name_class=1，lang_Code=ENG）
 * @author sunjiawei
 */
public class FMD0194 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			Pattern p = Pattern.compile("^[abcdefghijklmnopqrstuvwxyz].*");
			List<String> wordList=Arrays.asList("in","on","into","to","of","at","from","with","by","for","as","than",
					"after","since","until","and","or","a","an","the");
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					for(String subname:name.split(" ")){
						if(nameTmp.isOriginName()&&p.matcher(subname).matches()
								&&!wordList.contains(subname)
								&&!subname.substring(0, 1).equals(subname.substring(0, 1).toUpperCase())){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方原始英文名中"+subname+"首字母小写");
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

}
