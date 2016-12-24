package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-GLM60211	NAME非空检查	DHM	
 * 检查条件：该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则：当LANG_CODE=""CHI""或""CHT""时，NAME不能为空，否则报出：**名称不能为空
 * 备注：**是NAME_TYPE+NAME_CLASS+LANG_CODE的中文描述（如：标准化官方英文，标准化简称中文等等）
 * @author zhangxiaoyi
 */
public class FMGLM60211 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			for(IxPoiName nameTmp:poiObj.getIxPoiNames()){
				if(nameTmp.isCH()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
								getTypeName(nameTmp.getNameType())+getClassName(nameTmp.getNameClass())+"中文名称不能为空");
						return;
					}
				}
			}
		}
	}
	
	//1表示标准，2表示原始
    private String getTypeName(int nameType){
        if (1==nameType){return "标准";}
        if (2==nameType){return "原始";}
        return null;
    }

    private String getClassName(int nameClass){
        if (1==nameClass){return "官方";}
        if (3==nameClass){return "别名";}
        if (4==nameClass){return "菜单";}
        if (5==nameClass){return "简称";}
        if (6==nameClass){return "曾用名";}
        if (7==nameClass){return "古称";}
        if (8==nameClass){return "站点线路名";}
        if (9==nameClass){return "子冠父名";}
        return null;
    }
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
