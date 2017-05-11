package com.navinfo.dataservice.engine.fcc.tips;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.HBaseController;
import com.navinfo.dataservice.dao.fcc.SearchSnapshot;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.hbase.async.KeyValue;

import java.sql.Connection;
import java.util.*;
import java.util.Map.Entry;

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
			JSONArray types, String mdFlag, String wktIndexName) throws Exception {
		JSONArray array = new JSONArray();

		String rowkey = null;

		try {

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			JSONArray stages = new JSONArray();

			if ("d".equals(mdFlag)) {

				stages.add(1);

				stages.add(2);

				stages.add(5);
				
				stages.add(6);

			} else if ("m".equals(mdFlag)) {

				stages.add(1);

				stages.add(2);

				stages.add(3);

			}
			// f是预处理渲染，如果不是，则需要过滤没有提交的预处理tips
			boolean isPre = false;

			if ("f".equals(mdFlag)) {
				isPre = true;
			}

			List<JSONObject> snapshots = conn.queryTipsWebType(wkt, types,
					stages, false, isPre, wktIndexName);

			for (JSONObject json : snapshots) {

				rowkey = json.getString("id");
				
				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(json.getString("id"));

				int type = Integer.valueOf(json.getString("s_sourceType"));

				snapshot.setT(String.valueOf(type));

				JSONObject geojson = JSONObject.fromObject(json
						.getString("g_location"));
				// 渲染的坐标都是屏幕坐标
				Geojson.coord2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				JSONObject m = new JSONObject();

				// 日编月编状态

				if ("d".equals(mdFlag)) {

					// 如果日编有问题待确认，则直接返回2. 20170208 和王屯 钟晓明确认结果
					if (json.getInt("t_dInProc") == 1) {

						m.put("a", 2);

					} else {

						m.put("a", json.getString("t_dStatus"));
					}

				} else if ("m".equals(mdFlag)) {

					// 如果月编有问题待确认，则直接返回2. 20170208 和王屯 钟晓明确认结果
					if (json.getInt("t_mInProc") == 1) {

						m.put("a", 2);
					} else {
						m.put("a", json.getString("t_mStatus"));
					}

				}

				JSONObject deep = JSONObject.fromObject(json.getString("deep"));

				// fc预处理8001要求返回功能等级
				if (type == 8001) {
					m.put("b", deep.getString("fc"));
					JSONObject geo=JSONObject.fromObject(deep.get("geo"));
					Geojson.coord2Pixel(geo, z, px, py);
					m.put("c", geo.getJSONArray("coordinates"));

				} else {
					m.put("b", json.getString("t_lifecycle"));
				}

				//20170412赵航输入，转为屏幕坐标
				JSONObject g_guide = JSONObject.fromObject(json
						.getString("g_guide"));
				Geojson.coord2Pixel(g_guide, z, px, py);

				// 8001和8002的的数据，新增guide已经赋值，无需特殊处理了
				m.put("h", g_guide.getJSONArray("coordinates"));

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
						|| type == 1308 || type == 1114 || type == 1115) {

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
					m.put("c", deep.getString("rdName"));
					m.put("d", deep.getString("num"));
					m.put("e", deep.getString("src"));
				} else if (type == 1202) {
					m.put("c", String.valueOf(deep.getInt("num")));
				} else if (type == 1510 || type == 1514 || type == 1501
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
					// 20170207修改，需求来源于：赵航——有个需求是，如果上传的步行街有时间段，我们要渲染不同的图标，现在渲染接口没有时间段这个字段
					if (type == 1507) {
						m.put("f", deep.getString("time"));
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

						m.put("d", deep.getString("name"));
					}

				}

				else if (type == 1801 || type == 1806 || type == 8002) {

					JSONObject feebackObj = JSONObject.fromObject(json
							.getString("feedback"));

					JSONArray f_array = feebackObj.getJSONArray("f_array");

					JSONArray a = new JSONArray();

					for (int j = 0; j < f_array.size(); j++) {
						JSONObject feedback = f_array.getJSONObject(j);

						if (feedback.getInt("type") == 6) {
							JSONArray content = feedback
									.getJSONArray("content");

							for (int i = 0; i < content.size(); i++) {
								JSONObject obj = content.getJSONObject(i);

								JSONObject geo = obj.getJSONObject("geo");

								String style = obj.getString("style");

								JSONObject o = new JSONObject();

								Geojson.coord2Pixel(geo, z, px, py);

								o.put("t", geo.getString("type"));

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

				// 20170217修改，变更输入：王屯 赵航
				if (type == 2001) {
					JSONObject obj = new JSONObject();
					obj.put("ln", deep.getInt("ln"));
					obj.put("kind", deep.getInt("kind"));
					m.put("e", obj);
				}

				// 返回差分结果：20160213修改
				JSONObject tipdiff = null;

				if (json.containsKey("tipdiff")) {

					tipdiff = JSONObject.fromObject(json.getString("tipdiff"));

					// 坐标转换，需要根据类型转换为屏幕坐标
					JSONObject convertGeoDiff = converDiffGeo(type, tipdiff, z,
							px, py);

					if (convertGeoDiff != null) {
						m.put("i", convertGeoDiff);
					}
				}

				// 20170220新增：是否有附件、是否有时间段、是否有线编号 （需要判空）--输入：陈清友 王屯

				// 20170220新增：返回退出线的编号和坐标位置

				// 1.是否有照片
				m.put("k", 0); // 默认：put一个0（有可能有f_array为空的情况），如果有信息，则再put 1。

				hasAttachement(json, m);

				// 2.是否有时间段

				m.put("l", 0); // 默认：put一个0，如果有信息，则再put 1。

				asTimeAndNotNull(type, m, deep);

				// 3.是否有退出线编号
				m.put("n", 0); // 默认无
				// 4. 查找线编号
				// 3.1   4.1 判断是否有线编号同时返回线编号和坐标
				getOutNumAndGeo(type, z, px, py, m, deep);
				
                //20170508 tips渲染接口增加2个返回值：
                // 中线状态（1是中线成果0不是中线成果），快线状态（1是快线成果0不是快线成果）
                int s_qTaskId = json.getInt("s_qTaskId");//快线任务号
                if(s_qTaskId != 0) {
                    s_qTaskId = 1;
                }
                m.put("quickFlag", s_qTaskId);

                int s_mTaskId = json.getInt("s_mTaskId");//快线任务号
                if(s_mTaskId != 0) {
                    s_mTaskId = 1;
                }
                m.put("mediumFlag", s_mTaskId);

				snapshot.setM(m);

				array.add(snapshot.Serialize(null));

			}
		} catch (Exception e) {
			logger.error("渲染报错，数据错误：" + e.getMessage() + rowkey);
			throw new Exception(e.getMessage() + "rowkey:" + rowkey, e);
		} finally {
			try {

			} catch (Exception e) {

			}
		}
		return array;
	}

	/**
	 * @Description:TOOD
	 * @param json
	 * @param m
	 * @author: y
	 * @time:2017-2-20 下午2:53:24
	 */
	private void hasAttachement(JSONObject json, JSONObject m) {
		if (json.containsKey("feedback")) {

			m.put("k", 0); // 先put一个0（有可能有f_array为空的情况），如果有，则put 1。

			JSONObject feedBack = JSONObject.fromObject(json
					.get("feedback"));

			JSONArray f_array = feedBack.getJSONArray("f_array");

			for (Object object : f_array) {

				JSONObject info = JSONObject.fromObject(object);

				if (info.getInt("type") == 1
						|| info.getInt("type") == 2
						|| info.getInt("type") == 3) {

					m.put("k", 1);

					break;
				}
			}
		}
	}

	/**
	 * @Description:TOOD
	 * @param type
	 * @param m
	 * @param deep
	 * @author: y
	 * @time:2017-2-20 下午2:52:19
	 */
	private void asTimeAndNotNull(int type, JSONObject m, JSONObject deep) {
		// 2.1deep.time(一级属性)
		if (type == 1304 || type == 1305 || type == 1203
				|| type == 1514 || type == 1507 || type == 1517
				|| type == 1515 || type == 1516) {

			if (!StringUtils.isEmpty(deep.getString("time"))) {

				m.put("l", 1);
			}
		}

		// 2.2二级属性.不同tips类型不同解析方式

		// [c_array].time
		else if (1308 == type) {

			JSONArray c_array = deep.getJSONArray("c_array");

			for (Object object : c_array) {

				JSONObject info = JSONObject.fromObject(object);

				if (StringUtils.isNotEmpty(info.getString("time"))) {

					m.put("l", 1);

					break;
				}

			}
		}

		// 1310、1204 [ln].time

		else if (1310 == type || 1204 == type) {

			JSONArray c_array = deep.getJSONArray("ln");

			for (Object object : c_array) {

				JSONObject info = JSONObject.fromObject(object);

				if (StringUtils.isNotEmpty(info.getString("time"))) {

					m.put("l", 1);

					break;
				}

			}
		}

		// 1311 [ln].[o_array].time

		else if (1311 == type) {

			JSONArray c_array = deep.getJSONArray("ln");

			for (Object object : c_array) {

				JSONObject info = JSONObject.fromObject(object);

				JSONArray o_array = info.getJSONArray("o_array");

				for (Object object2 : o_array) {

					JSONObject oInfo = JSONObject.fromObject(object2);

					if (StringUtils.isNotEmpty(oInfo.getString("time"))) {

						m.put("l", 1);

						break;
					}
				}
			}
		}

		// 1111 [d_array].time

		else if (1111 == type) {

			JSONArray c_array = deep.getJSONArray("d_array");

			for (Object object : c_array) {

				JSONObject info = JSONObject.fromObject(object);

				if (StringUtils.isNotEmpty(info.getString("time"))) {

					m.put("l", 1);

					break;
				}
			}
		}

		// 1105 [w_array].time
		else if (1105 == type) {

			JSONArray c_array = deep.getJSONArray("w_array");

			for (Object object : c_array) {

				JSONObject info = JSONObject.fromObject(object);

				if (StringUtils.isNotEmpty(info.getString("time"))) {

					m.put("l", 1);

					break;
				}
			}
		}

		// 1302 [o_array].time

		else if (1302 == type) {

			JSONArray c_array = deep.getJSONArray("o_array");

			for (Object object : c_array) {

				JSONObject info = JSONObject.fromObject(object);

				if (StringUtils.isNotEmpty(info.getString("time"))) {

					m.put("l", 1);

					break;
				}
			}
		}

		// 1303 [o_array].[c_array].time

		else if (1303 == type) {

			JSONArray c_array = deep.getJSONArray("o_array");

			for (Object object : c_array) {

				JSONObject info = JSONObject.fromObject(object);

				JSONArray o_array = info.getJSONArray("c_array");

				for (Object object2 : o_array) {

					JSONObject oInfo = JSONObject.fromObject(object2);

					if (StringUtils.isNotEmpty(oInfo.getString("time"))) {

						m.put("l", 1);

						break;
					}
				}
			}
		}
	}

	/**
	 * @Description:获取线编号和线编号坐标,同时判断是否有线编号
     * @param type
	 * @param z
	 * @param px
	 * @param py
	 * @param m
	 *            ：渲染返回值中的m
	 * @param deep
	 * @author: y
	 * @time:2017-2-20 下午2:02:17
	 */
	private void getOutNumAndGeo(int type, int z, double px, double py,
			JSONObject m, JSONObject deep) {

		JSONArray reusltArr = new JSONArray();

		// 1301 （车信） [o_array].[d_array].[out] num geo
		if (type == 1301) {

			JSONArray o_array = deep.getJSONArray("o_array");

			for (Object object : o_array) {

				JSONObject info = JSONObject.fromObject(object);

				JSONArray d_array = info.getJSONArray("d_array");

				for (Object object2 : d_array) {

					JSONObject dInfo = JSONObject.fromObject(object2);

					JSONObject out = dInfo.getJSONObject("out");

				/*	if (outArr != null && !outArr.isEmpty()) {

						for (Object object3 : outArr) {
*/
							JSONObject obj = assembleOutNumAndGeoResultFromObj(
									z, px, py, out);

							reusltArr.add(obj);
						//}

				//	}
				}
			}
		}

		// 1310（公交车道） [ln].[o_array] num geo
		else if (type == 1310) {

			JSONArray lnArr = deep.getJSONArray("ln");

			for (Object object : lnArr) {

				JSONObject info = JSONObject.fromObject(object);

				JSONArray o_array = info.getJSONArray("o_array");

				if (o_array != null && !o_array.isEmpty()) {

					for (Object object3 : o_array) {

						JSONObject obj = assembleOutNumAndGeoResultFromObj(z,
								px, py, object3);

						reusltArr.add(obj);
					}

				}
			}
		}
		// 1311（可变导向车道）[ln].[o_array].out  （out是个对象） num geo

		else if (type == 1311) {

			JSONArray lnArr = deep.getJSONArray("ln");

			for (Object object : lnArr) {

				JSONObject info = JSONObject.fromObject(object);

				JSONArray o_array = info.getJSONArray("o_array");

				for (Object object2 : o_array) {

					JSONObject dInfo = JSONObject.fromObject(object2);

					JSONObject outObj = dInfo.getJSONObject("out"); // 是个对象

					JSONObject obj = assembleOutNumAndGeoResultFromObj(z, px,
							py, outObj);
					
					reusltArr.add(obj);

				}
			}
		}
		// 1407（高速分歧）         [o_array].out  （out是个对象） num geo
		// 1406(实景图)     [o_array].out  （out是个对象） num geo
		else if (type == 1407 || type == 1406) {

			JSONArray o_array = deep.getJSONArray("o_array");

			for (Object object : o_array) {

				JSONObject info = JSONObject.fromObject(object);

				JSONObject dInfo = JSONObject.fromObject(info);

				JSONObject outObj = dInfo.getJSONObject("out"); // 是个对象

				JSONObject obj = assembleOutNumAndGeoResultFromObj(z, px,
						py, outObj);
				
				reusltArr.add(obj);

			}
		}
		
		// 1302（普通交限标记） [o_array].[out] num geo
		// 1303（卡车交限标记）[o_array].[out] num geo
		// 1306（路口语音引导）[o_array].[out] num geo
		else if (type == 1302 || type == 1303 || type == 1306 ) {

			JSONArray o_array = deep.getJSONArray("o_array");

			for (Object object : o_array) {

				JSONObject info = JSONObject.fromObject(object);

				JSONArray ourArr = info.getJSONArray("out");

				for (Object object2 : ourArr) {

					JSONObject outInfo = JSONObject.fromObject(object2);

					JSONObject obj = assembleOutNumAndGeoResultFromObj(z, px,
							py, outInfo);
					
					reusltArr.add(obj);

				}
			}
		}
		
		// 1102  [f_array].f  (f唯一是对象) num geo
		
		else if (type == 1102 ) {

			JSONArray o_array = deep.getJSONArray("f_array");

			for (Object object : o_array) {

				JSONObject info = JSONObject.fromObject(object);

				JSONObject dInfo = JSONObject.fromObject(info);

				JSONObject outObj = dInfo.getJSONObject("f"); // 是个对象

				JSONObject obj = assembleOutNumAndGeoResultFromObj(z, px,
						py, outObj);
				
				reusltArr.add(obj);

			}
		}
		
		// ------------公共的
		if (reusltArr.size() != 0) {
			m.put("n", 1);  //有线编号

			m.put("f", reusltArr);
		}
	}

	/**
	 * @Description:TOOD
	 * @param z
	 * @param px
	 * @param py
	 * @param object3
	 * @author: y
	 * @time:2017-2-20 下午2:06:29
	 */
	private JSONObject assembleOutNumAndGeoResultFromObj(int z, double px,
			double py, Object object3) {
		JSONObject outInfo = JSONObject.fromObject(object3);

		int num = outInfo.getInt("num");

		JSONObject geo = outInfo.getJSONObject("geo");

		// 渲染的坐标都是屏幕坐标
		Geojson.coord2Pixel(geo, z, px, py);

		JSONObject obj = new JSONObject();

		obj.put("num", num);

		obj.put("geo", geo);

		return obj;
	}

	/**
	 * @Description:tipdiff数据坐标转换，将差分结果中的坐标转换为屏幕坐标
	 * @param type
	 * @param tipdiff
	 * @return
	 * @author:liya
	 * @param py
	 * @param px
	 * @param z
	 * @time:2017-2-13下午1:34:53
	 */
	private JSONObject converDiffGeo(int type, JSONObject tipdiff, int z,
			double px, double py) {

		if (tipdiff == null || tipdiff.isEmpty())
			return null;

		JSONArray diffArr = tipdiff.getJSONArray("diff_array");

		JSONArray diffArrNew = new JSONArray();

		for (Object object : diffArr) {

			JSONObject json = JSONObject.fromObject(object);

			if (json.containsKey("geometry")) {

				JSONObject geojson = JSONObject.fromObject(json
						.getString("geometry"));
				// 渲染的坐标都是屏幕坐标
				Geojson.coord2Pixel(geojson, z, px, py);

				json.put("geometry", geojson);

			}

			diffArrNew.add(json);

		}

		tipdiff.put("diff_array", diffArrNew);

		return tipdiff;
	}

	public JSONArray searchDataByWkt(String wkt, JSONArray types, String mdFlag, String wktIndexName)
			throws Exception {
		JSONArray array = new JSONArray();

		try {

			JSONArray stages = new JSONArray();

			List<JSONObject> snapshots = conn.queryTipsWebType(wkt, types,
					stages, true, wktIndexName);

			for (JSONObject json : snapshots) {
				JSONObject result = new JSONObject();

				int type = Integer.valueOf(json.getString("s_sourceType"));

				String geometry = json.getString("g_location");

				// 采集、日编、月编状态
				if ("c".equals(mdFlag)) {

					result.put("status", json.getString("t_cStatus"));

				} else if ("d".equals(mdFlag)) {

					result.put("status", json.getString("t_dStatus"));

				} else if ("m".equals(mdFlag)) {

					result.put("status", json.getString("t_mStatus"));

				}

				JSONObject deep = JSONObject.fromObject(json.getString("deep"));

				// g字段重新赋值的（显示坐标：取Tips的geo）
				if (type == 1601 || type == 1602 || type == 1603
						|| type == 1604 || type == 1605 || type == 1606
						|| type == 1607 || type == 1901 || type == 2001) {

					JSONObject deepGeo = deep.getJSONObject("geo");

					geometry = deepGeo.toString();

				} else if (type == 1501 || type == 1502 || type == 1503
						|| type == 1504 || type == 1505 || type == 1506
						|| type == 1507 || type == 1508 || type == 1509
						|| type == 1510 || type == 1511 || type == 1512
						|| type == 1513 || type == 1514 || type == 1515
						|| type == 1516 || type == 1517) {

					JSONObject gSLoc = deep.getJSONObject("gSLoc");

					geometry = gSLoc.toString();

				}

				result.put("geometry", geometry);

				array.add(result);

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
				System.out.println(kv);
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
	 * 通过rowkey获取Tips(返回符合规格模型的数据)
	 * 
	 * @param rowkey
	 * @return Tips JSON对象
	 * @throws Exception
	 */
	public JSONObject searchDataByRowkeyNew(String rowkey) throws Exception {
		JSONObject json = new JSONObject();

		try {

			HBaseController controller = new HBaseController();

			ArrayList<KeyValue> list = controller.getTipsByRowkey(rowkey);

			if (list.isEmpty()) {
				throw new Exception("未找到rowkey对应的数据!");
			}

			json.put("rowkey", rowkey);

			for (KeyValue kv : list) {
				System.out.println(kv);
				JSONObject injson = JSONObject
						.fromObject(new String(kv.value()));

				String key = new String(kv.qualifier());
				
				System.out.println("key:"+key);

			/*	if (key.equals("feedback")) {
					json.put("feedback", injson);
				} else {
					json.putAll(injson);
				}*/
				json.put(key, injson);
				
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
	 * @param subtaskId :日编任务号
	 * @return
	 * @throws Exception
	 */
	public JSONObject getStats(JSONArray grids, JSONArray stages, int subtaskId)
			throws Exception {
		JSONObject jsonData = new JSONObject();
		
		Set<Integer> taskSet = getTaskIdsUnderSameProject(subtaskId); //查询该任务所对应的项目下的所有的任务号（快线任务号），月编作业方式还没定，暂时不管

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTipsWeb(wkt, stages, taskSet);

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
		return getTipsCountByStageAndWkt(wkt, stages);
	}

	/**
	 * 统计子任务的tips总作业量,grid范围内滿足stage的数据条数
	 * 
	 * @param wkt
	 * @param stages
	 * @return
	 * @throws Exception
	 */
	public int getTipsCountByStageAndWkt(String wkt, int stages)
			throws Exception {

		// String wkt = GridUtils.grids2Wkt(grids);

		JSONArray stageJsonArr = new JSONArray();

		stageJsonArr.add(stages);

		List<JSONObject> tips = conn.queryTipsWeb(wkt, stageJsonArr);

		int total = tips.size();

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
	public int getTipsCountByStageAndTdStatus(JSONArray grids, int stages,
			int tdStatus) throws Exception {
		String wkt = GridUtils.grids2Wkt(grids);
		return getTipsCountByStageAndTdStatusAndWkt(wkt, stages, tdStatus);
	}

	/**
	 * 统计子任务的tips总作业量,grid范围内滿足stage、tdStatus的数据条数
	 * 
	 * @param wkt
	 * @param stages
     * @param tdStatus
	 * @return
	 * @throws Exception
	 */
	public int getTipsCountByStageAndTdStatusAndWkt(String wkt, int stages,
			int tdStatus) throws Exception {

		List<JSONObject> tips = conn.queryTips(wkt, stages, tdStatus);

		int total = tips.size();

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
	 * @param subtaskid :日编任务号
	 * @return
	 * @throws Exception
	 */
	public JSONArray getSnapshot(JSONArray grids, JSONArray stages, int type,
			int dbId, String mdFlag, int subtaskid) throws Exception {
		JSONArray jsonData = new JSONArray();

		String wkt = GridUtils.grids2Wkt(grids);

		// f是预处理渲染，如果不是，则需要过滤没有提交的预处理tips
		boolean isPre = false;

		if ("f".equals(mdFlag)) {
			isPre = true;
		}
		
		Set<Integer> taskSet = getTaskIdsUnderSameProject(subtaskid); //查询该任务所对应的项目下的所有的任务号（快线任务号），月编作业方式还没定，暂时不管

		List<JSONObject> tips = conn.queryWebTips(wkt, type, stages, isPre,taskSet);

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
						|| type == 1311 || type == 1114 || type == 1115) {
					JSONObject f = deep.getJSONObject("f");

					if (f != null && !f.isNullObject()) {
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
						|| type == 1410) {
					JSONObject f = deep.getJSONObject("in");
					if (f != null && !f.isNullObject()) {
						if (f.getInt("type") == 1) {
							linkPids.add(Integer.valueOf(f.getString("id")));
						}
					}
				} else if (type == 1110 || type == 1106 || type == 1104) {
					JSONObject f = deep.getJSONObject("out");
					if (f != null && !f.isNullObject()) {
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

				snapshot.setT(json.getString("s_sourceType"));

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
						|| type == 1311 || type == 1114 || type == 1115) {
					JSONObject f = deep.getJSONObject("f");
					if (f != null && !f.isNullObject()) {
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
						} else if (type == 1101 || type == 1111 || type == 1114
								|| type == 1115) {

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

					if (f != null && !f.isNullObject()) {
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
					if (f != null && !f.isNullObject()) {
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
						|| type == 1607) {

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
						m.put("e", a.get(0).toString());
					}
				}
				// 里程桩
				else if (type == 1707) {
					m.put("e",
							deep.getString("rdName") + "("
									+ deep.getString("num") + ")");
				} else if (type == 1501) {
					m.put("e", "上下线分离");
				} else if (type == 1801) {
					m.put("e", "立交");
				} else if (type == 1806) {
					m.put("e", "草图");
				} else if (type == 8002) {
					m.put("e", "接边标识");
				} else if (type == 8001) {
					m.put("e", "FC预处理");
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
	 * @Description:调用任务管理api，获取该任务所对应项目下的所有快线任务号
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-4-19 下午1:32:21
	 */
	private Set<Integer> getTaskIdsUnderSameProject(int subtaskId) throws Exception {
		// 调用 manapi 获取 任务类型、及任务号
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		
		Set<Integer>  taskSet = manApi.getCollectTaskIdByDaySubtask(subtaskId);
		
		return taskSet;
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

		boolean flag = conn.checkTipsMobile(wkt, date,
				TipsUtils.notExpSourceType);

		if (flag) {
			return 1;
		}

		return 0;
	}

	/**
	 * 范围查询Tips 分类查询
	 * 
	 * @param wkt
	 * @return Tips JSON数组
	 * @throws Exception
	 */
	public JSONArray searchDataBySpatial(String wkt, int editTaskId, int type, JSONArray stages)
			throws Exception {
		JSONArray array = new JSONArray();

		//查询日编或者月编任务对应的采集任务ID
		Set<Integer> taskList = getTaskIdsUnderSameProject(editTaskId);
		List<JSONObject> snapshots = conn
				.queryTipsWeb(wkt, type, stages, false, taskList);

		for (JSONObject snapshot : snapshots) {

			snapshot.put("t", 1);

			array.add(snapshot);
		}
		return array;
	}

	/**
	 * @Description:通过rowkey数组返回数据列表
	 * @param rowkeyArr
	 *            rowkey数组
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-2-18 下午3:34:09
	 */
	public JSONArray searchDataByRowkeyArr(JSONArray rowkeyArr)
			throws Exception {

		JSONArray resultArr = new JSONArray();

		try {

			org.apache.hadoop.hbase.client.Connection hbaseConn = HBaseConnector
					.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			List<Get> gets = new ArrayList<Get>();

			for (int i = 0; i < rowkeyArr.size(); i++) {

				String rowkey = rowkeyArr.getString(i);

				Get get = new Get(rowkey.getBytes());

				gets.add(get);
			}

			Result[] results = htab.get(gets);

			for (Result result : results) {

				if (result.isEmpty()) {
					continue;
				}

				JSONObject obj = new JSONObject();
				obj.put("rowkey", new String(result.getRow()));
				List<Cell> ceList = result.listCells();
				if (ceList != null && ceList.size() > 0) {
					for (Cell cell : ceList) {
						String value = Bytes.toString(cell.getValueArray(),
								cell.getValueOffset(), cell.getValueLength());
						String colName = Bytes.toString(
								cell.getQualifierArray(),
								cell.getQualifierOffset(),
								cell.getQualifierLength());

						JSONObject injson = JSONObject.fromObject(value);

						if (colName.equals("feedback")) {
							obj.put("feedback", injson);
						} else {
							obj.putAll(injson);
						}

					}
				}

				resultArr.add(obj);
			}

			htab.close();
		} catch (Exception e) {
			throw new Exception("查询tips出错：" + e.getMessage(), e);
		}

		return resultArr;
	}

	/**
	 * @Description:根据任务号+tips类型返回任务号范围内的tips
	 * @param souceTypes:tips类型
	 * @param taskId:任务号
	 * @param taskType：任务类型
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-4-13 上午9:07:15
	 */
	public List<JSONObject> getTipsByTaskIdAndSourceTypes(JSONArray souceTypes,
			int taskId, int taskType) throws Exception {
		
		List<JSONObject> snapshots=conn.queryTipsByTaskTaskSourceTypes(souceTypes,taskId,taskType);
		
		return snapshots;
	}

	/**
	 * @Description:按照任务号查找tips
	 * @param taskId
	 * @param taskType
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-4-14 下午4:55:04
	 */
	public List<JSONObject> getTipsByTaskId(int taskId, int taskType) throws Exception {
		
		List<JSONObject> snapshots=conn.queryTipsByTask(taskId,taskType);
		
		return snapshots;
		
	}

	/**
	 * @Description:根据任务查询tips，返回tips的所有grids
	 * @param collectTaskid
	 * @param q_TASK_TYPE
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-4-19 下午8:51:14
	 */
	public Set<Integer> getGridsListByTask(int collectTaskid, int q_TASK_TYPE) throws Exception {
		
		List<JSONObject> tipsList=conn.queryTipsByTask(collectTaskid, q_TASK_TYPE);
		
		Set<Integer> gridsSet= new HashSet<Integer>();
		
		Set<String> grids=new  HashSet<String>();
		
		for (JSONObject json : tipsList) {
			
			String wkt=json.getString("wkt");
			
			Geometry geo =  GeoTranslator.wkt2Geometry(wkt);
			
			Set<String> grid=TipsGridCalculate.calculate(geo);
			
			grids.addAll(grid);
			
            }
		
		for (String str : grids) {
			
			Integer grid=Integer.valueOf(str);
			
			gridsSet.add(grid);
		}
		
		return gridsSet;
	}

	/**
	 * 快线tips日编状态实时统计
	 * @param collectTaskIds
	 * @return
	 */
	public List<Map> getCollectTaskTipsStats(Set<Integer> collectTaskIds) throws Exception {
		List<Map> list = new ArrayList<>();
		List<JSONObject> snapshots = conn.queryCollectTaskTips(collectTaskIds);
		Map<String,int[]> statsMap = new HashMap<>();
		for(JSONObject snapshot : snapshots) {
			String wkt = snapshot.getString("wkt");//统计坐标
			Point point = GeometryUtils.getPointByWKT(wkt);
			Coordinate coordinate = point.getCoordinates()[0];
			String gridId = CompGridUtil.point2Grids(coordinate.x, coordinate.y)[0];
			int dStatus = snapshot.getInt("t_dStatus");
			if(statsMap.containsKey(gridId)) {
				int[] statsArray = statsMap.get(gridId);
				if(dStatus == 0) {//未完成
					statsArray[0] += 1;
				}else if(dStatus == 1) {//已完成
					statsArray[1] += 1;
				}
			} else {
				int[] statsArray = new int[]{0,0};
				if(dStatus == 0) {//未完成
					statsArray[0] += 1;
				}else if(dStatus == 1) {//已完成
					statsArray[1] += 1;
				}
				statsMap.put(gridId, statsArray);
			}
		}
		if(statsMap.size() > 0) {
			for(String gridId : statsMap.keySet()) {
				Map<String, Integer> map = new HashMap<>();
				map.put("gridId", Integer.valueOf(gridId));
				int[] statsArray = statsMap.get(gridId);
				map.put("finished",statsArray[1]);
				map.put("unfinished",statsArray[0]);
				list.add(map);
			}
		}
		return list;
	}
    public static void main(String[] args) throws Exception {
        TipsSelector solrSelector = new TipsSelector();
        JSONArray types = new JSONArray();
        System.out.println("reusut:--------------\n"+solrSelector.searchDataByTileWithGap(13492, 6201, 14,
                40, types,"d","wktLocation"));
    }
}