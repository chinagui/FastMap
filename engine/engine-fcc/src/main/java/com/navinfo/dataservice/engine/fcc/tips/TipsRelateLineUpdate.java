package com.navinfo.dataservice.engine.fcc.tips;

import net.sf.json.JSONObject;

/** 
 * @ClassName: TipsRelateLineUpdate.java
 * @author y
 * @date 2017-4-13 上午9:54:08
 * @Description: TODO
 *  
 */
public class TipsRelateLineUpdate {
	
	private JSONObject json; //tips信息（solr）
	private JSONObject line1; //测线1
	private JSONObject line2; //测线2
	private  String sourceType="";

	/**
	 * @param json
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
* 		26类情报的tips 
* 		1.道路形状（测线） 2001
	2.道路挂接  1803
	3.立交(分层) 1116
	4.道路种别  1201
	5.道路方向（含时间段单方向道路） 1203
	6.车道数 1202
	7.SA  1205
	8.PA 1206
	9.匝道  1207
	10.IC\JCT   ?? 1211
	11.红绿灯（点属性）  1102
	12.收费站（点属性） 1107
	13.点限速（点属性） 1101
	14.车道信息（关系属性） 1301
	15.交通限制（关系属性）nk  1302
	16.上下分离 1501
	17.步行街 1507
	18.公交专用道  1508
	19.桥  1510
	20.隧道  1511
	21.施工 1514
	22.环岛  1601
	23.区域内道路  1604
	24.铁路道口 1702
	25.道路名 1901
	26.删除标记  2101
*/

	
	
	public void excute(){
		
		 switch (sourceType)
         {
             case "1101":// 点限速
                 updateSpeedLimitTips();
                 break;
             case "1102":// 红绿灯
                 updateTrafficSignalTips();
                 break;
             case "1116":// 红绿灯方位
                 updateGSCTips();
                 break;
             case "1107":// 收费站
                 updateTollgateTips();
                 break;
             case "1201":// 种别
            	 updateKindTips();
             case "1202":// 车道数
                 updateKindLaneTips();
                 break;
             case "1203":// 道路通行方向
                 updateLinkDirTips();
                 break;
             case "1302":// 普通交限
                 updateRestrictionTips();
                 break;
             case "1301"://车道信息
                 updateRdLaneTips();
                 break;
             case "1901"://道路名
                 updateRoadNameTips();
             //范围线类
             case "1601":// 环岛
            	 updatRrotaryIsland();
             case "1602":// 特殊交通类
             case "1607":// 风景路线
             case "1604":// 区域内道路
            	 updateRegionalRoad(); 
             case "1605":// POI连接路点
             case "1508":// 公交专用道
                 updateLineAttrTips();
                 break;
             case "1507":// 步行街
                 updateWalkStreetTips();
                 break;
             //起终点类
             case "1510":// 桥
            	 updateBridgeTips();
             case "1511":// 隧道
            	 updateTunnel();
             case "1514":// 施工
            	 updateConstruction();
             case "1702":// 铁路道口
                 updateRailwayCrossingTips();
                 break;
             case "1803":// 挂接
                 updateHookTips();
                 break;
             case "2101":// 删除道路标记
                 updateDelRoadMarkTips();
                 break;
             case "1501": // 上下线分离
                 updateUpDownSeparateLine();
                 break;
             // SA、PA、匝道转换停车场入口
             case "1205":
            	 updateSATips();
             case "1206":
            	 updatePATips();
             case "1207":
            	 updateRampTips();
         }
		
	}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:15:32
 */
private void updateDelRoadMarkTips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:15:17
 */
private void updateRoadNameTips() {
	// TODO Auto-generated method stub
	
}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午10:15:00
	 */
	private void updateRailwayCrossingTips() {
		// TODO Auto-generated method stub
		
	}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午10:13:39
	 */
	private void updateRegionalRoad() {
		// TODO Auto-generated method stub
		
	}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:12:49
 */
private void updatRrotaryIsland() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:10:36
 */
private void updateConstruction() {
	// TODO Auto-generated method stub
	
}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午10:10:04
	 */
	private void updateTunnel() {
		// TODO Auto-generated method stub
		
	}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:09:26
 */
private void updateBridgeTips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:08:54
 */
private void updateLineAttrTips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:08:42
 */
private void updateWalkStreetTips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:08:20
 */
private void updateUpDownSeparateLine() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:08:04
 */
private void updateRestrictionTips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:07:50
 */
private void updateRdLaneTips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:07:31
 */
private void updateSpeedLimitTips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:07:15
 */
private void updateTollgateTips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:06:58
 */
private void updateTrafficSignalTips() {
	// TODO Auto-generated method stub
	
}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午10:06:25
	 */
	private void updateRampTips() {
		// TODO Auto-generated method stub
		
	}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午10:06:23
	 */
	private void updatePATips() {
		// TODO Auto-generated method stub
		
	}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午10:06:20
 */
private void updateSATips() {
	// TODO Auto-generated method stub
	
}



	/**
 * @Description:TOOD
 * @author: y
 * @time:2017-4-13 上午9:59:13
 */
private void updateKindLaneTips() {
	// TODO Auto-generated method stub
	
}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午9:58:32
	 */
	private void updateHookTips() {
		// TODO Auto-generated method stub
		
	}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午9:58:25
	 */
	private void updateGSCTips() {
		// TODO Auto-generated method stub
		
	}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午9:58:15
	 */
	private void updateKindTips() {
		// TODO Auto-generated method stub
		
	}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-4-13 上午9:58:10
	 */
	private void updateLinkDirTips() {
		// TODO Auto-generated method stub
		
	}
}
	

	
	
	
	
