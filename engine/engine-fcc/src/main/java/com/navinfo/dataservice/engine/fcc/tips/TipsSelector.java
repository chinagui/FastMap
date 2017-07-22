package com.navinfo.dataservice.engine.fcc.tips;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.*;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.fcc.tips.solrquery.OracleWhereClause;
import com.navinfo.dataservice.engine.fcc.tips.solrquery.TipsRequestParam;
import com.navinfo.dataservice.engine.fcc.tips.solrquery.TipsRequestParamSQL;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hbase.async.KeyValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;
import java.util.Map.Entry;

/**
 * Tips查询
 */
public class TipsSelector {

	private static final Logger logger = Logger.getLogger(TipsSelector.class);

    public static String TIP_OLD_KEY_NAME = "old";

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
		TipsRequestParamSQL param = new TipsRequestParamSQL();
        String sql = param.getTipsWebSql(wkt);
        Connection oracleConn = DBConnector.getInstance().getTipsIdxConnection();
        try{
	        List<TipsDao> tips =  new TipsIndexOracleOperator(oracleConn).query(sql, wkt);
	
			for (TipsDao tip:tips) {
				JSONObject snapshot = JSONObject.fromObject(tip);
				snapshot.put("t", 1);
				array.add(snapshot);
			}
        }finally{
        	DbUtils.closeQuietly(oracleConn);
        }
		return array;
	}

	/**
	 * @Description:范围瓦片查询Tips
	 * @param parameter
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-7-2 上午10:08:16
	 */
	public JSONArray searchDataByTileWithGap(String parameter) throws Exception {
		JSONArray array = new JSONArray();

		String rowkey = null;
		Connection conn = null;
		try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

			String pType = null;
			if(jsonReq.containsKey("pType")){
				pType = jsonReq.getString("pType");
			}

            if(StringUtils.isEmpty(pType) || pType.equals("web")) {
                JSONArray workStatus = null;
                if(jsonReq.containsKey("workStatus")) {
                    workStatus = jsonReq.getJSONArray("workStatus");
                }
                if(workStatus == null || workStatus.size() == 0){
                    return array;
                }
            }else if(pType.equals("ms")) {
                JSONArray noQFilter = null;
                if (jsonReq.containsKey("noQFilter")) {
                    noQFilter = jsonReq.getJSONArray("noQFilter");
                }
                if(noQFilter == null || noQFilter.size() == 0){
                    return array;
                }
            }

            int x = jsonReq.getInt("x");
            int y = jsonReq.getInt("y");
            int z = jsonReq.getInt("z");
			int gap = jsonReq.getInt("gap");
			double px = MercatorProjection.tileXToPixelX(x);
			double py = MercatorProjection.tileYToPixelY(y);
            String mdFlag = null;
            if(jsonReq.containsKey("mdFlag")) {
                mdFlag = jsonReq.getString("mdFlag");
            }
            TipsRequestParamSQL param = new TipsRequestParamSQL();
			String sql = param.getByTileWithGap(parameter);
			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			conn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator operator = new TipsIndexOracleOperator(conn);
			List<TipsDao> snapshots = operator.query(sql, wkt);
			for (TipsDao tipsDao : snapshots) {
				JSONObject json = JSONObject.fromObject(tipsDao);

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
                if(StringUtils.isNotEmpty(mdFlag)) {
                    if ("d".equals(mdFlag)) {
                        // 如果日编有问题待确认，则直接返回2. 20170208 和王屯 钟晓明确认结果
                        int dEditStatus = json.getInt("t_dEditStatus");
                        m.put("a", String.valueOf(dEditStatus));
                    } else if ("m".equals(mdFlag)) {
                        // 如果月编有问题待确认，则直接返回2. 20170208 和王屯 钟晓明确认结果
                        int mEditStatus = json.getInt("t_mEditStatus");
                        m.put("a", String.valueOf(mEditStatus));
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
				} else if (type == 2001 || type == 1901 || type == 2201 || type == 2002) {

					JSONObject geo = deep.getJSONObject("geo");

					Geojson.coord2Pixel(geo, z, px, py);

					m.put("c", geo.getJSONArray("coordinates"));

                    if(type == 2201) {//20170517 地下通道过街天桥
                        m.put("d", deep.getInt("tp"));

                        JSONArray allGeoPArray = new JSONArray();//geoP
                        JSONArray allAccessArray = new JSONArray();//access
                        JSONArray pArray = deep.getJSONArray("p_array");
                        if (pArray != null && pArray.size() > 0) {
                            for (Object obj : pArray) {
                                JSONObject pInfo = JSONObject.fromObject(obj);
                                //geoP
                                JSONObject geoP = pInfo.getJSONObject("geoP");
                                Geojson.coord2Pixel(geoP, z, px, py);
                                JSONArray geoPArray = geoP.getJSONArray("coordinates");
                                allGeoPArray.add(geoPArray);

                                //access
                                String access = pInfo.getString("access");
                                JSONArray accessArray = TipsSelectorUtils.getCrossStreetAccess(access);
                                allAccessArray.add(accessArray);
                            }
                        }
                        m.put("e", allGeoPArray);
                        m.put("f", allAccessArray);
                    }
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
					} else if (type == 1104) {//20170707 第十迭代新增c_array
						m.put("d", String.valueOf(deep.getInt("tp")));
						m.put("e", String.valueOf(deep.getInt("dir")));
                        JSONArray arr = deep.getJSONArray("c_array");
                        m.put("f", arr);
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
					} else if(type == 1114) {//20170517 卡车限制Tips渲染接口新增参数
                        m.put("d", String.valueOf(deep.getInt("se")));
                        m.put("e", String.valueOf(deep.getInt("value")));
					}else if(type == 1115) {//20170517 车道变化点Tips渲染接口新增参数
                        m.put("d", String.valueOf(deep.getInt("inNum")));
                        m.put("e", String.valueOf(deep.getInt("outNum")));
                    }else if(type == 1101) {//20170605 限速增加返回值 赵航用
                        m.put("d", String.valueOf(deep.getInt("se")));
                        m.put("e", String.valueOf(deep.getInt("value")));
                        m.put("f", String.valueOf(deep.getInt("flag")));
					}else if(type == 1401 || type == 1402 || type == 1403 || type == 1404
                            || type == 1406 || type == 1407 || type == 1409 || type == 1410) {
                        m.put("d", deep.getString("ptn"));
                    }
				} else if (type == 1106 || type == 1211) {
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
				} else if (type == 1709) {//20170517 点位移
                    JSONObject geoO = deep.getJSONObject("geoO");
                    Geojson.coord2Pixel(geoO, z, px, py);
                    m.put("c", geoO.getJSONArray("coordinates"));

                    JSONObject geoN = deep.getJSONObject("geoN");
                    Geojson.coord2Pixel(geoN, z, px, py);
                    m.put("d", geoN.getJSONArray("coordinates"));
                } else if (type == 1202) {
					m.put("c", String.valueOf(deep.getInt("num")));
				} else if (type == 1510 || type == 1514 || type == 1501
						|| type == 1515 || type == 1502 || type == 1503
						|| type == 1504 || type == 1505 || type == 1506
						|| type == 1508 || type == 1513 || type == 1512
						|| type == 1516 || type == 1507 || type == 1511
						|| type == 1517 || type == 1509 || type == 1518
                        || type == 1520) {//20170707 第十迭代新增1520
					JSONObject gSLoc = deep.getJSONObject("gSLoc");
					Geojson.coord2Pixel(gSLoc, z, px, py);
					JSONObject gELoc = deep.getJSONObject("gELoc");
					Geojson.coord2Pixel(gELoc, z, px, py);
					m.put("c", gSLoc.getJSONArray("coordinates"));
					m.put("d", gELoc.getJSONArray("coordinates"));

					if (type == 1510 || type == 1507 || type == 1511
							|| type == 1509) {

						m.put("e", deep.getString("name"));
					}else if(type == 1518) {
                        m.put("e", deep.getInt("grade"));
                    }
					// 20170207修改，需求来源于：赵航——有个需求是，如果上传的步行街有时间段，我们要渲染不同的图标，现在渲染接口没有时间段这个字段
					else if (type == 1507 || type == 1520) {
						m.put("f", deep.getString("time"));
					}else if (type == 1517) {

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
				}else if(type == 1116) {
                    JSONArray fArray = deep.getJSONArray("f_array");
                    JSONArray cArray = new JSONArray();
                    if (fArray != null) {
                        for (int i = 0; i < fArray.size(); i++) {
                            JSONObject jsonObject = new JSONObject();
                            JSONObject f = fArray.getJSONObject(i);
                            jsonObject.put("z", f.getInt("z"));
							JSONObject geoJson = f.getJSONObject("geo");
                            Geojson.coord2Pixel(geoJson, z, px, py);
                            jsonObject.put("geo", geoJson.getJSONArray("coordinates"));
                            cArray.add(jsonObject);
                        }
                    }
                    m.put("c", cArray);
				}else if(type == 1117) {//20170707 第十迭代新增车道限高限宽
                    double agl = deep.getDouble("agl");
                    m.put("c", agl);

                    JSONArray htArray = deep.getJSONArray("ht");
                    m.put("d", htArray);

                    JSONArray wdArray = deep.getJSONArray("wd");
                    m.put("e", wdArray);
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
			DbUtils.rollback(conn);
			logger.error("渲染报错，数据错误：" + e.getMessage() + rowkey);
			throw new Exception(e.getMessage() + "rowkey:" + rowkey, e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
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
//     20170523 和于桐万冲确认该接口取消
//	public JSONArray searchDataByWkt(String parameter, boolean filterDelete)
//			throws Exception {
//		JSONArray array = new JSONArray();
//
//		try {
//            JSONObject jsonReq = JSONObject.fromObject(parameter);
//            String mdFlag = jsonReq.getString("flag");
//			TipsRequestParam param = new TipsRequestParam();
//			String query = param.getStatusByWkt(parameter, filterDelete);
//
//			List<JSONObject> snapshots = conn.queryTips(query, null);
//
//			for (JSONObject json : snapshots) {
//				JSONObject result = new JSONObject();
//
//				String type = json.getString("s_sourceType");
//
//				String geometry = json.getString("g_location");
//
//				// 采集、日编、月编状态
//				if ("c".equals(mdFlag)) {
//
//					result.put("status", json.getString("t_tipStatus"));
//
//				} else if ("d".equals(mdFlag)) {
//
//					result.put("status", json.getString("t_dEditStatus"));
//
//				} else if ("m".equals(mdFlag)) {
//
//					result.put("status", json.getString("t_mEditStatus"));
//
//				}
//
//				JSONObject deep = JSONObject.fromObject(json.getString("deep"));
//
//				// g字段重新赋值的（显示坐标：取Tips的geo）
//				if (TipsStatConstant.gGeoTipsType.contains(type)) {
//
//					JSONObject deepGeo = deep.getJSONObject("geo");
//
//					geometry = deepGeo.toString();
//
//				} else if (TipsStatConstant.gSLocTipsType.contains(type)) {
//
//					JSONObject gSLoc = deep.getJSONObject("gSLoc");
//
//					geometry = gSLoc.toString();
//
//				}
//
//				result.put("geometry", geometry);
//
//				array.add(result);
//
//			}
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			try {
//
//			} catch (Exception e) {
//
//			}
//		}
//		return array;
//	}

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
				System.out.println(new String(kv.qualifier()));
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

    public JSONArray searchDataByRowkeyNew(JSONArray rowkeyArray) throws Exception {
        JSONArray data = new JSONArray();
        for(int i = 0; i < rowkeyArray.size(); i ++) {
            String rowkey = rowkeyArray.getString(i);
            JSONObject jsonObject = this.searchDataByRowkeyNew(rowkey);
            data.add(jsonObject);
        }
        return data;
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
                String key = new String(kv.qualifier());
                if(key.equals(TIP_OLD_KEY_NAME)) {
                    JSONArray arrayJson = JSONArray
                            .fromObject(new String(kv.value()));
                    json.put(key, arrayJson);
                }else{
                    JSONObject injson = JSONObject
                            .fromObject(new String(kv.value()));
                    json.put(key, injson);
                }

			/*	if (key.equals("feedback")) {
					json.put("feedback", injson);
				} else {
					json.putAll(injson);
				}*/


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
     * 子任务Tips根据类型统计
     * @param parameter
     * @return
     * @throws Exception
     */
	public JSONObject getStats(String parameter)
			throws Exception {
		JSONObject jsonData = new JSONObject();

		TipsRequestParamSQL param = new TipsRequestParamSQL();
        OracleWhereClause whereClause = param.getTipStat(parameter);
		Connection oracelConn = DBConnector.getInstance().getTipsIdxConnection();
		try{
			TipsIndexOracleOperator operator = new TipsIndexOracleOperator(oracelConn);
			long total = operator.querCount("select count(1) from tips_index where "+whereClause.getSql(), whereClause.getValues());
			Map<Object, Object> data = operator.groupQuery("select s_sourcetype,count(1) from tips_index where "+whereClause.getSql()+" group by s_sourcetype", whereClause.getValues());
			jsonData.put("total", total);
			jsonData.put("rows", data);
			return jsonData;
		}finally{
			DbUtils.close(oracelConn);
		}
		
	}

//	/**
//	 * 统计子任务的tips总作业量,grid范围内滿足stage的数据条数
//	 *
//	 * @param grids
//	 * @param stages
//	 * @return
//	 * @throws Exception
//	 */
//	public int getTipsCountByStage(JSONArray grids, int stages)
//			throws Exception {
//
//		String wkt = GridUtils.grids2Wkt(grids);
//		return getTipsCountByStageAndWkt(wkt, stages, null);
//	}

	/**
	 * 统计子任务的tips总作业量,grid范围内滿足stage的数据条数
	 *
	 * @param wkt
	 * @param collectTaskIds
	 * @param collectTaskIds
	 * @param statType 统计类型：total，prepared 
	 * @param taskType 1是 质检任务、作业任务
	 * @return
	 * @throws Exception
	 */
	public int getTipsDayTotal(String wkt, Set<Integer> collectTaskIds, String statType, int taskType, int handler)
			throws Exception {
		TipsRequestParam param = new TipsRequestParam();
        JSONObject paramObj = new JSONObject();
        paramObj.put("statType", statType);
        paramObj.put("wkt", wkt);
        paramObj.put("collectTaskIds", collectTaskIds);
        paramObj.put("taskType", taskType);
        paramObj.put("handler", handler);
        String parameter = paramObj.toString();
        String query = param.getTipsDayTotal(parameter);

        SolrDocumentList sdList = conn.queryTipsSolrDoc(query, null);
        long totalNum = sdList.getNumFound();
        if (totalNum <= Integer.MAX_VALUE) {
            return (int)totalNum;
        }
		return 0;
	}


	/**
	 * 获取单种类型快照
	 *
	 * @param parameter
	 * @return
	 * @throws Exception
	 */
	public JSONArray getSnapshot(String parameter) throws Exception {
		JSONArray jsonData = new JSONArray();
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        int type = Integer.valueOf(jsonReq.getString("type"));
        int dbId = jsonReq.getInt("dbId");

        TipsRequestParamSQL param = new TipsRequestParamSQL();
        OracleWhereClause where = param.getSnapShot(parameter);
        
        Connection oracleConn = DBConnector.getInstance().getTipsIdxConnection();
        List<TipsDao> tips =null;
        try{
        	tips= new TipsIndexOracleOperator(oracleConn).query("select * from tips_index where "+where.getSql(), where.getValues());
        }finally{
        	DbUtils.closeQuietly(oracleConn);
        }
        List<JSONObject> tipsJsonList = convertToJsonList(tips);
        jsonData=convert2Snapshot(tipsJsonList,dbId,type);

		return jsonData;
	}

	private List<JSONObject> convertToJsonList(List<TipsDao> tips) {
		List<JSONObject> tipsJsonList = new ArrayList<JSONObject>();
        for(TipsDao tip:tips){
        	tipsJsonList.add(JSONObject.fromObject(tip));
        }
		return tipsJsonList;
	}
	/**
	 * @Description:按照快照接口的要求返回数据
	 * @param tips
	 * @param dbId
	 * @param type
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-26 下午6:00:53
	 */
	public JSONArray convert2Snapshot(List<JSONObject> tips, int dbId, int type) throws Exception {
		
		JSONArray jsonData = new JSONArray();

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
					JSONObject fObj = deep.getJSONObject("f");
                    int type2101 = fObj.getInt("type");
                    if(type2101 == 1) {//1道路LINK 2测线
                        linkPids.add(Integer.valueOf(Integer.valueOf(fObj
                                .getString("id"))));
                    }
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

//				if ("d".equals(mdFlag)) {
//
//					m.put("a", json.getString("t_dStatus"));
//
//				} else if ("m".equals(mdFlag)) {
//
//					m.put("a", json.getString("t_mStatus"));
//				}

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
		Connection oracleConn = DBConnector.getInstance().getTipsIdxConnection();
		String where = new TipsRequestParamSQL().getTipsMobileWhere(wkt,date,TipsUtils.notExpSourceType);
        long count = new TipsIndexOracleOperator(oracleConn).querCount("select count(1) from tips_index where "+where+" and rownum=1",wkt);
       
		return (count>0?1:0);
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
	public List<TipsDao> getTipsByTaskId(Connection tipsConn,int taskId, int taskType) throws Exception {

		List<TipsDao> snapshots=conn.queryTipsByTask(tipsConn,taskId,taskType);

		return snapshots;

	}

    /**
     * 情报矢量化提交任务数据筛选
     * @param taskId
     * @param taskType
     * @return
     * @throws Exception
     */
    public List<TipsDao> getTipsByTaskIdAndStatus(Connection tipsConn,int taskId, int taskType) throws Exception {	
    	List<TipsDao> sdList = conn.queryTipsByTask(tipsConn,taskId, taskType, 1);
        return sdList;
    }

    /**
     * 矢量化检查Tips查询
     * @param parameter
     * @return
     * @throws Exception
     */
    public List<String> getCheckRowkeyList(String parameter) throws Exception {
        TipsRequestParamSQL param = new TipsRequestParamSQL();
        String where = param.getTipsCheckWhere(parameter);
        Connection oracleConn = DBConnector.getInstance().getTipsIdxConnection();
        List<TipsDao> tipsList = new TipsIndexOracleOperator(oracleConn).query("select * from tips_index where "+where);
        List<String> rowkeyList = new ArrayList<String>();
        for (TipsDao t:tipsList) {
            rowkeyList.add(t.getId());
        }
        return rowkeyList;
    }

    /**
     * 查询矢量化子任务未提交的数据
     * @param parameter
     * @return
     * @throws Exception
     */
    public List<String> getUnCommitRowkeyList(String parameter) throws Exception {
    	List<String> rowkeyList = new ArrayList<>();
    	java.sql.Connection oracleConn = null;
    	try{
	        TipsRequestParamSQL param = new TipsRequestParamSQL();
	        
	        String query = param.getTipsCheckUnCommit(parameter);
	        
			oracleConn = DBConnector.getInstance().getTipsIdxConnection();
			
			TipsIndexOracleOperator tipsOp = new TipsIndexOracleOperator(oracleConn);
	        
			List<TipsDao> tis = tipsOp.query("select * from tips_index where " + query);
	        
			for(TipsDao tips: tis){
				rowkeyList.add(tips.getId());
			}
        }catch(Exception e){
        	logger.error(e.getMessage(), e);
        }finally{
        	DbUtils.closeQuietly(oracleConn);
        }
        return rowkeyList;
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
		Connection tipsConn=null;
		List<TipsDao> tipsList=null;
		try{
			tipsConn=DBConnector.getInstance().getTipsIdxConnection();
			tipsList=conn.queryTipsByTask(tipsConn,collectTaskid, q_TASK_TYPE);
		}catch (Exception e) {
			logger.error("", e);
			DbUtils.rollbackAndCloseQuietly(tipsConn);
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(tipsConn);
		}

		Set<Integer> gridsSet= new HashSet<Integer>();

		Set<String> grids=new  HashSet<String>();

		for (TipsDao json : tipsList) {
			
			Geometry geo = json.getWkt();

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
		List<TipsDao> tipsList=this.queryCollectTaskTips(collectTaskIds, TaskType.PROGRAM_TYPE_Q);
		Map<String,int[]> statsMap = new HashMap<>();
		for(TipsDao tip : tipsList) {
			JSONObject snapshot = JSONObject.fromObject(tip);
			String wkt = snapshot.getString("wkt");//统计坐标
			Point point = GeometryUtils.getPointByWKT(wkt);
			Coordinate coordinate = point.getCoordinates()[0];
			String gridId = CompGridUtil.point2Grids(coordinate.x, coordinate.y)[0];
			int tipStatus = snapshot.getInt("t_tipStatus");
            int dEditStatus = snapshot.getInt("t_dEditStatus");
			if(statsMap.containsKey(gridId)) {
				int[] statsArray = statsMap.get(gridId);
				if(tipStatus == 2 && dEditStatus != 2) {//未完成
					statsArray[0] += 1;
				}else if(tipStatus == 2 && dEditStatus == 2) {//已完成
					statsArray[1] += 1;
				}
			} else {
				int[] statsArray = new int[]{0,0};
				if(tipStatus == 2 && dEditStatus != 2) {//未完成
					statsArray[0] += 1;
				}else if(tipStatus == 2 && dEditStatus == 2) {//已完成
					statsArray[1] += 1;
				}
				statsMap.put(gridId, statsArray);
			}
		}
		List<Map> list = new ArrayList<>();
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

    private List<TipsDao> queryCollectTaskTips( Set<Integer> collectTaskIds, int taskType) throws Exception {
    	StringBuilder builder = new StringBuilder();
		String solrIndexFiled = null;
		if (taskType == TaskType.PROGRAM_TYPE_Q) {
			solrIndexFiled = "s_qTaskId";
		} else if (taskType == TaskType.PROGRAM_TYPE_M) {
			solrIndexFiled = "s_mTaskId";
		}
		if (collectTaskIds.size() > 0) {
			builder.append(solrIndexFiled);
			builder.append("in (");
			int index = 0;
			for (int collectTaskId : collectTaskIds) {
				if (index != 0)
					builder.append(",");
				builder.append(collectTaskId);
				index++;
			}
			builder.append(")");
		}
		logger.info("queryCollectTaskTips:" + builder.toString());
		Connection tipsConn = DBConnector.getInstance().getTipsIdxConnection();
		try{
			TipsIndexOracleOperator tipsOp = new TipsIndexOracleOperator(tipsConn);
		return  tipsOp.query("select * from tips_index where " + builder);
		}finally{
			DbUtils.closeQuietly(tipsConn);
		}
	}

	/**
     *
     * @param parameter
     * @return
     * @throws Exception
     */
    public JSONObject statInfoTask(String parameter) throws Exception {
    	TipsRequestParamSQL param = new TipsRequestParamSQL();
        String where = param.getTipsCheckWhere(parameter);
        Connection oracleConn = DBConnector.getInstance().getTipsIdxConnection();
        try{
	        TipsIndexOracleOperator operator = new TipsIndexOracleOperator(oracleConn);
			long count = operator.querCount("select count(1) from tips_index where "+where);
	
	        JSONObject statObj = new JSONObject();
	        statObj.put("total", count);
	
	        List<TipsDao> type2001Result = operator.query("select * from tips_index where "+where+" and type=2001");
	        int total2001 = type2001Result.size();
			statObj.put("total2001", total2001);
	        double length = 0;
	        for (int i = 0; i < total2001; i++) {
	        	TipsDao snapshot = type2001Result.get(i);
	            JSONObject geojson = JSONObject.fromObject(snapshot.getG_location());
	            length += GeometryUtils.getLinkLength(GeoTranslator
	                    .geojson2Jts(geojson));
	        }
	        if(length != 0) {
	            length = new BigDecimal(length).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	        }
	        statObj.put("length", length);
	        return statObj;
        }finally{
        	DbUtils.closeQuietly(oracleConn);
        }
    }


    public JSONObject listInfoTipsByPage(String parameter) throws Exception {

		JSONObject jsonObject = new JSONObject();
		java.sql.Connection conn = null;
    	try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			TipsRequestParam param = new TipsRequestParam();
			String queryTotal = param.getTipsCheckTotal(parameter);
			int curPage = jsonReq.getInt("curPage");
			int pageSize = jsonReq.getInt("pageSize");

			conn=DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator operator = new TipsIndexOracleOperator(conn);
			Page page = operator.queryPage(queryTotal, curPage, pageSize);

			long totalNum = page.getTotalCount();
			if (totalNum <= Integer.MAX_VALUE) {
				jsonObject.put("total", totalNum);
				List<TipsDao> tipsDaoList = (List<TipsDao>) page.getResult();
				JSONArray jsonArray = new JSONArray();
				for (TipsDao tipsDao : tipsDaoList) {
					JSONObject resultObj = new JSONObject();
					String rowkey = tipsDao.getId();
					resultObj.put("rowkey", rowkey);
					String sourceType = tipsDao.getS_sourceType();
					resultObj.put("sourceType", sourceType);
					int lifecycle = tipsDao.getT_lifecycle();
					resultObj.put("lifecycle", lifecycle);
					String date = tipsDao.getT_date();
					resultObj.put("date", date);
					jsonArray.add(resultObj);
				}
				jsonObject.put("result", jsonArray);
			} else {
				// 暂先不处理
			}
		}
        catch (Exception ex){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw ex;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

        return jsonObject;
    }

    public Set<Integer> getTipsMeshIdSet(Set<Integer> collectTaskSet) throws Exception {
        org.apache.hadoop.hbase.client.Connection hbaseConn = null;
        Table htab = null;
        Set<Integer> meshSet = new HashSet<>();
        try {
            List<JSONObject> snapshots = conn.queryCollectTaskTips(collectTaskSet, TaskType.PROGRAM_TYPE_M);
            hbaseConn = HBaseConnector.getInstance().getConnection();
            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
            for (JSONObject snapshot : snapshots) {
                String rowkey = snapshot.getString("id");
                //当前geometery
                JSONObject gLocation = JSONObject.fromObject(snapshot.getString("g_location"));
                Geometry curGeo = GeoTranslator.geojson2Jts(gLocation);
                Set<Integer> curMeshSet = this.calculateGeometeryMesh(curGeo);
                if(curMeshSet != null && curMeshSet.size() > 0) {
                    meshSet.addAll(curMeshSet);
                }

                Get get = new Get(rowkey.getBytes());
                get.addColumn("data".getBytes(), "old".getBytes());
                Result result = htab.get(get);
                if(!result.isEmpty()) {
                    JSONObject oldTip = JSONObject.fromObject(new String(result
                            .getValue("data".getBytes(), "old".getBytes())));
                    JSONObject oldGeoJson = JSONObject.fromObject(oldTip.getString("o_location"));
                    Geometry oldGeo = GeoTranslator.geojson2Jts(oldGeoJson);
                    Set<Integer> olcMeshSet = this.calculateGeometeryMesh(oldGeo);
                    if(olcMeshSet != null && olcMeshSet.size() > 0) {
                        meshSet.addAll(olcMeshSet);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return meshSet;
    }

    private Set<Integer> calculateGeometeryMesh(Geometry geometry) {
        Set<Integer> meshSet = new HashSet<>();
        if (geometry.getGeometryType() == GeometryTypeName.MULTILINESTRING
                || geometry.getGeometryType() == GeometryTypeName.MULTIPOLYGON
                || geometry.getGeometryType() == GeometryTypeName.MULTIPOINT) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Geometry subGeo = geometry.getGeometryN(i);
                String[] meshes = CompGeometryUtil.geo2MeshesWithoutBreak(subGeo);
                for(String mesh : meshes) {
                    meshSet.add(Integer.valueOf(mesh));
                }
            }
        }else {
            String[] meshes = CompGeometryUtil.geo2MeshesWithoutBreak(geometry);
            for(String mesh : meshes) {
                meshSet.add(Integer.valueOf(mesh));
            }
        }
        return meshSet;
    }

    public static void main(String[] args) throws Exception {
//        String parameter = "{\"mdFlag\":\"d\",\"gap\":10,\"types\":[\"1114\"],\"x\":1686,\"y\":775,\"z\":11}";
//
//            JSONObject jsonReq = JSONObject.fromObject(parameter);
//
//            int x = jsonReq.getInt("x");
//
//            int y = jsonReq.getInt("y");
//
//            int z = jsonReq.getInt("z");
//
//            int gap = jsonReq.getInt("gap");
//
//            String mdFlag = jsonReq.getString("mdFlag");
//
//            JSONArray types = new JSONArray();
//
//            if (jsonReq.containsKey("types")) {
//                types = jsonReq.getJSONArray("types");
//            }
//
//            JSONArray noQFilter = new JSONArray();
//            if (jsonReq.containsKey("noQFilter")) {
//                noQFilter = jsonReq.getJSONArray("noQFilter");
//            }

            TipsSelector selector = new TipsSelector();
//        Set<Integer> taskIds = new HashSet<>();
//        taskIds.add(25);
//        taskIds.add(121);
//selector.getTipsDayTotal("POLYGON ((116.25 39.75, 116.375 39.75, 116.375 39.83333, 116.25 39.83333, 116.25 39.75))", taskIds,"total");
//            JSONArray array = selector.searchDataByTileWithGap(x, y, z, gap,
//                    types, mdFlag, "wktLocation", noQFilter);

//        System.out.println("reusut:--------------\n"+array);
        String parameter= "{\"mdFlag\":\"d\",\"gap\":10,\"types\":[\"1510\",\"1515\",\"1202\",\"1203\",\"1702\",\"2001\",\"1901\",\"2101\",\"1601\",\"1803\",\"1301\",\"1507\"],\"x\":108944,\"y\":52057,\"z\":17,\"pType\":\"ms\"}";
//parameter = "{\"pType\":\"sl\",\"gap\":10,\"types\":[\"1107\",\"1201\",\"1202\",\"1203\",\"1702\",\"2001\",\"1901\",\"2101\",\"1601\",\"1803\",\"1301\",\"1507\"],\"x\":215849,\"y\":99266,\"z\":18}";
//        parameter = "{\"pType\":\"fc\",\"gap\":10,\"types\":[\"1107\",\"1201\",\"1202\",\"1203\",\"1702\",\"2001\",\"1901\",\"2101\",\"1601\",\"1803\",\"1301\",\"1507\"],\"x\":107891,\"y\":49669,\"z\":17}";
        parameter = "{\"mdFlag\":\"d\",\"gap\":10,\"types\":[\"8002\",\"1403\",\"1510\",\"1508\",\"1506\",\"1606\",\"1803\",\"1509\",\"2101\",\"1804\",\"1202\",\"1109\",\"1503\",\"8001\",\"1104\",\"1706\",\"1407\",\"1801\",\"1410\",\"1301\",\"1404\",\"2001\",\"1514\",\"1707\",\"1501\",\"1513\",\"1304\",\"1305\",\"1302\",\"1405\",\"1701\",\"1504\",\"1705\",\"1208\",\"1502\",\"1507\",\"1605\",\"1702\",\"1207\",\"1604\",\"1515\",\"1101\",\"1704\",\"1703\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1209\",\"1607\",\"1516\",\"1512\",\"1806\",\"1106\",\"1602\",\"1111\",\"1107\",\"1102\",\"1103\",\"1511\",\"1505\",\"1517\",\"1105\",\"1108\",\"1110\",\"1112\",\"1113\",\"1204\",\"1303\",\"1306\",\"1308\",\"1310\",\"1311\",\"1401\",\"1402\",\"1406\",\"1409\"],\"x\":215813,\"y\":99175,\"z\":18}";
		System.out.println("reusut:--------------\n"+selector.searchDataByTileWithGap(parameter));
	}

}