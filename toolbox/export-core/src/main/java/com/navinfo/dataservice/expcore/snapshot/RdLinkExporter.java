package com.navinfo.dataservice.expcore.snapshot;

import java.io.ByteArrayInputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.commons.lang.StringUtils;

public class RdLinkExporter {

	public static void run(Connection sqliteConn, Statement stmt,
			Connection conn, String operateDate, Set<Integer> meshes)
			throws Exception {

		// creating a LINESTRING table
		stmt.execute("create table gdb_rdLine(pid integer primary key)");
	/*	stmt.execute("select addgeometrycolumn('gdb_rdLine','geometry',4326,'GEOMETRY','XY')");//add GEOMETRY column
	*/	/*stmt.execute("select createspatialindex('gdb_rdLine','geometry')");
	*/	
		stmt.execute("alter table gdb_rdLine add isADAS integer;");
		stmt.execute("alter table gdb_rdLine add display_style text;");
		stmt.execute("alter table gdb_rdLine add display_text text;");
		stmt.execute("alter table gdb_rdLine add meshid text;");
		stmt.execute("alter table gdb_rdLine add kind integer;");
		stmt.execute("alter table gdb_rdLine add direct integer;");
		stmt.execute("alter table gdb_rdLine add appInfo integer;");
		stmt.execute("alter table gdb_rdLine add tollInfo integer;");
		stmt.execute("alter table gdb_rdLine add multiDigitized integer;");
		stmt.execute("alter table gdb_rdLine add specialTraffic integer;");
		stmt.execute("alter table gdb_rdLine add fc integer;");
		stmt.execute("alter table gdb_rdLine add laneNum integer;");
		stmt.execute("alter table gdb_rdLine add laneLeft integer;");
		stmt.execute("alter table gdb_rdLine add laneRight integer;");
		stmt.execute("alter table gdb_rdLine add isViaduct integer;");
		stmt.execute("alter table gdb_rdLine add paveStatus integer;");
		stmt.execute("alter table gdb_rdLine add forms Blob;");
		stmt.execute("alter table gdb_rdLine add styleFactors Blob;");
		stmt.execute("alter table gdb_rdLine add speedLimit Blob;");
		stmt.execute("alter table gdb_rdLine add op_date text;");
		stmt.execute("alter table gdb_rdLine add op_lifecycle integer;");
		stmt.execute("alter table gdb_rdLine add names Blob;");
		stmt.execute("alter table gdb_rdLine add sNodePid integer;");
		stmt.execute("alter table gdb_rdLine add eNodePid integer;");
		//********zl 2017.04.11 *************
//		stmt.execute("alter table gdb_rdLine add isADAS integer;");
		//***********************************

		String insertSql = "insert into gdb_rdLine values("
//				+ "?, GeomFromText(?, 4326), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				+ "?,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement prep = sqliteConn.prepareStatement(insertSql);

		//String sql = "select a.*,        display_text.name,        styleFactors1.types,        styleFactors2.lane_types,        speedlimits.from_speed_limit,        speedlimits.to_speed_limit,        forms.forms   from rd_link a,        (select a.link_pid,listagg(B.NAME,'/') within group(order by name_class,seq_num) name from rd_link_name a, rd_name b where a.name_groupid = b.name_groupid AND a.NAME_CLASS in (1,2) and b.lang_code = 'CHI' and a.u_record != 2  group by link_pid) display_text,        (select link_pid,                listagg(type, ',') within group(order by type) types           from (select a.link_pid, type                   from rd_link_limit a                  where (type in (0, 4, 5, 6) or (type=2 and vehicle=2147483784)) and a.u_record != 2)          group by link_pid) styleFactors1,        (select link_pid,                listagg(lane_type, ',') within group(order by lane_type) lane_types           from rd_lane a          where a.u_record != 2          group by link_pid) styleFactors2,        (select link_pid, from_speed_limit, to_speed_limit           from rd_link_speedlimit a          where speed_type = 0            and a.u_record != 2) speedlimits,        (select link_pid,                listagg(form_of_way, ',') within group(order by form_of_way) forms           from rd_link_form          where u_record != 2          group by link_pid) forms  where a.link_pid = display_text.link_pid(+)    and a.link_pid = styleFactors1.link_pid(+)    and a.link_pid = styleFactors2.link_pid(+)    and a.link_pid = speedlimits.link_pid(+)    and a.link_pid = forms.link_pid(+)    and a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?)))";
		//**********zl 2016.12.27 *************
		
		String sql = "select a.*, display_text.name, styleFactors1.types,styleFactors2.lane_types,"
				+ "speedlimits.from_speed_limit,speedlimits.to_speed_limit,forms.forms   "
				+ "from rd_link a,"
				+ "(select a.link_pid,listagg(B.NAME,'/') within group(order by name_class,seq_num) name "
					+ "from rd_link_name a, rd_name b "
					+ "where a.name_groupid = b.name_groupid AND a.NAME_CLASS in (1,2) and b.lang_code = 'CHI' and a.u_record != 2 and a.name_type != 15 "
					+ "group by link_pid) display_text,"
					+ "(select link_pid,"
					+ "listagg(type, ',') within group(order by type) types   "
					+ "from ("
					//**********zl 2017.03.10 增加遗漏条件 "存在TYPE=2且VEHICLE=2147483786（步行者、急救车、配送卡车）且TIME_DOMAIN为空"
						+ "select a.link_pid, type from rd_link_limit a where (type in (0, 4, 5, 6, 10) or (type=2 and vehicle=2147483784) ) and a.u_record != 2 "
						+ " union all "
						+ " select b.link_pid,98 type from rd_link_limit b where  type=2 and VEHICLE=2147483786 and TIME_DOMAIN is null and b.u_record != 2 "
					+ " )  group by link_pid) styleFactors1, "
						//***********************
					+ "(select link_pid, listagg(lane_type, ',') within group(order by lane_type) lane_types  from rd_lane a "
							+ "where a.u_record != 2 group by link_pid) styleFactors2,"
						+ "(select link_pid, from_speed_limit, to_speed_limit from rd_link_speedlimit a  where speed_type = 0 and a.u_record != 2) speedlimits, "
						+ "(select link_pid, listagg(form_of_way, ',') within group(order by form_of_way) forms  from rd_link_form "
							+ "where u_record != 2  group by link_pid) forms  where a.link_pid = display_text.link_pid(+)    and a.link_pid = styleFactors1.link_pid(+) "
							+ " and a.link_pid = styleFactors2.link_pid(+)    and a.link_pid = speedlimits.link_pid(+)    and a.link_pid = forms.link_pid(+) "
							+ " and a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?)))";
		//*************************************
		Clob clob = conn.createClob();
		clob.setString(1, StringUtils.join(meshes, ","));

		String sql3 = " select * from rd_link a where a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?)))";
//		
		PreparedStatement stmt3 = conn.prepareStatement(sql3);

		stmt3.setClob(1, clob);
		
		ResultSet resultSet3 = stmt3.executeQuery();

		resultSet3.setFetchSize(5000);

		//****************************************
		
		Set<Integer> linkPidSet = new HashSet<Integer>();//所有linkPid 的集合 无重复
		List<Integer> sNodePidList=new ArrayList<Integer>();//所有sNodePid 的集合 有重复
		List<Integer> eNodePidList=new ArrayList<Integer>();//所有eNodePid 的集合 有重复
		Map<Integer,Integer> pidKind = new HashMap<Integer,Integer>();// 所有 linkpid,kind的map集合
		Map<Integer,Double> pidLength = new HashMap<Integer,Double>();// 所有 linkpid,length的map集合
		Map<Integer,Integer> sNodeAndeNode = new HashMap<Integer,Integer>();// 所有 sNodePid,eNodePid的map集合
		Map<Integer,Integer> eNodeAndsNode = new HashMap<Integer,Integer>();// 所有eNodePid, sNodePid的map集合
		Map<Integer,Integer> sNodeAndLinkPid = new HashMap<Integer,Integer>();// 所有 sNodePid,linkPid的map集合
		Map<Integer,Integer> eNodeAndLinkPid = new HashMap<Integer,Integer>();// 所有eNodePid,linkPid的map集合
		
		
		while (resultSet3.next()) {
			if(resultSet3.getInt("kind") != 10){//排除十级路
				linkPidSet.add(resultSet3.getInt("link_pid"));
				sNodePidList.add(resultSet3.getInt("S_NODE_PID"));
				eNodePidList.add(resultSet3.getInt("E_NODE_PID"));
				pidKind.put(resultSet3.getInt("link_pid"), resultSet3.getInt("kind"));
				pidLength.put(resultSet3.getInt("link_pid"), resultSet3.getDouble("length"));
				sNodeAndeNode.put(resultSet3.getInt("S_NODE_PID"), resultSet3.getInt("E_NODE_PID"));
				eNodeAndsNode.put(resultSet3.getInt("E_NODE_PID"), resultSet3.getInt("S_NODE_PID"));
				sNodeAndLinkPid.put(resultSet3.getInt("S_NODE_PID"), resultSet3.getInt("link_pid"));
				eNodeAndLinkPid.put(resultSet3.getInt("E_NODE_PID"), resultSet3.getInt("link_pid"));
			}
			
		}
		//****************************************
		PreparedStatement stmt2 = conn.prepareStatement(sql);

		stmt2.setClob(1, clob);

		ResultSet resultSet = stmt2.executeQuery();

		resultSet.setFetchSize(5000);
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		int count = 0;

		while (resultSet.next()) {

			JSONObject json = enclosingRdLine(resultSet, operateDate,
					sNodePidList,eNodePidList,
					pidKind,pidLength,
					sNodeAndeNode,eNodeAndsNode,
					sNodeAndLinkPid,eNodeAndLinkPid);

			int pid = json.getInt("pid");

			if (map.containsKey(pid)) {
				continue;
			}

			map.put(pid, 0);

			prep.setInt(1, pid);

//			prep.setString(2, json.getString("geometry"));
			prep.setInt(2, json.getInt("isADAS"));

			prep.setString(3, json.getString("display_style"));

			prep.setString(4, json.getString("display_text"));

			prep.setString(5, json.getString("meshid"));

			prep.setInt(6, json.getInt("kind"));

			prep.setInt(7, json.getInt("direct"));

			prep.setInt(8, json.getInt("appInfo"));

			prep.setInt(9, json.getInt("tollInfo"));

			prep.setInt(10, json.getInt("multiDigitized"));

			prep.setInt(11, json.getInt("specialTraffic"));

			prep.setInt(12, json.getInt("fc"));

			prep.setInt(13, json.getInt("laneNum"));

			prep.setInt(14, json.getInt("laneLeft"));

			prep.setInt(15, json.getInt("laneRight"));

			prep.setInt(16, json.getInt("isViaduct"));

			prep.setInt(17, json.getInt("paveStatus"));

			byte[] forms = json.getString("forms").getBytes();

			prep.setBinaryStream(18, new ByteArrayInputStream(forms),
					forms.length);

			byte[] styleFactors = json.getString("styleFactors").getBytes();

			prep.setBinaryStream(19, new ByteArrayInputStream(styleFactors),
					styleFactors.length);

			byte[] speedLimit = json.getString("speedLimit").getBytes();

			prep.setBinaryStream(20, new ByteArrayInputStream(speedLimit),
					speedLimit.length);

			prep.setString(21, json.getString("op_date"));

			prep.setInt(22, json.getInt("op_lifecycle"));

			byte[] names = json.getString("names").getBytes();

			prep.setBinaryStream(23, new ByteArrayInputStream(names),
					names.length);

			//
			prep.setLong(24, json.getLong("sNodePid"));
			prep.setLong(25, json.getLong("eNodePid"));
			
//			prep.setInt(26, json.getInt("isADAS"));

			prep.executeUpdate();

			count += 1;

			if (count % 5000 == 0) {
				sqliteConn.commit();
			}
		}

		sqliteConn.commit();
	}

