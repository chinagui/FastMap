package com.navinfo.dataservice.expcore;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.UnifiedDb;
import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.navicommons.utils.StringUtils;

/** 
 * @ClassName: ScriptsInterface 
 * @author Xiao Xiaowen 
 * @date 2015-12-29 下午4:35:05 
 * @Description: TODO
 */
public class ScriptsInterface {
	protected BasicDataSource manDataSource;
	public ScriptsInterface(BasicDataSource manDataSource){
		this.manDataSource=manDataSource;
	}
	public void exportData(String srcDbName,String srcBizType,String targetDbName,String target,int extendCount)throws ExportException{
		ExportConfig expConfig = new ExportConfig();
		
	}
	
	public static void main(String[] args){
		try{
			if(args.length == 0){
				System.out.println("ERROR:need prjId,extendCount");
			}
			Map<String,String> map = new HashMap<String,String>();
			for(int i=0; i<args.length;i++){
			        map.put(args[i], args[i+1]);
		    }
			String itype = map.get("itype");
			if("gdb_create".equals(itype)){
				String name = map.get("name");
				String type = map.get("type");
				String descp = map.get("descp");
				if(StringUtils.isEmpty(type)){
					System.out.println("ERROR:need arg -type");
				}
				DbManager man = new DbManager();
				UnifiedDb db = null;
				if(StringUtils.isEmpty(name)){
					db = man.createDb(type, descp);
				}else{
					db = man.createDb(name,type, descp);
				}
				System.out.print(db.getConnectString());
			}else if("export_data".equals(itype)){
				
			}else{
				System.out.println("ERROR:need arg -itype");
			}
			ScriptsInterface face = new ScriptsInterface(MultiDataSourceFactory.getInstance().getManDataSource());
			//face.initPrjDb(,1);
			System.out.println("Over.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
