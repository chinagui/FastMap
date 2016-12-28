package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-YW-20-033	POI英文名中虚词大小写检查	DHM	
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 1）介词：in，on，into，to，of，at，from，with，by，for，as，than，after，since，until
 * 2）连接词：and，or
 * 3）a，an，the
 * 虚词出现在英文名称开头，则首字母大写，其他情况应小写，否则报
 * log1：英文名中虚词“**”首字母应小写
 * log2：英文名中虚词“**”首字母应大写
 * 检查名称：官方英文名称（NAME_TYPE={1,2}，NAME_CLASS=1，LANG_CODE=ENG） 
 * @author zhangxiaoyi
 */
public class FMYW20033 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			List<String> wordList=Arrays.asList("in","on","into","to","of","at","from","with","by","for","as","than",
					"after","since","until","and","or","a","an","the");
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.getNameClass()==1&&(nameTmp.getNameType()==1||nameTmp.getNameType()==2)){
					String nameStr = nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					String[] nameList = nameStr.split(" ");
					String firstWord=nameList[0];
					if(wordList.contains(firstWord.toLowerCase())){
						//首字母大写
						String rightWord=String.valueOf(firstWord.toCharArray()[0]).toUpperCase()+firstWord.substring(1);
						if(!rightWord.equals(firstWord)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "英文名中虚词“"+firstWord.toLowerCase()+"”首字母应大写");
							return;
						}
					}
					for(int i=0;i<nameList.length;i++){
						if(i==0){continue;}
						String subname=nameList[i];
						if(wordList.contains(subname.toLowerCase())&&!subname.toLowerCase().equals(subname)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "英文名中虚词“"+subname.toLowerCase()+"”首字母应小写");
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
