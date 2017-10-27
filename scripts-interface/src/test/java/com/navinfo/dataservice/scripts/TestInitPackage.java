package com.navinfo.dataservice.scripts;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.expcore.snapshot.GdbDataExporterSp9;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: Test 
* @author Zhang Xiaolong
* @date 2016年10月17日 上午9:55:34 
* @Description: TODO
*/
public class TestInitPackage extends ClassPathXmlAppContextInit{
	@Before
	public void before(){
//		initContext(new String[]{"dubbo-consumer-datahub-test.xml"});
		initContext(new String[]{"dubbo-test.xml"});//,"dubbo-scripts.xml"
	}
	
	//@Test
	public void testInit() throws Exception {
		Connection conn = DBConnector.getInstance().getConnectionById(19);
		try {
			
			PackageExec packageExec = new PackageExec(conn);
			String pckFile = "/com/navinfo/dataservice/scripts/resources/prj_utils.pck";
			packageExec.execute(pckFile);
		}
		catch (Exception e) {
			e.printStackTrace();
			DbUtils.rollbackAndClose(conn);
		}finally {
			DBUtils.closeConnection(conn);
		}
	}
	
	
//	@Test
	public void testMetadataDonwnload() throws Exception {
		System.out.println("start"); 
		JobScriptsInterface.initContext();//F:\tabfile
		String filePathString="f:/tabfile/bj.TAB";
		//String filePathString = String.valueOf(args[0]);
		Connection conn=DBConnector.getInstance().getManConnection();
		List<String> columnNameList=new ArrayList<String>();
		columnNameList.add("ID");
		/*List<Map<String, Object>> dataList = LoadAndCreateTab.readTab(filePathString, columnNameList);
		for(int i=0;i<dataList.size();i++){
			Map<String, Object> tmp=dataList.get(i);
			tmp.put("ID", i+1);
		}*/
		//ImportOracle.writeOracle(conn, "SUBTASK_REFER", dataList);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end"); 
		System.exit(0);
	}
	
//	@Test
	public void testgdbDonwnload() throws Exception{
		JobScriptsInterface.initContext();

		String path="f:/gdb/";
		String type="day";
		int regionId = 1;
		
		GdbExportScriptsInterface gdbInter = new GdbExportScriptsInterface();
		
		Map<Integer, Map<Integer, Set<Integer>>> map = gdbInter.getProvinceMeshList(type,regionId);

		DatahubApi datahub = (DatahubApi) ApplicationContextUtil
				.getBean("datahubApi");

		for (Map.Entry<Integer, Map<Integer, Set<Integer>>> entry : map
				.entrySet()) {

			int dbId = entry.getKey();
			
			System.out.println("export dbId : " + dbId);

			Map<Integer, Set<Integer>> data = entry.getValue();

			DbInfo dbinfo = datahub.getDbById(dbId);

			DbConnectConfig connConfig = DbConnectConfig
					.createConnectConfig(dbinfo.getConnectParam());

			DataSource datasource = MultiDataSourceFactory.getInstance()
					.getDataSource(connConfig);

//			Connection conn = datasource.getConnection();
			
			Connection conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();
					//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();


			for (Map.Entry<Integer, Set<Integer>> en : data.entrySet()) {

				int admincode = en.getKey();
//				if(admincode!=420000){
//					continue;
//				}
				System.out.println("export admincode "+admincode+" ...");
				
				Set<Integer> meshes = en.getValue();
				
				/*Set<Integer> meshes =new  HashSet<Integer>();
					meshes.add(625714);
					meshes.add(625713);
					meshes.add(625716);
					meshes.add(625860);*/
					
				String output = path + admincode / 10000;

				String filename = GdbDataExporterSp9.run(conn, output, meshes);
				
				System.out.println("export admincode "+admincode+" success: "+filename);
			}
		}

		System.out.println("Over.");
		System.exit(0);
	}
	
//	@Test
	public void testgdbDonwnloadSp9() throws Exception{
		JobScriptsInterface.initContext();

		String path="f:/gdb/sp9/";
		String type="month";
		
		GdbExportScriptsInterface gdbInter = new GdbExportScriptsInterface();
		
		Map<Integer, Map<Integer, Set<Integer>>> map = gdbInter.getProvinceMeshList(type,0);

		DatahubApi datahub = (DatahubApi) ApplicationContextUtil
				.getBean("datahubApi");

		for (Map.Entry<Integer, Map<Integer, Set<Integer>>> entry : map
				.entrySet()) {

			int dbId = entry.getKey();
			
			System.out.println("export dbId : " + dbId);

			Map<Integer, Set<Integer>> data = entry.getValue();

			DbInfo dbinfo = datahub.getDbById(dbId);

			DbConnectConfig connConfig = DbConnectConfig
					.createConnectConfig(dbinfo.getConnectParam());

			DataSource datasource = MultiDataSourceFactory.getInstance()
					.getDataSource(connConfig);

			Connection conn = datasource.getConnection();

			for (Map.Entry<Integer, Set<Integer>> en : data.entrySet()) {

				int admincode = en.getKey();
				System.out.println("admincode : "+admincode);
				//现在只跑 11 //12 13  &&  admincode != 120000 && admincode != 130000
				if(admincode != 110000){
					continue;
				}
				System.out.println("export admincode "+admincode+" ...");
				
				Set<Integer> meshes = en.getValue();
				
				/*Set<Integer> meshes =new  HashSet<Integer>();
					meshes.add(625714);
					meshes.add(625713);
					meshes.add(625716);
					meshes.add(625860);*/
					
				String output = path + admincode / 10000;

				String filename = GdbDataExporterSp9.run(conn, output, meshes);
				
				System.out.println("export admincode "+admincode+" success: "+filename);
			}
		}

		System.out.println("Over.");
		System.exit(0);
	}
	
//	@Test
	public void testImportSourceExcel() throws Exception{
		JobScriptsInterface.initContext();

//		String filePath="f:/source3.xlsx";
//		String filePath="f:/source333.xls";
//		String filePath="f:/source.xlsx";
		String filePath="f:/init_sourcebeijing.xls";
		
		ImportIxDealershipSourceExcle importSource = new ImportIxDealershipSourceExcle();
		
		importSource.imp(filePath);

		System.out.println("Over.");
		System.exit(0);
	}
	
//	@Test
	public void testImportPoiToRegionDb() throws Exception{
		JobScriptsInterface.initContext();
		
		JSONObject request = new JSONObject();
		request.put("sourceDbId", 382);
		request.put("targetDbId", 383);
		ImportPoiToRegionDb.execute(request);

		System.out.println("Over.");
		System.exit(0);
	}
	

