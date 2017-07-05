package com.navinfo.dataservice.engine.fcc.tips;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @ClassName: RelateTipsGuideAndAglUpdate.java
 * @author y
 * @date 2017-6-26 下午7:02:24
 * @Description: tips的引导坐标和角度计算
 */
public class RelateTipsGuideAndAglUpdate {

	private JSONObject json; // 关联tips信息（solr）

	private String sourceType = "";

	private List<JSONObject> linesAfterCut = null; // 打断后的测线

	public RelateTipsGuideAndAglUpdate(JSONObject json,
			List<JSONObject> linesAfterCut) {

		this.json = json;

		sourceType = json.getString("s_sourceType");

		this.linesAfterCut = linesAfterCut;
	}

	/**
	 * @Description:维护[显示坐标]、引导坐标、角度、[起点、终点、geo]
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-6-27 下午1:44:01
	 */
	public JSONObject excute() throws Exception {
		
		//说明：包含打断的线  才更新的哦  linesAfterCut
		
		//1.其他类型：修形不维护
		switch (sourceType) {
		case "2001"://测线
        case "1803"://挂接
        case "1806"://28.草图
        case "1116"://立交
        case "1102":// 11 .红绿灯   
        case "1901":// 25. 道路名 null
            return json;
           // return json;
        
        
        //2.打点类：维护g_guide维护：显示坐标到引导link的最近点
        case "1201":// 4.种别   f.id
        case "1203":// 5.道路通行方向    f.id
        case "1202":// 6. 车道数 f.id 
       // 7.SA、PA、匝道 f.id  g_location到f.id的
        case "1205": 
        case "1206": // 8 .PA f.id
        case "1207": // 9.匝道 f.id
        case "1211": // 10.IC\JCT  g_location到f.id
        case "1101":// 13. 点限速 f.id  g_location到f.id的垂足
        case "1706"://27.ADAS打点   g_location到f.id
        case "1702":// 24. 铁路道口  g_location到f.id
        case "2101":// 26.删除道路标记 null
            return updateSimpleFPointTips();
        	
        	
        case "1107":// 12.收费站  g_location到in.id的垂足
        case "1301":// 14. 车道信息 g_location到in.id的垂足
        case "1302":// 15. 普通交限 复杂的 g_location到in.id的垂足
            return updateSimpleInPointTips();
        	
            
        // 4.范围线类
        //范围线，测线打断后，范围线的g_location：根据修形后的link重新维护几何坐标）。范围线的引导坐标是，范围线组成线组成的多形的中心。geo根据修形后的link重新计算几何中心点.g_guide=geo
        case "1604":// 23. 区域内道路   范围线
        case "1601":// 22. 环岛     范围线  
            return updateAreaLine();
            
    
     
       
     
        //范围线，待补充~~~~~~~~
  
        case "1501": // 16. 上下线分离    ？？？ 起终点。  1.打断是否维护 显示坐标？？ 2.引导坐标就是起点。 起点已不在线上，其他线可能是rd_link
            return updateUpDownSeparateLine();
        case "1507":// 17.步行街     ？？？ 起终点
            return updateWalkStreetTips();
        case "1508":// 18.公交专用道     ？？？ 起终点
            return updateLineAttrTips();
            // 起终点类
        case "1510":// 19. 桥     ？？？ 起终点
            return updateBridgeTips();
        case "1511":// 20. 隧道     ？？？ 起终点
            return updateTunnel();
        case "1514":// 21.施工    ？？？ 起终点
            return updateConstruction();
            
       
	    default:
		    return null;
	}

	}

