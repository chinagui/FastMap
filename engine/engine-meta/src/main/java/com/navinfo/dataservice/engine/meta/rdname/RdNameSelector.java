package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameHwInfo;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameHwInfoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdNameSelector {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;
	
	public RdNameSelector() {
		
	} 

	public RdNameSelector(Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * @Title: searchByName
	 * @Description: 查询道路名
	 * @param name
	 * @param pageSize
	 * @param pageNum
	 * @return
	 * @throws Exception  JSONObject
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年3月20日 上午10:01:26 
	 */
	public JSONObject searchByName(String name, int pageSize, int pageNum)
			throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		int total = 0;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			if (name.length() == 0) {
				result.put("total", total);

				result.put("data", array);

				return result;
			}

			//*****zl 2017.06.30 *********
			ScPointAdminArea scPointAdminArea = new ScPointAdminArea(conn);
			Map<String,String> adminMap = scPointAdminArea.getAdminMap();
			
//			String sql = "SELECT *   FROM (SELECT c.*, rownum rn           FROM (select  count(1) over(partition by 1) total,        a.name_groupid, a.road_type    ,        a.name,        b.province   from rd_name a, cp_provincelist b  where a.name like :1    and a.admin_id = b.admincode) c          WHERE rownum <= :2)  WHERE rn >= :3";
			String sql = " SELECT *  FROM (SELECT c.*, rownum rn FROM (select distinct count(1) over(partition by 1) total,a.name_groupid, a.road_type,a.name,a.admin_id from rd_name a where  a.name like :1 ) c WHERE rownum <= :2 ) WHERE rn >= :3 ";
			
			int startRow = (pageNum-1) * pageSize+1;

			int endRow = pageNum * pageSize;

			pstmt = conn.prepareStatement(sql);

			log.info("rdname search :"+sql);
			pstmt.setString(1, "%"+name + "%");

			pstmt.setInt(2, endRow);

			pstmt.setInt(3, startRow);
			log.info("rdname search :"+sql +" 参数: "+name+" "+endRow+" "+startRow);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				if (total == 0) {
					total = resultSet.getInt("total");
				}

				int nameId = resultSet.getInt("name_groupid");

				String nameStr = resultSet.getString("name");

				String province = "";
				int adminId = resultSet.getInt("admin_id");

				if(adminId == 214){
					province = "全国";
				}else{
					if (!adminMap.isEmpty()) {
						if (adminMap.containsKey(String.valueOf(adminId))) {
							province=adminMap.get(String.valueOf(adminId));
						} 
					}
				}
				int roadType = resultSet.getInt("road_type");

				//*******zl 2017.3.10 315临时代码里处理province获取省份简称
				if(province != null && StringUtils.isNotEmpty(province)){
					province = getShortProvince(province);
				}
				//**************************************************
				
				JSONObject json = new JSONObject();

				json.put("nameId", nameId);

				json.put("name", nameStr);
				
				json.put("roadType", roadType);
				
				json.put("province", province);

				array.add(json);
			}

			result.put("total", total);

			result.put("data", array);

			return result;
		} catch (Exception e) {

			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

	}

	private String getShortProvince(String province) {
		String shortProvince = "";
		if(province.contains("内蒙古")){
			shortProvince = "内蒙古";
		}else if(province.contains("黑龙江")){
			shortProvince = "黑龙江";
		}else if(province.contains("中国")){
			shortProvince = "全国";
		}else{
			shortProvince = province.substring(0, 2);
		}
		return shortProvince;
	}

	/**
	 * @Description:判断名称是否存在--查询大区库
	 * @param name
	 * @param adminIdList
     * @param sourceType
	 * @return 所在行政区划
	 * @throws Exception
	 * @author: y
	 * @time:2016-6-28 下午3:42:20
	 */
	public int isNameExists(String name, List<Integer> adminIdList, String sourceType) throws Exception {
		int resultAdmin = 0;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
        StringBuffer subSF = new StringBuffer();
		for(int adminId : adminIdList) {
			if(subSF.length() > 0)
                subSF.append(",");
            subSF.append("?");
		}
		String sql = "SELECT N.ADMIN_ID									\n"
				+ "  FROM RD_NAME N                                      \n"
				+ " WHERE N.NAME = ?                                     \n"
				+ "   AND (N.ADMIN_ID in(" + subSF.toString() + ") OR N.ADMIN_ID = 214)           \n";
        if(sourceType.equals("1407")) {//出口编号
            sql += " AND N.ROAD_TYPE = 4";
        }else if(sourceType.equals("8006")) {
            sql += " AND N.ROAD_TYPE = 1";
        }

		try {

		/*	//***********************以下代码是路演环境临时使用*begin***********************
			String dbId= SystemConfigFactory.getSystemConfig().getValue("region_db_id");
			
			if(StringUtils.isEmpty(dbId)){
				throw new Exception("未配置region_db_id系统参数。");
			}

			//路演环境临时使用
			conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));*/
			
			//***********************end ***********************
			
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
            for(int i = 0; i < adminIdList.size(); i++) {
                pstmt.setInt(i + 2, adminIdList.get(i));
            }


			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				resultAdmin = resultSet.getInt("ADMIN_ID");
			}

		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(conn, pstmt, resultSet);
		}

		return resultAdmin;
	}

	public static void main(String[] args) throws Exception {
		JSONArray arr = new JSONArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		
		String types = arr.join(",");
//		System.out.println(types);
		
		
		
//		RdNameSelector selector = new RdNameSelector();
//
//		System.out.println(selector.searchByName("", 10, 1));
	}
	
	/**
	 * web端查询rdName
	 * @author wangdongbin
	 * @param params
	 * @param tips
	 * @return
	 * @throws Exception
	 */
