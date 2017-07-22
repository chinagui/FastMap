package com.navinfo.dataservice.dao.fcc.operator;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.lang.ArrayUtils;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;

import net.sf.json.JSONObject;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
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
	private static String insertSql = "INSERT INTO TIPS_INDEX(ID,STAGE,T_DATE,T_OPERATEDATE,T_LIFECYCLE,HANDLER,S_SOURCETYPE,WKT,S_QTASKID,S_MTASKID,S_QSUBTASKID,S_MSUBTASKID,WKT_LOCATION,T_TIPSTATUS,T_DEDITSTATUS,T_MEDITSTATUS,S_PROJECT,T_DEDITMETH,T_M_EDIT_METH,RELATE_LINKS,RELATE_NODES) VALUES (?,?,TO_TIMESTAMP(?,'yyyyMMddHH24miss'),TO_TIMESTAMP(?,'yyyyMMddHH24miss'),?,?,?,SDO_GEOMETRY(?,8307),?,?,?,?,SDO_GEOMETRY(?,8307),?,?,?,?,?,?)";
	private static String insertSqlLinks = "INSERT INTO TIPS_LINKS(ID,LINK_ID) VALUES(?,?)";
	private static String insertSqlNodes = "INSERT INTO TIPS_NODES(ID,NODE_ID) VALUES(?,?)";

	private static String deleteSql = "DELETE FROM  TIPS_INDEX  WHERE ID IN (select to_number(column_value) from table(clob_to_table(?))) ";
	private static String deleteSqlLinks = "DELETE FROM  TIPS_LINKS WHERE ID IN  (select to_number(column_value) from table(clob_to_table(?)))";
	private static String deleteSqlNodes = "DELETE FROM TIPS_NODES  WHERE ID IN  (select to_number(column_value) from table(clob_to_table(?))) ";

	public TipsIndexOracleOperator(Connection conn) {
		this.conn = conn;
		run = new QueryRunner();
	}

	@Override
	public List<TipsDao> searchDataByTileWithGap(String parameter) throws DaoOperatorException {
		return null;
	}

	@Override
	public void save(TipsDao ti) throws DaoOperatorException {
		try {
			run.update(conn, insertSql, ti.toIndexMainArr());
			String[][] links = ti.toIndexLinkArr();
			if (links != null) {
				run.batch(conn, insertSqlLinks, links);
			}
			String[][] nodes = ti.toIndexNodeArr();
			if (nodes != null) {
				run.batch(conn, insertSqlNodes, nodes);
			}
		} catch (Exception e) {
			log.error("Tips Index保存出错:" + e.getMessage(), e);
			throw new DaoOperatorException("Tips Index保存出错:" + e.getMessage(), e);
		}
	}

	@Override
	public void save(Collection<TipsDao> tis) throws DaoOperatorException {
		if (tis == null || tis.size() == 0) {
			return;
		}
		try {
			Object[][] tisCols = new Object[tis.size()][];
			String[][] links = null;
			String[][] nodes = null;
			int i = 0;
			for (TipsDao ti : tis) {
				tisCols[i] = ti.toIndexMainArr();
				links = (String[][]) ArrayUtils.addAll(links, ti.toIndexLinkArr());
				nodes = (String[][]) ArrayUtils.addAll(nodes, ti.toIndexNodeArr());
				i++;
			}
			run.batch(conn, insertSql, tisCols);
			run.batch(conn, insertSqlLinks, links);
			run.batch(conn, insertSqlNodes, nodes);
		} catch (Exception e) {
			log.error("Tips Index批量保存出错:" + e.getMessage(), e);
			throw new DaoOperatorException("Tips Index批量保存出错:" + e.getMessage(), e);
		}
	}

	public long querCount(String sql, Object... params) throws Exception {
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Long> resultSetHandler = new ResultSetHandler<Long>() {
			@Override
			public Long handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					return rs.getLong(1);
				}
				return 0L;
			}
		};
		return run.query(conn, sql, resultSetHandler, params);
	}

	public List<TipsDao> query(String sql, Object... params) throws Exception {
		List<TipsDao> result = new ArrayList<>();
		try {
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
			String[] queryColNames = { "deep", "geometry" };
			Map<String, JSONObject> hbaseMap = HbaseTipsQuery.getHbaseTipsByRowkeys(map.keySet(), queryColNames);
			for (String rowkey : map.keySet()) {
				if (!hbaseMap.containsKey(rowkey)) {
					throw new Exception("tip not found in hbase,rowkey:" + rowkey);
				}

				JSONObject hbaesTips = hbaseMap.get(rowkey);
				TipsDao dao = map.get(rowkey);
				JSONObject deep = hbaesTips.getJSONObject("deep");
				dao.setDeep(deep.toString());
				JSONObject geometry = hbaesTips.getJSONObject("geometry");
				dao.setG_guide(geometry.getJSONObject("g_guide").toString());
				dao.setG_location(geometry.getJSONObject("g_location").toString());
				result.add(dao);
			}
		} catch (Exception e) {
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

	@Override
	public void delete(Collection<TipsDao> tis) throws Exception {
		if (tis == null || tis.size() == 0) {
			return;
		}
		String ids = org.apache.commons.lang.StringUtils.join(tis, ",");
		Clob pidClod = null;
		pidClod = ConnectionUtil.createClob(conn);
		pidClod.setString(1, ids);

		try {
			Object[][] tisCols = new Object[tis.size()][];
			String[][] links = null;
			String[][] nodes = null;
			int i = 0;
			for (TipsDao ti : tis) {
				tisCols[i] = ti.toIndexMainArr();
				links = (String[][]) ArrayUtils.addAll(links, ti.toIndexLinkArr());
				nodes = (String[][]) ArrayUtils.addAll(nodes, ti.toIndexNodeArr());
				i++;
			}

			run.update(conn, deleteSql, pidClod);
			run.update(conn, deleteSqlLinks, pidClod);
			run.update(conn, deleteSqlNodes, pidClod);

		} catch (Exception e) {
			log.error("Tips Index批量保存出错:" + e.getMessage(), e);
			throw new DaoOperatorException("Tips Index批量保存出错:" + e.getMessage(), e);
		}

	}

	public void update(Collection<TipsDao> tis) throws Exception {
		if (tis == null || tis.size() == 0) {
			return;
		}
		this.delete(tis);
		this.save(tis);

	}

}
