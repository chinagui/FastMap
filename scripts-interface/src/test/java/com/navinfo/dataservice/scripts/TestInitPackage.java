package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.expcore.snapshot.GdbDataExporter;
import com.navinfo.dataservice.expcore.snapshot.GdbDataExporterSp9;
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
		initContext(new String[]{"dubbo-consumer-datahub-test.xml"});//,"dubbo-scripts.xml"
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
	
	@Test
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

				String filename = GdbDataExporter.run(conn, output, meshes);
				
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
	
	
	@Test
	public void testExportColumnQcQualityRate() throws Exception{
		JobScriptsInterface.initContext();
		
		ExportColumnQcQualityRate.execute("20170514","20170715");

		System.out.println("Over.");
		System.exit(0);
	}
}
