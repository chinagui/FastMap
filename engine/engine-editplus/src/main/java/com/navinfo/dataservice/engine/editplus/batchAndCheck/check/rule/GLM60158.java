package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * 	GLM60158		地铁、磁悬浮名称检查		DHM
	检查条件：
	  非删除POI对象
	检查原则：
	  地铁（230111）、磁悬浮（230114）的标准化官方中文名称(name_class=1，name_type=1，lang_code=CHI或CHT)
	  中如果包含括号，括号内不能出现一、二、三、四、五、六、七、八、九、十、百，否则log：地铁、磁悬浮名称括号内不能出现一、二、三、四、……十、百的汉字！
 * @author sunjiawei
 *
 */
public class GLM60158 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName br=poiObj.getOfficeStandardCHName();
			if(br==null){return;}
			String name=br.getName();
			boolean flag = false;
			if(name.contains("（")||name.contains("）")){
				 flag = true;
				 name = name.substring(name.indexOf("（")+1, name.indexOf("）"));
			}else if(name.contains("［")||name.contains("］")){
				 flag = true;
				 name = name.substring(name.indexOf("［")+1, name.indexOf("］"));
			}else if(name.contains("｛")||name.contains("｝")){
				 flag = true;
				 name = name.substring(name.indexOf("｛")+1, name.indexOf("｝"));
			} 

			if(flag){
				String kindCode= poi.getKindCode();
				if(kindCode.equals("230111")||kindCode.equals("230114")){
					String[] words = {"一","二","三","四","五","六","七","八","九","十","百"};
					List<String> errorList = new ArrayList<String>();
					for (String word:words) {
						if(name.contains(word)){
							errorList.add(word);
						}
					}
					if(errorList!=null&&errorList.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "地铁、磁悬浮名称括号内不能出现:“"
								+errorList.toString().replace("[", "").replace("]", "")+"”的汉字！");
						return;
					}
						
				}
			}
		}
	}
	

}
