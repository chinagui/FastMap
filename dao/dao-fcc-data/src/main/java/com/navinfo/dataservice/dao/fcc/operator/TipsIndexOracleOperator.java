package com.navinfo.dataservice.dao.fcc.operator;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import net.sf.json.JSONArray;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.DaoOperatorException;

import net.sf.json.JSONObject;

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
	private static String insertSql = "INSERT INTO TIPS_INDEX(ID,STAGE,T_DATE,T_OPERATEDATE,T_LIFECYCLE,HANDLER,S_SOURCETYPE,WKT,S_QTASKID,S_MTASKID,S_QSUBTASKID,S_MSUBTASKID,WKTLOCATION,T_TIPSTATUS,T_DEDITSTATUS,T_MEDITSTATUS,S_PROJECT,T_DEDITMETH,T_MEDITMETH) VALUES (?,?,TO_TIMESTAMP(?,'yyyyMMddHH24miss'),TO_TIMESTAMP(?,'yyyyMMddHH24miss'),?,?,?,SDO_GEOMETRY(?,8307),?,?,?,?,SDO_GEOMETRY(?,8307),?,?,?,?,?,?)";
	private static String insertSqlLinks = "INSERT INTO TIPS_LINKS(ID,LINK_ID) VALUES(?,?)";
	private static String insertSqlNodes = "INSERT INTO TIPS_NODES(ID,NODE_ID) VALUES(?,?)";

	private static String deleteSql = "DELETE FROM  TIPS_INDEX  WHERE ID IN (select column_value from table(clob_to_table(?))) ";
	private static String deleteSqlLinks = "DELETE FROM  TIPS_LINKS WHERE ID IN  (select column_value from table(clob_to_table(?)))";
	private static String deleteSqlNodes = "DELETE FROM TIPS_NODES  WHERE ID IN  (select column_value from table(clob_to_table(?))) ";

    private static String deleteOneSql = "DELETE FROM TIPS_INDEX WHERE ID = ?";
    private static String deleteOneSqlLinks = "DELETE FROM TIPS_LINKS WHERE ID = ?";
    private static String deleteOneSqlNodes = "DELETE FROM TIPS_NODES WHERE ID = ?";

	public TipsIndexOracleOperator(Connection conn) {
		this.conn = conn;
		run = new QueryRunner();
	}

	@Override
	public List<TipsDao> searchDataByTileWithGap(String parameter)
			throws DaoOperatorException {
		return null;
	}

	@Override
	public void save(TipsDao ti) throws DaoOperatorException {
		try {
			Object[] cols = ti.toIndexMainArr();
			replaceLongString2Clob(cols);
			run.update(conn, insertSql, cols);
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
			throw new DaoOperatorException("Tips Index保存出错:" + e.getMessage(),
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
				Object[] cols = ti.toIndexMainArr();
				replaceLongString2Clob(cols);
				tisCols[i] = cols;
				links = (String[][]) ArrayUtils.addAll(links,
						ti.toIndexLinkArr());
				nodes = (String[][]) ArrayUtils.addAll(nodes,
						ti.toIndexNodeArr());
				i++;
			}
			run.batch(conn, insertSql, tisCols);
			if(links!=null){
				run.batch(conn, insertSqlLinks, links);
			}
			if(nodes!=null){
				run.batch(conn, insertSqlNodes, nodes);
			}
		} catch (Exception e) {
			log.error("Tips Index批量保存出错:" + e.getMessage(), e);
			throw new DaoOperatorException(
					"Tips Index批量保存出错:" + e.getMessage(), e);
		}
	}

	public int querCount(String sql, Object... params) throws Exception {
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Integer> resultSetHandler = new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		};
		return run.query(conn, sql, resultSetHandler, params);
	}
	public Map<Object,Object> groupQuery(String sql,Object... params) throws Exception{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Map<Object,Object>> resultSetHandler = new ResultSetHandler<Map<Object,Object>>() {
			@Override
			public Map<Object,Object> handle(ResultSet rs) throws SQLException {
				Map<Object,Object> res = new HashMap<Object,Object>();
				while (rs.next()) {
					Object key = rs.getObject(1);
					Object value = rs.getObject(2);
					res.put(key, value);
				}
				return res;
			}
		};
		return run.query(conn, sql, resultSetHandler, params);
	}
	public List<TipsDao> query(String sql, Object... params) throws Exception {
		List<TipsDao> result;
		try {
			ResultSetHandler<Map<String, TipsDao>> resultSetHandler = new ResultSetHandler<Map<String, TipsDao>>() {
				@Override
				public Map<String, TipsDao> handle(ResultSet rs)
						throws SQLException {
					Map<String, TipsDao> map = new HashMap<>();
					while (rs.next()) {
						TipsDao dao = new TipsDao();
						dao.loadResultSet(rs);
						map.put(dao.getId(), dao);
					}
					return map;
				}
			};
			log.debug("tips query:"+sql);
			Map<String, TipsDao> map = run.query(conn, sql, resultSetHandler,
					params);
			result = loadHbaseProperties(map);
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
		JSONArray t = JSONArray.fromObject(null);
		System.out.println(t.size());
	}

	@Override
	public void delete(Collection<TipsDao> tis) throws Exception {
		if (tis == null || tis.size() == 0) {
			return;
		}
		List<String> idList = new ArrayList<>();
		for(TipsDao tipsDao : tis){
			idList.add(tipsDao.getId());
		}
		String ids = org.apache.commons.lang.StringUtils.join(idList, ",");
		Clob pidClod = null;
		pidClod = ConnectionUtil.createClob(conn);
		pidClod.setString(1, ids);

		try {
			run.update(conn, deleteSql, pidClod);
			run.update(conn, deleteSqlLinks, pidClod);
			run.update(conn, deleteSqlNodes, pidClod);

		} catch (Exception e) {
			log.error("Tips Index批量保存出错:" + e.getMessage(), e);
			throw new DaoOperatorException(
					"Tips Index批量保存出错:" + e.getMessage(), e);
		}

	}

	/**
	 * 根据ID获取一条tips数据
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public TipsDao getById(String id) throws Exception {
		String sql = "select * from tips_index i where i.id=?";
		List<TipsDao> snapshots = this.query(sql, id);
		if (snapshots == null || snapshots.size() == 0) {
			return null;
		}
		TipsDao snapshot = snapshots.get(0);
		return snapshot;
	}

	/**
	 * 根据查询条件查询符合条件的所有Tips
	 * 
	 * @param sql
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<TipsDao> queryWithLimit(String sql, int limit, Object... params)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("WITH FINAL_TABLE AS ( ");
		sb.append(sql);
		sb.append(") SELECT FINAL_TABLE.* ");
		sb.append(" FROM FINAL_TABLE");
		sb.append(" WHERE ROWNUM <= " + limit);
		return query(sb.toString(), params);
	}
    
    public void updateOne(TipsDao ti) throws Exception {
    	Collection<TipsDao> tis=new ArrayList<TipsDao>();
    	tis.add(ti);
		this.delete(tis);
		this.save(tis);

	}

	/***
	 * 修改tips
	 * @param tis
	 * @throws Exception
	 */
	@Override
	public void update(Collection<TipsDao> tis) throws Exception {
		if (tis == null || tis.size() == 0) {
			return;
		}
		this.delete(tis);
		this.save(tis);

	}

  	public Page queryPage(String sql, final int pageNum, final int pageSize ,Object... params) throws Exception{
		long pageStartNum = (pageNum - 1) * pageSize + 1;
		long pageEndNum = pageNum * pageSize;

		String pageSql = "WITH query AS "
				+" (" + sql
				+ ") "
				+"SELECT /*+FIRST_ROWS ORDERED*/ t.*,( SELECT COUNT(1) FROM query) AS "
				+"TOTAL_RECORD_NUM"
				+"  FROM (SELECT t.*, rownum AS rownum_ FROM query t WHERE rownum <= ?) t "
				+" WHERE t.rownum_ >= ? ";

		log.debug(pageSql);

		ResultSetHandler<Page> resultSetHandler = new ResultSetHandler<Page>() {
			int total=0;
			@Override
			public Page handle(ResultSet rs) throws SQLException {
				Page page = new Page(pageNum);
				page.setPageSize(pageSize);
				Map<String, TipsDao> map = new HashMap<>();
				while (rs.next()) {
					TipsDao dao = new TipsDao();
					dao.loadResultSet(rs);
					map.put(dao.getId(), dao);
					total=rs.getInt("TOTAL_RECORD_NUM");
				}
				page.setTotalCount(total);
				page.setResult(map);
				return page;
			}
		};
		Object[] newParams = new Object[params.length + 2];
		System.arraycopy(params, 0, newParams, 0, params.length);
		newParams[params.length] = pageEndNum;
		newParams[params.length + 1] = pageStartNum;

		Page page = run.query(conn, pageSql, resultSetHandler, newParams);
		Map<String, TipsDao> map = (Map<String, TipsDao>)page.getResult();
		if(map.size()>0) {
			List<TipsDao> result = loadHbaseProperties(map);
			page.setResult(result);
		}else{
			page.setResult(new ArrayList<TipsDao>());
		}
		return page;
	}


	private List<TipsDao> loadHbaseProperties(Map<String, TipsDao> map) throws Exception{
    	List<TipsDao> result = new ArrayList<>();
		String[] queryColNames = { "deep", "geometry", "feedback", "tipdiff" };
		Map<String, JSONObject> hbaseMap = HbaseTipsQuery.getHbaseTipsByRowkeys(map.keySet(), queryColNames);
		for (String rowkey : map.keySet()) {
			if (!hbaseMap.containsKey(rowkey)) {
				throw new Exception("tip not found in hbase,rowkey:" + rowkey);
			}

			JSONObject hbaseTips = hbaseMap.get(rowkey);
			TipsDao dao = map.get(rowkey);
			dao.loadHbase(hbaseTips);
			result.add(dao);
		}
		return result;
	}

    @Override
    public void delete(String rowkey) throws DaoOperatorException {
        try{
            run.update(conn, deleteOneSqlNodes, rowkey);
            run.update(conn, deleteOneSqlLinks, rowkey);
            run.update(conn, deleteOneSql, rowkey);
        }catch(Exception e){
            log.error("Tips Index删除出错:"+e.getMessage(),e);
            throw new DaoOperatorException("Tips Index删除出错:"+e.getMessage(),e);
        }
    }


}
