/**
 * 
 */
package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.fileConvert.LoadAndCreateTab;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
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
	
	
	@Test
	public void testMetadataDonwnload() throws Exception {
		System.out.println("start"); 
		JobScriptsInterface.initContext();//F:\tabfile
		String filePathString="f:/tabfile/bj.TAB";
		//String filePathString = String.valueOf(args[0]);
		Connection conn=DBConnector.getInstance().getManConnection();
		List<String> columnNameList=new ArrayList<String>();
		columnNameList.add("ID");
		List<Map<String, Object>> dataList = LoadAndCreateTab.readTab(filePathString, columnNameList);
		for(int i=0;i<dataList.size();i++){
			Map<String, Object> tmp=dataList.get(i);
			tmp.put("ID", i+1);
		}
		//ImportOracle.writeOracle(conn, "SUBTASK_REFER", dataList);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end"); 
		System.exit(0);
	}
}
