package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-GLM60173	英文名不能以THE/No.结尾；No.的“.”前后不能有空格	DHM	
 * 检查条件：该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 1、英文名字段不能以“半角空格+THE（不区分大小写）”结尾，否则报出：英文名全称和简称都不能以THE结尾
 * 2.英文名字段不能以“半角空格+No.（不区分大小写）”结尾，否则报出：英文名全称和简称都不能以No.结尾
 * 3.英文名字段中含有No.的“.”点的前后不能存在空格，否则报出：No.的“.”点的前后不能存在空格
 * 检查名称：官方英文名称（NAME_TYPE={1,2}，NAME_CLASS=1，LANG_CODE=ENG）
 * @author zhangxiaoyi
 */
public class FMGLM60173 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if((nameTmp.getNameType()==1||nameTmp.getNameType()==2)
						&&nameTmp.getNameClass()==1&&nameTmp.isEng()){
					String nameStr=nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					if ((nameStr.toUpperCase()).endsWith(" THE")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"英文名全称和简称都不能以THE结尾");
                        return;}
					if ((nameStr.toUpperCase()).endsWith(" NO.")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"英文名全称和简称都不能以No.结尾");
                        return;}
					Pattern p1 = Pattern.compile(".*[nN]+[oO]+( )+[.]+.*");
					Matcher m1 = p1.matcher(nameStr);
					Pattern p2 = Pattern.compile(".*[nN]+[oO]+[.]+( )+.*");
					Matcher m2 = p2.matcher(nameStr);
                    if(m1.matches()||m2.matches()){
                    	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"No.的“.”点的前后不能存在空格");
                    	return;}
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
