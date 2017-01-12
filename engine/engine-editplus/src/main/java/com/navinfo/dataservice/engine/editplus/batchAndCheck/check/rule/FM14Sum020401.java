package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-14Sum-02-04-01
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查；
 * 检查原则：
 * 1、名称（name）（官方原始中文名）中包含“哈根达斯、Dairy Queen、DQ、冰雪皇后、Baskin Robbins、酷圣石”，
 * 但分类kindCode不为冷饮店（110302），报log1：冷饮店分类错误
 * 2、名称（name）（官方原始中文名）中包含“哈根达斯、Dairy Queen、DQ、冰雪皇后、Baskin Robbins、酷圣石”，
 * 但分类kindCode为冷饮店（110302），但foodtype不为“3014”，报Log2：冷饮店风味类型错误
 * @author zhangxiaoyi
 */
public class FM14Sum020401 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}
			IxPoiName name = poiObj.getOfficeOriginCHName();
			if(name==null){return;}
			String nameStr = name.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			if(!nameStr.contains("哈根达斯")&&!nameStr.toLowerCase().contains("dairy queen")
					&&!nameStr.toLowerCase().contains("dq")&&!nameStr.contains("冰雪皇后")
					&&!nameStr.toLowerCase().contains("baskin robbins")&&!nameStr.contains("酷圣石")){return;}
			if(!kind.equals("110302")){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "冷饮店分类错误");
				return;
			}
			List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
			if(restList==null||restList.isEmpty()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "冷饮店风味类型错误");
				return;
			}
			for(IxPoiRestaurant res:restList){
				String foodtype=res.getFoodType();
				if(foodtype==null||foodtype.isEmpty()){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "冷饮店风味类型错误");
					return;
				}
				if(!foodtype.equals("3014")){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "冷饮店风味类型错误");
					return;}
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
