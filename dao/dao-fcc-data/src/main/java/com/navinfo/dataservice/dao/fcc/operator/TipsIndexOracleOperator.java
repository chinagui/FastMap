package com.navinfo.dataservice.dao.fcc.operator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
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
	public List<TipsDao> searchDataByTileWithGap(String parameter) throws DaoOperatorException {
		return null;
	}

	@Override
	public void save(TipsDao ti) throws DaoOperatorException {
		try{
			run.update(conn, insertSql, ti.toColsObjectArr());
		}catch(Exception e){
			log.error("Tips Index保存出错:"+e.getMessage(),e);
			throw new DaoOperatorException("Tips Index保存出错:"+e.getMessage(),e);
		}
	}

	@Override
	public void save(Collection<TipsDao> tis) throws DaoOperatorException {
		if(tis==null||tis.size()==0){
			return;
		}
		try{
			Object[][] tisCols = new Object[tis.size()][];
			int i = 0;
			for(TipsDao ti:tis){
				tisCols[i]= ti.toColsObjectArr();
				i++;
			}
			run.batch(conn, insertSql, tisCols);
		}catch(Exception e){
			log.error("Tips Index批量保存出错:"+e.getMessage(),e);
			throw new DaoOperatorException("Tips Index批量保存出错:"+e.getMessage(),e);
		}
	}

	public List<JSONObject> query(String sql, Object... params) throws Exception{
		List<JSONObject> result = new ArrayList<>();
		try{
			QueryRunner run = new QueryRunner();

			ResultSetHandler<Map<String, TipsDao>> resultSetHandler = new ResultSetHandler<Map<String, TipsDao>>() {
				@Override
				public Map<String, TipsDao> handle(ResultSet rs) throws SQLException {
					Map<String, TipsDao> map = new HashMap<>();
					while (rs.next()) {
						TipsDao dao = new TipsDao();
						dao.setId(rs.getString("id"));
						dao.setStage(rs.getInt("stage"));
						dao.setT_date(DateUtils.dateToString(rs.getTimestamp("t_date")));
						dao.setT_operateDate(DateUtils.dateToString(rs.getTimestamp("t_operateDate")));
						dao.setT_lifecycle(rs.getInt("t_lifecycle"));
						dao.setHandler(rs.getInt("handler"));
						dao.setS_mTaskId(rs.getInt("s_mTaskId"));
						dao.setS_qTaskId(rs.getInt("s_qTaskId"));
						dao.setS_mSubTaskId(rs.getInt("s_mSubTaskId"));
						dao.setS_sourceType(rs.getString("s_sourceType"));
						dao.setT_dEditStatus(rs.getInt("t_dEditStatus"));
						dao.setT_mEditStatus(rs.getInt("t_mEditStatus"));
						dao.setTipdiff(rs.getString("tipdiff"));
						map.put(dao.getId(), dao);
					}
					return map;
				}
			};
			Map<String, TipsDao> map = run.query(conn, sql, resultSetHandler, params);
			String[] queryColNames = { "deep" ,"geometry"};
			Map<String, JSONObject> hbaseMap = HbaseTipsQuery.getHbaseTipsByRowkeys(map.keySet(),queryColNames);
			for(String rowkey : map.keySet()){
				if(!hbaseMap.containsKey(rowkey)){
					throw new Exception("tip not found in hbase,rowkey:"+rowkey);
				}

				JSONObject hbaesTips = hbaseMap.get(rowkey);
				TipsDao dao = map.get(rowkey);
				JSONObject deep = hbaesTips.getJSONObject("deep");
				dao.setDeep(deep.toString());
				JSONObject geometry = hbaesTips.getJSONObject("geometry");
				dao.setG_guide(geometry.getJSONObject("g_guide").toString());
				dao.setG_location(geometry.getJSONObject("g_location").toString());
				result.add(JSONObject.fromObject(dao));
			}
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new Exception("查询tips失败，原因为:" + e.getMessage(), e);
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