@SuppressWarnings({ "static-access", "unchecked" })
public JSONObject searchForWeb(JSONObject params,JSONArray tips) throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		try {
			JSONObject result = new JSONObject();
			
			conn = DBConnector.getInstance().getMetaConnection();
			
			ScPointAdminArea scPointAdminArea = new ScPointAdminArea(conn);
			Map<String,String> adminMap = scPointAdminArea.getAdminMap();
//			log.info(adminMap);	
			JSONObject param =  params.getJSONObject("params");
			String name = "" ;
			if(param.containsKey("name") && param.getString("name") != null){
				name = param.getString("name");
			}
			String nameGroupid = "";
			if(param.containsKey("nameGroupid") && param.getString("nameGroupid") != null){
				nameGroupid = param.getString("nameGroupid");
			}	
			String adminId = "";
			if(param.containsKey("adminId") && param.getString("adminId") != null){
				adminId = param.getString("adminId");
			}
			
			log.info("name: "+name+" nameGroupid: "+nameGroupid+" adminId: "+adminId);
			String sortby = params.getString("sortby");
			int pageSize = params.getInt("pageSize");
			int pageNum = params.getInt("pageNum");
			int flag = params.getInt("flag");//1是任务查，0是全库查
			log.info("flag: "+flag);
			int subtaskId = params.getInt("subtaskId");//获取subtaskid 
			log.info("searchForWeb :subtaskId: "+subtaskId);
			
			StringUtils sUtils = new StringUtils();
			
			StringBuilder sql = new StringBuilder();
			
			String ids = "";
			String tmep = "";
			Clob pidClod = null;
				if (tips.size()>0 || subtaskId >0) {
					//添加根据子任务id直接查询的sql 
					sql.append(" with q1 as ( ");
					
						sql.append("  SELECT  distinct r.NAME_GROUPID from rd_name r where r.src_resume = '\"task\":"+ subtaskId +"' ");
							// 添加过滤器条件
							if (name != null  && StringUtils.isNotEmpty(name) && !name.equals("null")) {
								sql.append(" and r.name like '%");
								sql.append(name);
								sql.append("%'");
							}
							if(nameGroupid != null  && StringUtils.isNotEmpty(nameGroupid) && !nameGroupid.equals("null")){
								sql.append(" and r.name_groupid ");
								sql.append("= ");
								sql.append(nameGroupid);
								sql.append(" ");
							}
							if(adminId != null  && StringUtils.isNotEmpty(adminId) && !adminId.equals("null")){
								sql.append(" and r.admin_id ");
								sql.append("= ");
								sql.append(adminId);
								sql.append(" ");
							}
							
							if (tips.size()>0) {
								sql.append(" union all  ");
								sql.append(" SELECT distinct tt.NAME_GROUPID FROM ");
								sql.append("( select substr(replace(t.src_resume,'\"',''),instr(replace(t.src_resume,'\"',''), ':') + 1,length(replace(src_resume,'\"',''))) as tipid,t.name,t.name_groupid,t.admin_id ");
								sql.append(" from rd_name t  where t.src_resume like '%tips%' ) tt");
								
								sql.append(" where 1=1");
								
								// 添加过滤器条件
								if (name != null  && StringUtils.isNotEmpty(name) && !name.equals("null")) {
									sql.append(" and tt.name like '%");
									sql.append(name);
									sql.append("%'");
								}
								if(nameGroupid != null  && StringUtils.isNotEmpty(nameGroupid) && !nameGroupid.equals("null")){
									sql.append(" and tt.name_groupid ");
									sql.append("= ");
									sql.append(nameGroupid);
									sql.append(" ");
								}
								if(adminId != null  && StringUtils.isNotEmpty(adminId) && !adminId.equals("null")){
									sql.append(" and tt.admin_id ");
									sql.append("= ");
									sql.append(adminId);
									sql.append(" ");
								}
								
								for (int i=0;i<tips.size();i++) {
									JSONObject tipsObj = tips.getJSONObject(i);
									ids += tmep;
									tmep = ",";
									ids +=tipsObj.getString("id");
								}
								pidClod = ConnectionUtil.createClob(conn);
								pidClod.setString(1, ids);
								sql.append(" and tt.tipid in (select column_value from table(clob_to_table(?)))");
							}
							
					sql.append(" ) ");
					
					sql.append("  SELECT * FROM (SELECT c.*, rownum rn FROM (select distinct COUNT(1) OVER(PARTITION BY 1) total, a.* from rd_name a, q1 q where 1 = 1  ");
							sql.append(" and a.name_groupid = q.NAME_GROUPID ");
							/*sql.append("  ORDER BY a.NAME_GROUPID DESC, a.NAME_ID DESC) c ");
					sql.append(" WHERE rownum <= ?)  WHERE rn >= ?");*/
			
					
				} else {
					result.put("total", 0);
					result.put("data", new JSONArray());
					return result;
				}
			
			// 添加排序条件
			if (sortby.length()>0) {
				int index = sortby.indexOf("-");
				if (index != -1) {
					sql.append(" ORDER BY ");
					String sortbyName = sUtils.toColumnName(sortby.substring(1));
					sql.append("  a.");
					sql.append(sortbyName);
					sql.append(" DESC");
				} else {
					sql.append(" ORDER BY ");
					String sortbyName = sUtils.toColumnName(sortby.substring(1));
					sql.append("  a.");
					sql.append(sortbyName);
				}
			} else {
				sql.append(" ORDER BY a.NAME_GROUPID DESC,a.NAME_ID DESC");
			}
			
			sql.append(" ) c");
			sql.append(" WHERE rownum <= ?)  WHERE rn >= ?");
			
			int startRow = (pageNum-1) * pageSize + 1;

			int endRow = pageNum * pageSize;
			log.info("rdname/websearch sql : " +sql.toString());
			pstmt = conn.prepareStatement(sql.toString());
			
			if (flag>0 && tips.size()>0) {
				pstmt.setClob(1, pidClod);
				pstmt.setInt(2, endRow);
				pstmt.setInt(3, startRow);
			} else {
				pstmt.setInt(1, endRow);

				pstmt.setInt(2, startRow);
			}
			resultSet = pstmt.executeQuery();
			
			int total = 0;
			
			List<JSONObject> data = new ArrayList<JSONObject>();
			
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				data.add(result2Json(resultSet, adminMap));
			}
			result.put("total", total);
			result.put("data", data);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}

