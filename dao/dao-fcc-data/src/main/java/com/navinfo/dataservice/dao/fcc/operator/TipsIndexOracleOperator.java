package com.navinfo.dataservice.dao.fcc.operator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.fcc.model.TipsIndexDao;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.DaoOperatorException;

/** 
 * @ClassName: TipsIndexOracleOperator
 * @author xiaoxiaowen4127
 * @date 2017年7月20日
 * @Description: TipsIndexOracleOperator.java
 */
public class TipsIndexOracleOperator implements TipsIndexOperator {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;
	private QueryRunner run;
	private static String insertSql = "INSERT INTO TIPS_INDEX(ID,STAGE,T_DATE,T_OPERATE_DATE,T_LIFECYCLE,T_COMMAND,HANDLER,S_SOURCE_TYPE,WKT,TIP_DIFF,S_Q_TASK_ID,S_M_TASK_ID,S_Q_SUBTASK_ID,S_M_SUBTASK_ID,WKT_LOCATION,T_TIP_STATUS,T_D_EDIT_STATUS,T_M_EDIT_STATUS,S_PROJECT,T_D_EDIT_METHOD,T_M_EDIT_METHOD,RELATE_LINKS,RELATE_NODES) VALUES (?,?,TO_TIMESTAMP(?,'yyyyMMddHH24miss'),TO_TIMESTAMP(?,'yyyyMMddHH24miss'),?,?,?,?,SDO_GEOMETRY(?,8307),?,?,?,?,?,SDO_GEOMETRY(?,8307),?,?,?,?,?,?,?,?)";
	public TipsIndexOracleOperator(Connection conn){
		this.conn=conn;
		run = new QueryRunner();
	}

	@Override
	public List<TipsIndexDao> searchDataByTileWithGap(String parameter) throws DaoOperatorException {
		return null;
	}

	@Override
	public void save(TipsIndexDao ti) throws DaoOperatorException {
		try{
			run.update(conn, insertSql, ti.toColsObjectArr());
		}catch(Exception e){
			log.error("Tips Index保存出错:"+e.getMessage(),e);
			throw new DaoOperatorException("Tips Index保存出错:"+e.getMessage(),e);
		}
	}

	@Override
	public void save(Collection<TipsIndexDao> tis) throws DaoOperatorException {
		if(tis==null||tis.size()==0){
			return;
		}
		try{
			Object[][] tisCols = new Object[tis.size()][];
			int i = 0;
			for(TipsIndexDao ti:tis){
				tisCols[i]= ti.toColsObjectArr();
				i++;
			}
			run.batch(conn, insertSql, tisCols);
		}catch(Exception e){
			log.error("Tips Index批量保存出错:"+e.getMessage(),e);
			throw new DaoOperatorException("Tips Index批量保存出错:"+e.getMessage(),e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
