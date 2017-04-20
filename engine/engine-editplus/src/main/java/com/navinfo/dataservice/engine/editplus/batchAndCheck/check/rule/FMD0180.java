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
 *  FM-D01-80	B-11官方原始英文合法字符集前后空格检查	DHM
	检查条件：
	 非删除POI对象
	检查原则：
	官方原始英文名中合法字符（不查括号，不查No.中的点）前存在空格，后不存在空格或前不存在空格，后存在空格时，报log：英文名合法字符前后空格错误！
	备注：符号-_/:;'"~^.,?!*<>$%&#@+
	备注：
	Bang&Bang    不用报log；
	Bang &Bang   要报log；
	Bang& Bang   要报log；
	Bang & Bang  不报log
	检查名称：官方原始英文名称（name_type=2，name_class=1，lang_Code=ENG）
 * @author sunjiawei
 */
public class FMD0180 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			Pattern p = Pattern.compile(".*[^ ]+[\\-_/:;'\"~^.,?!*<>$%&#@+]+ .*");
			Pattern p1 = Pattern.compile(".* +[\\-_/:;'\"~^.,?!*<>$%&#@+]+[^ ]+.*");
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()
						&&nameTmp.isOriginName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					String nameLow=name.toLowerCase();
					String[] nameList = nameLow.split("no.");
					for(String subname:nameList){
						if(p.matcher(subname).matches()||p1.matcher(subname).matches()){
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

}
