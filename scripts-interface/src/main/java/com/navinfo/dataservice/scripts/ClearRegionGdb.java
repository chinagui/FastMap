package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import net.sf.json.JSONObject;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.datahub.service.DatahubApiImpl;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.navicommons.database.sql.SqlExec;
import com.navinfo.navicommons.exception.ServiceException;

public class ClearRegionGdb {
	public static void main(String[] args) throws ServiceException {

		try {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					new String[] { "dubbo-consumer-4scripts.xml" });
			context.start();
			new ApplicationContextUtil().setApplicationContext(context);

			List<Region> list = RegionService.getInstance().list();
			for (Region region : list) {
				
				clearRegionGdb(region.getDailyDbId(), 0);
				
				clearRegionGdb(region.getMonthlyDbId(), 1);
				
			}
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
	
	private static void clearRegionGdb(int dbId, int type) throws Exception{

		DatahubApiImpl datahub = new DatahubApiImpl();
		
		DbInfo dbinfo = datahub.getDbById(dbId);
		
		DbConnectConfig connConfig = DbConnectConfig
				.createConnectConfig(dbinfo.getConnectParam());
		
		DataSource datasource = MultiDataSourceFactory.getInstance()
		.getDataSource(connConfig);
		
		Connection conn = datasource.getConnection();
		
		conn.setAutoCommit(false);
		
		String sqlFile;
		
		if(type == 0){ //日库
			sqlFile = "/com/navinfo/dataservice/scripts/resources/clear_region_gdb_day.sql";
		}
		else{ 	//月库
			sqlFile = "/com/navinfo/dataservice/scripts/resources/clear_region_gdb_month.sql";
		}
		
		SqlExec sqlExec = new SqlExec(conn);
		
		sqlExec.executeIgnoreError(sqlFile);
		
		conn.commit();
		
		conn.close();
	
	}
}
