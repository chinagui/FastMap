package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

/**17秋线上POI（只新增）导入一体化日大区库（生成新增履历）
 * @ClassName: ImportPoiToRegionDb
 * @author jch
 * @date 下午5:18:18
 * @Description: ImportPoiToRegionDb.java
 */
public class ImportPoiToRegionDb {
	
	

	public static  void execute(JSONObject request) throws Exception{
		//1)调用createDb Job创建空库
		JSONObject createDbReq=new JSONObject();
		createDbReq.put("serverType", "ORACLE");
		createDbReq.put("bizType", "copVersion");
		JobInfo jobInfo = new JobInfo(0,UuidUtils.genUuid());
		jobInfo.setType("createDb");
		jobInfo.setRequest(createDbReq);
		jobInfo.setTaskId(0);
		AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
		job.run();
		JSONObject createDbResponse=job.getJobInfo().getResponse();
		
		//2)调用diff Job执行差分，生成履历
		JSONObject diffReq=new JSONObject();
		diffReq.put("leftDbId",request.getInt("sourceDbId"));
		diffReq.put("rightDbId",createDbResponse.getInt("outDbId"));
		JobInfo jobDiff = new JobInfo(0,UuidUtils.genUuid());
		jobDiff.setType("diff");
		jobDiff.setRequest(diffReq);
		jobDiff.setTaskId(0);
		AbstractJob jobDiffPoi = JobCreateStrategy.createAsMethod(jobDiff);
		jobDiffPoi.run();
		
		Connection sourceConn = DBConnector.getInstance().getConnectionById(request.getInt("sourceDbId"));
		//3）写入子任务到履历；
		//更新log_action表，默认为0,不用修改
        
		//4）生成poi_edit_status记录；
		genPoiEditRecord(sourceConn);
		
		//5）修改日落月标记，改为未落过，默认生成的履历就是未落过（log_operation->con_sta）=0,不用修改
//		updateDaytoMonthFlag(sourceConn);		
		
		//6）修改出品状态，改为不出品
		updateProductFlag(sourceConn);	
		
        //7复制数据
		copyData(request.getInt("targetDbId"),request.getString("dblink"));
		
	}
	public static void main(String[] args) throws Exception{
		initContext();
		System.out.println("args.length:"+args.length);
		if(args==null||args.length!=3){
			System.out.println("ERROR:need args:");
			return;
		}
		JSONObject request = new JSONObject();
		request.put("sourceDbId", args[0]);
		request.put("targetDbId", args[1]);
		request.put("dblink", args[2]);
		execute(request);
//		System.out.println(response);
		System.out.println("Over.");
		System.exit(0);
	}
	
	
	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	public static void genPoiEditRecord(Connection conn) throws Exception{
	
		StringBuffer sb = new StringBuffer();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String  sql="INSERT INTO poi_edit_status(pid,status) SELECT pid,3 FROM ix_poi";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate(sql);
			}catch (Exception e) {
				throw new SQLException("加载ix_poi失败：" + e.getMessage(), e);
			} finally {
				DbUtils.closeQuietly(pstmt);
			}
		
	}
	
public static void updateProductFlag(Connection conn) throws Exception{
		
		PreparedStatement pstmt = null;
		String  sql="update log_day_release set rel_poi_sta=1,rel_all_sta=1";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate(sql);
			}catch (Exception e) {
				throw new SQLException("加载log_day_release失败：" + e.getMessage(), e);
			} finally {
				DbUtils.closeQuietly(pstmt);
				DbUtils.commitAndCloseQuietly(conn);
			}
		
	}

public static void copyData(int targetDbId,String dblink) throws Exception{
	
	Glm glm = GlmCache.getInstance().getGlm(SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
	Connection conn = DBConnector.getInstance().getConnectionById(targetDbId);
	String sql="";
	String[] logAndStatusTable = {"poi_edit_status","Log_Action","Log_Day_Release","Log_Detail","Log_Detail_Grid","log_operation"};

	try {
	for(GlmTable table:glm.getEditTables().values()){
		System.out.println(table.getName());
		sql="INSERT INTO "+table.getName()+" SELECT * FROM "+table.getName()+"@" +dblink;
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate(sql);
		DbUtils.closeQuietly(pstmt);
	}
	
	for(int i=0;i<logAndStatusTable.length;i++){
		System.out.println(logAndStatusTable[i]);
		sql="INSERT INTO "+logAndStatusTable[i]+" SELECT * FROM "+logAndStatusTable[i]+"@" +dblink;
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate(sql);
		DbUtils.closeQuietly(pstmt);
	}
	
	}catch (Exception e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				throw new SQLException("加载table失败：" + e.getMessage(), e);
			} finally {
				DbUtils.commitAndCloseQuietly(conn);
			}	
	}
}



	