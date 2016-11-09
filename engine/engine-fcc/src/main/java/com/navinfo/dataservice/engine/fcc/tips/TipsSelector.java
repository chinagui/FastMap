package com.navinfo.dataservice.engine.fcc.tips;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.test.JSONAssert;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hbase.async.KeyValue;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.HBaseController;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Tips查询
 */
public class TipsSelector {

	private static final Logger logger = Logger.getLogger(TipsSelector.class);

	private SolrController conn = new SolrController();

	public TipsSelector() {
	}

	/**
	 * 范围查询Tips
	 * 
	 * @param wkt
	 * @return Tips JSON数组
	 * @throws Exception
	 */
	public JSONArray searchDataBySpatial(String wkt) throws Exception {
		JSONArray array = new JSONArray();

		List<JSONObject> snapshots = conn.queryTipsWeb(wkt);

		for (JSONObject snapshot : snapshots) {

			snapshot.put("t", 1);

			array.add(snapshot);
		}

		return array;
	}

	/**
	 * @Description:范围瓦片查询Tips
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @param types
	 * @param mdFlag
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-7-2 上午10:08:16
	 */
	public JSONArray searchDataByTileWithGap(int x, int y, int z, int gap,
			JSONArray types, String mdFlag) throws Exception {
		JSONArray array = new JSONArray();

		try {

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			JSONArray stages = new JSONArray();

			if ("d".equals(mdFlag)) {

				stages.add(1);

				stages.add(2);

			} else if ("m".equals(mdFlag)) {

				stages.add(1);

				stages.add(2);

				stages.add(3);
			}

			List<JSONObject> snapshots = conn.queryTipsWebType(wkt, types,
					stages);

			for (JSONObject json : snapshots) {

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(json.getString("id"));

				int type = Integer.valueOf(json.getString("s_sourceType"));

				snapshot.setT(type);

				JSONObject geojson = JSONObject.fromObject(json
						.getString("g_location"));
				// 渲染的坐标都是屏幕坐标
				Geojson.coord2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				JSONObject m = new JSONObject();

				// 日编月编状态

				if ("d".equals(mdFlag)) {

					m.put("a", json.getString("t_dStatus"));

				} else if ("m".equals(mdFlag)) {

					m.put("a", json.getString("t_mStatus"));

				}

				m.put("b", json.getString("t_lifecycle"));

				JSONObject g_guide = JSONObject.fromObject(json
						.getString("g_guide"));

				m.put("h", g_guide.getJSONArray("coordinates"));

				JSONObject deep = JSONObject.fromObject(json.getString("deep"));

				// g字段重新赋值的（显示坐标：取Tips的geo）
				if (type == 1604 || type == 1601 || type == 1602
						|| type == 1605 || type == 1606 || type == 1607) {

					JSONObject deepGeo = deep.getJSONObject("geo");

					Geojson.coord2Pixel(deepGeo, z, px, py);

					snapshot.setG(deepGeo.getJSONArray("coordinates"));

				}

				if (type == 1201) {
					m.put("c", String.valueOf(deep.getInt("kind")));
				} else if (type == 2001 || type == 1901) {

					JSONObject geo = deep.getJSONObject("geo");

					Geojson.coord2Pixel(geo, z, px, py);

					m.put("c", geo.getJSONArray("coordinates"));
				} else if (type == 1203 || type == 1101 || type == 1407
						|| type == 1403 || type == 1401 || type == 1402
						|| type == 1405 || type == 1406 || type == 1409
						|| type == 1105 || type == 1109 || type == 1107
						|| type == 1110 || type == 1104 || type == 1111
						|| type == 1113 || type == 1304 || type == 1305
						|| type == 1404 || type == 1804 || type == 1108
						|| type == 1112 || type == 1306 || type == 1410
						|| type == 1310 || type == 1204 || type == 1311
						|| type == 1308) {

					if (deep.containsKey("agl")) {
						m.put("c", String.valueOf(deep.getDouble("agl")));
					}

					if (type == 1203 || type == 1108) {
						m.put("d", String.valueOf(deep.get("dr")));
					}
					if (type == 1112) {
						m.put("d", String.valueOf(deep.get("loc")));
					} else if (type == 1105) {

						JSONArray w_array = deep.getJSONArray("w_array");

						String d = "";

						for (int i = 0; i < w_array.size(); i++) {
							JSONObject w = w_array.getJSONObject(i);

							String tp = w.getString("tp");

							if (i == 0) {
								d = tp;
							} else {
								d += "|" + tp;
							}
						}

						m.put("d", d);
					} else if (type == 1107) {
						m.put("d", deep.getString("name"));
					} else if (type == 1306) {
						JSONArray arrResult = new JSONArray();
						JSONArray arr = deep.getJSONArray("info");
						if (arr != null) {
							for (Object object : arr) {
								String info = JSONObject.fromObject(object)
										.getString("info");
								arrResult.add(info);
							}
						}
						m.put("d", arrResult);
					}

					else if (type == 1109) {

						String tp = TipsSelectorUtils.convertElecEyeKind(deep
								.getInt("tp"));

						String loc = TipsSelectorUtils
								.convertElecEyeLocation(deep.getInt("loc"));

						double value = deep.getDouble("value");

						String d = tp + "|" + loc;

						if ((int) value != 0) {
							d += "|" + value;
						}

						m.put("d", d);
					} else if (type == 1104) {
						m.put("d", String.valueOf(deep.getInt("tp")));
						m.put("e", String.valueOf(deep.getInt("dir")));
					} else if (type == 1111) {
						m.put("d", String.valueOf(deep.getDouble("value")));
						m.put("e", String.valueOf(deep.getDouble("se")));
					} else if (type == 1310 || type == 1204) {
						JSONObject gSLoc = deep.getJSONObject("gSLoc");
						Geojson.coord2Pixel(gSLoc, z, px, py);
						JSONObject gELoc = deep.getJSONObject("gELoc");
						Geojson.coord2Pixel(gELoc, z, px, py);
						m.put("d", gSLoc.getJSONArray("coordinates"));
						m.put("e", gELoc.getJSONArray("coordinates"));
					} else if (type == 1305) {
						String time = deep.getString("time");
						if (StringUtils.isEmpty(time)) {
							m.put("d", 0);
						} else {
							m.put("d", 1);
						}

					} else if (type == 1308) {

						JSONArray arr = deep.getJSONArray("c_array");
						boolean flag = false;
						if (arr != null && arr.size() != 0) {
							for (Object object : arr) {
								JSONObject timeObj = JSONObject
										.fromObject(object);
								String time = timeObj.getString("time");
								if (StringUtils.isEmpty(time)) {
									flag = true;
									break;
								}
							}
							if (flag) {
								m.put("d", 0);
							} else {
								m.put("d", 1);
							}
						}
					}

				} else if (type == 1106) {
					m.put("c", String.valueOf(deep.getInt("tp")));
				} else if (type == 1102) {
					m.put("c", String.valueOf(deep.getInt("inCt")));
				} else if (type == 1103) {
					m.put("c", String.valueOf(deep.getInt("loc")));
				} else if (type == 1607) {
					m.put("c", geojson.getJSONArray("coordinates"));
					m.put("d", deep.getString("name"));
				} else if (type == 1705) {
					m.put("c", deep.getString("name"));
				} else if (type == 1707) {
					m.put("c", deep.getString("rdNm"));
					m.put("d", deep.getString("num"));
					m.put("e", deep.getString("src"));
				} else if (type == 1209) {
					m.put("c", deep.getString("name"));
				} else if (type == 1202) {
					m.put("c", String.valueOf(deep.getInt("num")));
				}
				else if (type == 1510 || type == 1514 || type == 1501
						|| type == 1515 || type == 1502 || type == 1503
						|| type == 1504 || type == 1505 || type == 1506
						|| type == 1508 || type == 1513 || type == 1512
						|| type == 1516 || type == 1507 || type == 1511
						|| type == 1517 || type == 1509) {
					JSONObject gSLoc = deep.getJSONObject("gSLoc");
					Geojson.coord2Pixel(gSLoc, z, px, py);
					JSONObject gELoc = deep.getJSONObject("gELoc");
					Geojson.coord2Pixel(gELoc, z, px, py);
					m.put("c", gSLoc.getJSONArray("coordinates"));
					m.put("d", gELoc.getJSONArray("coordinates"));

					if (type == 1510 || type == 1507 || type == 1511
							|| type == 1509) {

						m.put("e", deep.getString("name"));
					}

					if (type == 1517) {

						int tp = deep.getInt("tp");

						JSONArray vts = deep.getJSONArray("vt");

						String time = deep.getString("time");

						String vtName = "";
						// 类型拼接
						for (Object vt : vts) {
							vtName += "、"
									+ TipsSelectorUtils
											.convertUsageFeeVehicleType(Integer
													.parseInt(String
															.valueOf(vt)));
						}
						if (StringUtils.isNotEmpty(vtName)) {

							vtName = vtName.substring(1);
						}
						m.put("e", TipsSelectorUtils.convertUsageFeeType(tp)
								+ "|" + time + "|" + vtName);
					}
				} else if (type == 1604 || type == 1601 || type == 1602
						|| type == 1605 || type == 1606) {

					m.put("c", geojson.getJSONArray("coordinates"));

					if (type == 1601 || type == 1602) {

						m.put("d", geojson.getString("name"));
					}

				}

				else if (type == 1801 || type == 1806) {
					JSONArray feedbacks = JSONArray.fromObject(json
							.getString("feedback"));

					JSONArray a = new JSONArray();

					for (int j = 0; j < feedbacks.size(); j++) {
						JSONObject feedback = feedbacks.getJSONObject(j);

						if (feedback.getInt("type") == 6) {
							JSONArray content = feedback
									.getJSONArray("content");

							for (int i = 0; i < content.size(); i++) {
								JSONObject obj = content.getJSONObject(i);

								JSONObject geo = obj.getJSONObject("geo");

								String style = obj.getString("style");

								JSONObject o = new JSONObject();

								Geojson.coord2Pixel(geo, z, px, py);

								o.put("g", geo.getJSONArray("coordinates"));

								o.put("s", style);

								a.add(o);
							}

							break;
						}
					}
					m.put("c", a);
				}

				else if (type == 1803) {

					m.put("c", String.valueOf(deep.getDouble("agl")));

					int tp = deep.getInt("tp");

					if (tp == 1 || tp == 2) {

						m.put("d", deep.getString("pcd"));

					}
					// 暂时不实现
					else if (tp == 3) {

					}

				}
				// 路口名称
				else if (type == 1704) {
					m.put("c", deep.getString("name"));
				}
				snapshot.setM(m);

				array.add(snapshot.Serialize(null));

			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {

			}
		}
		return array;
	}

	/**
	 * 通过rowkey获取Tips
	 * 
	 * @param rowkey
	 * @return Tips JSON对象
	 * @throws Exception
	 */
	public JSONObject searchDataByRowkey(String rowkey) throws Exception {
		JSONObject json = new JSONObject();

		try {

			HBaseController controller = new HBaseController();

			ArrayList<KeyValue> list = controller.getTipsByRowkey(rowkey);

			if (list.isEmpty()) {
				throw new Exception("未找到rowkey对应的数据!");
			}

			json.put("rowkey", rowkey);

			for (KeyValue kv : list) {
				JSONObject injson = JSONObject
						.fromObject(new String(kv.value()));

				String key = new String(kv.qualifier());

				if (key.equals("feedback")) {
					json.put("feedback", injson);
				} else {
					json.putAll(injson);
				}
			}

		} catch (Exception e) {

			throw e;
		}

		return json;
	}

	/**
	 * 通过条件查询Tips
	 * 
	 * @param condition
	 *            查询条件
	 * @return Tips JSON数组
	 * @throws Exception
	 */
	public JSONArray searchDataByCondition(String condition) throws Exception {
		JSONArray array = new JSONArray();

		return array;
	}

	/**
	 * 统计tips
	 * 
	 * @param grids
	 * @param stages
	 * @return
	 * @throws Exception
	 */
	public JSONObject getStats(JSONArray grids, JSONArray stages)
			throws Exception {
		JSONObject jsonData = new JSONObject();

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTipsWeb(wkt, stages);

		for (JSONObject json : tips) {
			int type = Integer.valueOf(json.getInt("s_sourceType"));

			if (map.containsKey(type)) {
				map.put(type, map.get(type) + 1);
			} else {
				map.put(type, 1);
			}
		}

		JSONArray data = new JSONArray();

		Set<Entry<Integer, Integer>> set = map.entrySet();

		int num = 0;

		Iterator<Entry<Integer, Integer>> it = set.iterator();

		while (it.hasNext()) {
			Entry<Integer, Integer> en = it.next();

			num += en.getValue();

			JSONObject jo = new JSONObject();

			jo.put(en.getKey(), en.getValue());

			data.add(jo);
		}

		jsonData.put("total", num);

		jsonData.put("rows", data);

		return jsonData;
	}
	
	
	/**
	 * 统计子任务的tips总作业量,grid范围内滿足stage的数据条数
	 * 
	 * @param grids
	 * @param stages
	 * @return
	 * @throws Exception
	 */
	public int getTipsCountByStage(JSONArray grids, int stages)
			throws Exception {

		String wkt = GridUtils.grids2Wkt(grids);
		
		JSONArray stageJsonArr=new JSONArray();
		
		stageJsonArr.add(stages);
		
		List<JSONObject> tips = conn.queryTipsWeb(wkt, stageJsonArr);

		int total=tips.size();

		return total;
	}
	
	/**
	 * 统计子任务的tips总作业量,grid范围内滿足stage、tdStatus的数据条数
	 * 
	 * @param grids
	 * @param stages
	 * @return
	 * @throws Exception
	 */
	public int getTipsCountByStageAndTdStatus(JSONArray grids, int stages, int tdStatus)
			throws Exception {

		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTips(wkt, stages,tdStatus);

		int total=tips.size();

		return total;
	}

	/**
	 * 获取单种类型快照
	 * 
	 * @param grids
	 * @param stages
	 * @param type
	 * @param mdFlag
	 *            d:日编，m:月编。
	 * @return
	 * @throws Exception
	 */
	public JSONArray getSnapshot(JSONArray grids, JSONArray stages, int type,
			int dbId, String mdFlag) throws Exception {
		JSONArray jsonData = new JSONArray();

		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTipsWeb(wkt, type, stages);

		Map<Integer, String> map = null;

		Set<Integer> linkPids = new HashSet<Integer>();

		// 根据tip类型不同，查询关联对象的pid(这里是关联link)，用于e字段结果
		for (JSONObject json : tips) {

			JSONObject deep = JSONObject.fromObject(json.getString("deep"));

			try {
				if (type == 1201 || type == 1203 || type == 1101
						|| type == 1109 || type == 1111 || type == 1113
						|| type == 1202 || type == 1207 || type == 1208
						|| type == 1304 || type == 1305 || type == 1308
						|| type == 1311) {
					JSONObject f = deep.getJSONObject("f");

					if (f != null && ! f.isNullObject()) {
						if (f.getInt("type") == 1) {
							linkPids.add(Integer.valueOf(f.getString("id")));
						}
					}
				}

				else if (type == 1301 || type == 1407 || type == 1302
						|| type == 1403 || type == 1401 || type == 1402
						|| type == 1405 || type == 1406 || type == 1409
						|| type == 1105 || type == 1107 || type == 1703
						|| type == 1404 || type == 1804 || type == 1108
						|| type == 1112 || type == 1303 || type == 1306
						|| type == 1410 ) {
					JSONObject f = deep.getJSONObject("in");
					if (f != null && ! f.isNullObject()) {
						if (f.getInt("type") == 1) {
							linkPids.add(Integer.valueOf(f.getString("id")));
						}
					}
				} else if (type == 1110 || type == 1106 || type == 1104) {
					JSONObject f = deep.getJSONObject("out");
					if (f != null && ! f.isNullObject()) {
						if (f.getInt("type") == 1) {
							linkPids.add(Integer.valueOf(f.getString("id")));
						}
					}
				} else if (type == 1604 || type == 1514 || type == 1515
						|| type == 1502 || type == 1503 || type == 1504
						|| type == 1505 || type == 1506 || type == 1508
						|| type == 1513 || type == 1512 || type == 1516
						|| type == 1517 || type == 1605 || type == 1606
						|| type == 1310 || type == 1204) {
					JSONArray a = deep.getJSONArray("f_array");
					if (a != null) {
						for (int i = 0; i < a.size(); i++) {
							JSONObject f = a.getJSONObject(i);
							if (f.getInt("type") == 1) {
								linkPids.add(Integer.valueOf(f.getString("id")));
							}
						}

					}
				}
				// 删除记录
				else if (type == 2101) {
					linkPids.add(Integer.valueOf(Integer.valueOf(deep
							.getString("rId"))));
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(
						"data error：" + json.get("id") + ":" + e.getMessage(),
						e.getCause());
				throw new Exception("data error：" + json.get("id") + ":"
						+ e.getMessage(), e.getCause());

			}

		}

		Connection oraConn = null;

		try {

			oraConn = DBConnector.getInstance().getConnectionById(dbId);
			;

			RdLinkSelector selector = new RdLinkSelector(oraConn);

			// 关联link的道路名 map<linkPid, name>
			map = selector.loadNameByLinkPids(linkPids);

		} catch (Exception e) {

			throw e;
		} finally {
			try {
				oraConn.close();
			} catch (Exception e) {

			}
		}

		for (JSONObject json : tips) {

			try {

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(json.getString("id"));

				snapshot.setT(Integer.valueOf(json.getString("s_sourceType")));

				JSONObject glocation = JSONObject.fromObject(json
						.getString("g_location"));

				snapshot.setG(glocation.getJSONArray("coordinates"));

				JSONObject m = new JSONObject();

				if ("d".equals(mdFlag)) {

					m.put("a", json.getString("t_dStatus"));

				} else if ("m".equals(mdFlag)) {

					m.put("a", json.getString("t_mStatus"));
				}

				m.put("b", json.getString("t_lifecycle"));

				String operateDate = json.getString("t_operateDate");

				m.put("f",
						DateUtils.stringToLong(operateDate, "yyyyMMddHHmmss"));

				JSONObject deep = JSONObject.fromObject(json.getString("deep"));

				// 几个g需要显示：取Tips的geo
				if (type == 1604 || type == 1601 || type == 1602
						|| type == 1605 || type == 1606 || type == 1607) {

					JSONObject deepGeo = deep.getJSONObject("geo");

					snapshot.setG(deepGeo.getJSONArray("coordinates"));

				}

				// e字段的返回结果，不同类型不同
				// f
				if (type == 1201 || type == 1203 || type == 1101
						|| type == 1109 || type == 1111 || type == 1113 
						|| type == 1202 || type == 1207 || type == 1208 
						|| type == 1304 || type == 1305 || type == 1308 
						|| type == 1311) {
					JSONObject f = deep.getJSONObject("f");
					if (f != null && ! f.isNullObject()) {
						// type=1 :道路LINK，有名称，则显示道路名称，如果没有，则显示“无名路”
						String name = "无名路";

						if (f.getInt("type") == 1) {

							int linkPid = Integer.valueOf(f.getString("id"));

							if (map.containsKey(linkPid)) {

								name = map.get(linkPid);

							}
						}
						// type=2 :测线,时，显示“无道路”
						else {
							name = "无道路";
							m.put("e", name);
						}
						// 其他的，特殊的，需要补充其他字段说明的
						if (type == 1201) {

							int kind = deep.getInt("kind");

							name += "(K" + kind + ")";
						} else if (type == 1203) {

							int dr = deep.getInt("dr");

							if (dr == 1) {
								name += "(双方向)";
							} else {
								name += "(单方向)";
							}
						} else if (type == 1113) {

							JSONArray arr = deep.getJSONArray("value");

							String valueStr = "";

							for (Object object : arr) {
								double value = Double
										.valueOf(object.toString());
								valueStr += "|" + Math.round(value);

							}

							if (StringUtils.isNotEmpty(valueStr)) {
								valueStr = valueStr.substring(1);
							}
							name += "(" + valueStr + "km/h)";
						} else if (type == 1101 || type == 1111) {

							double value = deep.getDouble("value");

							name += "(" + Math.round(value) + "km/h)";
						} else if (type == 1202) {

							int side = deep.getInt("side");

							if (side == 0) {

								name += "(不应用)";

							} else if (side == 1) {

								name += "(左)";

							} else if (side == 1) {

								name += "(右)";

							}
						}
						m.put("e", name);
					}
				}
				// in

				else if (type == 1301 || type == 1407 || type == 1302
						|| type == 1403 || type == 1401 || type == 1402
						|| type == 1405 || type == 1406 || type == 1409
						|| type == 1105 || type == 1107 || type == 1703
						|| type == 1404 || type == 1804 || type == 1108
						|| type == 1112 || type == 1303 || type == 1306
						|| type == 1410 || type == 1104) {
					JSONObject f = deep.getJSONObject("in");

					if (f != null && ! f.isNullObject()) {
						if (f.getInt("type") == 1) {
							int linkPid = Integer.valueOf(f.getString("id"));

							if (map.containsKey(linkPid)) {

								String name = map.get(linkPid);

								m.put("e", name);
							} else {
								m.put("e", "无名路");
							}
						} else {
							m.put("e", "无道路");
						}
					}

				}
				// out
				else if (type == 1110 || type == 1106) {
					JSONObject f = deep.getJSONObject("out");
					if (f != null && ! f.isNullObject()) {
						String name = "无名路";
						// type=1 :道路LINK，有名称，则显示道路名称，如果没有，则显示“无名路”
						if (f.getInt("type") == 1) {
							int linkPid = Integer.valueOf(f.getString("id"));

							if (map.containsKey(linkPid)) {

								name = map.get(linkPid);
							}
						}
						// type=2 :测线,时，显示“无道路”
						else if (f.getInt("type") == 2) {
							name = "无道路";
						}
						// 其他的，特殊的，需要补充其他字段说明的
						// 退出线道路名（大门类型、大门方向）
						if (type == 1104) {
							// 0 EG； 1 KG； 2 PG（默认）；
							int gateType = deep.getInt("tp");
							// 0 未调查； 1 单向； 2 双向（默认）；
							int dir = deep.getInt("dir");

							String typeName = "PG";
							if (gateType == 0) {
								typeName = "EG";
							} else if (gateType == 1) {
								typeName = "KG";
							} else if (gateType == 2) {
								typeName = "PG";
							}

							String dirName = "双向";
							if (dir == 0) {
								dirName = "未调查";
							} else if (dir == 1) {
								dirName = "typeName";
							} else if (dir == 2) {
								dirName = " 双向";
							}

							name += "(" + typeName + "、" + dirName + ")";

						}

						m.put("e", name);
					}
				}
				// f_array
				else if (type == 1604 || type == 1514 || type == 1515
						|| type == 1502 || type == 1503 || type == 1504
						|| type == 1505 || type == 1506 || type == 1508
						|| type == 1513 || type == 1512 || type == 1516
						|| type == 1517 || type == 1605 || type == 1606
						|| type == 1310 || type == 1204) {
					JSONArray a = deep.getJSONArray("f_array");
					if (a != null) {
						boolean hasLink = false;

						for (int i = 0; i < a.size(); i++) {
							JSONObject f = a.getJSONObject(i);
							if (f.getInt("type") == 1) {

								hasLink = true;

								int linkPid = Integer
										.valueOf(f.getString("id"));

								if (map.containsKey(linkPid)) {

									String name = map.get(linkPid);

									m.put("e", name);

									break;
								}
							}
						}

						if (!hasLink) {
							m.put("e", "无道路");
						} else {
							if (!m.containsKey("e")) {
								m.put("e", "无名路");
							}
						}
						// 1515和1514确认了不需要加时间段
						/*
						 * if(type == 1515){ String name = m.getString("e");
						 * 
						 * String time = deep.getString("time");
						 * 
						 * if(time!=null && !time.isEmpty()){
						 * name+="("+time+")";
						 * 
						 * m.put("e", name); } }
						 */
					}
				}
				// 删除记录
				else if (type == 2101) {
					int linkPid = Integer.valueOf(deep.getString("rId"));
					String name = map.get(linkPid);
					m.put("e", name);
				} else if (type == 1704 || type == 1510 || type == 1107
						|| type == 1507 || type == 1511 || type == 1601
						|| type == 1602 || type == 1509 || type == 1705
						|| type == 1607 || type == 1209) {

					String name = deep.getString("name");

					if (name.equals("null")) {
						if (type == 1510) {
							m.put("e", "无名桥");
						} else {
							m.put("e", name);
						}
					} else {
						m.put("e", name);
					}
				} else if (type == 2001) {

					double length = deep.getDouble("len");

					double lengthInKM = Math.round(length / 10) / 100.0;

					m.put("e", "测线(" + lengthInKM + "公里)");
				} else if (type == 1901) {

					JSONArray a = deep.getJSONArray("n_array");

					if (a.size() > 0) {
						m.put("e", a.get(0));
					}
				}
				// 里程桩
				else if (type == 1707) {
					m.put("e",
							deep.getString("rdNm") + "("
									+ deep.getString("num") + ")");
				}
				 else if (type == 1501) {
					m.put("e", "上下线分离");
				} else if (type == 1801) {
					m.put("e", "立交");
				} else if (type == 1806) {
					m.put("e", "草图");
				} else if (type == 1205) {
					m.put("e", "SA");
				} else if (type == 1206) {
					m.put("e", "PA");
				} else if (type == 1102) {
					m.put("e", "红绿灯");
				} else if (type == 1103) {
					m.put("e", "红绿灯方位");
				} else if (type == 1701) {
					m.put("e", "障碍物");
				} else if (type == 1702) {
					m.put("e", "铁路道口");
				} else if (type == 1706) {
					m.put("e", "GPS打点");
				}

				if (!m.containsKey("e")) {
					m.put("e", JSONNull.getInstance());
				}

				snapshot.setM(m);

				jsonData.add(snapshot.Serialize(null));

			} catch (Exception e) {
				logger.error(
						"data convert error：rowkey:" + json.get("id")
								+ e.getMessage(), e.getCause());
				throw new Exception("data convert error：rowkey:"
						+ json.get("id") + e.getMessage(), e.getCause());
			}

		}

		return jsonData;
	}

	/**
	 * 根据grid和时间戳查询是否有可下载的数据
	 * 
	 * @param grid
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public int checkUpdate(String grid, String date) throws Exception {

		String wkt = GridUtils.grid2Wkt(grid);

		boolean flag = conn.checkTipsMobile(wkt, date);

		if (flag) {
			return 1;
		}

		return 0;
	}

	public static void main(String[] args) throws Exception {
		// JSONArray ja =
		// searchDataBySpatial("POLYGON ((113.70469 26.62879, 119.70818 26.62879, 119.70818 29.62948, 113.70469 29.62948, 113.70469 26.62879))");

		// System.out.println(ja.size());

		// System.out.println(checkUpdate("59567201","20151227163723"));
		// ConfigLoader
		// .initDBConn("C:/Users/wangshishuai3966/Desktop/config.properties");
		/*
		 * TipsSelector selector = new TipsSelector(); JSONArray types = new
		 * JSONArray(); types.add(1515);
		 * //selector.searchDataByTileWithGap(107946, 49617, 17, 20, types);
		 * selector.searchDataByRowkey("123"); //
		 * System.out.println(selector.searchDataByRowkey
		 * ("0212014bb47de20366413db30504af53243a00")); JSONArray grid =
		 * JSONArray .fromObject(
		 * "[59567101,59567102,59567103,59567104,59567201,60560301,60560302,60560303,60560304]"
		 * ); System.out.println(grid); JSONArray stage = new JSONArray();
		 * stage.add(1); int type = 1101; int projectId=11;
		 */
		// System.out.println(selector.getSnapshot(grid, stage, type,
		// projectId,"m"));
		// System.out.println(selector.getStats(a, b));

		// JSONArray types = new JSONArray();
		// types.add(1301);
		// types.add(1901);
		// System.out.println(selector.searchDataByTileWithGap(107944, 49615,
		// 17,
		// 20, types));

		JSONArray grids = new JSONArray();
		grids.add(59567513);
		grids.add(59567513);
		grids.add(59567503);
		
		String wkt = GridUtils.grids2Wkt(grids);

		System.out.println("0000000000----" + wkt);
		//POLYGON ((116.4375 40, 116.4375 40.02083, 116.46875 40.02083, 116.46875 40, 116.46875 39.97917, 116.46875 39.95833, 116.4375 39.95833, 116.4375 39.97917, 116.4375 40))
		//POLYGON ((116.4375 40, 116.4375 40.02083, 116.46875 40.02083, 116.46875 40, 116.46875 39.97917, 116.46875 39.95833, 116.4375 39.95833, 116.4375 39.97917, 116.4375 40))
		JSONObject b=new JSONObject();
		
		
		
		b.put("g_location", JSONObject.fromObject("{\"type\":\"Point\",\"coordinates\":[116.45815,40.00135]}"));
		
		Geometry g = GeoTranslator.geojson2Jts(JSONObject.fromObject("{\"type\":\"Point\",\"coordinates\":[116.45815,40.00135]}"));
		
		System.out.println(g);
		
	}

	/**
	 * 范围查询Tips 分类查询
	 * 
	 * @param wkt
	 * @return Tips JSON数组
	 * @throws Exception
	 */
	public JSONArray searchDataBySpatial(String wkt, int type, JSONArray stages)
			throws Exception {
		JSONArray array = new JSONArray();

		List<JSONObject> snapshots = conn.queryTipsWeb(wkt, type, stages);

		for (JSONObject snapshot : snapshots) {

			snapshot.put("t", 1);

			array.add(snapshot);
		}

		return array;
	}
}
