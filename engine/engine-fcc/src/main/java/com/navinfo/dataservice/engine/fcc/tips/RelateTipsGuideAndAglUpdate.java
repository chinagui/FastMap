package com.navinfo.dataservice.engine.fcc.tips;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

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

	public JSONObject excute() throws Exception {

		JSONObject lineLocation = new JSONObject(); // 关联测线的显示坐标

		lineLocation = maitanGuide();

		JSONObject guide = JSONObject.fromObject(json.getString("g_guide")); // 得到引导坐标

		json = updateAgl(lineLocation, guide);

		return json;

	}

	/**
	 * @Description:维护引导坐标  存在疑问 在不修改
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:44:01
	 */
	private JSONObject maitanGuide() {
		
		//说明：包含打断的线  才更新的哦  linesAfterCut
		switch (sourceType) {
        case "1803":// 2.挂接 null
            return updateHookTips();
        case "1116":// 3.立交 [f_array].id  有疑问？？？ 多条线的交点（拿不到其他关联先的几何） 是否需要同时维护显示坐标？
            return updateGSCTips();
        case "1201":// 4.种别  g_location到f.id的垂足
            return updateKindTips();
        case "1203":// 5.道路通行方向  g_location到f.id的垂足
            return updateLinkDirTips();
        case "1202":// 6. 车道数 f.id  g_location到f.id的垂足
            return updateKindLaneTips();
            // 7.SA、PA、匝道 f.id  g_location到f.id的垂足
        case "1205": 
            return updateSATips();
        case "1206": // 8 .PA f.id
            return updatePATips();
        case "1207": // 9.匝道 f.id
            return updateRampTips();
        case "1211": // 10.IC\JCT  g_location到f.id的垂足
            return updateJCTTips();
        case "1102":// 11 .红绿灯   ？？？显示坐标就是引导坐标。（显示坐标已不原来的线上，测线只是其中一条）如何计算   link是不受控。。没有引导link。 这个不需要维护引导坐标
            return updateTrafficSignalTips();
        case "1107":// 12.收费站  g_location到in.id的垂足
            return updateTollgateTips();
        case "1101":// 13. 点限速 f.id  g_location到f.id的垂足
            return updateSpeedLimitTips();
        case "1301":// 14. 车道信息 g_location到in.id的垂足
            return updateRdLaneTips();
        case "1302":// 15. 普通交限 复杂的 g_location到in.id的垂足
            return updateRestrictionTips();
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
            // 范围线类
        case "1601":// 22. 环岛   ？？？  范围线  （范围线，测线打断后，范围线的g_location要不要维护？？ 怎么维护？）。范围线的引导坐标是，范围线组成线组成的多形的中心。geo是否需要维护
            return updateFArray_Id();
        case "1604":// 23. 区域内道路  ？？？  范围线
            return updateFArray_Id();
        case "1702":// 24. 铁路道口  g_location到f.id的垂足
            return updateSimpleF();
        case "1901":// 25. 道路名 null
            return null;
        case "2101":// 26.删除道路标记 null
            return null;
        case "1706"://27.ADAS打点   g_location到f.id的垂足
            return updateSimpleF();
        case "1806"://28.草图
            return null;
	    default:
		    return null;
	}

	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:39
	 */
	private JSONObject updateSimpleF() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:33
	 */
	private JSONObject updateFArray_Id() {
		// TODO Auto-generated method stub
		return null;
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
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:08
	 */
	private JSONObject updateRestrictionTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:04
	 */
	private JSONObject updateRdLaneTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:46:02
	 */
	private JSONObject updateSpeedLimitTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:58
	 */
	private JSONObject updateTollgateTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:52
	 */
	private JSONObject updateTrafficSignalTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:48
	 */
	private JSONObject updateJCTTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:43
	 */
	private JSONObject updateRampTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:41
	 */
	private JSONObject updatePATips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:37
	 */
	private JSONObject updateSATips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:32
	 */
	private JSONObject updateKindLaneTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:22
	 */
	private JSONObject updateLinkDirTips() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:18
	 */
	private JSONObject updateKindTips() {
		
		return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:14
	 */
	private JSONObject updateGSCTips() {
		
		
		return null;
	}

	/**
	 * @Description:不维护
	 * @return
	 * @author: y
	 * @time:2017-6-27 下午1:45:09
	 */
	private JSONObject updateHookTips() {
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
