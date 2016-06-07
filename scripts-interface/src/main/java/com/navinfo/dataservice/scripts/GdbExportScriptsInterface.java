package com.navinfo.dataservice.scripts;

import java.sql.Connection;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.engine.edit.export.GdbDataExporter;
import com.navinfo.dataservice.engine.man.project.ProjectSelector;

public class GdbExportScriptsInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Connection conn = null;
		
		try {

			int projectId = Integer.valueOf(args[0]);
			
			String path = args[1];
			
			ProjectSelector prjselector = new ProjectSelector();
			
			int dbId = prjselector.getDbId(projectId);
			
			DbInfo db = DbService.getInstance().getDbById(dbId);
			
			conn = MultiDataSourceFactory.getInstance().getDataSource(db.getConnectParam()).getConnection();
			
			GdbDataExporter.exportBaseData2Sqlite(conn, path);
			
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
			
		}finally{
			if(conn != null){
				try{
					conn.close();
				}
				catch(Exception e){
					
				}
			}
		}

	}

}