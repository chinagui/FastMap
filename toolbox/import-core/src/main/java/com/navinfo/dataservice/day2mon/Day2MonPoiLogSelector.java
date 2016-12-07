package com.navinfo.dataservice.day2mon;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.impcore.selector.DefaultLogSelector;

public class Day2MonPoiLogSelector extends DefaultLogSelector{
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Date startTime;
	public Day2MonPoiLogSelector(OracleSchema logSchema) {
		super(logSchema);
	}
	/**
	 *  3.1）查询得到该城市最后一次成功同步时间戳day_mon_sync.sync_start_time
		3.2）获取当前时间戳curTimeStamp=sysdate
		3.3）insert into day_mon_sync 
		city_id为当前的city，sync_start_time=curTimeStamp，sync_end_time=null，sync_status=1 返回sid
		3.4）在日库上，获取在sync_time到curTimeStamp之间的、grid范围在city所含的grids的 poi履历,履历需要满足下面的条件
		     a)log_operation.op_dt>sync_time and log_operation.op_dt<=curTimeStamp and ob_nm=’IX_POI’ and com_sta=0（未日落月）；
			 b)poi_edit_status.status为已提交
		     c)排除掉父子关系不完整的数据的履历：如过当前的poi为提交状态，但是其子为未提交的；当前为子poi且是提交状态，但是其父是未提交状态的；
		3.5）扩履历
		返回履历临时表名称

	 * @return
	 * @throws Exception 
	 */
	@Override
	protected int selectLog(Connection conn) throws Exception{
		return super.selectLog(conn)-removeInvalidLogOperation(conn);
		
	}
	/**
	 * 将父子关系不完整的数据履历删除掉:如过当前的poi为提交状态，但是其子为未提交的；当前为子poi且是提交状态，但是其父是未提交状态的
	 * @param tempTable 履历初步筛选生成的临时表，记录op_id,op_dt;
	 * @return 
	 * @throws SQLException 
	 */
	private int removeInvalidLogOperation(Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from "+tempTable+" l\r\n" + 
				" where exists(with t3 as (select pid\r\n" + 
				"                      from poi_edit_status\r\n" + 
				"                     where status in (1, 2))\r\n" + 
				"   select p.op_id\r\n" + 
				"     from log_operation   p,\r\n" + 
				"    log_detail      d,\r\n" + 
				"    log_detail_grid g,\r\n" + 
				"    poi_edit_status s\r\n" + 
				"    where p.op_id = d.op_id\r\n" + 
				"      and d.op_id = l.op_id\r\n" + 
				"      and d.row_id = g.log_row_id\r\n" + 
				"      and (d.ob_pid = s.pid or d.geo_pid = s.pid)\r\n" + 
				"      and p.com_sta = 0\r\n" + 
				"      and (d.ob_nm = 'IX_POI' or d.geo_nm = 'IX_POI')\r\n" + 
				"      and s.status = 3\r\n" + 
				"      and (exists (select 1\r\n" + 
				"                     from ix_poi_children t, ix_poi_parent t2, t3\r\n" + 
				"                    where t.group_id = t2.group_id\r\n" + 
				"                      and t.child_poi_pid = s.pid\r\n" + 
				"                      and t2.parent_poi_pid = t3.pid) or exists\r\n" + 
				"           (select 1\r\n" + 
				"              from ix_poi_children t, ix_poi_parent t2, t3\r\n" + 
				"             where t.group_id = t2.group_id\r\n" + 
				"               and t2.parent_poi_pid = s.pid\r\n" + 
				"               and t.child_poi_pid = t3.pid) or exists\r\n" + 
				"           (select 1\r\n" + 
				"              from ix_samepoi      s1,\r\n" + 
				"                   ix_samepoi_part s2,\r\n" + 
				"                   ix_samepoi_part s3,\r\n" + 
				"                   t3\r\n" + 
				"             where s1.group_id = s2.group_id\r\n" + 
				"               and s1.group_id = s3.group_id\r\n" + 
				"               and s2.poi_pid = s.pid\r\n" + 
				"               and s3.poi_pid = t3.pid))\r\n") ;
				if (this.stopTime!=null){
					String stopTimeSqlFormat = DateUtils.dateToString(stopTime, DateUtils.DATE_COMPACTED_FORMAT);
					sb.append("   and p.op_dt < to_date('"+stopTimeSqlFormat+"', 'yyyymmddhh24miss')\r\n") ;
				}
				
				List<Object> values = new ArrayList<Object> ();
				if(grids!=null&&grids.size()>0){
					SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,grids," g.GRID_ID ");
					if (inClause!=null)
						sb .append(" AND "+ inClause.getSql());
					values.addAll(inClause.getValues());
				}
				sb.append(" )\r\n");
				SqlClause sqlClause = new SqlClause(sb.toString(),values);
				log.info("removeInvalidLogOperation:"+sqlClause);
				return sqlClause.update(conn);
		
	}
	@Override
	protected SqlClause getPrepareSql(Connection conn) throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(tempTable);
		sb.append("select distinct p.op_id, p.op_dt\r\n" + 
				"  from log_operation   p,\r\n" + 
				"       log_detail d,\r\n" +
				"       log_detail_grid g,\r\n" + 
				"       poi_edit_status s\r\n" + 
				" where p.op_id = d.op_id\r\n" + 
				"   and d.row_id = g.log_row_id\r\n" + 
				"   and (d.ob_pid = s.pid or d.geo_pid = s.pid)\r\n"+ 
				"   and p.com_sta = 0"+ 
				"   and (d.ob_nm = 'IX_POI' or d.geo_nm = 'IX_POI')"+ 
				"   and s.status = 3");
		if (this.stopTime!=null){
			String stopTimeSqlFormat = DateUtils.dateToString(stopTime, DateUtils.DATE_COMPACTED_FORMAT);
			sb.append("   and p.op_dt < to_date('"+stopTimeSqlFormat+"', 'yyyymmddhh24miss')\r\n") ;
		}
		if(this.startTime!=null){
			String startTimeSqlFormat = DateUtils.dateToString(startTime, DateUtils.DATE_COMPACTED_FORMAT);
			sb.append("   and p.op_dt >= to_date('"+startTimeSqlFormat+"', 'yyyymmddhh24miss')\r\n") ;
		}
				 
		List<Object> values = new ArrayList<Object> ();
		if(grids!=null&&grids.size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,grids," g.GRID_ID ");
			if (inClause!=null)
				sb .append(" AND "+ inClause.getSql());
			values.addAll(inClause.getValues());
		}
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		log.info("getPrepareSql:"+sqlClause);
		return sqlClause;
		
	}
	
	public void setGrids(List<Integer> gridsOfCity) {
		this.grids = gridsOfCity;
		
	}
	public void setStartTime(Date lastSyncTime) {
		this.startTime = lastSyncTime;
		
	}
	public void setStopTime(Date syncTimeStamp) {
		this.stopTime = syncTimeStamp;
		
	}
	
}
