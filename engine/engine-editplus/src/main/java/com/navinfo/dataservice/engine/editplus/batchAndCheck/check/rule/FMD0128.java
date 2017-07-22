package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 *  FM-D01-28	POI别名曾用名中文名称格式检查		DHM	
	检查条件：
	非删除POI对象；
	检查原则：
	  1、检查POI别名曾用名中文名称中“（”与“）”、中括号“［”和“］”、大括号“｛”、“｝”、书名号“《”、“》”应成对出现，（官方别名曾用名中文名称都是全角,如果存在半角，则认为是不成对）报log1：POI中文名称中括号需要成对出现！
	  2、括号“（”和“）”、中括号“［”和“］”、大括号“｛”、“｝”、书名号“《”、“》”中间必须有内容，报log2：POI中文名称中括号中必须存在内容！
	  3、不允许括号嵌套(以上几种括号均包含)，否则报log3：POI中文名称中不能出现括号嵌套括号情况！
	检查名称：别名曾用名中文名称（name_type=1，name_class={３}，langCode=CHI或CHT）
备注：此处只能是全角的括号
 * @author sunjiawei
 */
public class FMD0128 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			for(IxPoiName nameTmp:poiObj.getIxPoiNames()){
				if(nameTmp.isCH()&&nameTmp.isStandardName()&&nameTmp.isAliasName()){
					String name=nameTmp.getName();
					if(name.contains("(")||name.contains(")")||name.contains("[")||name.contains("]")
							||name.contains("{")||name.contains("}")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI中文名称中存在半角空格");
						return;
					}
					String errorMsg=null;
					if(name.contains("（")||name.contains("）")){
						errorMsg = CheckUtil.isRightKuohao(name, "(", ")");
					}else if(name.contains("［")||name.contains("］")){
						errorMsg = CheckUtil.isRightKuohao(name, "[", "]");
					}else if(name.contains("｛")||name.contains("｝")){
						errorMsg = CheckUtil.isRightKuohao(name, "{", "}");
					}else if(name.contains("《")||name.contains("》")){
						errorMsg = CheckUtil.isRightKuohao(name, "《", "》");
					}
					
					if(errorMsg!=null){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI中文名称中"+errorMsg);
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