	private static JSONObject enclosingRdLine(ResultSet rs, String operateDate, List<Integer> sNodePidList, 
			List<Integer> eNodePidList, Map<Integer, Integer> pidKind, 
			Map<Integer, Double> pidLength, Map<Integer, Integer> sNodeAndeNode,
			Map<Integer, Integer> eNodeAndsNode,
			Map<Integer, Integer> sNodeAndLinkPid,Map<Integer, Integer> eNodeAndLinkPid)
			throws Exception {

		JSONObject json = new JSONObject();

		int pid = rs.getInt("link_pid");

		json.put("pid", pid);

		String meshid = rs.getString("mesh_id");

		json.put("meshid", meshid);

		JSONArray names = new JSONArray();

		String name = rs.getString("name");

		String display_text = "";

		if (name != null) {

			String[] sss = name.split("/");

			boolean flag1 = false;

			for (String s : sss) {
				if (s != null) {
					if (flag1) {
						display_text += "/";
					}

					display_text += s;

					JSONObject namejson = new JSONObject();

					namejson.put("name", s);

					names.add(namejson);

					flag1 = true;
				}
			}
		}

		json.put("names", names);

		json.put("display_text", display_text);

		int kind = rs.getInt("kind");

		json.put("kind", kind);

		int direct = rs.getInt("direct");

		json.put("direct", direct);

		int appInfo = rs.getInt("app_info");

		json.put("appInfo", appInfo);

		int tollInfo = rs.getInt("toll_info");

		json.put("tollInfo", tollInfo);

		int multiDigitized = rs.getInt("multi_digitized");

		json.put("multiDigitized", multiDigitized);

		int specialTraffic = rs.getInt("special_traffic");

		json.put("specialTraffic", specialTraffic);

		int fc = rs.getInt("function_class");

		json.put("fc", fc);

		int laneNum = rs.getInt("lane_num");

		json.put("laneNum", laneNum);

		int laneLeft = rs.getInt("lane_left");

		json.put("laneLeft", laneLeft);

		int laneRight = rs.getInt("lane_right");

		json.put("laneRight", laneRight);

		int isViaduct = rs.getInt("is_viaduct");

		json.put("isViaduct", isViaduct);

		int paveStatus = rs.getInt("pave_status");

		json.put("paveStatus", paveStatus);

		STRUCT struct = (STRUCT) rs.getObject("geometry");

		JGeometry geom = JGeometry.load(struct);

		WKT wkt = new WKT();

		String geometry = new String(wkt.fromJGeometry(geom));

		json.put("geometry", geometry);

		String forms = rs.getString("forms");

		forms = "[" + (forms == null ? "" : forms) + "]";

		JSONArray array = JSONArray.fromObject(forms);

		JSONArray formsArray = new JSONArray();

		for (int i = 0; i < array.size(); i++) {
			JSONObject form = new JSONObject();

			form.put("form", array.getInt(i));

			formsArray.add(form);
		}

		json.put("forms", formsArray);

		JSONArray styleFactors = new JSONArray();

		String types = rs.getString("types");

		if (types != null && types.length() > 0) {
			String[] splits = types.split(",");

			for (String s : splits) {
				JSONObject jo = new JSONObject();

				jo.put("factor", Integer.parseInt(s));

				styleFactors.add(jo);
			}
		}

		String laneTypes = rs.getString("lane_types");

		if (laneTypes != null && laneTypes.length() > 0) {

			String[] splits = laneTypes.split(",");

			for (String s : splits) {

				String bin = Integer.toBinaryString(Integer.valueOf(s));

				int len = bin.length();

				boolean flag = false;

				if (bin.length() >= 12) {
					String p = bin.substring(len - 12, len - 11);

					if ("1".equals(p)) {
						flag = true;
					}
				}

				if (flag) {
					JSONObject jo = new JSONObject();
					jo.put("factor", 99);
					styleFactors.add(jo);
					break;
				}
			}
		}

		json.put("styleFactors", styleFactors);

		int style = computeStyle(formsArray,
				styleFactors,multiDigitized);

		json.put("display_style", kind + "," + style);

		int from_speed_limit = rs.getInt("from_speed_limit");

		int to_speed_limit = rs.getInt("to_speed_limit");

		JSONObject jo = new JSONObject();

		jo.put("from", from_speed_limit);

		jo.put("to", to_speed_limit);

		json.put("speedLimit", jo);

		json.put("op_date", operateDate);

		json.put("op_lifecycle", 0);
		//s,enodpis
		json.put("sNodePid",rs.getLong("S_NODE_PID"));
		json.put("eNodePid",rs.getLong("E_NODE_PID"));
		
		//****zl 2017.04.11 *********
		int adasFlag = rs.getInt("ADAS_FLAG");
		double linkLength = rs.getDouble("LENGTH");
		int isADAS  = 2;
		if(adasFlag == 1){
			isADAS = 1;
		}else if(adasFlag == 0 || adasFlag == 2){
			List<Integer> formList = new ArrayList<>();
			for (int i = 0; i < formsArray.size(); i++) {
				JSONObject formsJson = formsArray.getJSONObject(i);
				formList.add(formsJson.getInt("form"));
			}
			if(kind > 7){//7级以下道路
				isADAS = 3;
			}else if(formList.contains(35) && direct == 1){//双向调头口
				isADAS = 3;
			}else if(kind == 7 && linkLength < 1000 ){//RD_LINK.KIND=7且link的长度小于1公里且为断头路
				//计算此link 的sNode和eNode 出现的次数
				List<Integer> nodeList = new ArrayList<>();
				nodeList.addAll(sNodePidList);
				nodeList.addAll(eNodePidList);
				
				Integer sNodePid = rs.getInt("S_NODE_PID");
				Integer eNodePid = rs.getInt("E_NODE_PID");
				
				//计算断头路 
				double length = linkLength;
				for(int i=0; i<nodeList.size() ;i++){
					int sNodeCount = 1;
					int eNodeCount = 1;
					if(sNodePid > 0){
						sNodeCount =getNodeCount(sNodePid,nodeList);
					}
					if(eNodeCount > 0){
						eNodeCount =getNodeCount(eNodePid,nodeList);
					}
					if((sNodeCount ==1 && eNodeCount ==2) || (sNodeCount ==2 && eNodeCount ==1)){//
						Map<String,Object> map = 
								getLengthAll(sNodeCount,eNodeCount,
												sNodePid,eNodePid,
												sNodePidList,eNodePidList,
												pidKind,pidLength,
												sNodeAndeNode,eNodeAndsNode,
												sNodeAndLinkPid,eNodeAndLinkPid,
												nodeList);
							if(map.containsKey("sNodePid")){
								sNodePid=(Integer) map.get("sNodePid");
							}else{
								sNodePid=0;
							}
							if(map.containsKey("eNodePid")){
								eNodePid=(Integer) map.get("eNodePid");
							}else{
								eNodePid=0;
							}
							double len = (double) map.get("length");
							length+= len;
					}else{
						break;
					}
					
				}
				
				if(length < 1000){
					isADAS =3;
				}
				
			}
			
		}
		json.put("isADAS", isADAS);

		return json;
	}