	public static void main(String[] args) throws ParseException {
		Set<String> grids = new HashSet<String>();
		grids.add("48580601");
		grids.add("48580620");
		grids.add("48580602");
		grids.add("47587523");
		grids.add("48580621");
		grids.add("48580612");
		grids.add("48580622");
		grids.add("48580611");
		grids.add("47587631");
		grids.add("47587630");
		grids.add("47587621");
		grids.add("47587632");
		grids.add("47587620");
	
		System.out.println(grids.toArray().toString());
		JSONArray jsonarray = JSONArray.fromObject(grids); 
		
		System.out.println(jsonarray);
		
		
		/*Set<String> grids1 = new HashSet<String>();
		grids1.add("47587520");
		grids1.add("47586531");
		grids1.add("47586530");
		grids1.add("47586533");
		grids1.add("47586532");
		grids1.add("47585603");
		grids1.add("47585602");
		grids1.add("47585410");
		grids1.add("47585411");
		grids1.add("47586603");
		grids1.add("47586602");
		grids1.add("47586601");
		grids1.add("47587413");
		grids1.add("47586433");
		grids1.add("47586431");
		grids1.add("47586432");
		grids1.add("47585730");
		grids1.add("47586403");
		grids1.add("47586402");
		grids1.add("47586401");
		grids1.add("47586400");
		grids1.add("47584632");
		grids1.add("47584633");
		grids1.add("47584730");
		grids1.add("47586700");
		grids1.add("47585613");
		grids1.add("47585612");
		grids1.add("47586631");
		grids1.add("47586632");
		grids1.add("47586630");
		grids1.add("47587402");
		grids1.add("47587403");
		grids1.add("47585720");
		grids1.add("47585433");
		grids1.add("47586410");
		grids1.add("47585432");
		grids1.add("47584720");
		grids1.add("47585431");
		grids1.add("47586412");
		grids1.add("47585430");
		grids1.add("47586411");
		grids1.add("47586413");
		grids1.add("47584623");
		grids1.add("47587600");
		grids1.add("47587602");
		grids1.add("47587601");
		grids1.add("47585632");
		grids1.add("47585633");
		grids1.add("47585710");
		grids1.add("47585631");
		grids1.add("47587501");
		grids1.add("47587500");
		grids1.add("47586622");
		grids1.add("47587503");
		grids1.add("47586621");
		grids1.add("47587502");
		grids1.add("47586620");
		grids1.add("47585422");
		grids1.add("47586421");
		grids1.add("47585421");
		grids1.add("47586420");
		grids1.add("47585420");
		grids1.add("47586423");
		grids1.add("47586422");
		grids1.add("47586523"); 
		grids1.add("47586520");
		grids1.add("47586613");
		grids1.add("47587611");
		grids1.add("47587610");
		grids1.add("47585623");
		grids1.add("47585700");
		grids1.add("47587510");
		grids1.add("47585621");
		grids1.add("47585622");
		grids1.add("47586610");
		grids1.add("47587513");
		grids1.add("47587512");
		grids1.add("47586612"); 
		grids1.add("47587511");
		grids1.add("47586611");*/
	
		
		/*String geo = GridUtils.grids2Wkt(grids1);
		
		Geometry blockGeo = CompGridUtil.grids2Jts(grids1);
		System.out.println(blockGeo);*/
	}
	
//	@Test
	public void testExportQualityPoiReport() throws Exception{
		JobScriptsInterface.initContext();
		
//		ExportQualityPoiReport.execute("550","D://");

		System.out.println("Over.");
		System.exit(0);
	}
	
	
//	@Test
	public void testExportColumnQcQualityRate() throws Exception{
		JobScriptsInterface.initContext();
		
		ExportColumnQcQualityRate.execute("20170514","20170715");

		System.out.println("Over.");
		System.exit(0);
	}
	
//	@Test
	public void testPoiToTab() throws Exception{
		JobScriptsInterface.initContext();
		//HUB_ThnklPMtnF  192.168.4.62
		String json = "{'db_conf':{'db_ip':'192.168.4.62','db_port':'1521','service_name':'orcl','db_username':'HUB_ThnklPMtnF','db_password':'HUB_ThnklPMtnF'},'data':[{'taskId':109,'subTaskId':'546'},{'taskId':123,'subTaskId':'546'},{'taskId':77}]}";
		JSONObject request1 = JSONObject.fromObject(json);
//		JSONObject request = new JSONObject();
//		request.put("sourceDbId", 382);
//		request.put("targetDbId", 383);
		System.out.println(request1.toString());
		JSONObject response = InitDataPoi2Tab.execute(request1);
		
		System.out.println("response: "+response.toString());
		System.out.println("Over.");
		System.exit(0);
	}
	
//	@Test
	public void testInsertFmPoiCutoutStat() throws Exception{
		JobScriptsInterface.initContext();
		//HUB_ThnklPMtnF  192.168.4.62
		String json = "{'db_conf':{'db_ip':'192.168.4.62','db_port':'1521','service_name':'orcl','db_username':'HUB_ThnklPMtnF','db_password':'HUB_ThnklPMtnF'},'data':[{'taskId':109,'subTaskId':'546'},{'taskId':123,'subTaskId':'546'},{'taskId':77}]}";
		JSONObject request = JSONObject.fromObject(json);
		JSONObject db_conf = request.getJSONObject("db_conf");
		String db_ip = db_conf.getString("db_ip");
		String db_port = db_conf.getString("db_port");
		String service_name = db_conf.getString("service_name");
		String db_username = db_conf.getString("db_username");
		String db_password = db_conf.getString("db_password");
		
		DataSource dataSource = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
				"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@"+db_ip+":"+db_port+"/"+service_name+"", db_username, db_password);
		Connection conn = dataSource.getConnection();
		
