package com.navinfo.dataservice.diff.job;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.dataservice.diff.exception.InitException;
import com.navinfo.navicommons.database.sql.PackageExec;

/** 
 * @ClassName: DiffTool
 * @author xiaoxiaowen4127
 * @date 2017年8月31日
 * @Description: DiffTool.java
 */
public abstract class DiffTool {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected DiffJobRequest req;
	protected OracleSchema leftSchema;
	protected OracleSchema rightSchema;
	protected GlmTable objMainTable;
	protected Set<GlmTable> diffTables;
	protected Set<GlmTable> logTables;
	public DiffTool(DiffJobRequest req){
		this.req=req;
	}
	public String init()throws Exception{
		//shcema
		DatahubApi datahub=(DatahubApi)ApplicationContextUtil.getBean("datahubApi");
		DbInfo leftDb = datahub.getDbById(req.getLeftDbId());
		leftSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(leftDb.getConnectParam()));
		DbInfo rightDb = datahub.getDbById(req.getRightDbId());
		rightSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(rightDb.getConnectParam()));
		//安装EQUALS
		installPcks(leftSchema);
		//datahub创建时统一都赋上了跨用户访问权限
		// cross user...
		//diff tables
		Glm glm = GlmCache.getInstance().getGlm(req.getGdbVersion());
		objMainTable = glm.getGlmTable(req.getObjName());
		
		diffTables = new HashSet<GlmTable>();
		
		List<String> specific = req.getSpecificTables();
		List<String> excluded = req.getExcludedTables();
		if(specific!=null&&specific.size()>0){
			for(String name:specific){
				diffTables.add(glm.getEditTables().get(name));
			}
		}else{
			if(excluded!=null&&excluded.size()>0){
				for(GlmTable table:glm.getEditTables().values()){
					if(!excluded.contains(table.getName())){
						diffTables.add(table);
					}
				}
			}else{
				diffTables.addAll(glm.getEditTables().values());
			}
		}
		log.debug("需要差分的表的个数为：" + diffTables.size());
		logTables = Collections.synchronizedSet(new HashSet<GlmTable>());
		return "差分初始化完成";
	}
	protected void installPcks(OracleSchema schema)throws InitException{
		Connection conn = null;
		try{
			conn = schema.getPoolDataSource().getConnection();
			//安装EQUALS
			String afle = "/com/navinfo/dataservice/diff/resources/equals.pck";
			PackageExec packageExec = new PackageExec(conn);
			packageExec.execute(afle);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			try{
				DbUtils.rollback(conn);
			}catch(Exception err){
				log.error(err);
			}
			throw new InitException("安装差分需要的包是发生错误:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public abstract String diff()throws DiffException;
	/**
	 * 返回本次差分履历的act_id
	 * @param userId
	 * @param actName
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 */
	public abstract String writeLog(long userId,String actName, long subtaskId)throws Exception;
	
	public void releaseResources(){
		
	}
}
