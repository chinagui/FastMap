package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-YW-20-090	F-1英文全称和英文简称存在首字母小写	DHM
 * 检查条件：
 *     以下条件其中之一满足时，需要进行检查：
 *     (1)存在IX_POI_NAME新增；
 *     (2)存在IX_POI_NAME修改或KIND_CODE字段修改；
 *     检查原则：
 *     1）官方原始英文名中存在"" a"","" b"","" c"","" d"","" e"","" f"","" g"","" h"","" i"","" j"","" k"","" l"",
 *     "" m"","" n"","" o"","" p"","" q"","" r"","" s"","" t"","" u"","" v"","" w"","" x"","" y"","" z""时，
 *     报log1:官方原始英文名中***（按实际单词报）首字母小写
 *     2）官方标准化英文名中存在"" a"","" b"","" c"","" d"","" e"","" f"","" g"","" h"","" i"","" j"","" k"","" l"",
 *     "" m"","" n"","" o"","" p"","" q"","" r"","" s"","" t"","" u"","" v"","" w"","" x"","" y"","" z""时，
 *     报log2：官方标准化英文名中***（按实际单词报）首字母小写
 *     备注：1、如果单词本身为如下单词则不报错
 *     1）介词：in，on，into，to，of，at，from，with，by，for，as，than，after，since，until
 *     2）连接词：and，or
 *     3）a，an，the
 *     2、对于首个单词的首个字母也需要检查是否为小写
 * @author zhangxiaoyi
 */
public class FMYW20090 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
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
						//System.out.println(subname.substring(0, 1));
						if(nameTmp.isOriginName()&&p.matcher(subname).matches()
								&&!wordList.contains(subname)
								&&!subname.substring(0, 1).equals(subname.substring(0, 1).toUpperCase())){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方原始英文名中"+subname+"首字母小写");
							return;
						}
						if(nameTmp.isStandardName()&&p.matcher(subname).matches()
								&&!wordList.contains(subname)
								&&!subname.substring(0, 1).equals(subname.substring(0, 1).toUpperCase())){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方标准化英文名中"+subname+"首字母小写");
							return;
						}
					}					
				}
			}
		}
	}
	
	/**
	 * 以下条件其中之一满足时，需要进行检查：
	 *  (1)存在IX_POI_NAME新增；
	 *  (2)存在IX_POI_NAME修改或修改分类存在；
	 * @param poiObj
	 * @return true满足检查条件，false不满足检查条件
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String newKindCode=poi.getKindCode();
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			if(!newKindCode.equals(oldKindCode)){return true;}
		}
		//(1)存在IX_POI_NAME的新增；(2)存在IX_POI_NAME的修改；
		List<IxPoiName> names = poiObj.getIxPoiNames();
		for (IxPoiName br:names){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}}
		}
		return false;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