	private static int computeStyle(JSONArray forms, 
			JSONArray styleFactors, int multiDigitized) {
		int style = -1;
		int count = 0;
		
		if (multiDigitized == 1) {
			style = 32;
			count+=1;
		}

		List<Integer> formList = new ArrayList<>();

		for (int i = 0; i < forms.size(); i++) {
			JSONObject json = forms.getJSONObject(i);

			formList.add(json.getInt("form"));
		}

		if (formList.contains(50)) {
			style = 33;
			count+=1;
		}

		if (formList.contains(15)) {
			style = 4;
			count+=1;
		}

		if (formList.contains(36) 
				&& !(formList.contains(12) || formList.contains(13) || formList.contains(53) || formList.contains(54))) {
			style = 14;
			count+=1;
		}

		if (formList.contains(34) && (!formList.contains(35) || !formList.contains(39))) {
			style = 12;
			count+=1;
		}
		/*if (formList.contains(22)) {
			style = 14;
			count+=1;
		}*/

		/*if (formList.contains(24)) {
			
			style = 14;
			count+=1;
		}*/

		/*if (formList.contains(30)) {
			style = 14;
			count+=1;
		}*/

		/*if (formList.contains(34)) {
			return 12;
		}*/

		List<Integer> styleList = new ArrayList<>();
		for (int i = 0; i < styleFactors.size(); i++) {
			JSONObject json = styleFactors.getJSONObject(i);

			styleList.add(json.getInt("factor"));
		}

		if (styleList.contains(98)) {
			style = 29;
			count+=1;
		}
		
		if (formList.contains(52)) {
			style = 15;
			count+=1;
		}
		
		if (styleList.contains(2)) {
			style = 31;
			count+=1;
		}

		if (styleList.contains(10)) {
			style = 37;
			count+=1;
		}

		if (count >= 2 ) {
			style = 36;
		}
		
		if (count == 0) {
			style = 255;
		}
		System.out.println("style: "+style);
		System.out.println("count: "+count);
		return style;
	}
	
	
	private static Map<String, Object> getLengthAll(int sNodeCount,int eNodeCount,
			int sNodePid, int eNodePid,
			List<Integer> sNodePidList,List<Integer> eNodePidList, 
			Map<Integer, Integer> pidKind, Map<Integer, Double> pidLength,
			Map<Integer, Integer> sNodeAndeNode,Map<Integer, Integer> eNodeAndsNode,
			Map<Integer, Integer> sNodeAndLinkPid,Map<Integer, Integer> eNodeAndLinkPid,
			List<Integer> nodeList) {
		Map<String,Object> map = new HashMap<String,Object>();
		double length = 0;
		int flag = 0;
		//第一步
		
		if(sNodeCount == 1 && eNodeCount == 2){
			//追踪eNode  返回的是7级路的总长度
				map = traceNode(0,
						 sNodePid,eNodePid,
						 sNodePidList,eNodePidList,
						pidKind,pidLength,
						sNodeAndeNode,eNodeAndsNode,
						sNodeAndLinkPid,eNodeAndLinkPid,nodeList);
		}else if(eNodeCount == 1 && sNodeCount == 2){
			//追踪sNode  返回的是7级路的总长度
				map =  traceNode(1,
						 sNodePid,eNodePid,
						 sNodePidList,eNodePidList,
						 pidKind,pidLength,
						 sNodeAndeNode,eNodeAndsNode,
						 sNodeAndLinkPid,eNodeAndLinkPid,nodeList);
		}else{
			map.put("sNodePid", -1);
			map.put("eNodePid", -1);
			map.put("length", length);
		}
		
		
		return map;
	}
	/**
	 * @Title: getNodeCount
	 * @Description: 
	 * @param nodePid
	 * @param nodeList
	 * @return  int
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月26日 上午10:15:10 
	 */
	private static int getNodeCount(int nodePid,List<Integer> nodeList){
		int nodeCount = 1;
		if(nodeList != null && nodeList.size() >0 ){
			for(int i=0;i < nodeList.size();i++ ){
				nodeCount = 1;  
	            for (int j = i + 1; j < nodeList.size(); j++) {  
	                if (nodeList.get(i) == nodeList.get(j)) {  
	                	nodeCount++;// 次数+1  
	                	nodeList.remove(j);  
	                    j--;// 这里是重点，因为集合remove（）后，长度改变了，对应的下标也不再是原来的下标，//仔细体会  
	                }  
	            }
			
			}
		
		}
		return nodeCount;
	}
	private static Map<String,Object> traceNode(int flag ,
			int sNodePid, int eNodePid, 
			List<Integer> sNodePidList, List<Integer> eNodePidList,
			Map<Integer, Integer> pidKind, Map<Integer, Double> pidLength, 
			Map<Integer, Integer> sNodeAndeNode,Map<Integer, Integer> eNodeAndsNode,
			Map<Integer, Integer> sNodeAndLinkPid,Map<Integer, Integer> eNodeAndLinkPid, 
			List<Integer> nodeList) {
		Map<String,Object> map = new HashMap<String,Object>();
		double length = 0;
		if(flag > 0){//追踪sNode  返回的是所7级路的总长度
			//终点只挂接了本身
			int nexteNode1 = eNodeAndsNode.get(sNodePid);//以此起点为终点去获取下一个终点
			int nextLinkPid = sNodeAndLinkPid.get(nexteNode1);
			int nextKind = pidKind.get(nextLinkPid);
			if(nextKind == 7){
				length+=pidLength.get(nextLinkPid);
			}
			map.put("sNodePid", nexteNode1);
			map.put("eNodePid", -1);
			map.put("length", length);
			
		}else{//追踪eNode  返回的是所7级路的总长度
			//启点只挂接了本身
			int nexteNode1 = sNodeAndeNode.get(eNodePid);//以此终点为起点去获取下一个终点
			int nextLinkPid = sNodeAndLinkPid.get(nexteNode1);
			int nextKind = pidKind.get(nextLinkPid);
			if(nextKind == 7){
				length+=pidLength.get(nextLinkPid);
			}
			map.put("sNodePid", -1);
			map.put("eNodePid", nexteNode1);
			map.put("length", length);
			
		}
		return map;
	}
	public static void main(String[] args) {
		List list = new ArrayList<>();
	}
}
