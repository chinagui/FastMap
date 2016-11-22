package com.navinfo.dataservice.dao.glm.search.specialMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.iface.SpecialMapType;
import com.navinfo.navicommons.database.sql.DBUtils;

public class SpecialMapUtils {

	private Connection conn;

	public SpecialMapUtils(Connection conn) throws Exception {

		this.conn = conn;

	}

	/**
	 * 控制输出JSON的格式
	 * 
	 * @return JsonConfig
	 */
	private JsonConfig getJsonConfig() {
		JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.registerJsonValueProcessor(String.class,
				new JsonValueProcessor() {

					@Override
					public Object processObjectValue(String key, Object value,
							JsonConfig arg2) {
						if (value == null) {
							return null;
						}

						if (JSONUtils.mayBeJSON(value.toString())) {
							return "\"" + value + "\"";
						}

						return value;

					}

					@Override
					public Object processArrayValue(Object value,
							JsonConfig arg1) {
						return value;
					}
				});

		return jsonConfig;
	}

	/**
	 * 根据瓦片空间查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	public JSONObject searchDataByTileWithGap(List<String> types, int x, int y,
			int z, int gap) throws Exception {

		JSONObject json = new JSONObject();

		try {

			for (String type : types) {

				List<SearchSnapshot> list = getSearchSnapshot(type, x, y, z,
						gap);

				JSONArray array = new JSONArray();

				for (SearchSnapshot snap : list) {

					array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig());
				}

				json.accumulate(type, array, getJsonConfig());
			}
		} catch (Exception e) {

			throw e;

		} finally {
		}
		return json;
	}

	/**
	 * 根据瓦片空间查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	private List<SearchSnapshot> getSearchSnapshot(String type, int x, int y,
			int z, int gap) throws Exception {

		List<SearchSnapshot> list = null;

		try {

			SpecialMapType specialMapType = SpecialMapType.valueOf(type);

			switch (specialMapType) {
			// 1 卡车限制信息
			case rdLinkLimitTruck:
				list = rdLinkLimitTruck(x, y, z, gap);
				break;
			// 2 link限制信息数量（普通限制信息）
			case rdLinkLimit:
				list = rdLinkLimit(x, y, z, gap);
				break;
			// 3 普通线限速限速等级
			case rdlinkSpeedlimitSpeedClass:
				list = rdlinkSpeedlimitSpeedClass(x, y, z, gap);
				break;
			// 4 普通线限速限速等级赋值标识
			case rdlinkSpeedlimitSpeedClassWork:
				list = rdlinkSpeedlimitSpeedClassWork(x, y, z, gap);
				break;
			// 5 普通线限速限速来源
			case rdlinkSpeedlimitSpeedLimitSrc:
				list = rdlinkSpeedlimitSpeedLimitSrc(x, y, z, gap);
				break;
			// 6 link车道等级
			case rdLinkLaneClass:
				list = rdLinkLaneClass(x, y, z, gap);
				break;
			// 7 link功能等级
			case rdLinkFunctionClass:
				list = rdLinkFunctionClass(x, y, z, gap);
				break;
			// 8 车道数（总数）
			case rdLinkLaneNum:
				list = rdLinkLaneNum(x, y, z, gap);
				break;
			// 9 开发状态
			case rdLinkDevelopState:
				list = rdLinkDevelopState(x, y, z, gap);
				break;
			// 10 上下线分离
			case rdLinkMultiDigitized:
				list = rdLinkMultiDigitized(x, y, z, gap);
				break;
			// 11 铺设状态
			case rdLinkPaveStatus:
				list = rdLinkPaveStatus(x, y, z, gap);
				break;
			// 12 收费信息
			case rdLinkTollInfo:
				list = rdLinkTollInfo(x, y, z, gap);
				break;
			// 13 特殊交通
			case rdLinkSpecialTraffic:
				list = rdLinkSpecialTraffic(x, y, z, gap);
				break;
			// 14 高架
			case rdLinkIsViaduct:
				list = rdLinkIsViaduct(x, y, z, gap);
				break;
			// 15 供用信息
			case rdLinkAppInfo:
				list = rdLinkAppInfo(x, y, z, gap);
				break;
			// 16 交叉点内道路
			case rdLinkForm50:
				list = rdLinkForm50(x, y, z, gap);
				break;
			// 17 道路名内容
			case rdLinkNameContent:
				list = rdLinkNameContent(x, y, z, gap);
				break;
			// 18 道路名组数
			case rdLinkNameGroupid:
				list = rdLinkNameGroupid(x, y, z, gap);
				break;
			// 19名称类型
			case rdLinkNameType:
				list = rdLinkNameType(x, y, z, gap);
				break;
			// 20 条件线限速个数
			case rdlinkSpeedlimitConditionCount:
				list = rdlinkSpeedlimitConditionCount(x, y, z, gap);
				break;
			// 21 禁止穿行
			case rdLinkLimitType3:
				list = rdLinkLimitType3(x, y, z, gap);
				break;

			default:
				list = new ArrayList<SearchSnapshot>();
			}

		} catch (Exception e) {

			throw e;

		} finally {
		}

		return list;
	}

	/**
	 * 1 卡车限制信息 业务说明：卡车限制信息按照卡车限制信息的有无来渲染link，卡车限制信息字表有记录和无记录需要对link进行区别选人
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkLimitTruck(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID,A.GEOMETRY, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_LIMIT_TRUCK T WHERE T.LINK_PID = A.LINK_PID AND T.U_RECORD != 2) TRUCKCOUNT FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("TRUCKCOUNT"));

				snapshot.setM(m);

				snapshot.setT(501);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 2 link限制信息数量（普通限制信息） 业务说明：按照限制信息的数量区分渲染显示，限制信息无记录与限制信息存在一条记录和多条记录区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkLimit(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_LIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.U_RECORD != 2) LIMITCOUNT FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("LIMITCOUNT"));

				snapshot.setM(m);

				snapshot.setT(502);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 3 普通线限速限速等级 业务说明：按照普通线限速的限速等级不同的值区分渲染link
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdlinkSpeedlimitSpeedClass(int x, int y,
			int z, int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY, (SELECT /*+ index(t) */ SPEED_CLASS FROM RD_LINK_SPEEDLIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.SPEED_TYPE = 0 AND T.U_RECORD != 2 AND ROWNUM <= 1) SPEED_CLASS FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				int speedClass = resultSet.getInt("SPEED_CLASS");

				if (resultSet.wasNull()) {
					speedClass = 99;
				}

				m.put("a", speedClass);

				snapshot.setM(m);

				snapshot.setT(503);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 4 普通线限速限速等级赋值标识 业务说明：根据等级赋值标识（SPEED_CLASS_WORK）的值区分渲染link
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdlinkSpeedlimitSpeedClassWork(int x, int y,
			int z, int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY, (SELECT /*+ index(t) */ SPEED_CLASS_WORK FROM RD_LINK_SPEEDLIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.SPEED_TYPE = 0 AND T.U_RECORD != 2 AND ROWNUM <= 1) SPEED_CLASS_WORK FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				int speedClassWork = resultSet.getInt("SPEED_CLASS_WORK");

				if (resultSet.wasNull()) {
					speedClassWork = 99;
				}

				m.put("a", speedClassWork);

				snapshot.setM(m);

				snapshot.setT(504);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 5 普通线限速限速来源
	 * 业务说明：普通线限速的额限速来源（FROM_LIMIT_SRC、TO_LIMIT_SRC）按照线限速的方向表达，上方向的link
	 * ，顺方向和逆方向的限速来源分别记录，渲染原则如下： A、单方向道路：根据当前link线限速的限速来源值进行渲染； B、双方向道路：
	 * 顺方向和逆方向两侧的限速来源值相同，则按照当前的限速来源值渲染该段link； 顺方向和逆方向两侧的限速来源值不同，则视为“混合”渲染该段link；
	 * *
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdlinkSpeedlimitSpeedLimitSrc(int x, int y,
			int z, int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY,DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ FROM_LIMIT_SRC||','||TO_LIMIT_SRC FROM RD_LINK_SPEEDLIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.SPEED_TYPE = 0 AND T.U_RECORD != 2 AND ROWNUM <= 1) LIMIT_SRC FROM TMP1 A ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				int direct = resultSet.getInt("DIRECT");

				int speedClassWork = 99;

				String strClassWork = resultSet.getString("LIMIT_SRC");

				if (!resultSet.wasNull()) {

					// 第0位顺向限速来源；第1位逆向限速来源
					String[] classWorks = strClassWork.split(",");

					if (direct == 2) {
						speedClassWork = Integer.parseInt(classWorks[0]);
					} else if (direct == 3) {
						speedClassWork = Integer.parseInt(classWorks[1]);
					} else {
						if (classWorks[0].equals(classWorks[1])) {
							speedClassWork = Integer.parseInt(classWorks[0]);
						} else {
							// 顺方向和逆方向两侧的限速来源值不同，则视为“混合”值为10
							speedClassWork = 10;
						}

					}
				}

				m.put("a", speedClassWork);

				snapshot.setM(m);

				snapshot.setT(505);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 6 link车道等级 业务说明：根据车道等级的值区分渲染link
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkLaneClass(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,LANE_CLASS FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("LANE_CLASS"));

				snapshot.setM(m);

				snapshot.setT(506);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 7 link功能等级 业务说明：根据功能等级的值区分渲染link
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkFunctionClass(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,FUNCTION_CLASS FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				int functionClass = resultSet.getInt("FUNCTION_CLASS");

				if (z < 14 && functionClass > 3) {

					continue;
				}

				m.put("a", functionClass);

				snapshot.setM(m);

				snapshot.setT(507);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 8 车道数（总数） 业务说明：根据总车道数的值区分渲染link,如果总车道数为0，根据左右车道数是否有值渲染link
	 * A、总车道数为7以及7以下的，按照车道数的值区分渲染link（7种颜色）； B、总车道数大于7的统一渲染link（1种颜色）；
	 * C、当总车道数为0，左右车道数不为“0”时，需要渲染；（1种颜色） D、如果总车道数、左右车道数都为0，需要渲染；（1种颜色）
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkLaneNum(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,LANE_NUM,LANE_LEFT,LANE_RIGHT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				int laneNum = resultSet.getInt("LANE_NUM");

				int laneLeft = resultSet.getInt("LANE_LEFT");

				int laneRight = resultSet.getInt("LANE_RIGHT");

				if (laneNum > 0) {

					if (laneNum < 8) {

						m.put("a", laneNum);

					} else {

						m.put("a", 8);
					}
				} else {

					if (laneLeft != 0 || laneRight != 0) {

						m.put("a", 9);

					} else {

						m.put("a", 10);
					}
				}

				snapshot.setM(m);

				snapshot.setT(508);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 9 开发状态, 业务说明：按照开发状态的类型特殊渲染link，共三个值域，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkDevelopState(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,DEVELOP_STATE FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("DEVELOP_STATE"));

				snapshot.setM(m);

				snapshot.setT(509);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 10 上下线分离, 业务说明：按照是否具有上下分离属性特殊渲染link，共2个值域，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkMultiDigitized(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,MULTI_DIGITIZED FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("MULTI_DIGITIZED"));

				snapshot.setM(m);

				snapshot.setT(510);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 11 铺设状态, 业务说明：按照铺设状态的类型特殊渲染link，共2个值域，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkPaveStatus(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,PAVE_STATUS FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("PAVE_STATUS"));

				snapshot.setM(m);

				snapshot.setT(511);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 12 收费信息, 业务说明：按照收费信息的类型特殊渲染link，共2个值域，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkTollInfo(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,TOLL_INFO FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("TOLL_INFO"));

				snapshot.setM(m);

				snapshot.setT(512);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 13 特殊交通, 业务说明：按照是否具有特殊交通类型的属性特殊渲染link，共2个值域，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkSpecialTraffic(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,SPECIAL_TRAFFIC FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("SPECIAL_TRAFFIC"));

				snapshot.setM(m);

				snapshot.setT(513);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 14 高架, 业务说明：按照是否具有高架类型的属性特殊渲染link，共2个值域，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkIsViaduct(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,IS_VIADUCT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("IS_VIADUCT"));

				snapshot.setM(m);

				snapshot.setT(514);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 15 供用信息, 业务说明：按照供用信息的类型特殊渲染link，共5个值域，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkAppInfo(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY,APP_INFO FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("APP_INFO"));

				snapshot.setM(m);

				snapshot.setT(515);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 16 交叉点内道路, 业务说明：按照道路是否具有交叉口内道路形态特殊渲染link
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkForm50(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_FORM T WHERE T.LINK_PID = A.LINK_PID AND T.FORM_OF_WAY = 50 AND T.U_RECORD != 2) FORMCOUNT FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("FORMCOUNT"));

				snapshot.setM(m);

				snapshot.setT(516);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 17 道路名内容, 业务说明：按照道路名的顺序该link所有的中文名称标注在link上
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkNameContent(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(N) */ T.LINK_PID, N.NAME_GROUPID, N.SEQ_NUM, T.GEOMETRY FROM TMP1 T LEFT JOIN RD_LINK_NAME N ON T.LINK_PID = N.LINK_PID AND N.U_RECORD != 2)  SELECT /*+ index(RN) */  T2.*, RN.NAME FROM TMP2 T2 LEFT JOIN RD_NAME RN ON T2.NAME_GROUPID = RN.NAME_GROUPID AND (RN.LANG_CODE = 'CHI' OR RN.LANG_CODE = 'CHT') ORDER BY T2.LINK_PID, T2.SEQ_NUM";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		int flagLinkPid = 0;

		SearchSnapshot snapshot = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			String content = "";

			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						m.put("a", content.trim());

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					content = "";

					snapshot.setT(517);

					snapshot.setI(String.valueOf(currLinkPid));

					STRUCT struct = (STRUCT) resultSet.getObject("geometry");

					JSONObject geojson = Geojson.spatial2Geojson(struct);

					JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

					snapshot.setG(jo.getJSONArray("coordinates"));
					flagLinkPid = currLinkPid ;
				}

				content += resultSet.getString("NAME") + " ";
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				m.put("a", content.trim());

				snapshot.setM(m);

				list.add(snapshot);
			}

		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 18 道路名组数, 业务说明：根据道路名具有的道路名组数特殊渲染link；0~4分别渲染，4以上统一渲染一个颜色
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkNameGroupid(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY, (SELECT /*+ index(t) */ COUNT(NAME_GROUPID) FROM RD_LINK_NAME T WHERE T.LINK_PID = A.LINK_PID AND T.U_RECORD != 2) GROUPCOUNT FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("GROUPCOUNT"));

				snapshot.setM(m);

				snapshot.setT(518);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	private Map<Integer, Integer> getNameTypeMap() {
		Map<Integer, Integer> typeMap = new HashMap<Integer, Integer>();

		typeMap.put(5, 1);
		typeMap.put(1, 2);
		typeMap.put(2, 3);
		typeMap.put(4, 4);
		typeMap.put(15, 5);
		typeMap.put(9, 6);
		typeMap.put(7, 7);
		typeMap.put(0, 8);
		typeMap.put(3, 9);
		typeMap.put(6, 10);
		typeMap.put(8, 11);
		typeMap.put(14, 12);
		typeMap.put(99, 99);// link无名称

		return typeMap;
	}

	/**
	 * 19 名称类型, 业务说明：按照道路名的名称类型特殊渲染link，共12个值，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkNameType(int x, int y, int z, int gap)
			throws Exception {

		Map<Integer, Integer> typeMap = getNameTypeMap();

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2)  SELECT /*+ index(N) */ T.LINK_PID, T.GEOMETRY, N.NAME_TYPE FROM TMP1 T LEFT JOIN RD_LINK_NAME N ON T.LINK_PID = N.LINK_PID AND N.U_RECORD != 2 ORDER BY N.LINK_PID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		SearchSnapshot snapshot = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			int flagType = 99;// 优先级最低

			int flagLinkPid = 0;

			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						m.put("a", flagType);

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					flagType = 99;// 优先级最低

					snapshot.setT(519);

					snapshot.setI(String.valueOf(currLinkPid));

					STRUCT struct = (STRUCT) resultSet.getObject("geometry");

					JSONObject geojson = Geojson.spatial2Geojson(struct);

					JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

					snapshot.setG(jo.getJSONArray("coordinates"));
					
					flagLinkPid = currLinkPid;
				}

				int currtype = resultSet.getInt("NAME_TYPE");

				if (resultSet.wasNull()) {
					currtype = 99;
				}

				if (typeMap.get(flagType) > typeMap.get(currtype)) {
					flagType = currtype;
				}
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				m.put("a", flagType);

				snapshot.setM(m);

				list.add(snapshot);
			}

		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 20 条件线限速个数, 业务说明：按照条件线限速的个数特殊渲染link，共5个值，区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdlinkSpeedlimitConditionCount(int x, int y,
			int z, int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_SPEEDLIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.SPEED_TYPE = 3 AND T.U_RECORD != 2) CONDITIONCOUNT FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("CONDITIONCOUNT"));

				snapshot.setM(m);

				snapshot.setT(520);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 21 禁止穿行 业务说明：根据RD_LINK_LIMIT限制信息中是否有“穿行限制”记录进行渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkLimitType3(int x, int y, int z, int gap)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_LIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.TYPE = 3 AND T.U_RECORD != 2) LIMITCOUNT FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("LIMITCOUNT"));

				snapshot.setM(m);

				snapshot.setT(502);

				snapshot.setI(String.valueOf(resultSet.getInt("LINK_PID")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

}
