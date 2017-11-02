package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;

import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;

import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class DetailImporter {
	private static Set<String> deepkcs = null;

	public static int run(Connection conn, Statement stmt, JSONObject poi)
			throws Exception {

		if (JSONUtils.isNull(poi)) {
			return 0;
		}
		initdeepKcs();
		String kindCode = poi.getString("kindCode");
		if (!deepkcs.contains(kindCode)) {
			return 0;
		}

		IxPoiDetail det = new IxPoiDetail();

		det.setPoiPid(poi.getInt("pid"));

		if (!"200201".equals(kindCode)) {
			det.setWebSite(JsonUtils.getString(poi, "website"));
		}
		if (!JSONUtils.isNull(poi.get("contacts"))) {
			Set<String> contSet = new HashSet<String>();
			for (Object obj : poi.getJSONArray("contacts")) {
				JSONObject cont = (JSONObject) obj;
				if (11 == cont.getInt("type")) {
					contSet.add(cont.getString("number"));
				}
			}
			if (contSet.size() > 0) {
				det.setFax(StringUtils.join(contSet, "|"));
			}
		}
		if (!JSONUtils.isNull(poi.get("attachments"))) {
			for (Object obj : poi.getJSONArray("attachments")) {
				JSONObject att = (JSONObject) obj;
				if (4 == att.getInt("type") && 41 == att.getInt("tag")) {
					det.setBriefDesc(JsonUtils.getString(att, "url"));
				}
			}
		}
		det.setHwEntryexit(JsonUtils.getInt(poi, "hwEntryExit"));

		if ("120101".equals(kindCode)) {
			if (!JSONUtils.isNull(poi.get("hotel"))) {
				det.setStarHotel(JsonUtils.getString(
						poi.getJSONObject("hotel"), "rating"));
			}
		}
		if ("170100".equals(kindCode) || "170101".equals(kindCode)
				|| "170102".equals(kindCode)) {
			if (!JSONUtils.isNull(poi.get("hospital"))) {
				det.setHospitalClass(JsonUtils.getInt(
						poi.getJSONObject("hospital"), "rating"));
			}
		}

		BasicOperator operator = new BasicOperator(conn, det);

		operator.insertRow2Sql(stmt);

		return 1;
	}

	private static void initdeepKcs() {
		deepkcs = new HashSet<String>();
		deepkcs.add("110102");
		deepkcs.add("120101");
		deepkcs.add("180400");
		deepkcs.add("180304");
		deepkcs.add("160205");
		deepkcs.add("130501");
		deepkcs.add("150101");
		deepkcs.add("110200");
		deepkcs.add("120102");
		deepkcs.add("130102");
		deepkcs.add("130105");
		deepkcs.add("230108");
		deepkcs.add("170101");
		deepkcs.add("230103");
		deepkcs.add("160206");
		deepkcs.add("180210");
		deepkcs.add("200103");
		deepkcs.add("160203");
		deepkcs.add("190107");
		deepkcs.add("170102");
		deepkcs.add("170100");
		deepkcs.add("190114");
		deepkcs.add("110103");
		deepkcs.add("190403");
		deepkcs.add("180207");
		deepkcs.add("190400");
		deepkcs.add("190402");
		deepkcs.add("180100");
		deepkcs.add("160202");
		deepkcs.add("160208");
		deepkcs.add("160201");
		deepkcs.add("180308");
		deepkcs.add("160105");
		deepkcs.add("160207");
		deepkcs.add("190100");
		deepkcs.add("200101");
		deepkcs.add("190401");
		deepkcs.add("120201");
		deepkcs.add("160102");
		deepkcs.add("120202");
		deepkcs.add("150103");
		deepkcs.add("190201");
		deepkcs.add("160101");
		deepkcs.add("160103");
		deepkcs.add("190109");
		deepkcs.add("130106");
		deepkcs.add("190111");
		deepkcs.add("180306");
		deepkcs.add("190103");
		deepkcs.add("230100");
		deepkcs.add("180203");
		deepkcs.add("190104");
		deepkcs.add("200104");
		deepkcs.add("110301");
		deepkcs.add("180309");
		deepkcs.add("190108");
		deepkcs.add("130602");
		deepkcs.add("190106");
		deepkcs.add("130402");
		deepkcs.add("190102");
		deepkcs.add("120103");
		deepkcs.add("160106");
		deepkcs.add("230129");
		deepkcs.add("210301");
		deepkcs.add("130700");
		deepkcs.add("230101");
		deepkcs.add("130702");
		deepkcs.add("240100");
		deepkcs.add("220100");
		deepkcs.add("220200");
		deepkcs.add("130408");
		deepkcs.add("230126");
		deepkcs.add("130101");
		deepkcs.add("220300");
		deepkcs.add("150200");
		deepkcs.add("110101");
		deepkcs.add("190101");
		deepkcs.add("210207");
		deepkcs.add("130401");
		deepkcs.add("160100");
		deepkcs.add("130301");
		deepkcs.add("180310");
		deepkcs.add("130705");
		deepkcs.add("130200");
		deepkcs.add("130104");
		deepkcs.add("130103");
		deepkcs.add("110303");
		deepkcs.add("130204");
		deepkcs.add("110302");
		deepkcs.add("130701");
		deepkcs.add("130202");
		deepkcs.add("120104");
		deepkcs.add("130207");
		deepkcs.add("130801");
		deepkcs.add("130206");
		deepkcs.add("110304");
		deepkcs.add("180209");
		deepkcs.add("130410");
		deepkcs.add("130800");
		deepkcs.add("130411");
		deepkcs.add("130304");
		deepkcs.add("130203");
		deepkcs.add("180307");
		deepkcs.add("170105");
		deepkcs.add("180302");
		deepkcs.add("170109");
		deepkcs.add("200105");
		deepkcs.add("170103");
		deepkcs.add("170106");
		deepkcs.add("150104");
		deepkcs.add("130502");
		deepkcs.add("170104");
		deepkcs.add("180102");
		deepkcs.add("230207");
		deepkcs.add("140203");
		deepkcs.add("130303");
		deepkcs.add("190404");
		deepkcs.add("170107");
		deepkcs.add("130703");
		deepkcs.add("180301");
		deepkcs.add("130704");
		deepkcs.add("130201");
		deepkcs.add("130803");
		deepkcs.add("130302");
		deepkcs.add("130205");
		deepkcs.add("130407");
		deepkcs.add("180202");
		deepkcs.add("130603");
		deepkcs.add("130406");
		deepkcs.add("180111");

	}

}
