package com.navinfo.dataservice.dao.glm.search.specialMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			case rdLinkLimitTruck:
				list = rdLinkLimitTruck(x, y, z, gap, specialMapType);
				break;
			case rdLinkLimit:
				list = rdLinkLimit(x, y, z, gap, specialMapType);
				break;
			case rdlinkSpeedlimitSpeedClass:
				list = rdlinkSpeedlimitSpeedClass(x, y, z, gap, specialMapType);
				break;
			case rdlinkSpeedlimitSpeedClassWork:
				list = rdlinkSpeedlimitSpeedClassWork(x, y, z, gap,
						specialMapType);
				break;
			case rdlinkSpeedlimitSpeedLimitSrc:
				list = rdlinkSpeedlimitSpeedLimitSrc(x, y, z, gap,
						specialMapType);
				break;
			case rdLinkLaneClass:
				list = rdLinkLaneClass(x, y, z, gap, specialMapType);
				break;
			case rdLinkFunctionClass:
				list = rdLinkFunctionClass(x, y, z, gap, specialMapType);
				break;
			case rdLinkLaneNum:
				list = rdLinkLaneNum(x, y, z, gap, specialMapType);
				break;
			case rdLinkDevelopState:
				list = rdLinkDevelopState(x, y, z, gap, specialMapType);
				break;
			case rdLinkMultiDigitized:
				list = rdLinkMultiDigitized(x, y, z, gap, specialMapType);
				break;
			case rdLinkPaveStatus:
				list = rdLinkPaveStatus(x, y, z, gap, specialMapType);
				break;
			case rdLinkTollInfo:
				list = rdLinkTollInfo(x, y, z, gap, specialMapType);
				break;
			case rdLinkSpecialTraffic:
				list = rdLinkSpecialTraffic(x, y, z, gap, specialMapType);
				break;
			case rdLinkIsViaduct:
				list = rdLinkIsViaduct(x, y, z, gap, specialMapType);
				break;
			case rdLinkAppInfo:
				list = rdLinkAppInfo(x, y, z, gap, specialMapType);
				break;
			case rdLinkForm50:
				list = rdLinkForm50(x, y, z, gap, specialMapType);
				break;
			case rdLinkNameContent:
				list = rdLinkNameContent(x, y, z, gap, specialMapType);
				break;
			case rdLinkNameGroupid:
				list = rdLinkNameGroupid(x, y, z, gap, specialMapType);
				break;
			case rdLinkNameType:
				list = rdLinkNameType(x, y, z, gap, specialMapType);
				break;
			case rdlinkSpeedlimitConditionCount:
				list = rdlinkSpeedlimitConditionCount(x, y, z, gap,
						specialMapType);
				break;
			case rdLinkFormOfWay10:
			case rdLinkFormOfWay11:
			case rdLinkFormOfWay12:
			case rdLinkFormOfWay13:
			case rdLinkFormOfWay14:
			case rdLinkFormOfWay15:
			case rdLinkFormOfWay16:
			case rdLinkFormOfWay17:
			case rdLinkFormOfWay20:
			case rdLinkFormOfWay31:
			case rdLinkFormOfWay33:
			case rdLinkFormOfWay34:
			case rdLinkFormOfWay35:
			case rdLinkFormOfWay36:
			case rdLinkFormOfWay37:
			case rdLinkFormOfWay38:
			case rdLinkFormOfWay39:
				list = rdLinkFormOfWay(x, y, z, gap, specialMapType);
				break;
			case rdLinkLimitType0:
			case rdLinkLimitType2:
			case rdLinkLimitType3:
			case rdLinkLimitType8:
			case rdLinkLimitType9:
			case rdLinkLimitType10:
				list = rdLinkLimitType(x, y, z, gap, specialMapType);
				break;
			case rdLinkLimitType1:
			case rdLinkLimitType5:
			case rdLinkLimitType6:
			case rdLinkLimitType7:
				list = rdLinkLimitTypeDirect(x, y, z, gap, specialMapType);
				break;
			case rdLinkRticRank:
				list = rdLinkRticRank(x, y, z, gap, specialMapType);
				break;
			case rdLinkIntRticRank:
				list = rdLinkIntRticRank(x, y, z, gap, specialMapType);
				break;
			case rdLinkZoneTpye:
				list = rdLinkZoneTpye(x, y, z, gap, specialMapType);
				break;
			case rdLinkZoneCount:
				list = rdLinkZoneCount(x, y, z, gap, specialMapType);
				break;
			case rdLinkZoneSide:
				list = rdLinkZoneSide(x, y, z, gap, specialMapType);
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

	private JSONArray setLinkGeo(ResultSet resultSet, double px, double py,
			int z) throws Exception {
		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		JSONObject geojson = Geojson.spatial2Geojson(struct);

		JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

		return jo.getJSONArray("coordinates");
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
	private List<SearchSnapshot> rdLinkLimitTruck(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID,A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_LIMIT_TRUCK T WHERE T.LINK_PID = A.LINK_PID AND T.U_RECORD != 2) TRUCKCOUNT FROM TMP1 A";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkLimit(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_LIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.U_RECORD != 2) LIMITCOUNT FROM TMP1 A";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
			int z, int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ SPEED_CLASS FROM RD_LINK_SPEEDLIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.SPEED_TYPE = 0 AND T.U_RECORD != 2 AND ROWNUM <= 1) SPEED_CLASS FROM TMP1 A";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
			int z, int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ SPEED_CLASS_WORK FROM RD_LINK_SPEEDLIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.SPEED_TYPE = 0 AND T.U_RECORD != 2 AND ROWNUM <= 1) SPEED_CLASS_WORK FROM TMP1 A";

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

				m.put("d", resultSet.getString("direct"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
			int z, int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ FROM_LIMIT_SRC||','||TO_LIMIT_SRC FROM RD_LINK_SPEEDLIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.SPEED_TYPE = 0 AND T.U_RECORD != 2 AND ROWNUM <= 1) LIMIT_SRC FROM TMP1 A ";

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

				m.put("d", String.valueOf(direct));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkLaneClass(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,LANE_CLASS FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
			int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,FUNCTION_CLASS FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkLaneNum(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,LANE_NUM,LANE_LEFT,LANE_RIGHT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkDevelopState(int x, int y, int z,
			int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,DEVELOP_STATE FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
			int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,MULTI_DIGITIZED FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkPaveStatus(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,PAVE_STATUS FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkTollInfo(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,TOLL_INFO FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
			int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,SPECIAL_TRAFFIC FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkIsViaduct(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,IS_VIADUCT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkAppInfo(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT LINK_PID, GEOMETRY, DIRECT,APP_INFO FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkForm50(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_FORM T WHERE T.LINK_PID = A.LINK_PID AND T.FORM_OF_WAY = 50 AND T.U_RECORD != 2) FORMCOUNT FROM TMP1 A";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	private List<SearchSnapshot> rdLinkNameContent(int x, int y, int z,
			int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(N) */ T.LINK_PID, N.NAME_GROUPID, N.SEQ_NUM, T.GEOMETRY, T.DIRECT FROM TMP1 T LEFT JOIN RD_LINK_NAME N ON T.LINK_PID = N.LINK_PID AND N.U_RECORD != 2) SELECT /*+ index(RN) */ T2.*, RN.NAME FROM TMP2 T2 LEFT JOIN RD_NAME RN ON T2.NAME_GROUPID = RN.NAME_GROUPID AND (RN.LANG_CODE = 'CHI' OR RN.LANG_CODE = 'CHT') ORDER BY T2.LINK_PID, T2.SEQ_NUM ";

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

			String direct = "";

			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				direct = resultSet.getString("DIRECT");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						m.put("a", content.trim());

						m.put("d", String.valueOf(direct));

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					content = "";

					snapshot.setT(specialMapType.getValue());

					snapshot.setI(currLinkPid);

					JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

					snapshot.setG(geoArray);

					flagLinkPid = currLinkPid;
				}

				if (content.length() > 0) {
					content += "\\";
				}

				content += resultSet.getString("NAME");
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				m.put("a", content.trim());

				m.put("d", String.valueOf(direct));

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
	private List<SearchSnapshot> rdLinkNameGroupid(int x, int y, int z,
			int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ COUNT(NAME_GROUPID) FROM RD_LINK_NAME T WHERE T.LINK_PID = A.LINK_PID AND T.U_RECORD != 2) GROUPCOUNT FROM TMP1 A";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	 * 获取名称类型优先级
	 * 
	 * @return
	 */
	private int getPriorityNameType(int type) {

		switch (type) {

		case 5:
			return 1;
		case 1:
			return 2;
		case 2:
			return 3;
		case 4:
			return 4;
		case 15:
			return 5;
		case 9:
			return 6;
		case 7:
			return 7;
		case 0:
			return 8;
		case 3:
			return 9;
		case 6:
			return 10;
		case 8:
			return 11;
		case 14:
			return 12;
		default:
			return 99;
		}

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
	private List<SearchSnapshot> rdLinkNameType(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2)  SELECT /*+ index(N) */ T.LINK_PID, T.GEOMETRY,T.DIRECT, N.NAME_TYPE FROM TMP1 T LEFT JOIN RD_LINK_NAME N ON T.LINK_PID = N.LINK_PID AND N.U_RECORD != 2 ORDER BY N.LINK_PID";

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

			String direct = "";

			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				direct = resultSet.getString("DIRECT");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						m.put("a", flagType);

						m.put("d", String.valueOf(direct));

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					flagType = 99;// 优先级最低

					snapshot.setT(specialMapType.getValue());

					snapshot.setI(currLinkPid);

					JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

					snapshot.setG(geoArray);

					flagLinkPid = currLinkPid;
				}

				int currtype = resultSet.getInt("NAME_TYPE");

				if (resultSet.wasNull()) {
					currtype = 99;
				}

				if (getPriorityNameType(flagType) > getPriorityNameType(currtype)) {
					flagType = currtype;
				}
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				m.put("a", flagType);

				m.put("d", String.valueOf(direct));

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
			int z, int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_SPEEDLIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.SPEED_TYPE = 3 AND T.U_RECORD != 2) CONDITIONCOUNT FROM TMP1 A";

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

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	 * 
	 * @param specialMapType
	 * @return
	 */
	private int getLinkLimitType(SpecialMapType specialMapType) {
		switch (specialMapType) {

		case rdLinkLimitType0:
			return 0;
		case rdLinkLimitType1:
			return 1;
		case rdLinkLimitType2:
			return 2;
		case rdLinkLimitType3:
			return 3;
		case rdLinkLimitType5:
			return 5;
		case rdLinkLimitType6:
			return 6;
		case rdLinkLimitType7:
			return 7;
		case rdLinkLimitType8:
			return 8;
		case rdLinkLimitType9:
			return 9;
		case rdLinkLimitType10:
			return 10;
		default:
			return -1;
		}
	}

	/**
	 * 普通限制：适用 21 禁止穿行 、22 道路维修中、24 外地车限行、25 尾号限行、26 在建、27 车辆限制
	 * 业务说明：根据RD_LINK_LIMIT限制信息中是否有**记录进行渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkLimitType(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		int tpye = getLinkLimitType(specialMapType);

		if (tpye == -1) {
			return list;
		}

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_LIMIT T WHERE T.LINK_PID = A.LINK_PID AND T.TYPE = :2 AND T.U_RECORD != 2) LIMITCOUNT FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			pstmt.setInt(2, tpye);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("LIMITCOUNT"));

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	 * 根据优先级获取 限制方向
	 * 
	 * @param dirs
	 * @return
	 */
	private int getLimitDir(Set<Integer> dirs) {

		if (dirs.contains(1) || (dirs.contains(2) && dirs.contains(3))) {
			return 1;
		}
		if (dirs.contains(2)) {
			return 2;
		}
		if (dirs.contains(3)) {
			return 3;
		}
		if (dirs.contains(0)) {
			return 0;
		}
		if (dirs.contains(9)) {
			return 9;
		}

		return 99;
	}

	/**
	 * 未完成
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkLimitTypeDirect(int x, int y, int z,
			int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		int tpye = getLinkLimitType(specialMapType);

		if (tpye == -1) {

			return list;
		}

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT /*+ index(N) */ T.LINK_PID, T.GEOMETRY,T.DIRECT, N.LIMIT_DIR FROM TMP1 T LEFT JOIN RD_LINK_LIMIT N ON T.LINK_PID = N.LINK_PID AND N.TYPE = :2 AND N.U_RECORD != 2 ORDER BY N.LINK_PID ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		SearchSnapshot snapshot = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			pstmt.setInt(2, tpye);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			Set<Integer> dirs = new HashSet<Integer>();

			int flagLinkPid = 0;

			String direct = "";
			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				direct = resultSet.getString("DIRECT");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						int type = getLimitDir(dirs);

						m.put("a", type);

						m.put("d", String.valueOf(direct));

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					dirs = new HashSet<Integer>();

					snapshot.setT(specialMapType.getValue());

					snapshot.setI(currLinkPid);

					JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

					snapshot.setG(geoArray);

					flagLinkPid = currLinkPid;
				}

				int currDir = resultSet.getInt("LIMIT_DIR");

				if (resultSet.wasNull()) {

					continue;
				}

				dirs.add(currDir);
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				int type = getLimitDir(dirs);

				m.put("a", type);

				m.put("d", String.valueOf(direct));

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
	 * 获取形态信息
	 * 
	 * @param specialMapType
	 * @return 形态值
	 */
	private int getFormOfWay(SpecialMapType specialMapType) {

		switch (specialMapType) {
		// IC
		case rdLinkFormOfWay10:
			return 10;
			// JCT
		case rdLinkFormOfWay11:
			return 11;
			// SA
		case rdLinkFormOfWay12:
			return 12;
			// PA
		case rdLinkFormOfWay13:
			return 13;
			// 全封闭道路
		case rdLinkFormOfWay14:
			return 14;
			// 匝道
		case rdLinkFormOfWay15:
			return 15;
			// 跨线天桥
		case rdLinkFormOfWay16:
			return 16;
			// 跨线地道
		case rdLinkFormOfWay17:
			return 17;
			// 步行街
		case rdLinkFormOfWay20:
			return 20;
			// 隧道
		case rdLinkFormOfWay31:
			return 31;
			// 环岛
		case rdLinkFormOfWay33:
			return 33;
			// 辅路
		case rdLinkFormOfWay34:
			return 34;
			// 调头口
		case rdLinkFormOfWay35:
			return 35;
			// POI连接路
		case rdLinkFormOfWay36:
			return 36;
			// 提右
		case rdLinkFormOfWay37:
			return 37;
			// 提左
		case rdLinkFormOfWay38:
			return 38;
			// 主辅路出入口
		case rdLinkFormOfWay39:
			return 39;
		default:
			return -1;
		}
	}

	/**
	 * 22 按照是否具有某个道路形态值特殊渲染link，每个道路形态区分渲染
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkFormOfWay(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		int formOfWay = getFormOfWay(specialMapType);

		if (formOfWay == -1) {

			return list;
		}

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_FORM T WHERE T.LINK_PID = A.LINK_PID AND T.FORM_OF_WAY = :2 AND T.U_RECORD != 2) FORMCOUNT FROM TMP1 A";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			pstmt.setInt(2, formOfWay);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("FORMCOUNT"));

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	 * 获取rtic等级
	 * 
	 * @param dirs
	 * @return
	 */
	private int getRticRank(Set<Integer> ranks) {

		if (ranks.size() > 1) {
			return 5;
		}
		if (ranks.contains(0)) {
			return 0;
		}
		if (ranks.contains(1)) {
			return 1;
		}
		if (ranks.contains(2)) {
			return 2;
		}
		if (ranks.contains(3)) {
			return 3;
		}
		if (ranks.contains(4)) {
			return 4;
		}

		return 99;
	}

	/**
	 * RTIC等级
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkRticRank(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2)  SELECT /*+ index(N) */ T.LINK_PID, T.GEOMETRY,T.DIRECT, N.RANK FROM TMP1 T LEFT JOIN RD_LINK_RTIC N ON T.LINK_PID = N.LINK_PID AND N.U_RECORD != 2 ORDER BY N.LINK_PID ";

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

			Set<Integer> ranks = new HashSet<Integer>();

			int flagLinkPid = 0;

			String direct = "";

			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				direct = resultSet.getString("DIRECT");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						int type = getRticRank(ranks);

						m.put("a", type);

						m.put("d", String.valueOf(direct));

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					ranks = new HashSet<Integer>();

					snapshot.setT(specialMapType.getValue());

					snapshot.setI(currLinkPid);

					JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

					snapshot.setG(geoArray);

					flagLinkPid = currLinkPid;
				}

				int currRank = resultSet.getInt("RANK");

				if (resultSet.wasNull()) {

					continue;
				}

				ranks.add(currRank);
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				int type = getRticRank(ranks);

				m.put("a", type);

				m.put("d", String.valueOf(direct));

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
	 * IntRtic等级
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkIntRticRank(int x, int y, int z,
			int gap, SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2)  SELECT /*+ index(N) */ T.LINK_PID, T.GEOMETRY,T.DIRECT, N.RANK FROM TMP1 T LEFT JOIN RD_LINK_INT_RTIC N ON T.LINK_PID = N.LINK_PID AND N.U_RECORD != 2 ORDER BY N.LINK_PID ";

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

			Set<Integer> ranks = new HashSet<Integer>();

			int flagLinkPid = 0;

			String direct = "";

			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				direct = resultSet.getString("DIRECT");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						int type = getRticRank(ranks);

						m.put("a", type);

						m.put("d", String.valueOf(direct));

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					ranks = new HashSet<Integer>();

					snapshot.setT(specialMapType.getValue());

					snapshot.setI(currLinkPid);

					JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

					snapshot.setG(geoArray);

					flagLinkPid = currLinkPid;
				}

				int currRank = resultSet.getInt("RANK");

				if (resultSet.wasNull()) {

					continue;
				}

				ranks.add(currRank);
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				int type = getRticRank(ranks);

				m.put("a", type);

				m.put("d", String.valueOf(direct));

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
	 * 获取rtic等级
	 * 
	 * @param dirs
	 * @return
	 */
	private int getZoneLnkType(Set<Integer> types) {

		if (types.size() > 1) {
			return 4;
		}
		if (types.contains(0)) {
			return 0;
		}
		if (types.contains(1)) {
			return 1;
		}
		if (types.contains(2)) {
			return 2;
		}
		if (types.contains(3)) {
			return 3;
		}

		return 99;
	}

	/**
	 * ZONE类型
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkZoneTpye(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2)  SELECT /*+ index(N) */ T.LINK_PID, T.GEOMETRY,T.DIRECT, N.TYPE FROM TMP1 T LEFT JOIN RD_LINK_ZONE N ON T.LINK_PID = N.LINK_PID AND N.U_RECORD != 2 ORDER BY N.LINK_PID";

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

			Set<Integer> types = new HashSet<Integer>();

			int flagLinkPid = 0;

			String direct = "";

			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				direct = resultSet.getString("DIRECT");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						int type = getZoneLnkType(types);

						m.put("a", type);

						m.put("d", String.valueOf(direct));

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					types = new HashSet<Integer>();

					snapshot.setT(specialMapType.getValue());

					snapshot.setI(currLinkPid);

					JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

					snapshot.setG(geoArray);

					flagLinkPid = currLinkPid;
				}

				int currType = resultSet.getInt("TYPE");

				if (resultSet.wasNull()) {

					continue;
				}

				types.add(currType);
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				int type = getZoneLnkType(types);

				m.put("a", type);

				m.put("d", String.valueOf(direct));

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
	 * 35 zone个数 :按照link所具有的ZONE的组数渲染link；zone信息个数：0，1，2，三个颜色渲染显示
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkZoneCount(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.LINK_PID, A.GEOMETRY,A.DIRECT, (SELECT /*+ index(t) */ COUNT(1) FROM RD_LINK_ZONE T WHERE T.LINK_PID = A.LINK_PID AND T.U_RECORD != 2) ZONECOUNT FROM TMP1 A";

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

				m.put("a", resultSet.getString("ZONECOUNT"));

				m.put("d", resultSet.getString("DIRECT"));

				snapshot.setM(m);

				snapshot.setT(specialMapType.getValue());

				snapshot.setI(resultSet.getInt("LINK_PID"));

				JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

				snapshot.setG(geoArray);

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
	 * 获取zone区划号码位置信息
	 * 
	 * @param dirs
	 * @return
	 */
	private int getZoneLnkSide(Map<Integer, Integer> infos) {

		if (infos.size() == 0) {
			return 3;
		}
		if (infos.size() == 1) {
			return 4;
		}
		if (infos.size() == 2 && infos.containsKey(1) && infos.containsKey(0)) {
			if (infos.get(0) == infos.get(1)) {
				return 1;
			} else {
				return 2;
			}
		}

		return 2;
	}

	/**
	 * link的左右ZONE号码
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	private List<SearchSnapshot> rdLinkZoneSide(int x, int y, int z, int gap,
			SpecialMapType specialMapType) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, DIRECT FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT /*+ index(N) */ T.LINK_PID, T.GEOMETRY,T.DIRECT, N.SIDE, N.REGION_ID FROM TMP1 T LEFT JOIN RD_LINK_ZONE N ON T.LINK_PID = N.LINK_PID AND N.U_RECORD != 2 ORDER BY N.LINK_PID";

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

			// Map<Integer:SIDE, Integer:REGION_ID>
			Map<Integer, Integer> infos = new HashMap<Integer, Integer>();

			int flagLinkPid = 0;

			String direct = "";

			while (resultSet.next()) {

				int currLinkPid = resultSet.getInt("LINK_PID");

				direct = resultSet.getString("DIRECT");

				if (flagLinkPid != currLinkPid) {

					if (snapshot != null) {

						JSONObject m = new JSONObject();

						int regionInfo = getZoneLnkSide(infos);

						m.put("a", regionInfo);

						m.put("d", String.valueOf(direct));

						snapshot.setM(m);

						list.add(snapshot);
					}

					snapshot = new SearchSnapshot();

					infos = new HashMap<Integer, Integer>();

					snapshot.setT(specialMapType.getValue());

					snapshot.setI(currLinkPid);

					JSONArray geoArray = setLinkGeo(resultSet, px, py, z);

					snapshot.setG(geoArray);

					flagLinkPid = currLinkPid;
				}

				int currSide = resultSet.getInt("SIDE");

				if (resultSet.wasNull()) {

					continue;
				}

				int currRegionId = resultSet.getInt("REGION_ID");

				if (resultSet.wasNull()) {

					continue;
				}

				infos.put(currSide, currRegionId);
			}
			if (snapshot != null) {

				JSONObject m = new JSONObject();

				int regionInfo = getZoneLnkSide(infos);

				m.put("a", regionInfo);

				m.put("d", String.valueOf(direct));

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

}