		//insertFmPoiCutoutStat(conn, 123);
		
		
		//12.1 在中间库向 中间库fm_poi_cutout 插入 pid,taskId,subtaskId,poi_num,
//		insertFmPoiCutout(conn);
		
		//12.2 根据 taskid 及 subtaskId 去man 库查询相关数据
//		insertFmPoiCutoutFromMan(conn,123,"546");
		
		insertIxPoiParentTable(conn);
		
		System.out.println("Over.");
		System.exit(0);
	}
	
	//**************************
	
	private static void insertIxPoiParentTable(Connection conn) throws SQLException {
		System.out.println("开始新增表:ix_poi_parent");
		StringBuilder createAndInsertIxPoiParentTableSql = new StringBuilder();
		createAndInsertIxPoiParentTableSql.append( " insert into  ix_poi_parent  select distinct p.*  from ix_poi_parent@DBLINK_TAB p,ix_poi_children s ,ix_poi_parent i    "
				+ "  where  p.GROUP_ID = s.GROUP_ID  and p.group_id != i.group_id ");
		
		System.out.println("createAndInsertIxPoiParentTableSql.toString(): "+createAndInsertIxPoiParentTableSql.toString());
		
		try {
			QueryRunner r = new QueryRunner();
			
			r.update(conn, createAndInsertIxPoiParentTableSql.toString());
			conn.commit();
			System.out.println("新增表:ix_poi_parent 完毕.");
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
		}
		
	}
	
	//**************************
	private static void insertFmPoiCutout(Connection conn) throws Exception {
		try {
			QueryRunner r = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("declare                                                                                              ");
			sb.append("    num   number;                                                                                    ");
			sb.append("begin                                                                                                ");
			sb.append("    select count(1) into num from user_tables where table_name = upper('fm_poi_cutout') ;    ");
			sb.append("    if num = 0 then                                                                                  ");
			sb.append("        execute immediate 'create table fm_poi_cutout (pid  NUMBER(10) not null,poi_num  VARCHAR2(36), "
					+ "task_id  NUMBER(10) default 0 not null,task_name  VARCHAR2(200) ,gdbversion  VARCHAR2(20) ,"
					+ "program_id NUMBER(10),program_name  VARCHAR2(200), subtask_id  NUMBER(10) default 0 not null,"
					+ "subtask_name VARCHAR2(200),region_id   NUMBER(10),province  VARCHAR2(20) ,inforcode  NUMBER(10),"
					+ "fetchdate  VARCHAR2(50))' ;                                       ");
			sb.append("    end if;                                                                                          ");
			sb.append("end;                                                                                                 ");
			r.execute(conn, sb.toString());
			
			String currentDate = com.navinfo.dataservice.commons.util.StringUtils.getCurrentTime();	
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			String insertFmPoiCutoutSql = " insert into  fm_poi_cutout(gdbversion,pid,poi_num,task_id,subtask_id,fetchdate) "
					+ "   select '"+gdbVersion+"' gdbversion,p.pid,i.poi_num,p.task_id,p.subtask_id,'"+currentDate+"' fetchdate from poi_task_tab p,ix_poi i where p.pid = i.pid  ";
			System.out.println("insertFmPoiCutoutSql: "+insertFmPoiCutoutSql.toString());
			r.execute(conn, insertFmPoiCutoutSql);	
			conn.commit();
			
			System.out.println(" 完毕.");
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	
private static void insertFmPoiCutoutFromMan(Connection conn, int taskId, String subts) throws SQLException {
		
		Connection connMan = null;
		PreparedStatement perstmt = null;
		try{
			String updateFmPoiCutoutSql = "update  fm_poi_cutout set subtask_name = ?,task_name = ?,program_id = ?,program_name = ?,inforcode = ?,province = ?  where subtask_id = ? ";
			//获取man库连接
			connMan = DBConnector.getInstance().getManConnection();
			Clob clob = connMan.createClob();
			clob.setString(1, subts);
			String selectSql = "  select s.subtask_id,s.subtask_name ,t.task_id,t.name task_name,p.program_id,p.name program_name,p.infor_id inforcode ,c.province_name province  " 
					+" from ( select s.subtask_id,s.name subtask_name ,"+taskId+" task_id  from subtask s  "
					+ "where s.task_id = "+taskId+" or s.subtask_id in (select to_number(column_value) from table(clob_to_table(?)) )) s ,"
					+ "task t,program p,city c "
					+" where s.task_id =t.task_id(+)  and t.program_id = p.program_id(+) "
					+"  and p.city_id = c.city_id(+)    ";
			System.out.println("querysubtaskIdNumsByTaskId: "+selectSql);
			ResultSetHandler< List<Map<String, Object>>> rs = new ResultSetHandler< List<Map<String, Object>>>() {
				@Override
				public  List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("subtask_id", rs.getLong("subtask_id"));
						map.put("subtask_name", rs.getString("subtask_name"));
						map.put("task_id", rs.getLong("task_id"));
						map.put("task_name", rs.getString("task_name"));
						map.put("program_id",  rs.getLong("program_id"));
						map.put("program_name", rs.getString("program_name"));
						map.put("inforcode", rs.getLong("inforcode"));
						map.put("province", rs.getString("province"));
						
						results.add(map);
					}
					return results;
				}
			};
			QueryRunner run = new QueryRunner();
			List<Map<String, Object>> result = run.query(connMan,selectSql,clob, rs);
			
			if(result != null && result.size() > 0){
				if(perstmt==null){
					perstmt = conn.prepareStatement(updateFmPoiCutoutSql);
				}
				for(Map<String, Object> fmPoiCutoutMap : result ){
					//
					long subtask_id = (long) fmPoiCutoutMap.get("subtask_id");
					String subtask_name = (String) fmPoiCutoutMap.get("subtask_name");
//					long task_id = (long) fmPoiCutoutMap.get("task_id");
					String task_name = (String) fmPoiCutoutMap.get("task_name");
					long program_id = (long) fmPoiCutoutMap.get("program_id");
					String program_name = (String) fmPoiCutoutMap.get("program_name");
					long inforcode = (long) fmPoiCutoutMap.get("inforcode");
					String province = (String) fmPoiCutoutMap.get("province");
					
					
					perstmt.setString(1,subtask_name);
					perstmt.setString(2,task_name);
					perstmt.setLong(3, program_id);
					perstmt.setString(4,program_name);
					perstmt.setLong(5, inforcode);
					perstmt.setString(6,province);
					perstmt.setLong(7, subtask_id);
					perstmt.addBatch();
					
				}
				if(perstmt!=null){
					perstmt.executeBatch();
					conn.commit();
				}
			}
		}catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
			System.out.println(e.getMessage());
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
			DbUtils.close(perstmt);
		}
		
	}
	
	
	//**************************
	private static void insertFmPoiCutoutStat(Connection conn, int taskId) throws Exception {
		PreparedStatement perstmt = null;

		try {
			QueryRunner r = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("declare                                                                                              ");
			sb.append("    num   number;                                                                                    ");
			sb.append("begin                                                                                                ");
			sb.append("    select count(1) into num from user_tables where table_name = upper('fm_poi_cutout_stat') ;    ");
			sb.append("    if num = 0 then                                                                                  ");
			sb.append("        execute immediate 'create table fm_poi_cutout_stat (id number(10) , task_id    NUMBER(10) default 0 not null,task_poi_num number(6) default 0 not null,subtask_id_num  CLOB,fetchdate  VARCHAR2(50))' ;                                       ");
			sb.append("        execute immediate 'create sequence fm_poi_cutout_stat_SEQ  minvalue 1 maxvalue 9999999999 start with 1  increment by 1 cache 20 ' ;                                       ");
			sb.append("    end if;                                                                                          ");
			sb.append("end;                                                                                                 ");
			r.execute(conn, sb.toString());
			
			String currentDate = com.navinfo.dataservice.commons.util.StringUtils.getCurrentTime();	
			String insertFmPoiCutoutStat = " insert into  fm_poi_cutout_stat(id,task_id,task_poi_num,subtask_id_num,fetchdate) VALUES(fm_poi_cutout_stat_SEQ.NEXTVAL,?,?,?,'"+currentDate+"')  ";
			System.out.println("insertFmPoiCutoutStat.toString(): "+insertFmPoiCutoutStat.toString());
			List<Map<String, Object>>  subtaskIdNums = querysubtaskIdNumsByTaskId(conn,taskId);
			int task_poi_num =queryTotalByTaskId(conn, taskId);
				if(perstmt==null){
					perstmt = conn.prepareStatement(insertFmPoiCutoutStat);
				}
				
				perstmt.setInt(1, taskId);
				perstmt.setInt(2, task_poi_num);
				JSONArray jsonArray = JSONArray.fromObject(subtaskIdNums);
//				JSONObject jsonObject = JSONObject.fromObject(subtaskIdNums);
				Clob clob = conn.createClob();
					clob.setString(1, jsonArray.toString());
				perstmt.setClob(3, clob);
				
				if(perstmt!=null){
					perstmt.execute();
					conn.commit();
				}
			System.out.println(" 完毕.");
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	private static List<Map<String, Object>> querysubtaskIdNumsByTaskId(Connection conn, int taskId) throws Exception {
		try{
			String selectSql = " select p.subtask_id,count(1) NUM  from POI_TASK_TAB p WHERE P.TASK_ID = "+taskId+"  group by p.subtask_id  ";
			System.out.println("querysubtaskIdNumsByTaskId: "+selectSql);
			ResultSetHandler< List<Map<String, Object>>> rs = new ResultSetHandler< List<Map<String, Object>>>() {
				@Override
				public  List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("subtask_id", rs.getLong("subtask_id"));
						map.put("num",  rs.getInt("num"));
						results.add(map);
					}
					return results;
				}
			};
			QueryRunner run = new QueryRunner();
			List<Map<String, Object>> result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			System.out.println(e.getMessage());
			throw new Exception("查询subtaskIdNums 失败，原因为:"+e.getMessage(),e);
		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private static int queryTotalByTaskId(Connection conn, int taskId) throws Exception {
		try{
			String selectSql = " select count(1) total  from POI_TASK_TAB p WHERE P.TASK_ID = "+taskId+" ";
			System.out.println("queryTotalByTaskId: "+selectSql);
			ResultSetHandler< Integer> rs = new ResultSetHandler< Integer>() {
				@Override
				public  Integer handle(ResultSet rs) throws SQLException {
					int result = 0;
					while(rs.next()){
						result =  rs.getInt("total");
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			int result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			System.out.println(e.getMessage());
			throw new Exception("查询subtaskIdNums 失败，原因为:"+e.getMessage(),e);
		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
