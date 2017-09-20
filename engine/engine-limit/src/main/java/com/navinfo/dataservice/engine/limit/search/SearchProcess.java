package com.navinfo.dataservice.engine.limit.search;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresInfoSearch;
import com.navinfo.dataservice.engine.limit.search.mate.ScPlateresGeometrySearch;
import com.navinfo.dataservice.engine.limit.search.mate.ScPlateresGroupSearch;
import com.navinfo.dataservice.engine.limit.search.mate.ScPlateresManoeuvreSearch;
import com.navinfo.dataservice.engine.limit.search.mate.ScPlateresRdlinkSearch;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 查询进程
 */
public class SearchProcess {

	private static final Logger logger = Logger.getLogger(SearchProcess.class);

	Connection conn;

	public SearchProcess(Connection conn) throws Exception {

		this.conn = conn;
	}

	public SearchProcess() throws Exception {

	}

	public List<IRow> searchMetaDataByCondition(LimitObjType type, JSONObject condition)
			throws Exception {
		List<IRow> rows = new ArrayList<>();
		try {
			switch (type) {
				case SCPLATERESGEOMETRY:
					rows = new ScPlateresGeometrySearch(this.conn).searchDataByCondition(condition);
					break;
				case SCPLATERESGROUP:
					rows = new ScPlateresGroupSearch(this.conn).searchDataByCondition(condition);
					break;

				case SCPLATERESMANOEUVRE:
					rows = new ScPlateresManoeuvreSearch(this.conn).searchDataByCondition(condition);
					break;
				case SCPLATERESRDLINK:
					rows = new ScPlateresRdlinkSearch(this.conn).searchDataByCondition(condition);
					break;
				default:
					return rows;
			}

			return rows;
		} catch (Exception e) {

			throw e;

		} finally {

		}
	}

	public List<IRow> searchLimitDataByCondition(LimitObjType type, JSONObject condition)
			throws Exception {
		List<IRow> rows = new ArrayList<>();
		try {
			switch (type) {
				 case SCPLATERESINFO:
					rows = new ScPlateresInfoSearch(this.conn).searchDataByCondition(condition);
					break;

				default:
					return rows;
			}

			return rows;
		} catch (Exception e) {

			throw e;

		} finally {

		}
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
	public static void main(String[] args) throws Exception {


	}
}
