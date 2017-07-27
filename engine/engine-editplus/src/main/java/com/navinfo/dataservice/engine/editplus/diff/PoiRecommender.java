package com.navinfo.dataservice.engine.editplus.diff;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.engine.editplus.utils.AdFaceSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class PoiRecommender {

	public static Connection conn = null;

	public static List<FastPoi> loadPoi(Geometry geometry,MetadataApi metadataApi) throws Exception {
		List<FastPoi> poiList = new ArrayList<FastPoi>();

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("WITH A AS ");
			sb.append(" (SELECT I.POI_NUM,");
			sb.append("         I.PID,");
			sb.append("         I.KIND_CODE,");
			sb.append("         I.CHAIN,");
			sb.append("         I.POST_CODE,");
			sb.append("         I.X_GUIDE,");
			sb.append("         I.Y_GUIDE,");
			sb.append("         I.GEOMETRY,");
			sb.append("         P1.NAME OFFICENAME,");
			sb.append("        (SELECT NAME");
			sb.append("            FROM IX_POI_NAME");
			sb.append("           WHERE POI_PID = I.PID");
			sb.append("             AND NAME_CLASS = 3");
			sb.append("             AND NAME_TYPE = 1");
			sb.append("             AND U_RECORD <> 2");
			sb.append("             AND LANG_CODE IN ('CHI', 'CHT')) SHORT_NAME,");
			sb.append("         A.FULLNAME");
			sb.append("    FROM IX_POI I, IX_POI_NAME P1, IX_POI_ADDRESS A");
			sb.append("   WHERE sdo_within_distance(I.geometry, sdo_geometry(:1  , 8307), 'mask=anyinteract') = 'TRUE'");
			sb.append("     AND I.PID = P1.POI_PID");
			sb.append("     AND P1.U_RECORD <> 2");
			sb.append("     AND P1.NAME_CLASS = 1");
			sb.append("     AND P1.NAME_TYPE = 1");
			sb.append("    AND P1.LANG_CODE IN ('CHI', 'CHT')");
			sb.append("     AND I.PID = A.POI_PID");
			sb.append("     AND A.U_RECORD <> 2");
			sb.append("    AND A.LANG_CODE IN ('CHI', 'CHT')),");
			sb.append(" B AS");
			sb.append(" (SELECT C.POI_PID,");
			sb.append("         LISTAGG(C.CONTACT, '|') WITHIN GROUP(ORDER BY C.POI_PID) AS TEL");
			sb.append("    FROM IX_POI_CONTACT C,A ");
			sb.append("   WHERE  C.POI_PID = A.PID ");
			sb.append("     AND (C.CONTACT_TYPE IN (1,2,3,4) AND C.CONTACT_DEPART IN (0, 16, 8))");
			sb.append("     AND C.U_RECORD <> 2");
			sb.append("   GROUP BY C.POI_PID)");
			sb.append(" SELECT POI_NUM,");
			sb.append("       PID,");
			sb.append("       KIND_CODE,");
			sb.append("       CHAIN,");
			sb.append("       POST_CODE,");
			sb.append("       X_GUIDE,");
			sb.append("       Y_GUIDE,");
			sb.append("       GEOMETRY,");
			sb.append("       OFFICENAME,");
			sb.append("       SHORT_NAME,");
			sb.append("       FULLNAME,");
			sb.append("       TEL");
			sb.append("  FROM A, B");
			sb.append(" WHERE A.PID = B.POI_PID");

			pstmt = conn.prepareStatement(sb.toString());
			Geometry buffer = geometry.buffer(GeometryUtils.convert2Degree(2000));

			String wkt = GeoTranslator.jts2Wkt(buffer);
			Clob geom = ConnectionUtil.createClob(conn);
			geom.setString(1, wkt);
			pstmt.setClob(1, geom);

			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				FastPoi fastPoi = new FastPoi();
				fastPoi.setAddr(resultSet.getString("FULLNAME") != null ? resultSet.getString("FULLNAME") : "");
				fastPoi.setChain(resultSet.getString("CHAIN") != null ? resultSet.getString("CHAIN") : "");
				
				fastPoi.setKindCode(resultSet.getString("KIND_CODE") != null ? resultSet.getString("KIND_CODE") : "");
				fastPoi.setName(resultSet.getString("OFFICENAME") != null ? resultSet.getString("OFFICENAME") : "");
				fastPoi.setPid(resultSet.getInt("PID"));
				fastPoi.setPoiNum(resultSet.getString("POI_NUM"));
				fastPoi.setPostCode(resultSet.getString("POST_CODE") != null ? resultSet.getString("POST_CODE") : "");
				fastPoi.setShortName(
						resultSet.getString("SHORT_NAME") != null ? resultSet.getString("SHORT_NAME") : "");
				String tel=resultSet.getString("TEL") != null ? resultSet.getString("TEL") : "";
				tel=StringUtil.sortPhone(StringUtil.contactFormat(tel));
				fastPoi.setTel(tel);
				fastPoi.setxGuide(resultSet.getDouble("X_GUIDE"));
				fastPoi.setyGuide(resultSet.getDouble("Y_GUIDE"));
				
				Geometry geometryPoi=GeoTranslator.struct2Jts((STRUCT) resultSet.getObject("GEOMETRY"));
				JSONArray array = GeoTranslator.jts2JSONArray(geometryPoi);
				fastPoi.setX(array.getDouble(0));
				fastPoi.setY(array.getDouble(1));
				
				int adminCode = new AdFaceSelector(conn).getAminIdByGeometry(geometryPoi);
				String adminCodeStr = String.valueOf(adminCode);
				//省份、城市的json
				JSONObject resultJson = metadataApi.getProvinceAndCityByAdminCode(adminCodeStr);
				if(resultJson!=null){
					fastPoi.setProvnm(resultJson.getString("province"));
				}else
				{
					fastPoi.setProvnm(null);
				}
				
				poiList.add(fastPoi);
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return poiList;
	}

	public static FastPoi buildFastPoi(BasicObj obj) {

		IxPoi p = (IxPoi) obj.getMainrow();
		IxPoiObj poiObj = (IxPoiObj) obj;

		IxPoiName poiName = poiObj.getOfficeStandardCHName();
		IxPoiAddress poiAddr = poiObj.getChiAddress();

		FastPoi fp = new FastPoi();
		JSONArray array = GeoTranslator.jts2JSONArray(p.getGeometry());

		fp.setX(array.getDouble(0));
		fp.setY(array.getDouble(1));
		if (poiAddr == null || poiAddr.getAddrname() == null) {
			fp.setAddr("");
		}
		if (poiName == null || poiName.getName() == null) {
			fp.setName("");
		}

		StringBuffer sb = new StringBuffer();
		String telephone = "";
		for (IxPoiContact c : poiObj.getIxPoiContacts()) {
			sb.append(c.getContact()).append(";");
		}
		if (sb.length() > 0)
			telephone = sb.toString().substring(0, sb.toString().length() - 1);
		telephone = StringUtil.sortPhone(telephone);

		fp.setTel(telephone);
		fp.setPostCode(p.getPostCode());
		fp.setPoiNum(p.getPoiNum());
		return fp;
	}

	public static FastResult buildFastResult(IxDealershipResult dealResult) {
		FastResult fr = new FastResult();
		JSONArray array = GeoTranslator.jts2JSONArray(dealResult.getGeometry());
		fr.setX(array.getDouble(0));
		fr.setY(array.getDouble(1));
		fr.setAddr(dealResult.getAddress());
		fr.setName(dealResult.getName());
		fr.setTel(StringUtil.sortPhone(StringUtil.contactFormat(dealResult.getPoiTel())));
		fr.setPostCode(dealResult.getPostCode());
		fr.setProvnm(dealResult.getProvince());
		return fr;
	}

	// 推荐匹配poi
	public static void recommenderPoi(IxDealershipResult dealResult,MetadataApi metadataApi) throws Exception {
		FastResult fr = buildFastResult(dealResult);
		// 外扩两公里查询poi
		List<FastPoi> poiList = loadPoi(dealResult.getGeometry(),metadataApi);
		Map<String, Double> matchPoi = new HashMap<String, Double>();
		for (FastPoi obj : poiList) {
			double sim = similarityDealership(fr, obj);
			if (sim > 0) {
				BigDecimal b = new BigDecimal(sim);
				double f1 = b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				matchPoi.put(obj.getPoiNum(), f1);
			}
		}
		List<Entry<String, Double>> matchPoiList = sortMap(matchPoi);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < matchPoiList.size(); i++) {
			Entry<String, Double> mapping = (Entry<String, Double>) matchPoiList.get(i);
			if (i != 4) {
				sb.append(mapping.getValue()).append("|");
			}
			if (i == 0) {
				dealResult.setPoiNum1(mapping.getKey());
				dealResult.setCfmPoiNum(mapping.getKey());
			} else if (i == 1) {
				dealResult.setPoiNum2(mapping.getKey());
			} else if (i == 2) {
				dealResult.setPoiNum3(mapping.getKey());
			} else if (i == 3) {
				dealResult.setPoiNum4(mapping.getKey());
			} else if (i == 4) {
				dealResult.setPoiNum5(mapping.getKey());
				sb.append(mapping.getValue());
				break;
			}
		}

		dealResult.setSimilarity(sb.toString());

	}

	public static List<Entry<String, Double>> sortMap(Map matchPoi) {
		// 这里将map.entrySet()转换成list
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(matchPoi.entrySet());
		// 然后通过比较器来实现排序
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			// 升序排序
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}

		});
		return list;
	}

	public static double similarity(FastResult s1, FastPoi s2) {

		if (s1 == null || s2 == null)
			return 0;

		double r1, r2, r3, r4, r5, sim1, sim2, q = 0;
		double x1, y1, x2, y2;
		double d;
		String tel1 = "", tel2 = "";
		x1 = s1.getX();
		y1 = s1.getY();
		x2 = s2.getX();
		y2 = s2.getY();
		d = LngLat.distanceByLngLat(y1, x1, y2, x2);

		r1 = 1 / (1 + d);

		if (s1.getName() == null || s2.getName() == null || s1.getName().equals("") || s2.getName().equals(""))
			r2 = 0;
		else
			r2 = LevenshteinExtend.getSimilarityRatio(s1.getName(), s2.getName());

		if (s1.getAddr() == null || s2.getAddr() == null || s1.getAddr().equals("") || s2.getAddr().equals(""))
			r3 = 0;
		else
			r3 = LevenshteinExtend.getSimilarityRatio(s1.getAddr(), s2.getAddr());

		if (s1.getPostCode() == null || s2.getPostCode() == null || s1.getPostCode().equals("")
				|| s2.getPostCode().equals(""))
			r4 = 0;
		else
			r4 = LevenshteinExtend.getSimilarityRatio(s1.getPostCode(), s2.getPostCode());
		String[] tels1 = s1.getTel().split(";");
		String[] tels2 = s2.getTel().split(";");
		boolean flag = false;
		r5 = 1;
		if (tels1 != null)
			for (int i = 0; i < tels1.length; i++) {
				double tempR = 0;
				if (tels2 != null)
					for (int j = 0; j < tels2.length; j++) {
						tel1 = tels1[i];
						tel2 = tels2[j];
						tel1 = tel1.replace("+86", "");
						tel1 = tel1.replaceAll("\\D", "");
						tel2 = tel2.replace("+86", "");
						tel2 = tel2.replaceAll("\\D", "");
						double t = LevenshteinExtend.getSimilarityRatio(tel1, tel2);

						if (!(tels1[i] == null || tels2[j] == null || tels1[i].equals("") || tels2[j].equals(""))) {
							if (t > tempR)
								tempR = t;
							flag = true;
						}
					}
				r5 *= (1 - tempR);
			}
		r5 = 1 - r5;
		if (r5 < 1)
			r5 = 0;
		sim1 = 1 - (1 - r1) * (1 - r2) * (1 - 0.9 * r3 - 0.1 * r4) * (1 - r5);
		if (r1 != 0)
			q += 0.1;
		if (r2 != 0)
			q += 0.33;
		if (r3 != 0)
			q += 0.31;
		if (r4 != 0)
			q += 0.03;
		if (flag)
			q += 0.23;
		sim2 = (0.1 * r1 + 0.33 * r2 + 0.31 * r3 + 0.03 * r4 + 0.23 * r5);
		if (q != 0 && q > 0.3)
			sim2 /= q;
		return (sim1 + sim2) / 2;
	}

	public static double similarityDealership(FastResult s1, FastPoi s2) {

		if (s1 == null || s2 == null)
			return 0;
		if (s1.getProvnm() != null && s2.getProvnm() != null && (!s1.getProvnm().equals(""))
				&& (!s2.getProvnm().equals("")) && (!s1.getProvnm().equals(s2.getProvnm())))
			return 0;
		double r1, r2, r3, r4, r5, sim1, sim2, q = 0;
		double x1, y1, x2, y2;
		double d;
		String tel1 = "", tel2 = "";
		x1 = s1.getX();
		y1 = s1.getY();
		x2 = s2.getX();
		y2 = s2.getY();
		d = LngLat.distanceByLngLat(y1, x1, y2, x2);

		r1 = 1 / (1 + d);

		if (s1.getName() == null || s2.getName() == null || s1.getName().equals("") || s2.getName().equals(""))
			r2 = 0;
		else
			r2 = LevenshteinExtend.getSimilarityRatio(s1.getName(), s2.getName());

		if (s1.getAddr() == null || s2.getAddr() == null || s1.getAddr().equals("") || s2.getAddr().equals(""))
			r3 = 0;
		else
			r3 = LevenshteinExtend.getSimilarityRatio(s1.getAddr(), s2.getAddr());

		if (s1.getPostCode() == null || s2.getPostCode() == null || s1.getPostCode().equals("")
				|| s2.getPostCode().equals(""))
			r4 = 0;
		else
			r4 = LevenshteinExtend.getSimilarityRatio(s1.getPostCode(), s2.getPostCode());
		String[] tels1 = s1.getTel().split(";");
		String[] tels2 = s2.getTel().split(";");

		r5 = 1;
		boolean flag = false;
		if (tels1 != null)
			for (int i = 0; i < tels1.length; i++) {
				double tempR = 0;
				if (tels2 != null)
					for (int j = 0; j < tels2.length; j++) {
						tel1 = tels1[i];
						tel2 = tels2[j];
						tel1 = tel1.replace("+86", "");
						tel1 = tel1.replaceAll("\\D", "");
						tel2 = tel2.replace("+86", "");
						tel2 = tel2.replaceAll("\\D", "");
						double t = LevenshteinExtend.getSimilarityRatio(tel1, tel2);

						if (!(tels1[i] == null || tels2[j] == null || tels1[i].equals("") || tels2[j].equals(""))) {
							if (t > tempR)
								tempR = t;
							flag = true;
						}
					}

				r5 *= (1 - tempR);
			}

		r5 = 1 - r5;
		if (r5 != 1)
			r5 = 0;

		sim1 = 1 - (1 - r1) * (1 - r2) * (1 - 0.9 * r3 - 0.1 * r4) * (1 - r5);
		if (r1 != 0)
			q += 0.2;
		if (r2 != 0)
			q += 0.3;
		if (r3 != 0)
			q += 0.27;
		if (r4 != 0)
			q += 0.03;
		if (flag)
			q += 0.2;
		sim2 = (0.2 * r1 + 0.3 * r2 + 0.27 * r3 + 0.03 * r4 + 0.2 * r5);
		if (q != 0 && q > 0.3)
			sim2 /= q;
		return (sim1 + sim2) / 2;
	}

}
