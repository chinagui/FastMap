package com.navinfo.dataservice.engine.fcc.tips;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;

/**
 * @ClassName: TipsRelateLineUpdate.java
 * @author y
 * @date 2017-4-13 上午9:54:08
 * @Description: TODO
 * 
 */
public class TipsRelateLineUpdate {

	private JSONObject json; // tips信息（solr）
	private JSONObject line1; // 测线1
	private JSONObject line2; // 测线2
	private String sourceType = "";

	/**
	 * @param json
	 *            :要维护的tips
	 * @param line1
	 * @param line2
	 */
	public TipsRelateLineUpdate(JSONObject json, JSONObject line1,
			JSONObject line2) {
		super();
		this.json = json;
		this.line1 = line1;
		this.line2 = line2;
		sourceType = json.getString("s_sourceType");
	}

	/*
	 * 26类情报的tips 1.道路形状（测线） 2001 2.道路挂接 1803 3.立交(分层) 1116 4.道路种别 1201
	 * 5.道路方向（含时间段单方向道路） 1203 6.车道数 1202 7.SA 1205 8.PA 1206 9.匝道 1207 10.IC\JCT
	 * ?? 1211 11.红绿灯（点属性） 1102 12.收费站（点属性） 1107 13.点限速（点属性） 1101 14.车道信息（关系属性）
	 * 1301 15.交通限制（关系属性）nk 1302 16.上下分离 1501 17.步行街 1507 18.公交专用道 1508 19.桥
	 * 1510 20.隧道 1511 21.施工 1514 22.环岛 1601 23.区域内道路 1604 24.铁路道口 1702 25.道路名
	 * 1901 26.删除标记 2101
	 */

	public JSONObject excute() {
		
		switch (sourceType) {
		case "1101":// 点限速
			return updateSpeedLimitTips();
		case "1102":// 红绿灯
			return updateTrafficSignalTips();
		case "1116":// 红绿灯方位
			return updateGSCTips();
		case "1107":// 收费站
			return updateTollgateTips();
		case "1201":// 种别
			return updateKindTips();
		case "1202":// 车道数
			return updateKindLaneTips();
		case "1203":// 道路通行方向
			return updateLinkDirTips();
		case "1302":// 普通交限
			return updateRestrictionTips();
		case "1301":// 车道信息
			return updateRdLaneTips();
		case "1901":// 道路名
			return updateRoadNameTips();
			// 范围线类
		case "1601":// 环岛
		case "1602":// 特殊交通类
		case "1607":// 风景路线
			return updatRrotaryIsland();
		case "1604":// 区域内道路
		case "1605":// POI连接路点
			return updateRegionalRoad();
		case "1508":// 公交专用道
			return updateLineAttrTips();
		case "1507":// 步行街
			return updateWalkStreetTips();
		// 起终点类
		case "1510":// 桥
			return updateBridgeTips();
		case "1511":// 隧道
			return updateTunnel();
		case "1514":// 施工
			return updateConstruction();
		case "1702":// 铁路道口
			return updateRailwayCrossingTips();
		case "1803":// 挂接
			return updateHookTips();
		case "2101":// 删除道路标记
			return updateDelRoadMarkTips();
		case "1501": // 上下线分离
			return updateUpDownSeparateLine();
		// SA、PA、匝道转换停车场入口
		case "1205":
			return updateSATips();
		case "1206":
			return updatePATips();
		case "1207":
			return updateRampTips();
		default:
			return null;
		}
		

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:15:32
	 */
	private JSONObject updateDelRoadMarkTips() {
		
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:15:17
	 */
	private JSONObject updateRoadNameTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:15:00
	 */
	private JSONObject updateRailwayCrossingTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:13:39
	 */
	private JSONObject updateRegionalRoad() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:12:49
	 */
	private JSONObject updatRrotaryIsland() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:10:36
	 */
	private JSONObject updateConstruction() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:10:04
	 */
	private JSONObject updateTunnel() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:09:26
	 */
	private JSONObject updateBridgeTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:08:54
	 */
	private JSONObject updateLineAttrTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:08:42
	 */
	private JSONObject updateWalkStreetTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:08:20
	 */
	private JSONObject updateUpDownSeparateLine() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:08:04
	 */
	private JSONObject updateRestrictionTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:07:50
	 */
	private JSONObject updateRdLaneTips() {
		return null;

	}

	/**
	 * @Description:1101 点限速，关联测线修改
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:07:31
	 */
	private JSONObject updateSpeedLimitTips() {

		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));

		JSONObject f = deep.getJSONObject("f");

		// 关联link是测线的
		if (f != null && f.getInt("type") == 2) {
			
			String id=getNearlestLineId();
			
			f.put("id", id);

			deep.put("f", f);
			
			json.put("deep", deep);
			
			return json;
		}
		//关联的不是测线，则不返回
		else{
			return null;
		}
		
		

	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-4-17 下午5:55:08
	 */
	private String getNearlestLineId() {
		String id;
		//tip的引导坐标
		JSONObject geometryTips = JSONObject.fromObject(this.line1
				.getString("geometry"));

		JSONObject g_guide = geometryTips.getJSONObject("g_guide");
		
		Point point=(Point)GeoTranslator.geojson2Jts(g_guide);
		
		//两个线的显示坐标

		JSONObject geometry1 = JSONObject.fromObject(this.line1
				.getString("geometry"));

		JSONObject g_location1 = geometry1.getJSONObject("g_location");

		Geometry geo1 = GeoTranslator.geojson2Jts(g_location1);

		
		JSONObject geometry2 = JSONObject.fromObject(this.line2
				.getString("geometry"));

		JSONObject g_location2 = geometry2.getJSONObject("g_location");

		Geometry geo2 = GeoTranslator.geojson2Jts(g_location2);
		
		//计算 tips的引导坐标到显示坐标的距离，取最近的测线作为引导link
		
		if(point.distance(geo1)<=point.distance(geo2)){
			
			id=line1.getString("id");
		}else{
			id=line1.getString("id");
		}
		return id;
	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:07:15
	 */
	private JSONObject updateTollgateTips() {
		return null;

	}

	/**
	 * @Description:"1102":// 红绿灯
	 * [f_array].f.id
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:06:58
	 */
	private JSONObject updateTrafficSignalTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:06:25
	 */
	private JSONObject updateRampTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:06:23
	 */
	private JSONObject updatePATips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午10:06:20
	 */
	private JSONObject updateSATips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午9:59:13
	 */
	private JSONObject updateKindLaneTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午9:58:32
	 */
	private JSONObject updateHookTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午9:58:25
	 */
	private JSONObject updateGSCTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午9:58:15
	 */
	private JSONObject updateKindTips() {
		return null;

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return 
	 * @time:2017-4-13 上午9:58:10
	 */
	private JSONObject updateLinkDirTips() {
		return null;

	}
}
