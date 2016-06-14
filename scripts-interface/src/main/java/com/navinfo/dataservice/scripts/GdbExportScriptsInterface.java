package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import net.sf.json.JSONObject;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.datahub.service.DatahubApiImpl;
import com.navinfo.dataservice.engine.edit.export.GdbDataExporter;
import com.navinfo.dataservice.engine.man.region.Region;
import com.navinfo.dataservice.engine.man.region.RegionService;

public class GdbExportScriptsInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			
			String path = args[0];
			
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
	                new String[] { "dubbo-consumer-4scripts.xml" }); 
			context.start();
			new ApplicationContextUtil().setApplicationContext(context);
			
			RegionService s = new RegionService();
			
			List<Region> list = s.list(new JSONObject());
			
			for(Region region : list){
				
				DatahubApiImpl datahub = new DatahubApiImpl();
				
				DbInfo dbinfo = datahub.getDbById(region.getMonthlyDbId());
				
				DbConnectConfig connConfig = MultiDataSourceFactory
						.createConnectConfig(dbinfo.getConnectParam());
				
				DataSource datasource = MultiDataSourceFactory.getInstance()
				.getDataSource(connConfig);
				
				Connection conn = datasource.getConnection();
				
				if(!path.endsWith("/")){
					path += "/";
				}
				
				path += region.getRegionId();
				
				GdbDataExporter.exportBaseData2Sqlite(conn, path);
			}

			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
			
		}
	}

}