	/**
	 * @Description:打点tips维护1
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-7-4 下午8:16:36
	 */
	private JSONObject updateSimpleFPointTips() throws Exception {
		
		//打点tips的显示坐标.
		JSONObject g_location  =JSONObject.fromObject(json.getString("g_location")) ; 
		Point point = (Point) GeoTranslator.geojson2Jts(g_location);
		
		//打点tips的关联link_id
		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));
		JSONObject f = deep.getJSONObject("f");
		String lineId=f.getString("id");
		
		//测线的坐标
		JSONObject lineLocation=null;
		
		//测线的坐标，如果只有一条，则就就是当前测线的坐标。如果是多条。则是tips中记录的关联测线
		for (JSONObject line : linesAfterCut) {
			
			if(lineId.equals(line.getString("id"))){
				
				lineLocation=line.getJSONObject("g_location");
				
				break;
			}
		}
		
		Geometry lineGeo= GeoTranslator.geojson2Jts(lineLocation);
		
		JSONObject  g_guide= getNearLeastPoint(point, lineGeo);
		
		json.put("g_guide", g_guide);
		
		updateAgl(lineLocation, g_guide); //更新角度
		
		return json;
	}
	
	
	/**
	 * @Description:打点tips维护2
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-7-4 下午8:16:36
	 */
	private JSONObject updateSimpleInPointTips() throws Exception {
		
		//打点tips的显示坐标.
		JSONObject g_location  =JSONObject.fromObject(json.getString("g_location")) ; 
		Point point = (Point) GeoTranslator.geojson2Jts(g_location);
		
		//打点tips的关联link_id
		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));
		JSONObject f = deep.getJSONObject("in");
		String lineId=f.getString("id");
		
		//测线的坐标
		JSONObject lineLocation=null;
		
		//测线的坐标，如果只有一条，则就就是当前测线的坐标。如果是多条。则是tips中记录的关联测线
		for (JSONObject line : linesAfterCut) {
			
			if(lineId.equals(line.getString("id"))){
				
				lineLocation=line.getJSONObject("g_location");
				
				break;
			}
		}
		
		Geometry lineGeo= GeoTranslator.geojson2Jts(lineLocation);
		
		JSONObject  g_guide= getNearLeastPoint(point, lineGeo);
		
		json.put("g_guide", g_guide);
		
		double agl=calAngle(lineLocation, g_guide);
		
		if(deep.containsKey("agl")){
			
			deep.put("agl", agl);
			
			json.put("deep", deep);
		}
		
		return json;
	}
	
	

	/**
	 * @Description:TOOD
	 * @param point
	 * @param lineGeo
	 * @return
	 * @author: y
	 * @time:2017-7-4 下午8:44:12
	 */
	private JSONObject getNearLeastPoint(Point point, Geometry lineGeo) {
		//打点tips到 测线的最近的点
		Coordinate targetPoint = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(point,
	                0.00001, 5).getCoordinate(), GeoTranslator.transform(lineGeo, 0.00001, 5));
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{targetPoint.x, targetPoint.y});
        
		return geoPoint;
	}


	/**
	 * @Description:范围线，修形维护
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-6-27 下午1:46:33
	 */
	private JSONObject updateAreaLine() throws Exception {
		
		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));

		JSONArray f_array = deep.getJSONArray("f_array");
		
		int index=-1; //旧测线在数组中的位置，用户更新g_location(范围线使用)
		
		//只有一条说明就是原有测线本身，是修形，替换g_location
		if(linesAfterCut.size()==1){
			String oldRowkey= linesAfterCut.get(0).getString("id");
			int i=-1;
			
			for (Object object : f_array) {
				i++;
				JSONObject fInfo = JSONObject.fromObject(object); // 是个对象
				
				// 关联link是测线的
	            if(fInfo != null && fInfo.containsKey("type")) {
	                int type = fInfo.getInt("type");
	                String id = fInfo.getString("id");
	                if (type == 2 && id.equals(oldRowkey)) {
	                    index=i;
	                    break;
	                }
	            }
			}
		}
		
		// 1.如果是测线修形（未打断），则需要更新g_location.(已打断的，再打断时G_location已经更新)
		if (index>-1) {
			
			//1.更新g_location
			json=GLocationUpdate.updateAreaLineLocation(index,json,sourceType,linesAfterCut); //更新g_location
			
		}
		
		//2.如果是范围线：更新geo、更新g_guide=geo
		if("1601".equals(sourceType)||"1604".equals(sourceType)){
			
			JSONObject gLocation=json.getJSONObject("g_location");
			
			Geometry geometry=GeoTranslator.geojson2Jts(gLocation);
			
			Point point=(Point)GeometryUtils.getPointFromGeo(geometry); //中心
			
			JSONObject pointGeo = GeoTranslator.jts2Geojson(point);
			
			//2.1更新geo
			deep.put("geo", pointGeo);
			
			json.put("deep", deep); //1.修改deep
			
			//2.2更新guide
			json.put("g_guide",pointGeo);
			
		}

		return json;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:31
	 */
	private JSONObject updateConstruction() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:25
	 */
	private JSONObject updateTunnel() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:23
	 */
	private JSONObject updateBridgeTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:18
	 */
	private JSONObject updateLineAttrTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:15
	 */
	private JSONObject updateWalkStreetTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:10
	 */
	private JSONObject updateUpDownSeparateLine() {
		// TODO Auto-generated method stub
		return null;
	}





	/**
	 * @Description:更新tips的角度
	 * @param lineLocation
	 * @param guide
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-6-27 上午10:43:24
	 */
	private JSONObject updateAgl(JSONObject lineLocation, JSONObject guide)
			throws Exception {

		JSONObject deep = JSONObject.fromObject(json.getString("deep"));

		if (deep != null && deep.containsKey("agl")) {

			double agl = calAngle(lineLocation, guide);

			deep.put("agl", agl);

			json.put("deep", deep);
		}
		return json;
	}

	/**
	 * @Description:计算tips的角度
	 * @param lineLocation
	 *            ：测线的显示坐标
	 * @param guide
	 *            ：引导坐标
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-6-27 上午10:42:42
	 */
	private double calAngle(JSONObject lineLocation, JSONObject guide)
			throws Exception {
		double calAngle = 0;

		Point guidePoint = (Point) GeoTranslator.geojson2Jts(guide);

		Geometry line = GeoTranslator.geojson2Jts(lineLocation);

		// 查询和引导坐标最近的一条线段
		LineString nearLeastline = getLineComposedByRecentTwoPoint(line,
				guidePoint);

		double[] points = new double[4];
		Coordinate[] cs = nearLeastline.getCoordinates();
		points[0] = cs[0].x;
		points[1] = cs[0].y;
		points[2] = cs[1].x;
		points[3] = cs[1].y;

		calAngle = TipsGeomUtils.calAngle(points);

		return calAngle;
	}

	/**
	 * 找某条link上离指定点位最近的两个形状点
	 * 
	 * @param guidePoint
	 * @param line
	 * @param rdLink
	 * @param geoPoint
	 * @return LineString 返回两形状点组成的线
	 * @throws Exception
	 */
	protected LineString getLineComposedByRecentTwoPoint(Geometry line,
			Point guidePoint) throws Exception {
		Double minDis = null;
		LineString newLine = null; // 离引导坐标最近的两个形状点的组成的线段

		int geoNum = line.getNumGeometries();
		for (int i = 0; i < geoNum; i++) {
			Geometry subGeo = line.getGeometryN(i);
			if (subGeo instanceof LineString) {
				Coordinate[] c_array = subGeo.getCoordinates();
				int num = -1; // 存放离的最近的形状点的顺序号
				for (int k = 0; k < c_array.length; k++) {
					double tmpDis = GeometryUtils.getDistance(
							guidePoint.getCoordinate(), c_array[k]);
					if (minDis == null || tmpDis < minDis) {
						minDis = tmpDis;
						num = k;
					}
				}
				Coordinate c_start = null;
				Coordinate c_end = null;

				if (num != -1) {
					if (num == 0) {
						c_start = c_array[0];
						c_end = c_array[1];
					} else if (num == c_array.length - 1) {
						c_start = c_array[c_array.length - 2];
						c_end = c_array[num];
					} else {
						double dis_last = GeometryUtils.getDistance(
								guidePoint.getCoordinate(), c_array[num - 1]);
						double dis_next = GeometryUtils.getDistance(
								guidePoint.getCoordinate(), c_array[num + 1]);
						if (dis_last < dis_next) {
							c_start = c_array[num - 1];
							c_end = c_array[num];
						} else {
							c_start = c_array[num];
							c_end = c_array[num + 1];
						}
					}

					Coordinate[] coordinates = new Coordinate[] { c_start,
							c_end };

					newLine = JtsGeometryFactory.createLineString(coordinates);
				}
			}
		}

		return newLine;
	}

	public static void main(String[] args) {

		SolrController conn = new SolrController();

		// 关联的tips
		JSONObject solrIndex;
		try {
			solrIndex = conn.getById("0216045BC2F25E98B54D4991F63B57FD9EE7F6");
			// 测线
			JSONObject solrLine = conn
					.getById("022001091D1890CA8849EC9908F32A1667C2C2");

			JSONObject lineLocation = JSONObject.fromObject(solrLine
					.getString("g_location"));

			JSONObject guide = JSONObject.fromObject(solrIndex
					.getString("g_guide"));

			RelateTipsGuideAndAglUpdate u = new RelateTipsGuideAndAglUpdate(
					solrIndex,null);
			u.updateAgl(lineLocation, guide);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
