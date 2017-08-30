package com.navinfo.dataservice.bizcommons.upload.stat;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.DaoOperatorException;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.Collection;

/**
 * 
 * @ClassName: UploadRegionInfoOperator
 * @author xiaoxiaowen4127
 * @date 2017年8月24日
 * @Description: UploadRegionInfoOperator.java
 */
public class UploadRegionInfoOperator {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;
	private QueryRunner run;
	private static String insertSql = "INSERT INTO UPLOAD_CROSS_REGION_INFO(USER_ID,FROM_SUBTASK_ID,UPLOAD_TIME,UPLOAD_TYPE,OUT_REGION_ID,OUT_GRID_ID,OUT_GRID_NUMBER) VALUES (?,?,sysdate,?,?,?,?)";

	public UploadRegionInfoOperator(Connection conn) {
		this.conn = conn;
		run = new QueryRunner();
	}

	public void save(UploadCrossRegionInfoDao dao) throws DaoOperatorException {
		try {
			Object[] cols = dao.attrArray();
			replaceLongString2Clob(cols);
			run.update(conn, insertSql, cols);
		} catch (Exception e) {
			log.error("UploadCrossRegionInfoDao保存出错:" + e.getMessage(), e);
			throw new DaoOperatorException("UploadCrossRegionInfoDao保存出错:" + e.getMessage(),
					e);
		}
	}
	
	private void replaceLongString2Clob(Object[] cols)throws Exception{
		if(cols==null||cols.length==0){
			return;
		}
		for(int i=0;i<cols.length;i++){
			Object o = cols[i];
			if(o!=null&&o instanceof String&&((String)o).length()>1000){
				cols[i] = ConnectionUtil.createClob(conn, (String)o);
			}
		}
	}

	public void save(Collection<UploadCrossRegionInfoDao> daos) throws DaoOperatorException {
		if (daos == null || daos.size() == 0) {
			return;
		}
		try {
			Object[][] daosCols = new Object[daos.size()][];
			int i = 0;
			for (UploadCrossRegionInfoDao dao : daos) {
				Object[] cols = dao.attrArray();
				replaceLongString2Clob(cols);
				daosCols[i] = cols;
				i++;
			}
			run.batch(conn, insertSql, daosCols);
		} catch (Exception e) {
			log.error("UploadCrossRegionInfoDao批量保存出错:" + e.getMessage(), e);
			throw new DaoOperatorException(
					"UploadCrossRegionInfoDao批量保存出错:" + e.getMessage(), e);
		}
	}


}