/**
 * @Title: searchForWeb
 * @Description: 查全库的道路名
 * @param params
 * @return
 * @throws Exception  JSONObject
 * @throws 
 * @author zl zhangli5174@navinfo.com
 * @date 2017年4月9日 下午4:56:39 
 */
public JSONObject searchForWeb(JSONObject params) throws Exception {
	
	PreparedStatement pstmt = null;

	ResultSet resultSet = null;

	Connection conn = null;
	
	try {
		JSONObject result = new JSONObject();
		
		conn = DBConnector.getInstance().getMetaConnection();
		
		ScPointAdminArea scPointAdminArea = new ScPointAdminArea(conn);
		//log.info(scPointAdminArea);		
		Map<String,String> adminMap = scPointAdminArea.getAdminMap();
		//log.info(adminMap);	
		JSONObject param =  params.getJSONObject("params");
		String name = "" ;
		if(param.containsKey("name") && param.getString("name") != null 
				&& StringUtils.isNotEmpty(param.getString("name")) && !param.getString("name").equals("null")){
			name = param.getString("name");
		}
		String nameGroupid = "";
		if(param.containsKey("nameGroupid") && param.getString("nameGroupid") != null 
				&& StringUtils.isNotEmpty(param.getString("nameGroupid")) && !param.getString("nameGroupid").equals("null")){
			nameGroupid = param.getString("nameGroupid");
		}	
		String adminId = "";
		if(param.containsKey("adminId") && param.getString("adminId") != null 
				&& StringUtils.isNotEmpty(param.getString("adminId")) && !param.getString("adminId").equals("null")){
			adminId = param.getString("adminId");
		}
		String roadTypes = "";
		if(param.containsKey("roadTypes") && param.getJSONArray("roadTypes") != null && param.getJSONArray("roadTypes").size() > 0 ){
			JSONArray arr = param.getJSONArray("roadTypes");
			roadTypes = arr.join(",");
		}
		
		log.info("name: "+name+" nameGroupid: "+nameGroupid+" adminId: "+adminId);
		String sortby = params.getString("sortby");
		int pageSize = params.getInt("pageSize");
		int pageNum = params.getInt("pageNum");
		
		StringUtils sUtils = new StringUtils();
		
		StringBuilder sql = new StringBuilder();
		
			sql.append("with q1 as(");
			sql.append(" select distinct a.NAME_GROUPID  from rd_name a where 1=1 ");
				// 添加过滤器条件
				Iterator<String> keys = param.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					if (key.equals("name")) {
						if((!param.getString(key).isEmpty()) && !param.getString("name").equals("null")){
							sql.append(" and a.name like '%");
							sql.append(param.getString(key));
							sql.append("%'");
						}
						
					} else if(key.equals("nameGroupid") ){
						if(!param.getString(key).isEmpty() && !param.getString("nameGroupid").equals("null")){
							String columnName = sUtils.toColumnName(key);
							sql.append(" and a.");
							sql.append(columnName);
							sql.append(" = ");
							sql.append(param.getString(key));
							sql.append(" ");
						}
						
					}else if(key.equals("adminId") ){
						if(!param.getString(key).isEmpty() && !param.getString("adminId").equals("null")){
							String columnName = sUtils.toColumnName(key);
							sql.append(" and a.");
							sql.append(columnName);
							sql.append(" = ");
							sql.append(param.getString(key));
							sql.append(" ");
						}
						
					}else if(key.equals("roadTypes")){
						if(StringUtils.isNotEmpty(roadTypes) && !param.getString("roadTypes").equals("null")){
							String columnName = "road_type";
							sql.append(" and a.");
							sql.append(columnName);
							sql.append("  in( ");
							sql.append(roadTypes);
							sql.append(") ");
						}
						
					}else {
						String columnName = sUtils.toColumnName(key);
						if (!param.getString(key).isEmpty()) {
							sql.append(" and a.");
							sql.append(columnName);
							sql.append("='");
							sql.append(param.getString(key));
							sql.append("'");
						}
					}
				}

			sql.append(") ");
			sql.append("SELECT * ");
			sql.append(" FROM (SELECT c.*, rownum rn");
			sql.append(" FROM (select distinct COUNT (1) OVER (PARTITION BY 1) total,a.* ");
			sql.append(" from rd_name a , q1 q  where 1=1 and a.name_groupid = q.NAME_GROUPID ");
		
		// 添加排序条件
		if (sortby.length()>0) {
			int index = sortby.indexOf("-");
			if (index != -1) {
				sql.append(" ORDER BY ");
				String sortbyName = sUtils.toColumnName(sortby.substring(1));
				sql.append("  a.");
				sql.append(sortbyName);
				sql.append(" DESC");
			} else {
				sql.append(" ORDER BY ");
				String sortbyName = sUtils.toColumnName(sortby.substring(1));
				sql.append("  a.");
				sql.append(sortbyName);
			}
		} else {
			sql.append(" ORDER BY a.NAME_GROUPID DESC,a.NAME_ID DESC");
		}
		
		sql.append(" ) c");
		sql.append(" WHERE rownum <= ?)  WHERE rn >= ?");
		
		int startRow = (pageNum-1) * pageSize + 1;

		int endRow = pageNum * pageSize;
		log.info("rdname/websearch sql : " +sql.toString());
		pstmt = conn.prepareStatement(sql.toString());
		
		pstmt.setInt(1, endRow);

		pstmt.setInt(2, startRow);
			
		resultSet = pstmt.executeQuery();
		
		int total = 0;
		
		List<JSONObject> data = new ArrayList<JSONObject>();
		
		while (resultSet.next()) {
			if (total == 0) {
				total = resultSet.getInt("total");
			}
			data.add(result2Json(resultSet, adminMap));
		}
		result.put("total", total);
		result.put("data", data);
		return result;
	} catch (Exception e) {
		throw e;
	} finally {
		DbUtils.closeQuietly(resultSet);
		DbUtils.closeQuietly(pstmt);
		DbUtils.closeQuietly(conn);
	}
}
	
	/**
	 * 判断是否存在重复的名称
	 * @author wangdongbin
	 * @param rdName
	 * @return
	 * @throws Exception
	 */
	public JSONObject checkRdNameExists(RdName rdName) throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		JSONObject resultObj = new JSONObject();
		Integer nameGroupid  = rdName.getNameGroupid();
		
		// 检查是否存在同一行政区划、同一道路类型（默认是未区分）的相同的道路名数据
		//（对于“行政区划”为“全国”时，不判断是否重名，即允许重复名称记录存在，但NAME_GROUPID不同；）
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM rd_name");
		sb.append(" WHERE name=:1");
		sb.append(" AND road_type=:2");
		sb.append(" AND admin_id=:3");
		// “行政区划”为“全国”
		if (rdName.getAdminId() != null && rdName.getAdminId().equals(214)){//如果行政区划为全国
			if(nameGroupid !=null && nameGroupid != 0) {//如果nameGroupId不为空,继续查数据库
				sb.append(" AND name_groupid=:4");
			}else{//满足 “行政区划”为“全国” 并且 NAME_GROUPID不同 条件,则不查重
				return resultObj;
			}
		}
		if (rdName.getNameId() != null) {
			sb.append(" AND name_id !="+rdName.getNameId());
		}
		
		try {
			
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, rdName.getName());

			pstmt.setInt(2, rdName.getRoadType());
			
			pstmt.setInt(3, rdName.getAdminId());
			
			if (rdName.getAdminId() != null && rdName.getAdminId() == 214 && rdName.getNameGroupid() !=null 
					&& rdName.getNameGroupid() != 0) {
				pstmt.setInt(4, rdName.getNameGroupid());
			}

			resultSet = pstmt.executeQuery();
			
			if (resultSet.next()) {
				resultObj = result2Json(resultSet,new HashMap<String,String>());
				if(resultObj.get("nameGroupid") != null){
					Integer newNameGroupId = resultObj.getInt("nameGroupid");//获取查询出来的nameGroupid
		 			if(rdName.getAdminId() != null && rdName.getAdminId() == 214 && newNameGroupId != null && newNameGroupId != 0 
		 					&& !newNameGroupId.equals(nameGroupid)){//在 行政区划为全国的情况下 如果查询数据库返回的nameGroupid 存在且与上传的nameGroupid不同,怎不查重
		 				return new JSONObject();
		 			}
				}
					
			}
			
			return resultObj;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 将查询结果转为json型
	 * @param resultSet
	 * @return
	 * @throws Exception
	 */
	private JSONObject result2Json(ResultSet resultSet,Map<String,String> adminMap) throws Exception{
		JSONObject rdNameObj = new JSONObject();
		try {//String c = a == null ? "" : a;
			rdNameObj.put("nameId", resultSet.getInt("NAME_ID"));
			rdNameObj.put("nameGroupid", resultSet.getInt("NAME_GROUPID"));
			rdNameObj.put("langCode", resultSet.getString("LANG_CODE") == null ? "" : resultSet.getString("LANG_CODE"));
			rdNameObj.put("name", resultSet.getString("NAME")  == null ? "" : resultSet.getString("NAME"));
			rdNameObj.put("type", resultSet.getString("TYPE")  == null ? "" : resultSet.getString("TYPE"));
			rdNameObj.put("base", resultSet.getString("BASE")  == null ? "" : resultSet.getString("BASE"));
			rdNameObj.put("prefix", resultSet.getString("PREFIX")  == null ? "" : resultSet.getString("PREFIX"));
			rdNameObj.put("infix", resultSet.getString("INFIX")  == null ? "" : resultSet.getString("INFIX"));
			rdNameObj.put("suffix", resultSet.getString("SUFFIX")  == null ? "" : resultSet.getString("SUFFIX"));
			rdNameObj.put("namePhonetic", resultSet.getString("NAME_PHONETIC")  == null ? "" : resultSet.getString("NAME_PHONETIC"));
			rdNameObj.put("typePhonetic", resultSet.getString("TYPE_PHONETIC")  == null ? "" : resultSet.getString("TYPE_PHONETIC"));
			rdNameObj.put("basePhonetic", resultSet.getString("BASE_PHONETIC")  == null ? "" : resultSet.getString("BASE_PHONETIC"));
			rdNameObj.put("prefixPhonetic", resultSet.getString("PREFIX_PHONETIC")  == null ? "" : resultSet.getString("PREFIX_PHONETIC"));
			rdNameObj.put("infixPhonetic", resultSet.getString("INFIX_PHONETIC")  == null ? "" : resultSet.getString("INFIX_PHONETIC"));
			rdNameObj.put("suffixPhonetic", resultSet.getString("SUFFIX_PHONETIC")  == null ? "" : resultSet.getString("SUFFIX_PHONETIC"));
			rdNameObj.put("srcFlag", resultSet.getInt("SRC_FLAG"));
			rdNameObj.put("roadType", resultSet.getInt("ROAD_TYPE"));
			
			int adminId = resultSet.getInt("ADMIN_ID");
			rdNameObj.put("adminId", adminId);
			if(adminId == 214){
				rdNameObj.put("adminName","全国");
			}else{
				if (!adminMap.isEmpty()) {
					if (adminMap.containsKey(String.valueOf(adminId))) {
						rdNameObj.put("adminName", adminMap.get(String.valueOf(adminId)));
					} else {
						rdNameObj.put("adminName","");
					}
					
				}
			}
			rdNameObj.put("codeType", resultSet.getInt("CODE_TYPE"));
			rdNameObj.put("voiceFile", resultSet.getString("VOICE_FILE")  == null ? "" : resultSet.getString("VOICE_FILE"));
			rdNameObj.put("srcResume", resultSet.getString("SRC_RESUME")  == null ? "" : resultSet.getString("SRC_RESUME"));
			/*try{
				rdNameObj.put("tipsId", resultSet.getString("tipid")  == null ? "" : resultSet.getString("tipid"));	
			}catch(Exception e){
				//tipsId 不存在
				log.warn("tipsId没有获取", e);
			}*/
			rdNameObj.put("paRegionId", resultSet.getInt("PA_REGION_ID"));
			rdNameObj.put("splitFlag", resultSet.getInt("SPLIT_FLAG"));
			rdNameObj.put("memo", resultSet.getString("MEMO")  == null ? "" : resultSet.getString("MEMO"));
			rdNameObj.put("routeId", resultSet.getInt("ROUTE_ID"));
			rdNameObj.put("processFlag", resultSet.getInt("PROCESS_FLAG"));
			if(resultSet.getString("CITY") != null && StringUtils.isNotEmpty(resultSet.getString("CITY"))){
				rdNameObj.put("city", resultSet.getString("CITY"));
			}else{
				rdNameObj.put("city", "");
			}
			//******zl 2017.04.09******
			//hw 信息标识
			Integer nameGroupId = resultSet.getInt("NAME_GROUPID");
			ScRoadnameHwInfoService scRoadnameHwInfoService = new ScRoadnameHwInfoService();
			ScRoadnameHwInfo scRoadnameHwInfo = new ScRoadnameHwInfo();
			scRoadnameHwInfo.setNameGroupid(nameGroupId);
			ScRoadnameHwInfo newScRoadnameHwInfo = scRoadnameHwInfoService.query(scRoadnameHwInfo);
			if(newScRoadnameHwInfo != null){
				rdNameObj.put("hwInfoFlag", 1);
			}else{
				rdNameObj.put("hwInfoFlag", 0);
			}
			//*************************
			
			return rdNameObj;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * @Title: searchForWebByNameId
	 * @Description: 根据rdNamed的NameId查询
	 * @param nameId
	 * @return  JSONObject
	 * @throws Exception 
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月17日 下午8:16:07 
	 */
	public JSONObject searchForWebByNameId(String nameId) {
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		try {
			//JSONObject result = new JSONObject();
			conn = DBConnector.getInstance().getMetaConnection();
			ScPointAdminArea scPointAdminArea = new ScPointAdminArea(conn);
			Map<String,String> adminMap = scPointAdminArea.getAdminMap();
			StringBuilder sql = new StringBuilder();
				sql.append("SELECT * ");
				sql.append(" from rd_name a where 1=1 ");
				sql.append(" And a.name_id = ?");
			pstmt = conn.prepareStatement(sql.toString());
				pstmt.setString(1, nameId);
			resultSet = pstmt.executeQuery();
			List<JSONObject> data = new ArrayList<JSONObject>();
			while (resultSet.next()) {
				data.add(result2Json(resultSet, adminMap));
			}
			//result.put("data", data.get(0));
			//return result;
			return data.get(0);
		} catch (Exception e) {
			
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
		return null;
	
	}

	/**
	 * @Title: udateResultStatusById
	 * @Description: 修改某条道路名检查结果的状态
	 * @param valExceptionId
	 * @param qaStatus 
	 * @return  JSONObject
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月17日 下午8:32:34 
	 */
	public void udateResultStatusById(int valExceptionId, int qaStatus) {
		Connection conn = null; 
		JSONObject result = null;
		PreparedStatement pstmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" update ni_val_exception set qa_status = ? ");
			sql.append(" where 1=1 ");
			sql.append(" and  val_exception_id = ?");
			result = new JSONObject();
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1, qaStatus);
			pstmt.setInt(2, valExceptionId);
			pstmt.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * @Title: searchRdNameFix
	 * @Description: 获取道路名 前 后 中 缀
	 * @param langCode
	 * @return
	 * @throws Exception  JSONObject
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年3月7日 上午9:31:03 
	 */
	public JSONObject searchRdNameFix(String langCode) throws Exception {
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		try {
			JSONObject result = new JSONObject();
			
			conn = DBConnector.getInstance().getMetaConnection();
			
			StringBuilder sql = new StringBuilder();
			
			String thislangCode = "CHI";
			if(langCode != null && StringUtils.isNotEmpty(langCode) && (langCode.equals("CHI") || langCode.equals("CHT"))){
				thislangCode = langCode;
			}
			//添加根据子任务id直接查询的sql 
			sql.append("select i.name,i.englishName,i.lang_code,2 type from sc_roadname_infix i  where i.lang_code = '"+thislangCode+"' ");
			sql.append(" union all  ");
			sql.append(" select s.name,s.englishName,s.Lang_code,1 type from sc_roadname_suffix s  where s.lang_code = '"+thislangCode+"' ");
					
			
			log.info("rdname/searchFix sql : " +sql.toString());
			pstmt = conn.prepareStatement(sql.toString());
			resultSet = pstmt.executeQuery();
			
			JSONArray suffixArry = new JSONArray();
			JSONArray infixArry = new JSONArray();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				String englishName = resultSet.getString("englishName");
				String lang_code = resultSet.getString("lang_code");
				int type = resultSet.getInt("type");
				
				if(!langCode.equals("CHI") && !langCode.equals("CHT")){//英文或者葡文
					JSONObject rdNameFixObj = new JSONObject();
					rdNameFixObj.put("id", englishName);
					rdNameFixObj.put("label", englishName);
					//rdNameFixObj.put("langCode", langCode);
					if(type == 1){//前后缀
						suffixArry.add(rdNameFixObj);
					}else{//中缀
						infixArry.add(rdNameFixObj);
					}
				}else{
					JSONObject rdNameFixObj = new JSONObject();
					rdNameFixObj.put("id", name);
					rdNameFixObj.put("label", name);
					//rdNameFixObj.put("langCode", langCode);
					if(type == 1){//前后缀
						suffixArry.add(rdNameFixObj);
					}else{//中缀
						infixArry.add(rdNameFixObj);
					}
				}
			}
			result.put("suffix", suffixArry);
			result.put("infix", infixArry);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}


}
