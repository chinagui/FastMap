package com.navinfo.dataservice.diff.job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjAllSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * 目前只实现了POI的差分
 * @ClassName: DiffByJava
 * @author xiaoxiaowen4127
 * @date 2017年8月31日
 * @Description: DiffByJava.java
 */
public class DiffByJava extends DiffTool{
	Map<Long,BasicObj> leftPois = null;
	Map<Long,BasicObj> rightPois = null;
	protected QueryRunner run;
	public DiffByJava(DiffJobRequest req) {
		super(req);
		run = new QueryRunner();
	}

	@Override
	public String diff() throws DiffException {
		Connection leftConn = null;
		Connection rightConn = null;
		try{
			leftConn = leftSchema.getPoolDataSource().getConnection();
			rightConn = rightSchema.getPoolDataSource().getConnection();
			Set<String> tabs = parseDiffTabNames();
			if(DiffConfig.CONDITION_ALL.equals(req.getCondition())){
				leftPois = ObjAllSelector.selectAll(leftConn, req.getObjName(), tabs, false, false, false);
				rightPois = ObjAllSelector.selectAll(rightConn, req.getObjName(), tabs, false, false, false);
			}else if(DiffConfig.CONDITION_PID_TABLE.equals(req.getCondition())){
				String sql = mainObjPidSql(req.getPidTable());
				List<Long> leftPids = run.query(leftConn, sql, new ListLongResultSetHandler());
				List<Long> rightPids = run.query(rightConn, sql, new ListLongResultSetHandler());
				leftPois = ObjBatchSelector.selectByPids(leftConn, req.getObjName(), tabs, false, leftPids, false, false);
				rightPois = ObjBatchSelector.selectByPids(rightConn, req.getObjName(), tabs, false, rightPids, false, false);
				
			}else{
				log.warn("暂不支持的差分条件类型，差分结束。");
			}

			log.info("load data finished.");
		}catch(Exception e){
			log.error("diff err:"+e.getMessage(),e);
			throw new DiffException("差分时加载对象发生错误："+e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(leftConn);
			DbUtils.closeQuietly(rightConn);
		}
		return null;
	}
	
	private String mainObjPidSql(String pidTable)throws Exception{
		if(objMainTable!=null&&objMainTable.isMaintable()){
			return "SELECT T.PID FROM "+pidTable+" T,IX_POI P WHERE T.PID=P.PID AND P.U_RECORD IN (0,1,3)";
		}else{
			throw new Exception("差分对象配置错误");
		}
	}
	
	private Set<String> parseDiffTabNames(){
		if(diffTables==null||diffTables.size()==0){
			return null;
		}
		Set<String> tabs = new HashSet<String>();
		for(GlmTable glmTab:diffTables){
			tabs.add(glmTab.getName());
		}
		return tabs;
	}

	@Override
	public String writeLog(long userId,String actName, long subtaskId) throws Exception {
		Connection leftConn = null;
		try{
			leftConn = leftSchema.getPoolDataSource().getConnection();
			DiffOperation op = new DiffOperation(leftConn,null);
			op.setWriteData(false);
			DiffOperationCommand cmd = new DiffOperationCommand(leftPois,rightPois);
			op.operate(cmd);
			op.persistChangeLog(OperationSegment.SG_ROW, userId);
			return null;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(leftConn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(leftConn);
		}
	}
	
	class ListLongResultSetHandler implements ResultSetHandler<List<Long>>{

		@Override
		public List<Long> handle(ResultSet rs) throws SQLException {
			List<Long> list = new ArrayList<Long>();
			while(rs.next()){
				list.add(rs.getLong(1));
			}
			return list;
		}

	}
	
	public static void main(String[] args) {
		List<Long> list1 = new ArrayList<Long>();
		List<Long> list2 = new ArrayList<Long>();
		list1.add(1L);list1.add(2L);list1.add(2L);
		list2.add(2L);list2.add(3L);
		List<Long> list3 = ListUtils.retainAll(list1, list2);
		System.out.println(StringUtils.join(list1,","));
		System.out.println(StringUtils.join(list2,","));
		System.out.println(StringUtils.join(list3,","));
	}

}
