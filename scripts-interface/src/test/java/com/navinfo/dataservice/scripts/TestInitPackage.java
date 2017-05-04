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
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.database.sql.PackageExec;

/** 
* @ClassName: Test 
* @author Zhang Xiaolong
* @date 2016年10月17日 上午9:55:34 
* @Description: TODO
*/
public class TestInitPackage extends ClassPathXmlAppContextInit{
	@Before
	public void before(){
		initContext(new String[]{"dubbo-app-scripts.xml","dubbo-scripts.xml"});
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
		String type="month";
		
		GdbExportScriptsInterface gdbInter = new GdbExportScriptsInterface();
		
		Map<Integer, Map<Integer, Set<Integer>>> map = gdbInter.getProvinceMeshList(type);

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
	
	
}
