package com.navinfo.dataservice.dao.glm.search.specialMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
	public JSONObject searchDataByTileWithGap(String type, int x, int y, int z,
			int gap) throws Exception {

		JSONObject json = new JSONObject();

		try {

			List<SearchSnapshot> list = null;

			SpecialMapType specialMapType = SpecialMapType.valueOf(type);

			switch (specialMapType) {
			// 卡车限制信息
			case rdLinkLimitTruck:
				list = rdLinkLimitTruck(x, y, z, gap);
				break;
			// link限制信息数量（普通限制信息）
			case rdLinkLimit:
				list = rdLinkLimit(x, y, z, gap);
				break;
			// 普通线限速限速等级
			case rdlinkSpeedlimitSpeedClass:
				list = rdlinkSpeedlimitSpeedClass(x, y, z, gap);
				break;
			// 普通线限速限速等级赋值标识
			case rdlinkSpeedlimitSpeedClassWork:
				list = rdlinkSpeedlimitSpeedClassWork(x, y, z, gap);
				break;
			// 普通线限速限速来源
			case rdlinkSpeedlimitSpeedLimitSrc:
				list = rdlinkSpeedlimitSpeedLimitSrc(x, y, z, gap);
				break;
			// link车道等级
			case rdLinkLaneClass:
				list = rdLinkLaneClass(x, y, z, gap);
				break;
			// link功能等级
			case rdLinkFunctionClass:
				list = rdLinkFunctionClass(x, y, z, gap);
				break;
			// 车道数（总数）
			case rdLinkLaneNum:
				list = rdLinkLaneNum(x, y, z, gap);
				break;

			default:
				list = new ArrayList<SearchSnapshot>();
			}

			JSONArray array = new JSONArray();

			for (SearchSnapshot snap : list) {

				array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig());
			}

			json.accumulate(type.toString(), array, getJsonConfig());

		} catch (Exception e) {

			throw e;

		} finally {
		}

		return json;
	}

	/**
	 * 卡车限制信息 业务说明：卡车限制信息按照卡车限制信息的有无来渲染link，卡车限制信息字表有记录和无记录需要对link进行区别选人
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

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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
	 * link限制信息数量（普通限制信息） 业务说明：按照限制信息的数量区分渲染显示，限制信息无记录与限制信息存在一条记录和多条记录区分渲染
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

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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
	 * 普通线限速限速等级 业务说明：按照普通线限速的限速等级不同的值区分渲染link
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

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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
	 * 普通线限速限速等级赋值标识 业务说明：根据等级赋值标识（SPEED_CLASS_WORK）的值区分渲染link
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

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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
	 * 普通线限速限速来源
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

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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
	 * link车道等级 业务说明：根据车道等级的值区分渲染link
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

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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
	 * link功能等级 业务说明：根据功能等级的值区分渲染link
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

				m.put("a", resultSet.getInt("FUNCTION_CLASS"));

				snapshot.setM(m);

				snapshot.setT(507);

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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
	 * 车道数（总数） 业务说明：根据总车道数的值区分渲染link,如果总车道数为0，根据左右车道数是否有值渲染link
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

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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
