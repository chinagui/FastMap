
package com.navinfo.dataservice.engine.limit.search;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.search.gdb.RdLinkSearch;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresInfoSearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGeometrySearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGroupSearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresManoeuvreSearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresRdlinkSearch;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
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

	public int searchMetaDataByCondition(LimitObjType type, JSONObject condition, List<IRow> rows)
			throws Exception {
		int total = 0;
		try {
			switch (type) {
				case SCPLATERESGEOMETRY:
					total = new ScPlateresGeometrySearch(this.conn).searchDataByCondition(condition,rows);
					break;
				case SCPLATERESGROUP:
					total = new ScPlateresGroupSearch(this.conn).searchDataByCondition(condition,rows);
					break;

				case SCPLATERESMANOEUVRE:
					total = new ScPlateresManoeuvreSearch(this.conn).searchDataByCondition(condition,rows);
					break;
				case SCPLATERESRDLINK:
					total = new ScPlateresRdlinkSearch(this.conn).searchDataByCondition(condition,rows);
					break;
				default:
					return total;
			}

			return total;
		} catch (Exception e) {

			throw e;

		} finally {

		}
	}

	public int searchLimitDataByCondition(LimitObjType type, JSONObject condition,List<IRow> objList)
			throws Exception {
		int total = 0;
		try {
			switch (type) {
				 case SCPLATERESINFO:
					total = new ScPlateresInfoSearch(this.conn).searchDataByCondition(condition, objList);
					break;

				default:
					return total;
			}

			return total;
		} catch (Exception e) {

			throw e;

		} finally {

		}
	}
	
	public JSONObject searchRdLinkDataByCondition(int type, JSONObject condition) throws Exception {
		JSONObject result = new JSONObject();

		RdLinkSearch search = new RdLinkSearch(this.conn);

		try {
			switch (type) {
			case 1:
			case 2:
				result = search.searchDataByCondition(type, condition);
				break;
			case 3:
				result = search.searchDataByPid(condition);
			default:
				return result;
			}

			return result;
		} catch (Exception e) {

			throw e;

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