package com.navinfo.dataservice.FosEngine.edit.search;

import java.sql.Connection;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

import com.navinfo.dataservice.FosEngine.comm.db.DBOraclePoolManager;
import com.navinfo.dataservice.FosEngine.comm.mercator.MercatorProjection;
import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;

/**
 * 查询进程
 */
public class SearchProcess {


	private Connection conn;

	public SearchProcess(int projectId) throws Exception {

		this.conn = DBOraclePoolManager.getConnection(projectId);

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
	 * 根据矩形框空间查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	public JSONObject searchDataBySpatial(List<ObjType> types, String box)
			throws Exception {

		JSONObject json = new JSONObject();

		SearchFactory factory = new SearchFactory(conn);

		try {

			for (ObjType type : types) {

				ISearch search = factory.createSearch(type);

				List<SearchSnapshot> list = search.searchDataBySpatial(box);

				JSONArray array = new JSONArray();

				for (SearchSnapshot snap : list) {

					array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig());
				}

				json.accumulate(type.toString(), array, getJsonConfig());
			}
		} catch (Exception e) {
			

			throw e;

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					
				}
			}
		}
		return json;
	}
	
	
	/**
	 * 根据瓦片空间查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	public JSONObject searchDataByTileWithGap(List<ObjType> types, int x,int y,int z,int gap)
			throws Exception {

		JSONObject json = new JSONObject();

		SearchFactory factory = new SearchFactory(conn);

		try {

			for (ObjType type : types) {

				ISearch search = factory.createSearch(type);
				
				List<SearchSnapshot> list = search.searchDataByTileWithGap(x, y, z, gap);

				JSONArray array = new JSONArray();

				for (SearchSnapshot snap : list) {

					array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig());
				}

				json.accumulate(type.toString(), array, getJsonConfig());
			}
		} catch (Exception e) {
			

			throw e;

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					
				}
			}
		}
		return json;
	}
	
	/**
	 * 根据pid查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	public IObj searchDataByPid(ObjType type, int pid) throws Exception {

		try {
			SearchFactory factory = new SearchFactory(conn);

			ISearch search = factory.createSearch(type);

			IObj obj = search.searchDataByPid(pid);

			return obj;
		} catch (Exception e) {
			

			throw e;

		} finally {

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					
				}
			}
		}

	}
}
