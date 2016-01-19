package com.navinfo.dataservice.scripts;

import java.sql.Connection;

import com.navinfo.dataservice.FosEngine.export.GdbDataExporter;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.man.project.ProjectSelector;

public class GdbExportScriptsInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Connection conn = null;
		
		try {

			int projectId = Integer.valueOf(args[0]);
			
			String path = args[1];
			
			ProjectSelector prjselector = new ProjectSelector(
					MultiDataSourceFactory.getInstance().getManDataSource()
							.getConnection());
			
			int dbId = prjselector.getDbId(projectId);
			
			DbManager dbMan = new DbManager();
			
			OracleSchema db = (OracleSchema)dbMan.getDbById(dbId);
			
			conn = db.getPoolDataSource().getConnection();
			
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
