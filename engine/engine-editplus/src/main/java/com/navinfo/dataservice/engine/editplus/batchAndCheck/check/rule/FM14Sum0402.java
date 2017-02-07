package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-14Sum-04-02
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查；
 * 检查原则：
 * 根据分类业务逻辑控制表，查询出不能标记C级的设施标记了C级。
 * 1、在分类业务逻辑控制表中查询POI的分类对应的level，如果查询出的level不能为C级POI，而数据中的level是C级，报log：非C级分类标记C级
 * 备注：当kindcode="120101、230210、230213、230214"时不检查
 * @author zhangxiaoyi
 */
public class FM14Sum0402 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}
			if(kind.equals("120101")||kind.equals("230210")||kind.equals("230213")||kind.equals("230214")){return;}
			
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
